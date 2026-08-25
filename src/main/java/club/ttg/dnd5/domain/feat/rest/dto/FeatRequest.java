package club.ttg.dnd5.domain.feat.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import club.ttg.dnd5.domain.common.rest.dto.BaseRequest;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.model.prerequisite.FeatPrerequisite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Тело запроса на создание и правку черты — то, что шлёт мастерская.
 *
 * <p>Механика и разобранное требование идут <b>доменной моделью как есть</b>:
 * {@link FeatMechanics} и {@link FeatPrerequisite} — это и есть контракт с сайтом, а не
 * внутреннее представление, которое кто-то переводит по дороге. Ровно ту же форму отдаёт
 * {@code GET /feats/{url}/raw} ({@code FeatMapper.toRequest}) и деталка
 * ({@link FeatDetailResponse#getMechanics()}), поэтому редактор показывает и шлёт одно и
 * то же, ничего не пересобирая.</p>
 *
 * <p>Своих DTO со слагами у сайта нет намеренно. Слаги нужны листу персонажа VTTG, и его
 * словарь живёт в выгрузке ({@code VttgFeatMechanicsMapper}); заводить второй, сайтовый,
 * значило бы держать два перевода одной и той же модели и чинить расхождения между ними.
 * Сайту переводить нечего — подписи у него всё равно русские и берутся из своего
 * справочника, а хранит он ровно то, что показывает.</p>
 *
 * <p>Цена решения: имена полей и значения enum'ов механики — публичный контракт. Менять их
 * молча нельзя ни в модели, ни здесь: это ломает редактор и уже сохранённый JSONB.</p>
 *
 * <p>Сохранение перезаписывает механику ЦЕЛИКОМ ({@code feat.setMechanics(...)}), слияния
 * с прежним значением нет. Поэтому поле, которого мастерская не знает, стирается при
 * первом же сохранении — новый блок механики нужно заводить и в её схеме тоже.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class FeatRequest extends BaseRequest {
    @Schema(description = "Категория черты", examples = {"ORIGIN", "GENERAL", "EPIC_BOON", "FIGHTING_STYLE"})
    private FeatCategory category;

    /**
     * Требование строкой, как оно напечатано в книге.
     *
     * <p>Остаётся рядом с разобранным {@link #prerequisiteDetails}, а не заменяется им:
     * разобрать удаётся не всё («Эльф или полуэльф», «превращение в лича»), а показать
     * требование нужно всегда. Форма правит обе записи, и книжная строка — единственный
     * источник для черт, у которых разбора ещё нет.</p>
     */
    @Schema(description = "Предварительное условие как в книге")
    private String prerequisite;

    /** Требование в разобранном виде — по нему визард выбора черты фильтрует список. */
    @Schema(description = "Предварительное условие в разобранном виде")
    private FeatPrerequisite prerequisiteDetails;

    @Schema(description = "Повторяемость")
    private Boolean repeatability;

    /**
     * Механика влияния черты на лист персонажа — доменной моделью, см. javadoc класса.
     *
     * <p>Плоской проекции {@code abilities} здесь больше нет: колонка живёт только в
     * записи, где по ней работает SQL-фильтр «Характеристика», и пересобирается из
     * {@code mechanics.abilityBonuses} при сохранении
     * ({@code FeatMapper.syncAbilitiesWithMechanics}). Присланная форма её всё равно не
     * меняла — поле было лишним в контракте.</p>
     */
    @Schema(description = "Механика влияния черты на лист персонажа")
    private FeatMechanics mechanics;

    /**
     * Активные эффекты черты для экспорта в VTTG — рядом с механикой, а не внутри неё:
     * механика описывает дары, которые лист проставляет сам, а эффект меняет числа
     * готовой формулой (см. {@code Feat.activeEffects}).
     *
     * <p>Как и механика, перезаписывается ЦЕЛИКОМ: пустой список стирает прежние
     * эффекты записи.</p>
     */
    @Schema(description = "Активные эффекты черты для экспорта в VTTG")
    @Nullable
    @Valid
    private List<ActiveEffect> activeEffects;
}
