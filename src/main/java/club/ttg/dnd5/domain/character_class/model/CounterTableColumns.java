package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.model.mechanics.CounterScaling;
import club.ttg.dnd5.domain.common.model.mechanics.ResourceCounter;
import club.ttg.dnd5.util.SlugifyUtil;
import lombok.experimental.UtilityClass;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Колонка таблицы прогрессии, выведенная из ресурса класса.
 *
 * <p>Ресурс живёт счётчиком механики, а в книге его ряд по уровням привычнее читать
 * колонкой таблицы («Ярости», «Кости превосходства»). Дублировать этот ряд руками автору
 * не нужно: у счётчика он уже задан — ступенями по уровням либо формулой, — и колонка
 * собирается из него, когда автор отметил {@code showInTable}.</p>
 *
 * <p>Колонка выводится, только если ряд считается от одного уровня: ступени, число,
 * {@code @prof} и {@code @level} (с множителем и смещением). Максимум по модификатору
 * характеристики ({@code @mod.cha}) зависит от персонажа, а не от уровня, и одинакового
 * ряда для всех у него нет — такой ресурс колонкой не показывается.</p>
 */
@UtilityClass
public class CounterTableColumns {
    /** Наибольший уровень персонажа: дальше таблица прогрессии не идёт. */
    private static final int MAX_LEVEL = 20;

    /** Обозначение бонуса мастерства в формуле максимума. */
    private static final String PROFICIENCY_TOKEN = "@prof";

    /** Обозначение уровня персонажа в формуле максимума. */
    private static final String LEVEL_TOKEN = "@level";

    /** Формула максимума: источник с необязательными множителем и смещением. */
    private static final Pattern FORMULA_PATTERN = Pattern.compile(
            "^(@prof|@level|\\d+)\\s*(?:\\*\\s*(\\d+)\\s*)?(?:([+-])\\s*(\\d+))?$");

    /**
     * Ресурсы одного носителя механики и уровень, с которого они у персонажа есть.
     *
     * @param counters ресурсы механики.
     * @param startLevel уровень носителя: у умения — его собственный, у класса — первый.
     */
    public record Source(List<ResourceCounter> counters, int startLevel) {
    }

    /**
     * Таблица прогрессии вместе с колонками ресурсов, отмеченных {@code showInTable}.
     *
     * <p>Выведенные колонки идут за своими, а ресурс, у которого колонка уже есть в
     * записи, второй раз не показывается: ключ у них один и тот же.</p>
     *
     * @param table колонки, заданные в записи; {@code null} — их нет.
     * @param sources ресурсы записи и её умений.
     * @return таблица для показа; пустой список — показывать нечего.
     */
    public List<ClassTableColumn> extend(List<ClassTableColumn> table, List<Source> sources) {
        List<ClassTableColumn> result = new ArrayList<>();
        Set<String> takenKeys = new HashSet<>();

        if (table != null) {
            for (ClassTableColumn column : table) {
                if (column != null) {
                    takenKeys.add(columnKey(column));
                    result.add(column);
                }
            }
        }

        for (Source source : sources) {
            result.addAll(from(source.counters(), source.startLevel(), takenKeys));
        }
        return result;
    }

    /**
     * Ключ колонки таблицы: заданный в записи, иначе выведенный из подписи
     * (транслит-slug, чтобы кириллица не схлопывалась в пустоту).
     *
     * <p>Явный ключ нужен ресурсам: по нему лист хранит потраченный остаток, и перевод
     * подписи не должен обнулять счётчики на уже сохранённых листах.</p>
     *
     * @param column колонка таблицы.
     * @return ключ колонки.
     */
    public String columnKey(ClassTableColumn column) {
        if (StringUtils.hasText(column.getKey())) {
            return column.getKey();
        }
        String slug = SlugifyUtil.getSlug(column.getName() == null ? "" : column.getName());
        return StringUtils.hasText(slug) ? slug : "col";
    }

    /**
     * Колонки таблицы, выведенные из ресурсов.
     *
     * @param counters ресурсы механики записи либо её умения.
     * @param startLevel уровень, с которого ресурс есть у персонажа: у ресурса умения это
     *                   уровень самого умения, у ресурса класса — первый.
     * @param takenKeys ключи уже занятых колонок; пополняется выведенными. Ресурс, у
     *                  которого своя колонка ещё осталась от прежней записи, второй раз
     *                  не показывается.
     * @return колонки в порядке ресурсов; пустой список — показывать нечего.
     */
    public List<ClassTableColumn> from(List<ResourceCounter> counters, int startLevel, Set<String> takenKeys) {
        if (CollectionUtils.isEmpty(counters)) {
            return List.of();
        }

        List<ClassTableColumn> result = new ArrayList<>();
        for (ResourceCounter counter : counters) {
            ClassTableColumn column = column(counter, startLevel);
            if (column != null && takenKeys.add(column.getKey())) {
                result.add(column);
            }
        }
        return result;
    }

    /**
     * Колонка одного ресурса.
     *
     * @param counter ресурс механики.
     * @param startLevel уровень, с которого ресурс есть у персонажа.
     * @return колонка; {@code null} — ресурс в таблице не показывается либо его ряд от
     *         уровня не считается.
     */
    private ClassTableColumn column(ResourceCounter counter, int startLevel) {
        if (counter == null || !counter.isShowInTable() || !StringUtils.hasText(counter.getKey())) {
            return null;
        }

        List<ClassTableItem> scaling = values(counter, startLevel);
        if (scaling == null) {
            return null;
        }

        ClassTableColumn column = new ClassTableColumn(label(counter), scaling);
        column.setKey(counter.getKey());
        return column;
    }

    /**
     * Подпись колонки: краткое название ресурса, а без него — полное.
     *
     * <p>Краткое старше полного: колонка таблицы узкая, и «БК» в шапке читается лучше
     * «Божественного канала». Нет ни того, ни другого — остаётся ключ: безымянная колонка
     * ни о чём не говорит.</p>
     */
    private String label(ResourceCounter counter) {
        if (StringUtils.hasText(counter.getShortName())) {
            return counter.getShortName();
        }
        return StringUtils.hasText(counter.getName()) ? counter.getName() : counter.getKey();
    }

    /**
     * Значения ресурса по уровням: ступени старше формулы — ряд, который формулой не
     * пишется, задан ими и точнее любого выражения.
     *
     * @param counter ресурс механики.
     * @param startLevel уровень, с которого ресурс есть у персонажа.
     * @return значения по уровням; {@code null} — ряд от уровня не считается.
     */
    private List<ClassTableItem> values(ResourceCounter counter, int startLevel) {
        List<ClassTableItem> scaled = scaledValues(counter, startLevel);
        return scaled != null ? scaled : formulaValues(counter, startLevel);
    }

    /**
     * Значения по ступеням: ступень держится до следующей, до первой из них ресурса нет.
     *
     * @param counter ресурс механики.
     * @param startLevel уровень, с которого ресурс есть у персонажа.
     * @return значения по уровням; {@code null} — ступеней нет.
     */
    private List<ClassTableItem> scaledValues(ResourceCounter counter, int startLevel) {
        if (CollectionUtils.isEmpty(counter.getScaling())) {
            return null;
        }

        List<CounterScaling> steps = counter.getScaling().stream()
                .filter(step -> step != null && step.getLevel() != null && step.getMax() != null)
                .sorted(Comparator.comparingInt(CounterScaling::getLevel))
                .toList();
        if (steps.isEmpty()) {
            return null;
        }

        List<ClassTableItem> result = new ArrayList<>();
        Integer current = null;
        for (int level = 1; level <= MAX_LEVEL; level++) {
            for (CounterScaling step : steps) {
                if (step.getLevel() <= level) {
                    current = step.getMax();
                }
            }
            // Уровни до появления ресурса пропускаются, а не заполняются прочерком: так же
            // ведут себя колонки, набранные руками, и потребитель читает их одинаково
            if (current != null && level >= startLevel) {
                result.add(new ClassTableItem(level, String.valueOf(withMinimum(current, counter))));
            }
        }
        return result;
    }

    /**
     * Значения по формуле максимума. Считаются только формулы, зависящие от одного
     * уровня: число, бонус мастерства и уровень персонажа с множителем и смещением.
     *
     * @param counter ресурс механики.
     * @param startLevel уровень, с которого ресурс есть у персонажа.
     * @return значения по уровням; {@code null} — формула от уровня не считается.
     */
    private List<ClassTableItem> formulaValues(ResourceCounter counter, int startLevel) {
        String formula = counter.getMax() == null ? "" : counter.getMax().trim().toLowerCase();
        if (formula.isEmpty()) {
            return null;
        }

        Matcher matcher = FORMULA_PATTERN.matcher(formula);
        if (!matcher.matches()) {
            return null;
        }

        String source = matcher.group(1);
        int multiplier = matcher.group(2) == null ? 1 : Integer.parseInt(matcher.group(2));
        int offset = matcher.group(4) == null
                ? 0
                : Integer.parseInt(matcher.group(4)) * ("-".equals(matcher.group(3)) ? -1 : 1);

        List<ClassTableItem> result = new ArrayList<>();
        for (int level = Math.max(1, startLevel); level <= MAX_LEVEL; level++) {
            int value = Math.max(0, withMinimum(sourceValue(source, level) * multiplier + offset, counter));
            result.add(new ClassTableItem(level, String.valueOf(value)));
        }
        return result;
    }

    /** Значение источника формулы на уровне. */
    private int sourceValue(String source, int level) {
        if (PROFICIENCY_TOKEN.equals(source)) {
            return proficiencyBonus(level);
        }
        return LEVEL_TOKEN.equals(source) ? level : Integer.parseInt(source);
    }

    /** Максимум с оглядкой на нижнюю границу ресурса: она подпирает ряд снизу. */
    private int withMinimum(int value, ResourceCounter counter) {
        Integer min = counter.resolveMin();
        return min == null ? value : Math.max(value, min);
    }

    /** Бонус мастерства по уровню: 2 (1–4), 3 (5–8), 4 (9–12), 5 (13–16), 6 (17–20). */
    private int proficiencyBonus(int level) {
        return 2 + (Math.max(1, level) - 1) / 4;
    }
}
