package club.ttg.dnd5.domain.bastion.player.event;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionRules;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionLedgerEntry;
import club.ttg.dnd5.domain.bastion.player.activity.PlayerBastionLedgerRepository;
import club.ttg.dnd5.domain.bastion.player.event.PlayerBastionEvent.Decision;
import club.ttg.dnd5.domain.bastion.player.event.PlayerBastionEvent.Status;
import club.ttg.dnd5.domain.bastion.player.model.BastionGuest;
import club.ttg.dnd5.domain.bastion.player.model.MenagerieCreature;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionFacility;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionResponseMapper;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * События бастиона: сразу после приказа «Обслуживать» сервер бросает 1к100 по таблице
 * «События бастиона» за каждого персонажа бастиона и сразу применяет то, что правила
 * решают без выбора: деньги в казну, гибель защитников, неработающие сооружения, гостей.
 * Где выбор за игроком — взятка, удачный случай, просьба о помощи, какой предмет получен, —
 * событие ждёт решения.
 */
@Service
@RequiredArgsConstructor
public class BastionEventService {
    /** Цена удачного случая, зм. */
    public static final long OPPORTUNITY_COST_GP = 500;
    /** Сумма к6 отправленных защитников, с которой просьба о помощи решается без потерь. */
    public static final int AID_SUCCESS_TOTAL = 10;
    private static final int ATTACK_DICE = 6;

    /** Детали «Всё хорошо», 1к100: первые восемь — таблица правил, остальные разнообразят частое событие. */
    static final List<String> ALL_IS_WELL_DETAILS = List.of(
            "Отчётов о проблемах стало намного меньше.",
            "Течь в крыше устранена.",
            "Никаких вредителей, о которых нужно сообщать.",
            "Вы-Знаете-Кто снова потерял свои очки.",
            "Один из ваших наёмников приютил бродячего пса.",
            "Вы получили приятное письмо от друга.",
            "Кто-то подложил людям тухлые яйца в ботинки.",
            "Кто-то подумал, что увидел привидение.",
            "Повар испытал новый рецепт похлёбки, и её даже доели.",
            "Флюгер на башне наконец смазали, и он больше не скрипит.",
            "Кошка бастиона поймала мышь и с гордостью принесла её к дверям.",
            "Во дворе расцвели цветы, которые никто не сажал.",
            "Кладовщик пересчитал запасы, и всё сошлось до последнего мешка.",
            "Часовой всю ночь не спал и даже не жаловался.",
            "У ворот оставили корзину яблок без записки.",
            "Наёмники устроили соревнование по метанию подков, обошлось без травм.",
            "Новая метла оказалась удивительно хорошей.",
            "Кто-то наконец отмыл котёл, который все обходили стороной.",
            "Колодезная вода стала чуть слаще, чем обычно.",
            "Местный бард сочинил о бастионе песенку, и она почти не обидная.",
            "На кухне перестал дымить очаг.",
            "Ворона на стене выучила пару слов и пока повторяет только приличные.",
            "Дети из деревни спорили, кто храбрее, и подходили к воротам по очереди.",
            "Один из наёмников впервые в жизни написал письмо домой.",
            "Ласточки свили гнездо под крышей конюшни.",
            "Плотник выровнял скрипучую ступеньку, о которую все спотыкались.",
            "Странный запах из подвала оказался забытой головкой сыра.",
            "Ночью прошёл тёплый дождь, и к утру во дворе стало свежо.",
            "Все свечи в коридоре догорели ровно, ни одна не капнула на ковёр.",
            "Кузнец выковал новые петли для ворот, и они закрываются без грохота.",
            "Старый пёс бастиона весь день проспал на солнце.",
            "Торговец привёз соль дешевле, чем в прошлый раз.",
            "Сторож нашёл пропавший ключ от погреба в собственном кармане.",
            "Наёмники спорили о том, чей сапог воняет сильнее, и решили спор миром.",
            "Флаг над бастионом выстирали, и цвета снова яркие.",
            "В саду созрели первые помидоры.",
            "Кто-то оставил на столе в зале свежий хлеб для всех.",
            "Мыши ушли из амбара, видимо, к соседям.",
            "Часы на башне показывают правильное время уже третий день подряд.",
            "Прачка нашла в кармане старой куртки серебряную монетку.",
            "Во дворе подрались два петуха, победила курица.",
            "Новый наёмник наконец запомнил имена всех остальных.",
            "На стене обнаружили забавный рисунок углём, очень похожий на кастеляна.",
            "В библиотеке нашли закладку, оставленную лет двадцать назад.",
            "Мостик через ров починили, и он больше не качается.",
            "Наёмники затеяли игру в кости, никто не проигрался.",
            "В окрестностях видели оленя, охотники говорят, что красивого.",
            "Камин в главном зале разжёгся с первой искры.",
            "Гонец доставил письмо вовремя и даже не помял его.",
            "Сапожник подшил всем сапоги, и в дождь ноги остались сухими.",
            "Кто-то посадил у ворот рябину, и она прижилась.",
            "Повар обещает пирог к празднику, и все уже ждут.",
            "Козу, сбежавшую на прошлой неделе, нашли на крыше сарая.",
            "Старая лестница в башню выдержала ещё одну зиму.",
            "Наёмники спели хором, вышло неожиданно неплохо.",
            "На рынке хвалили порядок в бастионе.",
            "Мельник прислал муку с запиской «спасибо за защиту».",
            "Окна в зале отмыли, и стало видно, какие они на самом деле большие.",
            "Стражники поспорили, сколько звёзд на небе, и считали до утра.",
            "Ученик кузнеца выковал свой первый гвоздь и хранит его как реликвию.",
            "Кто-то наконец выбросил треснутый горшок, который стоял тут с основания бастиона.",
            "В погребе нашлась бочка эля, о которой все забыли.",
            "Соседский фермер одолжил телегу и вернул её целой.",
            "Во дворе поселился ёж, и наёмники кормят его молоком.",
            "Бастион обошли бродячие артисты, но оставили хорошее настроение.",
            "Мастер-каменщик похвалил кладку стен.",
            "Дым из трубы поднимается ровно, значит, погода будет хорошей.",
            "Наёмники вычистили конюшню без напоминаний.",
            "Колокол на воротах отполировали до блеска.",
            "В деревне говорят, что рядом с бастионом спокойнее спится.",
            "Пропавшая ложка нашлась в супе, и никто не пострадал.",
            "Из леса донёсся вой, но это был всего лишь ветер.",
            "Новый замок на кладовой работает, а старый стал украшением.",
            "Целитель никого не лечил всю неделю и откровенно скучает.",
            "Наёмница научила остальных завязывать морской узел.",
            "Мимо проезжал рыцарь и вежливо приподнял шлем.",
            "Обеденный стол почистили от воска, и он снова блестит.",
            "На заднем дворе нашли старую подкову и прибили над дверью на удачу.",
            "Писарь закончил переписывать хронику бастиона без единой кляксы.",
            "Хозяйственные счета сошлись с первого раза.",
            "Летучие мыши на чердаке ведут себя тихо.",
            "Наёмники вернулись с рынка со всем, что было в списке.",
            "У ворот выросла тыква размером с бочонок.",
            "Кто-то развесил во дворе гирлянду из сушёных трав, и стало приятно пахнуть.",
            "Ночной обход прошёл без происшествий, если не считать пары сов.",
            "Один из наёмников бросил курить трубку, остальные этому рады.",
            "Сломанное колесо телеги заменили, и она снова на ходу.",
            "Из столицы пришли добрые вести.",
            "Ворота смазали, и теперь они открываются бесшумно.",
            "В пекарне перестал подгорать хлеб.",
            "Сторожевой пёс подружился с кошкой, и оба выглядят озадаченными.",
            "Сапоги, пропавшие на прошлой неделе, нашлись под кроватью владельца.",
            "Кто-то вырезал на скамье забавную рожицу, и её решили не стирать.",
            "Утренний туман рассеялся к завтраку.",
            "Наёмники устроили баню и вышли из неё как новые.",
            "Старый гобелен в зале почистили, и на нём обнаружился дракон.",
            "Гуси во дворе подняли тревогу из-за листа, но в целом бдительны.",
            "Вечером во дворе все вместе смотрели на звездопад.",
            "Повар пообещал больше никогда не класть в кашу репу, и все вздохнули с облегчением.",
            "Вокруг бастиона за неделю не видели ни одного подозрительного лица.");

    /** Таблица гостей, 1к4. */
    static final List<String> GUESTS = List.of(
            "Очень известная личность гостит 7 дней и, уезжая, оставит рекомендательное письмо.",
            "Гость просит убежища от преследований за свои убеждения или преступления и уедет через 7 дней.",
            "Гость — наёмник: дополнительный защитник бастиона, пока его не отошлют или не убьют.",
            "Гость — дружественный монстр, например медный дракон или трент: при атаке он защитит бастион, "
                    + "и защитники не погибнут.");

    /** Таблица сокровищ, 1к100: верхняя граница строки и что бросать в главе 7. */
    static final List<TreasureRow> TREASURES = List.of(
            new TreasureRow(40, "предмет искусства стоимостью 25 зм"),
            new TreasureRow(63, "предмет искусства стоимостью 250 зм"),
            new TreasureRow(73, "предмет искусства стоимостью 750 зм"),
            new TreasureRow(75, "предмет искусства стоимостью 2 500 зм"),
            new TreasureRow(90, "обычный магический предмет из таблицы по вашему выбору "
                    + "(Магия, Вооружение, Инструменты или Реликвии)"),
            new TreasureRow(98, "необычный магический предмет из таблицы по вашему выбору "
                    + "(Магия, Вооружение, Инструменты или Реликвии)"),
            new TreasureRow(100, "редкий магический предмет из таблицы по вашему выбору "
                    + "(Магия, Вооружение, Инструменты или Реликвии)"));

    private final PlayerBastionEventRepository eventRepository;
    private final PlayerBastionLedgerRepository ledgerRepository;
    private final BastionDice dice;

    /** Строка таблицы сокровищ. */
    record TreasureRow(int to, String text) {
    }

    /** Защитник бастиона, которого может унести атака или просьба о помощи. */
    private record Defender(String name, Runnable remove) {
    }

    /**
     * Бросает событие за каждого персонажа: если «Обслуживать» отдают несколько
     * персонажей, каждое событие разрешается отдельно, даже если бастионы объединены.
     */
    public List<PlayerBastionEvent> rollForMaintain(PlayerBastion bastion,
                                                    Map<String, BastionFacility> reference,
                                                    UUID userId) {
        List<PlayerBastionEvent> events = new ArrayList<>();
        for (PlayerBastionMember member : bastion.getMembers()) {
            events.add(roll(bastion, member, reference, userId, null, null));
        }
        return events;
    }

    /** Обслуживали ли бастион с событиями в этот ход. */
    public boolean rolledOnTurn(PlayerBastion bastion, int turn) {
        return eventRepository.existsByBastionIdAndTurn(bastion.getId(), turn);
    }

    /**
     * Бросок 1к100 за персонажа и немедленное разрешение события.
     *
     * @param parentId событие, из-за которого бросают ещё раз
     * @param reroll   результат, который при повторном броске перебрасывается
     */
    private PlayerBastionEvent roll(PlayerBastion bastion, PlayerBastionMember member,
                                    Map<String, BastionFacility> reference, UUID userId,
                                    UUID parentId, BastionEventType reroll) {
        int roll;
        BastionEventType type;
        do {
            roll = dice.roll(100);
            type = BastionEventType.of(roll);
        } while (type == reroll);

        PlayerBastionEvent event = new PlayerBastionEvent();
        event.setBastionId(bastion.getId());
        event.setMemberId(member.getId());
        event.setCharacterName(member.getCharacterName());
        event.setTurn(bastion.getTurn());
        event.setRoll(roll);
        event.setType(type);
        event.setParentId(parentId);
        event.setResolvedBy(userId);

        BastionEventDetails details = event.getDetails();
        switch (type) {
            case ALL_IS_WELL -> {
                details.setDetailRoll(dice.roll(ALL_IS_WELL_DETAILS.size()));
                details.setDetail(ALL_IS_WELL_DETAILS.get(details.getDetailRoll() - 1));
                event.setSummary("Ничего существенного не происходит. " + details.getDetail());
            }
            case ATTACK -> event.setSummary(attack(bastion, member, reference, event));
            case CRIMINAL_HIRELING -> criminal(member, reference, event);
            case EXTRAORDINARY_OPPORTUNITY -> {
                event.setStatus(Status.PENDING);
                details.setAmountGp(OPPORTUNITY_COST_GP);
                event.setSummary("Бастиону выпала возможность провести фестиваль, профинансировать исследование "
                        + "заклинателя или умиротворить дворянина — детали с Мастером. Воспользоваться стоит "
                        + "%d зм: бастион получит признание, и Мастер бросит по таблице событий ещё раз."
                        .formatted(OPPORTUNITY_COST_GP));
            }
            case FRIENDLY_VISITORS -> {
                long amount = hundreds();
                details.setAmountGp(amount);
                String target = randomFacility(member, reference, false)
                        .map(facility -> remember(details, facility, reference))
                        .map("«%s»"::formatted)
                        .orElse("одно из специализированных сооружений");
                credit(bastion, amount, type, userId);
                event.setSummary("Дружелюбные посетители просят ненадолго воспользоваться %s и платят %d зм. "
                        .formatted(target, amount) + "Приказы сооружения это не прерывает.");
            }
            case GUEST -> guest(bastion, event, userId);
            case LOST_HIRELINGS -> event.setSummary(randomFacility(member, reference, true)
                    .map(facility -> {
                        String name = remember(details, facility, reference);
                        loseHirelings(facility, hirelings(facility, reference, event.getTurn()), event.getTurn());
                        disable(facility, event.getTurn() + 1, "Наёмники пропали");
                        return "Наёмники «%s» пропали: сооружение не работает в ход %d, в конце хода им бесплатно "
                                .formatted(name, event.getTurn() + 1) + "найдут замену.";
                    })
                    .orElse("Наёмники могли бы пропасть, но специализированных сооружений нет."));
            case MAGICAL_DISCOVERY -> {
                event.setStatus(Status.PENDING);
                event.setSummary("Наёмники бесплатно находят или случайно создают необычный магический предмет "
                        + "на ваш выбор — зелье или свиток.");
            }
            case REFUGEES -> refugees(bastion, event, userId);
            case REQUEST_FOR_AID -> {
                event.setStatus(Status.PENDING);
                event.setSummary("Местный лидер просит помощи: идёт поиск пропавшего или в округе орудуют "
                        + "разбойники. Отправьте защитников и бросьте к6 за каждого: от %d — награда 1к6 × 100 зм, "
                        .formatted(AID_SUCCESS_TOTAL) + "меньше — половина награды и гибель одного защитника.");
            }
            case TREASURE -> {
                details.setDetailRoll(dice.roll(100));
                details.setDetail(TREASURES.stream()
                        .filter(row -> details.getDetailRoll() <= row.to())
                        .findFirst()
                        .map(TreasureRow::text)
                        .orElseThrow());
                event.setStatus(Status.PENDING);
                event.setSummary("Бастион получает сокровище: %s. Как оно попало в бастион — наследство, подарок, "
                        .formatted(details.getDetail()) + "кража или находка — решаете вы.");
            }
        }
        if (event.isPending()) {
            event.setResolvedBy(null);
        }
        return eventRepository.save(event);
    }

    /**
     * Атака: враги терпят поражение, но за каждую 1 на 6к6 гибнет защитник. Дружественный
     * монстр защищает бастион без потерь и уходит; беженцы уходят в любом случае. Бастион,
     * полностью окружённый защитными стенами, бросает на 2 кубика меньше. Если
     * защитников нет, случайное специализированное сооружение повреждено на следующий ход.
     */
    private String attack(PlayerBastion bastion, PlayerBastionMember member,
                          Map<String, BastionFacility> reference, PlayerBastionEvent event) {
        BastionEventDetails details = event.getDetails();
        StringBuilder summary = new StringBuilder("Враждебные силы атакуют бастион и терпят поражение.");
        if (bastion.getGuests().removeIf(guest -> guest.getKind() == BastionGuest.Kind.REFUGEES)) {
            summary.append(" Беженцы покидают бастион.");
        }

        Optional<BastionGuest> monster = bastion.getGuests().stream()
                .filter(guest -> guest.getKind() == BastionGuest.Kind.MONSTER)
                .findFirst();
        if (monster.isPresent()) {
            bastion.getGuests().remove(monster.get());
            return summary.append(" %s защищает бастион, защитники не погибли, и монстр уходит."
                    .formatted(monster.get().getName())).toString();
        }

        List<Defender> defenders = defenders(bastion, member, reference);
        if (defenders.isEmpty()) {
            return summary.append(randomFacility(member, reference, true)
                    .map(facility -> {
                        String name = remember(details, facility, reference);
                        disable(facility, event.getTurn() + 1, "Повреждено при атаке");
                        return " Защитников нет: «%s» повреждено и не работает в ход %d, потом его бесплатно починят."
                                .formatted(name, event.getTurn() + 1);
                    })
                    .orElse(" Защитников и специализированных сооружений нет.")).toString();
        }

        int diceCount = bastion.isDefenseWallsEnclosed()
                ? ATTACK_DICE - BastionRules.DEFENSIVE_WALL_DEFENDER_DICE_REDUCTION
                : ATTACK_DICE;
        if (bastion.isDefenseWallsEnclosed()) {
            summary.append(" Бастион окружён защитными стенами.");
        }
        details.setDice(dice.rolls(diceCount, 6));
        long ones = details.getDice().stream().filter(value -> value == 1).count();
        List<String> lost = lose(defenders, (int) Math.min(ones, defenders.size()));
        details.setLost(lost);
        return summary.append(lost.isEmpty()
                ? " На %dк6 ни одной 1 — защитники целы.".formatted(diceCount)
                : " На %dк6 единиц: %d — погибли: %s.".formatted(diceCount, ones, String.join(", ", lost))).toString();
    }

    /** Наёмник с криминальным прошлым: случайное специализированное сооружение с наёмниками. */
    private void criminal(PlayerBastionMember member, Map<String, BastionFacility> reference,
                          PlayerBastionEvent event) {
        Optional<PlayerBastionFacility> facility = randomFacility(member, reference,
                item -> item.isReady() && hirelings(item, reference, event.getTurn()) > 0);
        if (facility.isEmpty()) {
            event.setSummary("За наёмником пришли с ордером на арест, но в специализированных сооружениях "
                    + "наёмников нет.");
            return;
        }
        BastionEventDetails details = event.getDetails();
        String name = remember(details, facility.get(), reference);
        details.setAmountGp(hundreds());
        event.setStatus(Status.PENDING);
        event.setSummary("Чиновники пришли с ордером на арест наёмника из «%s». Оставить его можно за взятку %d зм, "
                .formatted(name, details.getAmountGp()) + "иначе его арестуют и увезут.");
    }

    /** Гость по 1к4. */
    private void guest(PlayerBastion bastion, PlayerBastionEvent event, UUID userId) {
        BastionEventDetails details = event.getDetails();
        details.setDetailRoll(dice.roll(GUESTS.size()));
        details.setDetail(GUESTS.get(details.getDetailRoll() - 1));
        BastionGuest guest = new BastionGuest();
        guest.setId(UUID.randomUUID().toString());
        guest.setArrivedOnTurn(event.getTurn());
        String summary = "Дружелюбный гость приезжает в бастион. " + details.getDetail();
        switch (details.getDetailRoll()) {
            case 1 -> {
                guest.setKind(BastionGuest.Kind.CELEBRITY);
                guest.setName("Известная личность");
                guest.setLeavesOnTurn(event.getTurn() + 1);
            }
            case 2 -> {
                guest.setKind(BastionGuest.Kind.SANCTUARY);
                guest.setName("Гость, просящий убежища");
                guest.setLeavesOnTurn(event.getTurn() + 1);
                details.setAmountGp(hundreds());
                credit(bastion, details.getAmountGp(), event.getType(), userId);
                summary += " Перед отъездом он дарит %d зм.".formatted(details.getAmountGp());
            }
            case 3 -> {
                guest.setKind(BastionGuest.Kind.DEFENDER);
                guest.setName("Наёмник-гость");
            }
            default -> {
                guest.setKind(BastionGuest.Kind.MONSTER);
                guest.setName("Дружественный монстр");
            }
        }
        bastion.getGuests().add(guest);
        event.setSummary(summary);
    }

    /** Беженцы: 2к4 человек, платят 1к6 × 100 зм и остаются. */
    private void refugees(PlayerBastion bastion, PlayerBastionEvent event, UUID userId) {
        BastionEventDetails details = event.getDetails();
        details.setDice(dice.rolls(2, 4));
        details.setCount(details.getDice().stream().mapToInt(Integer::intValue).sum());
        details.setAmountGp(hundreds());
        credit(bastion, details.getAmountGp(), event.getType(), userId);

        BastionGuest guest = new BastionGuest();
        guest.setId(UUID.randomUUID().toString());
        guest.setKind(BastionGuest.Kind.REFUGEES);
        guest.setName("Беженцы");
        guest.setCount(details.getCount());
        guest.setArrivedOnTurn(event.getTurn());
        bastion.getGuests().add(guest);
        event.setSummary("Беженцы (%d), спасающиеся от катастрофы, ищут убежища и платят %d зм за гостеприимство. "
                .formatted(details.getCount(), details.getAmountGp())
                + "Если базового сооружения на них не хватает, они разбивают лагерь за стенами. Остаются, пока им "
                + "не найдут новый дом или бастион не атакуют.");
    }

    // ------------------------------------------------------------------ решения

    /**
     * Решение по событию, которое ждёт выбора игрока.
     *
     * @param defenders сколько защитников отправить на помощь
     * @param note      какой предмет получен
     */
    public void decide(PlayerBastion bastion, PlayerBastionEvent event, Decision decision, Integer defenders,
                       String note, Map<String, BastionFacility> reference, UUID userId) {
        if (!event.isPending()) {
            throw conflict("Событие уже разрешено");
        }
        if (decision == null || !decisions(event.getType()).contains(decision)) {
            throw badRequest("Для события «%s» такого решения нет".formatted(event.getType().getName()));
        }
        Optional<PlayerBastionMember> member = bastion.getMembers().stream()
                .filter(item -> item.getId().equals(event.getMemberId()))
                .findFirst();
        String outcome = switch (event.getType()) {
            case CRIMINAL_HIRELING -> decision == Decision.PAY
                    ? payBribe(bastion, event, userId)
                    : arrest(bastion, event, reference);
            case EXTRAORDINARY_OPPORTUNITY -> {
                if (decision == Decision.REFUSE) {
                    yield "Возможность упущена.";
                }
                charge(bastion, OPPORTUNITY_COST_GP, event.getType(), userId);
                PlayerBastionEvent next = roll(bastion, member.orElseThrow(
                                () -> conflict("Персонажа этого события в бастионе больше нет")),
                        reference, userId, event.getId(), BastionEventType.EXTRAORDINARY_OPPORTUNITY);
                yield "Заплачено %d зм — бастион получил признание. Новое событие: %s."
                        .formatted(OPPORTUNITY_COST_GP, next.getType().getName());
            }
            case REQUEST_FOR_AID -> decision == Decision.REFUSE
                    ? "Бастион не стал помогать."
                    : sendAid(bastion, member.orElse(null), event, defenders, reference, userId);
            default -> {
                String item = Optional.ofNullable(note).filter(StringUtils::hasText).map(String::trim)
                        .orElseThrow(() -> badRequest("Запишите, какой предмет получил бастион"));
                event.setNote(item);
                yield "Получено: %s.".formatted(item);
            }
        };
        event.setSummary(event.getSummary() + " " + outcome);
        event.setDecision(decision);
        event.setStatus(Status.RESOLVED);
        event.setResolvedBy(userId);
        eventRepository.save(event);
    }

    /** Решения, которые принимает событие. */
    public static List<Decision> decisions(BastionEventType type) {
        return switch (type) {
            case CRIMINAL_HIRELING, EXTRAORDINARY_OPPORTUNITY -> List.of(Decision.PAY, Decision.REFUSE);
            case REQUEST_FOR_AID -> List.of(Decision.SEND, Decision.REFUSE);
            case MAGICAL_DISCOVERY, TREASURE -> List.of(Decision.RECORD);
            default -> List.of();
        };
    }

    private String payBribe(PlayerBastion bastion, PlayerBastionEvent event, UUID userId) {
        charge(bastion, event.getDetails().getAmountGp(), event.getType(), userId);
        return "Взятка %d зм уплачена, наёмник остаётся.".formatted(event.getDetails().getAmountGp());
    }

    /** Наёмника увозят; сооружение без наёмников не работает в следующий ход. */
    private static String arrest(PlayerBastion bastion, PlayerBastionEvent event,
                                 Map<String, BastionFacility> reference) {
        BastionEventDetails details = event.getDetails();
        Optional<PlayerBastionFacility> facility = bastion.getMembers().stream()
                .flatMap(member -> member.getFacilities().stream())
                .filter(item -> item.getId().equals(details.getFacilityId()))
                .findFirst();
        if (facility.isEmpty()) {
            return "Наёмника арестовали и увезли, ему бесплатно найдут замену.";
        }
        int left = hirelings(facility.get(), reference, event.getTurn());
        loseHirelings(facility.get(), Math.min(1, left), event.getTurn());
        if (left <= 1) {
            disable(facility.get(), event.getTurn() + 1, "Наёмника арестовали");
            return "Наёмника арестовали и увезли: «%s» без наёмников не работает в ход %d, потом ему бесплатно "
                    .formatted(details.getFacilityName(), event.getTurn() + 1) + "найдут замену.";
        }
        return "Наёмника арестовали и увезли, ему бесплатно найдут замену.";
    }

    /** Отправленные защитники бросают к6: от 10 — награда, меньше — половина и гибель одного. */
    private String sendAid(PlayerBastion bastion, PlayerBastionMember member, PlayerBastionEvent event,
                           Integer count, Map<String, BastionFacility> reference, UUID userId) {
        List<Defender> defenders = member == null ? List.of() : defenders(bastion, member, reference);
        if (count == null || count < 1 || count > defenders.size()) {
            throw badRequest("Отправить можно от 1 до %d защитников".formatted(defenders.size()));
        }
        BastionEventDetails details = event.getDetails();
        details.setCount(count);
        details.setDice(dice.rolls(count, 6));
        int total = details.getDice().stream().mapToInt(Integer::intValue).sum();
        long reward = hundreds();
        if (total >= AID_SUCCESS_TOTAL) {
            details.setAmountGp(reward);
            credit(bastion, reward, event.getType(), userId);
            return "Отправлено защитников: %d, на к6 выпало %d — проблема решена, награда %d зм."
                    .formatted(count, total, reward);
        }
        details.setAmountGp(reward / 2);
        credit(bastion, reward / 2, event.getType(), userId);
        details.setLost(lose(new ArrayList<>(defenders), 1));
        return "Отправлено защитников: %d, на к6 выпало %d — проблема решена, но награда вдвое меньше: %d зм, "
                .formatted(count, total, reward / 2) + "погиб: %s.".formatted(details.getLost().getFirst());
    }

    // ------------------------------------------------------------------ гости

    /**
     * Гости, чей срок вышел, уезжают к наступающему ходу.
     *
     * @return строки для журнала хода
     */
    public List<String> departures(PlayerBastion bastion, int next) {
        List<String> summary = new ArrayList<>();
        bastion.getGuests().removeIf(guest -> {
            if (guest.getLeavesOnTurn() == null || guest.getLeavesOnTurn() > next) {
                return false;
            }
            summary.add(guest.getKind() == BastionGuest.Kind.CELEBRITY
                    ? "Известная личность уезжает и оставляет рекомендательное письмо"
                    : "%s уезжает".formatted(guest.getName()));
            return true;
        });
        return summary;
    }

    /** Защитники персонажа, которых можно отправить на помощь. */
    public int defenderCount(PlayerBastion bastion, PlayerBastionMember member, Map<String, BastionFacility> reference) {
        return member == null ? 0 : defenders(bastion, member, reference).size();
    }

    // ------------------------------------------------------------------ помощники

    /**
     * Защитники бастиона персонажа: живущие в бараке, существа зверинца, которые считаются
     * защитниками, и наёмники-гости.
     */
    private static List<Defender> defenders(PlayerBastion bastion, PlayerBastionMember member,
                                            Map<String, BastionFacility> reference) {
        List<Defender> defenders = new ArrayList<>();
        for (PlayerBastionFacility facility : member.getFacilities()) {
            BastionFacility source = reference.get(facility.getFacilityUrl());
            if (PlayerBastionResponseMapper.isBarrack(source)) {
                for (int index = 0; index < facility.getDefenders(); index++) {
                    defenders.add(new Defender("защитник из «%s»".formatted(source.getName()),
                            () -> facility.setDefenders(Math.max(0, facility.getDefenders() - 1))));
                }
            }
            for (MenagerieCreature creature : facility.getCreatures()) {
                if (creature.isDefender()) {
                    defenders.add(new Defender(creature.getName(), () -> facility.getCreatures().remove(creature)));
                }
            }
        }
        for (BastionGuest guest : bastion.getGuests()) {
            if (guest.getKind() == BastionGuest.Kind.DEFENDER) {
                defenders.add(new Defender(guest.getName(), () -> bastion.getGuests().remove(guest)));
            }
        }
        return defenders;
    }

    /** Случайно выбранные защитники погибают. */
    private List<String> lose(List<Defender> defenders, int count) {
        List<Defender> left = new ArrayList<>(defenders);
        List<String> lost = new ArrayList<>();
        for (int index = 0; index < count && !left.isEmpty(); index++) {
            Defender defender = dice.pick(left);
            left.remove(defender);
            defender.remove().run();
            lost.add(defender.name());
        }
        return lost;
    }

    /** Случайное специализированное сооружение персонажа; {@code ready} — только готовые. */
    private Optional<PlayerBastionFacility> randomFacility(PlayerBastionMember member,
                                                           Map<String, BastionFacility> reference,
                                                           boolean ready) {
        return randomFacility(member, reference, facility -> !ready || facility.isReady());
    }

    private Optional<PlayerBastionFacility> randomFacility(PlayerBastionMember member,
                                                           Map<String, BastionFacility> reference,
                                                           Predicate<PlayerBastionFacility> filter) {
        List<PlayerBastionFacility> special = member.getFacilities().stream()
                .filter(facility -> Optional.ofNullable(reference.get(facility.getFacilityUrl()))
                        .map(BastionFacility::getCategory)
                        .orElse(null) == FacilityCategory.SPECIAL)
                .filter(filter)
                .toList();
        return special.isEmpty() ? Optional.empty() : Optional.of(dice.pick(special));
    }

    /** Сколько наёмников сейчас в сооружении: по справочнику за вычетом забранных событиями. */
    private static int hirelings(PlayerBastionFacility facility, Map<String, BastionFacility> reference, int turn) {
        int total = Optional.ofNullable(reference.get(facility.getFacilityUrl()))
                .map(BastionFacility::getHirelings)
                .orElse(0);
        return Math.max(0, total - facility.missingHirelings(turn));
    }

    /**
     * Событие забирает наёмников в ход {@code turn}: замену им бесплатно находят в конце
     * следующего хода, поэтому вернутся они через ход.
     */
    private static void loseHirelings(PlayerBastionFacility facility, int count, int turn) {
        if (count < 1) {
            return;
        }
        facility.setHirelingsLost(facility.missingHirelings(turn) + count);
        facility.setHirelingsBackOnTurn(turn + 2);
    }

    private static String remember(BastionEventDetails details, PlayerBastionFacility facility,
                                   Map<String, BastionFacility> reference) {
        String name = Optional.ofNullable(reference.get(facility.getFacilityUrl()))
                .map(BastionFacility::getName)
                .orElse(facility.getFacilityUrl());
        details.setFacilityId(facility.getId());
        details.setFacilityName(name);
        return name;
    }

    private static void disable(PlayerBastionFacility facility, int turn, String reason) {
        facility.setUnavailableOnTurn(turn);
        facility.setUnavailableReason(reason);
    }

    /** 1к6 × 100 зм. */
    private long hundreds() {
        return dice.roll(6) * 100L;
    }

    private void credit(PlayerBastion bastion, long amount, BastionEventType type, UUID userId) {
        bastion.setTreasuryGp(bastion.getTreasuryGp() + amount);
        record(bastion, amount, type, userId);
    }

    private void charge(PlayerBastion bastion, long amount, BastionEventType type, UUID userId) {
        if (bastion.getTreasuryGp() < amount) {
            throw conflict("В казне %d зм, а нужно %d зм".formatted(bastion.getTreasuryGp(), amount));
        }
        bastion.setTreasuryGp(bastion.getTreasuryGp() - amount);
        record(bastion, -amount, type, userId);
    }

    private void record(PlayerBastion bastion, long amount, BastionEventType type, UUID userId) {
        PlayerBastionLedgerEntry entry = new PlayerBastionLedgerEntry();
        entry.setBastionId(bastion.getId());
        entry.setTurn(bastion.getTurn());
        entry.setAmountGp(amount);
        entry.setReason(PlayerBastionLedgerEntry.Reason.EVENT);
        entry.setNote("Событие «%s»".formatted(type.getName()));
        entry.setCreatedBy(userId);
        ledgerRepository.save(entry);
    }

    private static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    private static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }
}
