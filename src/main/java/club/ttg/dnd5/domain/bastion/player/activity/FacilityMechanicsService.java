package club.ttg.dnd5.domain.bastion.player.activity;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityMechanic;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.bastion.model.GroveCreature;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionLedgerEntry.Reason;
import club.ttg.dnd5.domain.bastion.player.event.BastionDice;
import club.ttg.dnd5.domain.bastion.player.event.BastionEventService;
import club.ttg.dnd5.domain.bastion.player.model.MenagerieCreature;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Механики специализированных сооружений ({@link FacilityMechanic}), которые разыгрываются
 * приказами: кого вербует сооружение и что выпадает, когда приказ выполнен.
 *
 * <p>Механики, которые срабатывают в событиях бастиона (рыцарь, лазарет, сторонники,
 * нежить, открытые кошельки), — в {@link BastionEventService}.</p>
 */
@Service
@RequiredArgsConstructor
public class FacilityMechanicsService {
    /** Статблок, которым действует сущность лиминального пространства. */
    static final String LIMINAL_ENTITY_NAME = "Неописуемый ужас";

    private final BastionDice dice;
    private final BastionEventService events;
    private final PlayerBastionLedgerRepository ledgerRepository;

    // ------------------------------------------------------------------ вербовка

    /** Приказ «Завербовать» поселяет в сооружении существо по механике сооружения. */
    public static boolean recruitsCreature(BastionFacility reference, BastionOrder order) {
        return order == BastionOrder.RECRUIT && isHousing(reference);
    }

    /** В сооружении живут существа-защитники: роща, кузница конструктов, лиминальное пространство. */
    public static boolean isHousing(BastionFacility reference) {
        return reference != null && (reference.has(FacilityMechanic.NATURE_GROVE)
                || reference.has(FacilityMechanic.CONSTRUCTS)
                || reference.has(FacilityMechanic.LIMINAL_ENTITIES));
    }

    /**
     * Вместимость сооружения с существами: роща — в восьмых большого, лиминальное
     * пространство — сущностей.
     *
     * @return вместимость; {@code null} — без ограничения
     */
    public static Integer housingCapacity(BastionFacility reference) {
        if (reference.has(FacilityMechanic.NATURE_GROVE)) {
            return BastionRules.GROVE_CAPACITY;
        }
        if (reference.has(FacilityMechanic.LIMINAL_ENTITIES)) {
            return BastionRules.LIMINAL_ENTITIES_MAX;
        }
        return null;
    }

    /** Сколько места занимает существо в сооружении. */
    public static int housingSpace(BastionFacility reference, MenagerieCreature creature) {
        return reference.has(FacilityMechanic.NATURE_GROVE) ? BastionRules.groveSpace(creature.getSize()) : 1;
    }

    /** Сколько места в сооружении уже занято. */
    public static int housingOccupied(PlayerBastionFacility facility, BastionFacility reference) {
        return Optional.ofNullable(facility.getCreatures()).orElse(List.of()).stream()
                .mapToInt(creature -> housingSpace(reference, creature))
                .sum();
    }

    private static boolean fits(PlayerBastionFacility facility, BastionFacility reference, MenagerieCreature creature) {
        Integer capacity = housingCapacity(reference);
        return capacity == null
                || housingOccupied(facility, reference) + housingSpace(reference, creature) <= capacity;
    }

    /**
     * Существо, которое сооружение вербует по приказу: из таблицы рощи, конструкт выбранного
     * варианта или сущность демиплана. В сооружении должно хватать места.
     */
    public MenagerieCreature recruit(PlayerBastionFacility facility,
                                     BastionFacility reference,
                                     ActivityRequests.GiveOrder request,
                                     Optional<FacilityOrderOption> option) {
        MenagerieCreature creature = new MenagerieCreature();
        creature.setId(UUID.randomUUID().toString());
        if (reference.has(FacilityMechanic.NATURE_GROVE)) {
            GroveCreature grove = Optional.ofNullable(request.creature())
                    .map(ActivityRequests.RecruitCreature::grove)
                    .orElseThrow(() -> badRequest("Выберите существо из таблицы «%s»".formatted(reference.getName())));
            creature.setGrove(grove);
            creature.setName(grove.getName());
            creature.setSize(grove.getSize());
        } else if (reference.has(FacilityMechanic.CONSTRUCTS)) {
            FacilityOrderOption chosen = option.orElseThrow(
                    () -> badRequest("Выберите, какой конструкт создать в «%s»".formatted(reference.getName())));
            creature.setName(constructName(chosen.getName()));
            creature.setCost(Optional.ofNullable(chosen.getCost()).orElse(0));
        } else {
            creature.setName(LIMINAL_ENTITY_NAME);
            creature.setCost(option.map(FacilityOrderOption::getCost).orElse(0));
        }
        if (!reference.has(FacilityMechanic.NATURE_GROVE) && request.creature() != null) {
            throw badRequest("«%s» не вербует существ по выбору".formatted(reference.getName()));
        }
        if (!fits(facility, reference, creature)) {
            throw conflict("В «%s» нет места для существа «%s»".formatted(reference.getName(), creature.getName()));
        }
        return creature;
    }

    /** «конструкт (Оживлённые доспехи)» → «Оживлённые доспехи». */
    static String constructName(String option) {
        int open = option.indexOf('(');
        int close = option.lastIndexOf(')');
        return open >= 0 && close > open ? option.substring(open + 1, close).trim() : option;
    }

    // ------------------------------------------------------------------ выполнение

    /** Разыгрывает ли сооружение что-то, когда выполнен этот приказ. */
    public static boolean handles(BastionFacility reference, PlayerBastionOrder order) {
        if (reference == null) {
            return false;
        }
        BastionOrder code = order.getOrder();
        return reference.has(FacilityMechanic.INVITATION)
                || recruitsCreature(reference, code)
                || reference.has(FacilityMechanic.SUPPORTERS) && code == BastionOrder.RECRUIT
                || reference.has(FacilityMechanic.PERILOUS_RESEARCH) && code == BastionOrder.RESEARCH
                || reference.has(FacilityMechanic.SALVAGE) && code == BastionOrder.HARVEST
                || reference.has(FacilityMechanic.PERFORMANCE) && code == BastionOrder.TRADE
                || reference.has(FacilityMechanic.CURIOSITIES) && code == BastionOrder.TRADE;
    }

    /**
     * Разыгрывает выполненный приказ: бросок приглашения, новое существо, выручку, диковины.
     *
     * @param reference справочник всех сооружений бастиона
     * @param next      ход, который наступает
     * @return что выпало — для журнала хода и итога приказа
     */
    public String complete(PlayerBastion bastion, PlayerBastionOrder order, PlayerBastionFacility facility,
                           Map<String, BastionFacility> reference, int next, UUID userId) {
        BastionFacility source = reference.get(facility.getFacilityUrl());
        BastionOrder code = order.getOrder();
        List<String> outcome = new ArrayList<>();

        if (source.has(FacilityMechanic.INVITATION)) {
            int roll = dice.roll(6);
            if (roll % 2 != 0) {
                outcome.add("на к6 выпало %d — приглашение отклонено, приказ без эффекта".formatted(roll));
                if (source.has(FacilityMechanic.LIMINAL_ENTITIES)) {
                    BastionEventService.loseHirelings(facility,
                            BastionEventService.hirelings(facility, reference, bastion.getTurn()), bastion.getTurn());
                    BastionEventService.disable(facility, next, "Сущность убила наёмника");
                    outcome.add("сущность убила наёмника: сооружение не работает в ход %d, потом наёмника заменят"
                            .formatted(next));
                }
                return String.join("; ", outcome);
            }
            outcome.add("на к6 выпало %d — приглашение принято".formatted(roll));
        }

        if (order.getCreature() != null && recruitsCreature(source, code)) {
            MenagerieCreature creature = order.getCreature();
            if (fits(facility, source, creature)) {
                facility.getCreatures().add(creature);
                outcome.add("%s поселяется в «%s» и защищает бастион".formatted(creature.getName(), source.getName()));
            } else {
                outcome.add("%s — в «%s» нет места".formatted(creature.getName(), source.getName()));
            }
        }
        if (source.has(FacilityMechanic.SUPPORTERS) && code == BastionOrder.RECRUIT) {
            facility.setSupporters(true);
            outcome.add("сторонники заменят защитника или наёмника, потерянного при следующем событии бастиона");
        }
        if (source.has(FacilityMechanic.PERILOUS_RESEARCH) && code == BastionOrder.RESEARCH) {
            outcome.add(perilousResearch(bastion, facility, reference, next));
        }
        PlayerBastionMember member = facility.getMember();
        if (source.has(FacilityMechanic.SALVAGE) && code == BastionOrder.HARVEST) {
            long amount = events.gold(member, reference, true, () -> dice.roll(4) * 100L);
            income(bastion, amount, "Спасатели — %s".formatted(source.getName()), userId);
            outcome.add("спасатели принесли сокровища на %d зм".formatted(amount));
        }
        if (source.has(FacilityMechanic.PERFORMANCE) && code == BastionOrder.TRADE) {
            long amount = events.gold(member, reference, true, () -> (dice.roll(6) + dice.roll(6)) * 10L);
            income(bastion, amount, "Представление — %s".formatted(source.getName()), userId);
            outcome.add("ваша доля выручки — %d зм".formatted(amount));
        }
        if (source.has(FacilityMechanic.CURIOSITIES) && code == BastionOrder.TRADE) {
            outcome.add(curiosities(bastion, order, reference));
        }
        return outcome.isEmpty() ? null : capitalize(String.join("; ", outcome));
    }

    /** Исследование в хранилище духов: при 1 на к6 наёмник погибает, сооружение простаивает. */
    private String perilousResearch(PlayerBastion bastion, PlayerBastionFacility facility,
                                    Map<String, BastionFacility> reference, int next) {
        int roll = dice.roll(6);
        if (roll != 1) {
            return "на к6 выпало %d — сведения получены, как от «Связи с иным планом»".formatted(roll);
        }
        BastionEventService.loseHirelings(facility,
                BastionEventService.hirelings(facility, reference, bastion.getTurn()), bastion.getTurn());
        BastionEventService.disable(facility, next, "Наёмник погиб при исследовании");
        return ("на к6 выпала 1 — работа истощила разум: наёмник погиб, сооружение не работает в ход %d "
                + "(если исследовали вы, вместо этого — 6к6 урона психической энергией)").formatted(next);
    }

    /**
     * Диковины: число предметов и шанс проклятия — по числу специализированных сооружений
     * бастиона, населённых призраками, на момент возвращения наёмника.
     */
    private String curiosities(PlayerBastion bastion, PlayerBastionOrder order, Map<String, BastionFacility> reference) {
        int haunted = (int) bastion.getMembers().stream()
                .flatMap(member -> member.getFacilities().stream())
                .filter(PlayerBastionFacility::isReady)
                .map(facility -> reference.get(facility.getFacilityUrl()))
                .filter(source -> source != null && source.getCategory() == FacilityCategory.SPECIAL
                        && source.has(FacilityMechanic.HAUNTED))
                .count();
        BastionRules.CuriosityRow row = BastionRules.curiosities(haunted);
        int items = row.itemsDice() == 1 ? 1 : dice.roll(row.itemsDice());
        StringBuilder text = new StringBuilder("сооружений с призраками: %d — наёмник принёс магических предметов: %d"
                .formatted(haunted, items));
        Optional.ofNullable(order.getOptionName()).ifPresent(option -> text.append(" (").append(option).append(')'));
        if (row.cursedTo() > 0) {
            int curse = dice.roll(100);
            text.append(curse <= row.cursedTo()
                    ? ", на к100 выпало %d — один из предметов проклят (какой и как — решает Мастер)".formatted(curse)
                    : ", на к100 выпало %d — проклятых нет".formatted(curse));
        }
        return text.toString();
    }

    private void income(PlayerBastion bastion, long amount, String note, UUID userId) {
        bastion.setTreasuryGp(bastion.getTreasuryGp() + amount);
        PlayerBastionLedgerEntry entry = new PlayerBastionLedgerEntry();
        entry.setBastionId(bastion.getId());
        entry.setTurn(bastion.getTurn());
        entry.setAmountGp(amount);
        entry.setReason(Reason.INCOME);
        entry.setNote(note);
        entry.setCreatedBy(userId);
        ledgerRepository.save(entry);
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    private static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }
}
