package club.ttg.dnd5.domain.common.url;

import club.ttg.dnd5.domain.common.model.NamedEntity;
import club.ttg.dnd5.exception.EntityExistException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.springframework.stereotype.Component;

/**
 * Новая запись каталога не занимает адрес, который числится прежним у другой: по нему
 * открывается та запись ({@link OldUrlRedirectFilter}), и новая стала бы недоступна.
 *
 * <p>Слушатель JPA создаётся Hibernate, а не Spring, поэтому реестр прежних адресов он
 * получает через статическое поле, которое заполняет компонент при старте. Без реестра
 * (юнит-тесты) проверка не выполняется.</p>
 */
public class CatalogUrlGuard {
    private static volatile EntityUrlHistoryRegistry history;

    @PrePersist
    public void beforeInsert(Object entity) {
        EntityUrlHistoryRegistry registry = history;
        if (registry == null || !(entity instanceof NamedEntity named) || named.getUrl() == null) {
            return;
        }
        Table table = tableOf(entity.getClass());
        if (table == null) {
            return;
        }
        CatalogTables.section(table.name()).ifPresent(section -> {
            if (registry.isOldUrl(section, named.getUrl())) {
                throw new EntityExistException(String.format(
                        "Адрес %s числится прежним у другой записи: по нему открывается она", named.getUrl()));
            }
        });
    }

    private static Table tableOf(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            Table table = current.getAnnotation(Table.class);
            if (table != null) {
                return table;
            }
        }
        return null;
    }

    /** Подключает реестр прежних адресов к слушателю. */
    @Component
    static class Installer {
        private final EntityUrlHistoryRegistry registry;

        Installer(EntityUrlHistoryRegistry registry) {
            this.registry = registry;
        }

        @PostConstruct
        void install() {
            history = registry;
        }

        @PreDestroy
        void uninstall() {
            history = null;
        }
    }
}
