package club.ttg.dnd5.domain.filter.rest;

import club.ttg.dnd5.domain.spell.model.enums.MagicSchool;
import club.ttg.dnd5.domain.spell.rest.dto.SpellQueryRequest;
import club.ttg.dnd5.dto.base.filters.QueryFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link QueryRequestArgumentResolver}.
 * Тестирует токенизацию, парсинг enum/long/string фильтров, singleton-ов,
 * базовых полей и полноценный resolveArgument.
 */
class QueryRequestArgumentResolverTest
{
    private QueryRequestArgumentResolver resolver;

    @BeforeEach
    void setUp()
    {
        resolver = new QueryRequestArgumentResolver();
    }

    // ======================== Токенизация ========================

    @Nested
    @DisplayName("tokenize()")
    class TokenizeTests
    {
        @Test
        @DisplayName("null → пустая Map")
        void tokenize_null_returnsEmpty()
        {
            Map<String, String> result = resolver.tokenize(null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("пустая строка → пустая Map")
        void tokenize_empty_returnsEmpty()
        {
            Map<String, String> result = resolver.tokenize("");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("одна пара key=value")
        void tokenize_singlePair()
        {
            Map<String, String> result = resolver.tokenize("school=EVOCATION");
            assertEquals(1, result.size());
            assertEquals("EVOCATION", result.get("school"));
        }

        @Test
        @DisplayName("несколько пар, урлдекодирование")
        void tokenize_multiplePairs()
        {
            Map<String, String> result = resolver.tokenize("search=fire+ball&page=2&size=20&source=PHB,DMG");
            assertEquals("fire ball", result.get("search"));
            assertEquals("2", result.get("page"));
            assertEquals("20", result.get("size"));
            assertEquals("PHB,DMG", result.get("source"));
        }

        @Test
        @DisplayName("дублирующиеся ключи → первый побеждает")
        void tokenize_duplicateKeys_firstWins()
        {
            Map<String, String> result = resolver.tokenize("level=1&level=5");
            assertEquals("1", result.get("level"));
        }

        @Test
        @DisplayName("пара без = пропускается")
        void tokenize_noEquals_skipped()
        {
            Map<String, String> result = resolver.tokenize("level=1&broken&size=10");
            assertEquals(2, result.size());
            assertNull(result.get("broken"));
        }
    }

    // ======================== splitToSet ========================

    @Nested
    @DisplayName("splitToSet()")
    class SplitToSetTests
    {
        @Test
        @DisplayName("null → пустой Set")
        void split_null()
        {
            Set<String> result = resolver.splitToSet(null);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("одно значение")
        void split_single()
        {
            Set<String> result = resolver.splitToSet("PHB");
            assertEquals(Set.of("PHB"), result);
        }

        @Test
        @DisplayName("CSV → Set без пробелов")
        void split_csv()
        {
            Set<String> result = resolver.splitToSet("PHB, DMG , XGE");
            assertEquals(Set.of("PHB", "DMG", "XGE"), result);
        }

        @Test
        @DisplayName("пустые токены отбрасываются")
        void split_emptyTokens()
        {
            Set<String> result = resolver.splitToSet("PHB,,DMG,");
            assertEquals(Set.of("PHB", "DMG"), result);
        }
    }

    // ======================== resolveArgument ========================

    @Nested
    @DisplayName("resolveArgument()")
    class ResolveArgumentTests
    {
        @Test
        @DisplayName("пустая строка → дефолтный SpellQueryRequest")
        void emptyQuery_returnsDefaults() throws Exception
        {
            SpellQueryRequest request = resolve(null);
            assertNull(request.getSearch());
            assertEquals(0, request.getPage());
            assertEquals(10000, request.getPageSize());
            assertTrue(request.getSource().isEmpty());
            assertNull(request.getSchool());
            assertNull(request.getLevel());
            assertNull(request.getRitual());
        }

        @Test
        @DisplayName("базовые поля: search, page, size, source")
        void baseFields() throws Exception
        {
            SpellQueryRequest request = resolve("search=fire&page=2&size=20&source=PHB,DMG");
            assertEquals("fire", request.getSearch());
            assertEquals(2, request.getPage());
            assertEquals(20, request.getPageSize());
            assertEquals(Set.of("PHB", "DMG"), request.getSource());
        }

        @Test
        @DisplayName("enum фильтр: school=EVOCATION,ABJURATION&school_mode=1")
        void enumFilter() throws Exception
        {
            SpellQueryRequest request = resolve("school=EVOCATION,ABJURATION&school_mode=1");
            QueryFilter<MagicSchool> filter = request.getSchool();
            assertNotNull(filter);
            assertEquals(Set.of(MagicSchool.EVOCATION, MagicSchool.ABJURATION), filter.getValues());
            assertTrue(filter.isExclude());
            assertFalse(filter.isUnion());
        }

        @Test
        @DisplayName("числовой фильтр: level=1,3,5")
        void longFilter() throws Exception
        {
            SpellQueryRequest request = resolve("level=1,3,5");
            QueryFilter<Long> filter = request.getLevel();
            assertNotNull(filter);
            assertEquals(Set.of(1L, 3L, 5L), filter.getValues());
            assertFalse(filter.isExclude());
        }

        @Test
        @DisplayName("singleton: ritual=1")
        void singletonPositive() throws Exception
        {
            SpellQueryRequest request = resolve("ritual=1");
            QueryFilter<String> filter = request.getRitual();
            assertNotNull(filter);
            assertTrue(filter.getValues().contains("1"));
            assertFalse(filter.isExclude());
        }

        @Test
        @DisplayName("singleton исключение: concentration=1&concentration_mode=1")
        void singletonNegative() throws Exception
        {
            SpellQueryRequest request = resolve("concentration=1&concentration_mode=1");
            QueryFilter<String> filter = request.getConcentration();
            assertNotNull(filter);
            assertTrue(filter.getValues().contains("1"));
            assertTrue(filter.isExclude());
        }

        @Test
        @DisplayName("строковый фильтр: className=Wizard,Cleric")
        void stringFilter() throws Exception
        {
            SpellQueryRequest request = resolve("className=Wizard,Cleric");
            QueryFilter<String> filter = request.getClassName();
            assertNotNull(filter);
            assertEquals(Set.of("Wizard", "Cleric"), filter.getValues());
        }

        @Test
        @DisplayName("фильтр длительности: duration=INSTANT,1_MINUTE,10_MINUTE&duration_mode=1")
        void durationFilter() throws Exception
        {
            SpellQueryRequest request = resolve("duration=INSTANT,1_MINUTE,10_MINUTE&duration_mode=1");
            QueryFilter<String> filter = request.getDuration();
            assertNotNull(filter);
            assertEquals(Set.of("INSTANT", "1_MINUTE", "10_MINUTE"), filter.getValues());
            assertTrue(filter.isExclude());
        }

        @Test
        @DisplayName("union флаг: school=EVOCATION&school_union=1")
        void unionFlag() throws Exception
        {
            SpellQueryRequest request = resolve("school=EVOCATION&school_union=1");
            QueryFilter<MagicSchool> filter = request.getSchool();
            assertNotNull(filter);
            assertTrue(filter.isUnion());
            assertFalse(filter.isExclude());
        }

        @Test
        @DisplayName("комбинированный запрос со всеми типами")
        void combinedQuery() throws Exception
        {
            SpellQueryRequest request = resolve(
                    "search=fire&source=PHB&school=EVOCATION&level=1,3&ritual=1&concentration=1&concentration_mode=1&className=Wizard&duration=INSTANT"
            );
            assertEquals("fire", request.getSearch());
            assertEquals(Set.of("PHB"), request.getSource());
            assertNotNull(request.getSchool());
            assertNotNull(request.getLevel());
            assertTrue(request.getRitual().getValues().contains("1"));
            assertTrue(request.getConcentration().getValues().contains("1"));
            assertTrue(request.getConcentration().isExclude());
            assertEquals(Set.of("Wizard"), request.getClassName().getValues());
            assertEquals(Set.of("INSTANT"), request.getDuration().getValues());
        }
    }

    // ======================== Хелперы ========================

    /**
     * Создаёт мок-запрос и вызывает resolveArgument.
     */
    private SpellQueryRequest resolve(String queryString) throws Exception
    {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getQueryString()).thenReturn(queryString);

        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        when(webRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpRequest);

        // Получаем MethodParameter для SpellQueryRequest
        Method method = TestController.class.getMethod("search", SpellQueryRequest.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        return (SpellQueryRequest) resolver.resolveArgument(parameter, null, webRequest, null);
    }

    /** Фиктивный контроллер для создания MethodParameter. */
    static class TestController
    {
        @SuppressWarnings("unused")
        public void search(SpellQueryRequest request) {}
    }
}
