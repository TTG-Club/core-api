package club.ttg.dnd5.domain.bastion.player.rest.dto;

import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.player.model.FacilityStatus;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.model.SelectedChoice;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.bastion.rest.dto.FacilitySpaceResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Бастион глазами того, кто его открыл.
 *
 * @param gameId          игра, в которую добавлен бастион; {@code null} — бастион вне игры
 * @param ownerId         владелец — тот, кто создал бастион
 * @param owned           открывший — владелец: может переименовать, удалить и добавить в игру
 * @param canManage       можно менять состав, казну, подтверждать требования и делать ходы:
 *                        мастер игры, а вне игры — владелец
 * @param canEdit         можно рисовать план и отдавать приказы
 * @param canLeaveGame    можно убрать бастион из игры: владелец или мастер этой игры
 * @param shareToken      секрет ссылки «поделиться»; видит только владелец, {@code null} — ссылки нет
 * @param editRequests    сколько запросов на правку ждут ответа; считается только для владельца
 * @param editor          владелец разрешил открывшему править бастион по ссылке
 */
@Schema(description = "Бастион игроков")
public record PlayerBastionResponse(
        UUID id,
        UUID gameId,
        UUID ownerId,
        String name,
        PlayerBastionStatus status,
        int turn,
        long treasuryGp,
        long version,
        boolean owned,
        boolean canManage,
        boolean canEdit,
        boolean canLeaveGame,
        UUID shareToken,
        long editRequests,
        boolean editor,
        List<Member> members,
        @Schema(description = "Гости бастиона из событий") List<Guest> guests,
        @Schema(description = "Защитные стены бастиона") DefenseWalls defenseWalls,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * Игрок с доступом и его персонаж.
     *
     * @param specialFacilityLimit сколько специализированных сооружений положено персонажу
     * @param canEditFacilities    открывший может менять сооружения этого персонажа: мастер или
     *                             сам игрок, пока бастион в закладке
     * @param basicComplete        выбраны оба стартовых базовых сооружения
     * @param canGiveOrders        открывший может отдавать приказы сооружениям персонажа,
     *                             строить и расширять их: бастион запущен, это мастер или сам игрок
     */
    @Schema(description = "Игрок с доступом к бастиону")
    public record Member(
            UUID id,
            UUID userId,
            String characterName,
            int characterLevel,
            UUID characterSheetId,
            int specialFacilityLimit,
            boolean canEditFacilities,
            boolean basicComplete,
            boolean canGiveOrders,
            List<Facility> facilities) {
    }

    /**
     * Сооружение персонажа вместе с данными справочника.
     *
     * @param prerequisiteConfirmed требование подтверждено мастером (у сооружений без требования — всегда)
     * @param hirelings             число наёмников сооружения
     * @param status                готово или строится
     * @param readyOnTurn           ход, на котором достроится
     * @param pendingSpace          пространство, до которого идёт расширение
     * @param pendingReadyOnTurn    ход, на котором закончится расширение
     * @param enlargeable           сооружение можно расширить сейчас: готово, не расширяется, и
     *                              расширять есть куда
     */
    @Schema(description = "Сооружение персонажа")
    public record Facility(
            UUID id,
            String facilityUrl,
            String name,
            String english,
            BastionLabel category,
            FacilitySpaceResponse space,
            Long level,
            Integer hirelings,
            List<BastionLabel> orders,
            BastionLabel prerequisite,
            boolean prerequisiteConfirmed,
            List<SelectedChoice> choices,
            FacilityStatus status,
            Integer readyOnTurn,
            FacilitySpaceResponse pendingSpace,
            Integer pendingReadyOnTurn,
            boolean enlargeable,
            @Schema(description = "Защитников в бараке; null — сооружение не барак") Integer defenders,
            @Schema(description = "Сколько защитников помещается в барак; null — не барак") Integer defenderCapacity,
            @Schema(description = "Что можно изготовить выбранными инструментами; null — у сооружения нет выбора инструментов")
            List<CraftableItem> craftableItems,
            @Schema(description = "Вольеры зверинца; null — сооружение не зверинец") Menagerie menagerie,
            @Schema(description = "Животные конюшни; null — сооружение не конюшня") Stable stable,
            @Schema(description = "Ход, в который сооружение не работает после события; null — работает")
            Integer unavailableOnTurn,
            @Schema(description = "Почему сооружение не работает") String unavailableReason,
            @Schema(description = "Наёмники, которых забрало событие и которым ещё ищут замену; "
                    + "null — все на месте") Integer hirelingsMissing) {
    }

    /**
     * Защитные стены вокруг бастиона.
     *
     * @param built       достроенные блоки (5-футовые квадраты): столько клеток стены можно
     *                    поставить на план
     * @param building    блоки в стройке
     * @param readyOnTurn ход, к которому блоки в стройке достроятся
     * @param enclosed    бастион полностью окружён стенами: при атаке на 2 кубика потерь меньше
     */
    @Schema(description = "Защитные стены бастиона")
    public record DefenseWalls(int built, int building, Integer readyOnTurn, boolean enclosed) {
    }

    /**
     * Гость бастиона из событий «Гость» и «Беженцы».
     *
     * @param count        сколько их: у беженцев 2к4
     * @param leavesOnTurn ход, на котором гость уедет сам; null — пока не отошлют
     */
    @Schema(description = "Гость бастиона")
    public record Guest(String id, BastionLabel kind, String name, int count, int arrivedOnTurn,
                        Integer leavesOnTurn) {
    }

    /**
     * Вольеры зверинца. Место считается в четвертях большого существа: большое занимает 4,
     * маленькое или среднее — 1.
     *
     * @param capacity       вместимость, четвертей
     * @param occupied       занято существами, четвертей
     * @param creatures      существа в вольерах
     * @param beasts         таблица «Существа зверинца»
     * @param challengeCosts цены существ не из таблицы по показателю опасности
     */
    @Schema(description = "Вольеры зверинца")
    public record Menagerie(int capacity,
                            int occupied,
                            List<Creature> creatures,
                            List<Beast> beasts,
                            List<BastionRules.ChallengeCost> challengeCosts) {
    }

    /**
     * Существо в вольерах.
     *
     * @param space    сколько места занимает, четвертей большого
     * @param defender считается защитником бастиона
     */
    @Schema(description = "Существо зверинца")
    public record Creature(String id, String name, BastionLabel size, String challenge, int space, boolean defender) {
    }

    /**
     * Конюшня. Место считается в половинах большого животного: большое занимает 2,
     * маленькое или среднее — 1.
     *
     * @param capacity         вместимость, половин
     * @param occupied         занято животными, половин
     * @param animals          животные в конюшне
     * @param kinds            таблица «Верховые и прочие животные» с обычными ценами
     * @param saleBonusPercent на сколько процентов дороже обычной цены покупатель платит при продаже
     */
    @Schema(description = "Конюшня")
    public record Stable(int capacity,
                         int occupied,
                         List<Animal> animals,
                         List<AnimalKind> kinds,
                         int saleBonusPercent) {
    }

    /**
     * Животное в конюшне.
     *
     * @param kind          животное из таблицы; null — другое, с согласия Мастера
     * @param space         сколько места занимает, половин большого
     * @param cost          обычная цена, зм
     * @param arrivedOnTurn ход, с которого животное в конюшне
     * @param bonded        прожило в конюшне 14 дней: проверки Ухода за животными с преимуществом
     * @param bestiaryUrl   статблок в бестиарии; null — не найден
     */
    @Schema(description = "Животное конюшни")
    public record Animal(String id, String kind, String name, BastionLabel size, int space, int cost,
                         int arrivedOnTurn, boolean bonded, String bestiaryUrl) {
    }

    /** Животное из таблицы «Верховые и прочие животные» с обычной ценой, зм. */
    @Schema(description = "Животное из таблицы конюшни")
    public record AnimalKind(String value, String name, BastionLabel size, int space, int cost, String bestiaryUrl) {
    }

    /** Существо из таблицы зверинца с ценой вербовки, зм. */
    @Schema(description = "Существо из таблицы зверинца")
    public record Beast(String value, String name, BastionLabel size, int space, int cost) {
    }

    /**
     * Предмет, который мастерская может изготовить.
     *
     * @param priceGp покупная стоимость, зм; null — в справочнике нет числовой цены
     * @param days    сколько дней изготавливать с наёмниками сооружения; null — цена неизвестна
     */
    @Schema(description = "Предмет для изготовления")
    public record CraftableItem(String url, String name, Double priceGp, Integer days) {
    }
}
