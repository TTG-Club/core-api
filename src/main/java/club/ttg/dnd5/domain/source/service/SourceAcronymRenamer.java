package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.common.url.CatalogUrlRenamer;
import club.ttg.dnd5.domain.filter.model.AbstractSavedFilter;
import club.ttg.dnd5.domain.source.event.SourceAcronymChangedEvent;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.vttg.service.VttgSourceKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Смена акронима уже сохранённого источника.
 *
 * <p>Акроним — первичный ключ {@code source}; через JPA его не поменять, поэтому ключ
 * меняется нативным запросом. Ссылки разделов каталога переезжают каскадом внешних
 * ключей ({@code ON UPDATE CASCADE}); следом те же колонки обновляются явно — на случай
 * таблицы, где внешнего ключа исторически нет. Вслед за акронимом меняются адреса записей
 * источника ({@link CatalogUrlRenamer}). Работает только внутри транзакции
 * вызывающего: либо поменялось везде, либо нигде.</p>
 */
@Component
public class SourceAcronymRenamer {
    private static final ObjectMapper JSON = new ObjectMapper();

    @PersistenceContext
    private EntityManager entityManager;
    private final ApplicationEventPublisher events;
    private final CatalogUrlRenamer urlRenamer;

    @Autowired
    public SourceAcronymRenamer(ApplicationEventPublisher events, CatalogUrlRenamer urlRenamer) {
        this.events = events;
        this.urlRenamer = urlRenamer;
    }

    SourceAcronymRenamer(EntityManager entityManager, ApplicationEventPublisher events, CatalogUrlRenamer urlRenamer) {
        this.entityManager = entityManager;
        this.events = events;
        this.urlRenamer = urlRenamer;
    }

    /**
     * Переименовывает акроним. После вызова контекст Hibernate пуст: сущности,
     * загруженные раньше, отсоединены, источник нужно перечитать.
     *
     * @param source      источник с прежним акронимом
     * @param newAcronym  новый акроним, свободный
     * @param username    кто меняет; {@code null}, если неизвестен
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void rename(Source source, String newAcronym, String username) {
        String oldAcronym = source.getAcronym();
        // Ключ выгрузки VTTG остаётся прежним: к нему привязаны записи у пользователей VTTG.
        String vttgKey = VttgSourceKeys.of(source);

        entityManager.flush();
        entityManager.createNativeQuery("""
                        update source
                        set acronym = :newAcronym, vttg_key = :vttgKey
                        where acronym = :oldAcronym
                        """)
                .setParameter("newAcronym", newAcronym)
                .setParameter("vttgKey", vttgKey)
                .setParameter("oldAcronym", oldAcronym)
                .executeUpdate();

        for (SourceReferences.Reference reference : references()) {
            entityManager.createNativeQuery(
                            "update " + quote(reference.table()) + " set " + quote(reference.column())
                                    + " = :newAcronym where " + quote(reference.column()) + " = :oldAcronym")
                    .setParameter("newAcronym", newAcronym)
                    .setParameter("oldAcronym", oldAcronym)
                    .executeUpdate();
        }

        renameInSavedFilters(oldAcronym, newAcronym);

        // Адреса записей источника (fireball-phb → fireball-ph) меняются вслед за акронимом.
        entityManager.flush();
        urlRenamer.rename(oldAcronym, newAcronym, username);

        entityManager.flush();
        entityManager.clear();

        events.publishEvent(new SourceAcronymChangedEvent(oldAcronym, newAcronym, username));
    }

    private Set<SourceReferences.Reference> references() {
        List<Class<?>> entityClasses = entityManager.getMetamodel().getEntities().stream()
                .map(EntityType::getJavaType)
                .collect(Collectors.toList());
        return SourceReferences.of(entityClasses);
    }

    /** Меняет акроним в сохранённых фильтрах пользователей, сохраняя их отметки. */
    private void renameInSavedFilters(String oldAcronym, String newAcronym) {
        @SuppressWarnings("unchecked")
        List<Object> ids = entityManager.createNativeQuery(
                        "select id from saved_filter where cast(filter as text) like :pattern escape '!'")
                .setParameter("pattern", "%" + escapeLike(jsonString(oldAcronym)) + "%")
                .getResultList();
        for (Object id : ids) {
            AbstractSavedFilter saved = entityManager.find(AbstractSavedFilter.class, id);
            if (saved != null && SavedFilterAcronyms.rename(saved.getFilter(), oldAcronym, newAcronym)) {
                // Свойство jsonb сравнивается по содержимому — изменение уйдёт в базу при flush.
                saved.setFilter(saved.getFilter());
            }
        }
    }

    /** Акроним в том виде, в каком он лежит в тексте jsonb (в кавычках, с экранированием). */
    private static String jsonString(String value) {
        try {
            return JSON.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    /** Имя таблицы или колонки в кавычках: среди таблиц каталога есть {@code class}. */
    private static String quote(String identifier) {
        return '"' + identifier.replace("\"", "\"\"") + '"';
    }
}
