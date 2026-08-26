package club.ttg.dnd5.domain.vttg.rest.dto;

import club.ttg.dnd5.domain.common.model.ActiveEffect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Предыстория (background) в формате компендиума VTTG.
 *
 * <p>Соответствует целевому формату SRD-бэкапа VTTG (см. {@code backgrounds/*.json}):
 * самоописывающаяся запись с {@code id}/{@code type}/{@code isSRD} и блоками наград
 * ({@code abilityGrant}/{@code skillGrant}/{@code toolGrant}/{@code featGrant}/
 * {@code equipmentOptions}).</p>
 *
 * <p>Блоки-списки наград отдаются ВСЕГДА, пустыми при отсутствии данных: мастер
 * настройки предыстории в VTTG читает их поля напрямую ({@code toolGrant.items},
 * {@code skillGrant.skills}, ...), и вырезанный по {@code NON_NULL} блок роняет
 * его на первом же обращении. Опускается только {@code featGrant} — «черты нет»
 * не имеет осмысленного пустого значения (у {@code featName} нет пустого
 * аналога), и потребитель обязан обрабатывать его отсутствие.</p>
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VttgBackground {
    private String id;
    /** Дублирует {@code id} (как в эталоне). */
    private String key;
    private String name;
    private String nameEn;
    private String description;
    /** Слаг листа дерева разделов, в котором показывается запись — всегда "backgrounds". */
    private String section;
    /**
     * Раздел сайта в адресе страницы-источника ({@code backgrounds} в {@code /backgrounds/sage-phb}).
     * По паре {@code srcSection}/{@code srcUrl} VTTG находит запись в компендиуме, когда в
     * описании кликают ссылку.
     */
    private String srcSection;
    /** Слаг страницы-источника на сайте; с {@code srcSection} составляет адрес ссылки. */
    private String srcUrl;
    /** Ключ источника: "phb"/"dmg"/... */
    private String sourceKey;
    private AbilityGrant abilityGrant;
    private SkillGrant skillGrant;
    private ToolGrant toolGrant;
    private FeatGrant featGrant;
    private List<EquipmentOption> equipmentOptions;
    /**
     * Расширенные дары предыстории — то же поле записи компендиума, что у черты
     * ({@code GameItem.featData}). Владения, языки, защиты, чувства и выборы игрока,
     * которых канонические блоки наград не выражают.
     */
    private VttgFeatData featData;
    /**
     * Активные эффекты предыстории — соседом {@link #featData}, а не его частью: дары лист
     * проставляет сам, а эффект меняет числа готовой формулой и работает на столе.
     */
    private List<ActiveEffect> activeEffects;
    /** Канонический тип сущности для VTTG — всегда "background". */
    private String type;

    @Getter(AccessLevel.NONE)
    private boolean isSRD;

    @JsonProperty("isSRD")
    public boolean isSRD() {
        return isSRD;
    }

    /** Бонусные характеристики (slug'и: "strength".."charisma"). */
    public record AbilityGrant(List<String> abilities) {
    }

    /** Владения навыками (camelCase slug'и: "sleightOfHand", "insight"...). */
    public record SkillGrant(List<String> skills) {
    }

    /**
     * Владения инструментами (slug'и: "thieves-tools", "calligraphers-supplies"...) и
     * владение на выбор игрока.
     *
     * <p>{@code items} — ключи вокабуляра стола: в мастерской инструменты выбираются
     * ссылками на карточки раздела «Предметы», а адрес страницы
     * ({@code calligrapher-s-supplies}) справочник листа не знает и молча выбросил бы
     * такое владение (см. {@code VttgToolKeys}). У записей, которые на ссылки ещё не
     * перевели, владение хранится свободным текстом — он и уезжает, как уезжал.</p>
     *
     * <p>{@code choices} опускается, когда выбора нет: у «выбрать ноль инструментов» нет
     * осмысленного пустого значения, а сам блок {@code toolGrant} отдаётся всегда.</p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ToolGrant(List<String> items, ToolChoice choices) {

        /** Владение без выбора — как отдавалось до его появления. */
        public ToolGrant(List<String> items) {
            this(items, null);
        }
    }

    /**
     * Владение инструментами на выбор игрока: «выберите один музыкальный инструмент».
     *
     * @param count сколько инструментов выбирают
     * @param from  пул выбора ключами вокабуляра; пусто — любой инструмент
     */
    public record ToolChoice(Integer count, List<String> from) {
    }

    /**
     * Даруемая черта.
     *
     * <p>{@code featSuffix} — уточнение черты, которым предыстория отвечает за игрока:
     * «Мудрец» даёт «Посвящённого в магию (Волшебник)», и список заклинаний назван ею
     * самой. В названии черты его нет — {@code featName} приходит из каталога, — а без
     * него потребитель спросил бы список второй раз и предложил передумать за
     * предысторию.</p>
     *
     * <p>Текстом источника, а не ключом класса: уточнением бывает не только класс, и
     * переводить его здесь значило бы завести второй словарь названий классов рядом с
     * тем, который уже есть у потребителя. Скобки сняты — они оформление страницы, а не
     * часть значения.</p>
     *
     * @param featId     id черты в схеме эталона
     * @param featName   название черты из каталога
     * @param featNameEn английское название черты
     * @param featSuffix уточнение черты; пусто — предыстория ничего не уточняет
     * @param featChoices черты на выбор, когда предыстория не называет одну; пусто — черта
     *                    одна и лежит в {@code featId}
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FeatGrant(String featId, String featName, String featNameEn, String featSuffix,
                            List<String> featChoices) {

        /** Единственная черта — как отдавалось до появления выбора. */
        public FeatGrant(String featId, String featName, String featNameEn, String featSuffix) {
            this(featId, featName, featNameEn, featSuffix, null);
        }
    }

    /**
     * Вариант стартового снаряжения; {@code goldAlternative} — альтернатива золотом (опц.).
     *
     * <p>Описание и позиции идут вместе: строка нужна для чтения — в ней живые ссылки на
     * карточки, — а позиции для того, чтобы мастер настройки положил предметы в инвентарь
     * сам. У вариантов, заданных в источнике свободным текстом, позиций нет.</p>
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EquipmentOption(String description, Integer goldAlternative,
                                  List<VttgEquipmentItem> items, Integer coins, String coin) {

        /** Вариант из свободного текста: позиций и монет у него нет. */
        public EquipmentOption(String description, Integer goldAlternative) {
            this(description, goldAlternative, null, null, null);
        }
    }
}
