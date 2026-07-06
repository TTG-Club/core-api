package club.ttg.dnd5.domain.revision.model;

/**
 * Тип операции, зафиксированной в истории изменений сущности.
 */
public enum RevisionOperation {
    CREATE,
    UPDATE,
    DELETE,
    REVERT
}
