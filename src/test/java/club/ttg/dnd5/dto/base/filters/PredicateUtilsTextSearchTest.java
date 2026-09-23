package club.ttg.dnd5.dto.base.filters;

import club.ttg.dnd5.util.YoUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Текстовый поиск не различает «ё» и «е»: в названиях буква пишется
 * непоследовательно («Посвящённый в магию», но «Посвященный Зеленых певцов»).
 */
class PredicateUtilsTextSearchTest
{
    private static final StringPath NAME = Expressions.stringPath("name");

    private static String sql(final Predicate predicate)
    {
        SQLSerializer serializer = new SQLSerializer(new Configuration(new PostgreSQLTemplates()));
        serializer.handle(predicate);
        return serializer.toString();
    }

    @Test
    @DisplayName("ё в запросе заменяется на е")
    void replacesYoInQuery()
    {
        assertEquals("Посвяще", YoUtils.replaceYo("Посвящё"));
        assertEquals("Еж", YoUtils.replaceYo("Ёж"));
        assertEquals(null, YoUtils.replaceYo(null));
    }

    @Test
    @DisplayName("колонка сравнивается с заменой ё на е")
    void wrapsColumnWithReplace()
    {
        Predicate predicate = PredicateUtils.buildTextSearch("Посвящё", NAME);
        String sql = sql(predicate);

        assertTrue(sql.contains("replace(replace(name, 'Ё', 'Е'), 'ё', 'е')"), sql);
        assertFalse(predicate.toString().contains("ё%"), predicate.toString());
    }
}
