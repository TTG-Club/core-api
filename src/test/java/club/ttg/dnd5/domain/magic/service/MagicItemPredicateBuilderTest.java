package club.ttg.dnd5.domain.magic.service;

import club.ttg.dnd5.domain.common.dictionary.Rarity;
import club.ttg.dnd5.domain.magic.model.MagicItemCategory;
import club.ttg.dnd5.domain.magic.model.MagicItemTheme;
import club.ttg.dnd5.domain.magic.rest.dto.MagicItemQueryRequest;
import club.ttg.dnd5.dto.base.filters.PredicateSql;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * «Исключать» у настройки, зарядов и проклятия не отменяет остальные фильтры.
 * <p>
 * Условие исключения было шаблоном с {@code or} без скобок, и в SQL выходило
 * {@code … and rarity in (…) and attunement is null or … and source in (…)}: во второй
 * ветке {@code or} терялись поиск, категория и редкость — {@code rarity=UNCOMMON} вместе с
 * {@code attunement_mode=1} отдавал предметы любой редкости.
 */
class MagicItemPredicateBuilderTest
{
    private static QueryFilter<String> excluded()
    {
        QueryFilter<String> filter = new QueryFilter<>();
        filter.setValues(Set.of("1"));
        filter.setExclude(true);
        return filter;
    }

    /** Запрос, в котором есть фильтры и до условия исключения, и после него. */
    private static MagicItemQueryRequest requestWithCategoryAndRarity()
    {
        QueryFilter<MagicItemCategory> category = new QueryFilter<>();
        category.setValues(Set.of(MagicItemCategory.RING));

        QueryFilter<Rarity> rarity = new QueryFilter<>();
        rarity.setValues(Set.of(Rarity.UNCOMMON));

        MagicItemQueryRequest request = new MagicItemQueryRequest();
        request.setSearch("кольцо");
        request.setCategory(category);
        request.setRarity(rarity);
        request.setSource(Set.of("DMG"));
        return request;
    }

    private static String sql(final MagicItemQueryRequest request)
    {
        return PredicateSql.render(MagicItemPredicateBuilder.build(request).getValue());
    }

    @Test
    @DisplayName("исключить настройку: редкость, категория и поиск остаются в силе")
    void excludedAttunementKeepsOtherFilters()
    {
        MagicItemQueryRequest request = requestWithCategoryAndRarity();
        request.setAttunement(excluded());

        PredicateSql.assertOrStaysInsideParentheses(sql(request));
    }

    @Test
    @DisplayName("исключить заряды: редкость, категория и поиск остаются в силе")
    void excludedChargesKeepsOtherFilters()
    {
        MagicItemQueryRequest request = requestWithCategoryAndRarity();
        request.setCharges(excluded());

        PredicateSql.assertOrStaysInsideParentheses(sql(request));
    }

    @Test
    @DisplayName("исключить проклятие: редкость, категория и поиск остаются в силе")
    void excludedCurseKeepsOtherFilters()
    {
        MagicItemQueryRequest request = requestWithCategoryAndRarity();
        request.setCurse(excluded());

        PredicateSql.assertOrStaysInsideParentheses(sql(request));
    }

    private static QueryFilter<MagicItemTheme> theme(final boolean exclude)
    {
        QueryFilter<MagicItemTheme> filter = new QueryFilter<>();
        filter.setValues(Set.of(MagicItemTheme.RELIC));
        filter.setExclude(exclude);
        return filter;
    }

    @Test
    @DisplayName("тематика: отбирает предметы выбранной тематики")
    void themeFiltersByValue()
    {
        MagicItemQueryRequest request = requestWithCategoryAndRarity();
        request.setTheme(theme(false));

        String sql = sql(request);

        assertTrue(sql.contains("theme = ?") || sql.contains("theme in "), sql);
        PredicateSql.assertOrStaysInsideParentheses(sql);
    }

    @Test
    @DisplayName("исключить тематику: предметы без тематики остаются, остальные фильтры в силе")
    void excludedThemeKeepsItemsWithoutTheme()
    {
        MagicItemQueryRequest request = requestWithCategoryAndRarity();
        request.setTheme(theme(true));

        String sql = sql(request);

        assertTrue(sql.contains("theme is null"), sql);
        PredicateSql.assertOrStaysInsideParentheses(sql);
    }

    @Test
    @DisplayName("скрытые из списка предметы не находятся поиском")
    void searchSkipsItemsHiddenFromList()
    {
        String sql = sql(requestWithCategoryAndRarity());

        assertTrue(sql.contains("showInList = ?") || sql.contains("showInList = true"), sql);
        PredicateSql.assertOrStaysInsideParentheses(sql);
    }

    @Test
    @DisplayName("выбор предмета в листе персонажа находит и скрытые из списка предметы")
    void includeUnlistedKeepsItemsHiddenFromList()
    {
        MagicItemQueryRequest request = requestWithCategoryAndRarity();
        request.setIncludeUnlisted(true);

        String sql = sql(request);

        assertFalse(sql.contains("showInList"), sql);
        PredicateSql.assertOrStaysInsideParentheses(sql);
    }
}
