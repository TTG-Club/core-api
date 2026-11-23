package club.ttg.dnd5.domain.character_class.repository;

import club.ttg.dnd5.domain.character_class.model.CharacterClassListItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/** Чтение классов и подклассов для списков — только поля краткого ответа. */
public interface ClassListItemRepository extends Repository<CharacterClassListItem, String>
{
    /**
     * Все подклассы в порядке общего списка: официальность и категория источника, затем
     * название родительского класса и самого подкласса. Порядок тот же, что у
     * {@link ClassRepository#findAllByParentIsNotNull()}.
     *
     * @return подклассы, включая скрытые (их отсекает вызывающий код).
     */
    @Query("""
    SELECT c FROM CharacterClassListItem c
    WHERE c.parent IS NOT NULL
    ORDER BY
      CASE c.source.officiality
        WHEN 'OFFICIAL' then 1
        WHEN 'TEST' then 2
        WHEN 'THIRD_PARTY' then 3
        WHEN 'HOMEBREW' then 4
      end,
      CASE c.source.category
        WHEN 'CORE' then 1
        WHEN 'SETTING' then 2
        WHEN 'ADVENTURE' then 3
      end,
      c.parent.name,
      c.name
    """)
    List<CharacterClassListItem> findAllSubclasses();
}
