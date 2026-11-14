package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.source.model.Source;
import jakarta.persistence.Inheritance;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Колонки таблиц каталога, в которых лежит акроним источника.
 *
 * <p>Список не зашит руками, а собирается по сущностям: любое поле типа {@link Source}
 * с {@link JoinColumn} — ссылка на источник. Новый раздел каталога попадает сюда сам.</p>
 */
final class SourceReferences {
    private SourceReferences() {
    }

    /**
     * @param table  таблица раздела
     * @param column колонка с акронимом источника
     */
    record Reference(String table, String column) {
    }

    /**
     * @param entityClasses классы сущностей приложения
     * @return ссылки на источник без повторов (наследники одной таблицы дают одну ссылку)
     */
    static Set<Reference> of(Collection<Class<?>> entityClasses) {
        Set<Reference> references = new LinkedHashSet<>();
        for (Class<?> entityClass : entityClasses) {
            String table = tableOf(entityClass);
            if (table == null) {
                continue;
            }
            for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                    if (joinColumn != null && Source.class.equals(field.getType()) && !joinColumn.name().isBlank()) {
                        references.add(new Reference(table, joinColumn.name()));
                    }
                }
            }
        }
        return references;
    }

    /** Таблица сущности: своя или, при наследовании в одной таблице, таблица предка. */
    private static String tableOf(Class<?> entityClass) {
        for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
            Table table = type.getAnnotation(Table.class);
            if (table != null && !table.name().isBlank()) {
                return table.name();
            }
            if (type.getAnnotation(Inheritance.class) != null) {
                return null;
            }
        }
        return null;
    }
}
