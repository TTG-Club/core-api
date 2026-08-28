package club.ttg.dnd5.domain.character_class.model;

import club.ttg.dnd5.domain.common.model.mechanics.ChoiceScaling;
import club.ttg.dnd5.domain.common.model.mechanics.CounterScaling;
import club.ttg.dnd5.domain.common.model.mechanics.MechanicChoice;
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
     * Механика одного носителя и уровень, с которого она у персонажа есть.
     *
     * <p>Колонку даёт и ресурс со ступенями, и выбор со ступенями количества: ряд в
     * книге у них одинаковый — число по уровням, — и собирать его дважды незачем.</p>
     *
     * @param counters ресурсы механики.
     * @param choices выборы механики.
     * @param startLevel уровень носителя: у умения — его собственный, у класса — первый.
     */
    public record Source(List<ResourceCounter> counters, List<MechanicChoice> choices, int startLevel) {
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
        Set<String> taken = new HashSet<>();

        if (table != null) {
            for (ClassTableColumn column : table) {
                if (column != null) {
                    taken.add(columnKey(column));
                    // Подпись занимает место наравне с ключом: у подкласса лежит своя копия
                    // родительской колонки, и ключа у неё чаще всего нет вовсе. Без сверки
                    // по подписи «Ярость» встала бы в таблицу дважды — своя и выведенная
                    taken.add(label(column.getName()));
                    result.add(column);
                }
            }
        }

        for (Source source : sources) {
            result.addAll(from(source.counters(), source.startLevel(), taken));
            result.addAll(fromChoices(source.choices(), source.startLevel(), taken));
        }
        return result;
    }

    /** Подпись колонки для сверки: без краёв и регистра — «Ярость» и «ярость» это одно. */
    private String label(String name) {
        return name == null ? "" : name.trim().toLowerCase();
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
            if (column != null && isFree(column, takenKeys)) {
                result.add(column);
            }
        }
        return result;
    }

    /**
     * Место в таблице свободно: ни ключа, ни подписи выведенной колонки там ещё нет.
     * Занятое — помечается, чтобы вторая такая колонка не пролезла следом.
     *
     * @param column выведенная колонка.
     * @param taken занятые ключи и подписи; пополняется на месте.
     * @return true — колонку можно показывать.
     */
    private boolean isFree(ClassTableColumn column, Set<String> taken) {
        String key = column.getKey();
        String label = label(column.getName());
        if (taken.contains(key) || taken.contains(label)) {
            return false;
        }
        taken.add(key);
        taken.add(label);
        return true;
    }

    /**
     * Колонки таблицы, выведенные из выборов со ступенями количества.
     *
     * @param choices выборы механики записи либо её умения.
     * @param startLevel уровень, с которого выбор есть у персонажа.
     * @param takenKeys ключи уже занятых колонок; пополняется выведенными.
     * @return колонки в порядке выборов; пустой список — показывать нечего.
     */
    public List<ClassTableColumn> fromChoices(List<MechanicChoice> choices, int startLevel,
                                              Set<String> takenKeys) {
        if (CollectionUtils.isEmpty(choices)) {
            return List.of();
        }

        List<ClassTableColumn> result = new ArrayList<>();
        for (MechanicChoice choice : choices) {
            ClassTableColumn column = column(choice, startLevel);
            if (column != null && isFree(column, takenKeys)) {
                result.add(column);
            }
        }
        return result;
    }

    /**
     * Колонка одного выбора: ряд количества по его ступеням.
     *
     * @param choice выбор механики.
     * @param startLevel уровень, с которого выбор есть у персонажа.
     * @return колонка; {@code null} — выбор в таблице не показывается либо ступеней нет.
     */
    private ClassTableColumn column(MechanicChoice choice, int startLevel) {
        if (choice == null || !Boolean.TRUE.equals(choice.getShowInTable())
                || !StringUtils.hasText(choice.getKey())
                || CollectionUtils.isEmpty(choice.getScaling())) {
            return null;
        }

        List<ChoiceScaling> steps = choice.getScaling().stream()
                .filter(step -> step != null && step.getLevel() != null && step.getCount() != null)
                .sorted(Comparator.comparingInt(ChoiceScaling::getLevel))
                .toList();
        if (steps.isEmpty()) {
            return null;
        }

        // Уровень открытия выбора старше уровня носителя: «ещё один приём» спрашивают с
        // него, а не с уровня умения
        int firstLevel = Math.max(startLevel,
                choice.getRequiredLevel() == null ? 1 : choice.getRequiredLevel());

        List<ClassTableItem> scaling = new ArrayList<>();
        Integer current = null;
        for (int level = 1; level <= MAX_LEVEL; level++) {
            for (ChoiceScaling step : steps) {
                if (step.getLevel() <= level) {
                    current = step.getCount();
                }
            }
            if (current != null && level >= firstLevel) {
                scaling.add(new ClassTableItem(level, String.valueOf(current)));
            }
        }
        if (scaling.isEmpty()) {
            return null;
        }

        ClassTableColumn column = new ClassTableColumn(label(choice), scaling);
        column.setKey(choice.getKey());
        return column;
    }

    /** Подпись колонки выбора: краткая, а без неё подпись выбора либо ключ. */
    private String label(MechanicChoice choice) {
        if (StringUtils.hasText(choice.getShortName())) {
            return choice.getShortName();
        }
        return StringUtils.hasText(choice.getLabel()) ? choice.getLabel() : choice.getKey();
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
