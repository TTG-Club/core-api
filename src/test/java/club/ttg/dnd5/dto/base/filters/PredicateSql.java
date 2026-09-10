package club.ttg.dnd5.dto.base.filters;

import com.querydsl.core.types.Predicate;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLSerializer;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * SQL, в который превращается предикат поиска, — с теми же {@code PostgreSQLTemplates},
 * что у {@code AbstractQueryDslSearchService}.
 * <p>
 * {@code toString()} предиката показывает представление QueryDSL, а не SQL, и расстановку
 * скобок не отражает. Шаблон {@code Expressions.booleanTemplate} QueryDSL вставляет в цепочку
 * {@code and} как есть: {@code or} внутри него без собственных скобок разрывает весь where,
 * и всё, что добавлено в builder раньше, перестаёт действовать.
 */
public final class PredicateSql
{
    private static final String TOP_LEVEL_OR = " or ";

    private PredicateSql()
    {
    }

    /**
     * Собирает where-фрагмент так, как его соберёт нативный поисковый запрос.
     */
    public static String render(final Predicate predicate)
    {
        SQLSerializer serializer = new SQLSerializer(new Configuration(new PostgreSQLTemplates()));
        serializer.handle(predicate);
        return serializer.toString();
    }

    /**
     * Падает, если {@code or} стоит вне скобок: предикат из цепочки {@code and} обязан
     * остаться конъюнкцией. Строковые литералы в кавычках пропускаются.
     */
    public static void assertOrStaysInsideParentheses(final String sql)
    {
        int depth = 0;
        boolean insideLiteral = false;

        for (int index = 0; index < sql.length(); index++)
        {
            char symbol = sql.charAt(index);

            if (symbol == '\'')
            {
                insideLiteral = !insideLiteral;
            }
            else if (insideLiteral)
            {
                continue;
            }
            else if (symbol == '(')
            {
                depth++;
            }
            else if (symbol == ')')
            {
                depth--;
            }
            else if (depth == 0 && sql.regionMatches(true, index, TOP_LEVEL_OR, 0, TOP_LEVEL_OR.length()))
            {
                fail("or вне скобок разрывает where на две ветки: " + sql);
            }
        }
    }
}
