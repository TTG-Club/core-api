package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembers;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionEditor;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionEditorRepository;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.CreatePlayerBastionRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.JoinGameRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.MyPlayerBastionsResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionGameResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionMemberRequest;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.rest.dto.UpdatePlayerBastionRequest;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Бастионы игроков.
 *
 * <p>Бастион принадлежит создавшему его пользователю, как лист персонажа, и может быть
 * добавлен в одну игру каталога. Права ({@link BastionViewer}):
 * <ul>
 *     <li>создавать, удалять и добавлять в игру — владелец;</li>
 *     <li>вести бастион (состав, казна, ходы, требования, архив) — мастер его игры, а вне
 *     игры — владелец;</li>
 *     <li>убрать бастион из игры — владелец или мастер этой игры;</li>
 *     <li>смотреть бастион в игре — любой её участник; вне игры — владелец и игроки с
 *     доступом;</li>
 *     <li>выбирать сооружения своего персонажа — его игрок, пока бастион в закладке.</li>
 * </ul></p>
 */
@Service
@RequiredArgsConstructor
public class PlayerBastionService {
    private static final String DEFAULT_CHARACTER_NAME = "Персонаж";

    private final PlayerBastionRepository bastionRepository;
    private final PlayerBastionEditorRepository editorRepository;
    private final GameMembershipClient membershipClient;
    private final PlayerBastionAccess access;
    private final PlayerBastionResponseMapper mapper;
    private final PlayerBastionFacilityService facilityService;
    private final PlayerBastionLimits limits;

    /** Свои бастионы и лимит на создание. */
    @Transactional(readOnly = true)
    public MyPlayerBastionsResponse findMine() {
        UUID userId = access.currentUserId();
        List<PlayerBastionResponse> bastions = mapper.toResponses(
                bastionRepository.findAllByOwnerIdOrderByCreatedAtAsc(userId),
                bastion -> access.requireViewer(bastion, userId));

        List<UUID> sharedIds = editorRepository.findBastionIdsByUserIdAndStatus(userId,
                PlayerBastionEditor.Status.APPROVED);
        List<PlayerBastionResponse> shared = sharedIds.isEmpty()
                ? List.of()
                : mapper.toResponses(bastionRepository.findAllByIdInOrderByCreatedAtAsc(sharedIds),
                        bastion -> access.requireViewer(bastion, userId));

        return new MyPlayerBastionsResponse(limits.forCurrentUser(), PlayerBastionLimits.SUBSCRIBER, bastions,
                shared);
    }

    /** Бастионы игры и, для мастера, игроки, которым можно дать доступ. */
    @Transactional(readOnly = true)
    public PlayerBastionGameResponse findForGame(UUID gameId) {
        UUID userId = access.currentUserId();
        GameMembership membership = access.requireParticipant(gameId, userId);
        boolean master = membership.role() == GameRole.MASTER;

        List<PlayerBastionGameResponse.Player> players = master
                ? membershipClient.members(gameId).players().stream()
                        .map(player -> new PlayerBastionGameResponse.Player(player.userId(), player.characterName()))
                        .toList()
                : List.of();

        List<PlayerBastionResponse> bastions = mapper.toResponses(
                bastionRepository.findAllByGameIdOrderByCreatedAtAsc(gameId),
                bastion -> BastionViewer.inGame(bastion, membership, userId));

        return new PlayerBastionGameResponse(gameId, membership.title(), membership.role(), true, players, bastions);
    }

    @Transactional(readOnly = true)
    public PlayerBastionResponse findById(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        return mapper.toResponse(bastion, access.requireViewer(bastion, userId));
    }

    /** Создаёт бастион вне игры; персонаж владельца сразу получает доступ. */
    @Transactional
    public PlayerBastionResponse create(CreatePlayerBastionRequest request) {
        UUID userId = access.currentUserId();
        int limit = limits.forCurrentUser();
        if (bastionRepository.countByOwnerId(userId) >= limit) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Достигнут лимит бастионов: %d. Удалите один из существующих".formatted(limit));
        }

        PlayerBastion bastion = new PlayerBastion();
        bastion.setOwnerId(userId);
        bastion.setName(request.name().trim());

        PlayerBastionMember member = new PlayerBastionMember();
        member.setBastion(bastion);
        member.setUserId(userId);
        member.setCharacterName(StringUtils.hasText(request.characterName())
                ? request.characterName().trim()
                : DEFAULT_CHARACTER_NAME);
        member.setCharacterLevel(request.characterLevel());
        member.setCharacterSheetId(request.characterSheetId());
        bastion.getMembers().add(member);

        PlayerBastion saved = bastionRepository.save(bastion);
        return mapper.toResponse(saved, BastionViewer.outsideGame(saved, userId));
    }

    @Transactional
    public PlayerBastionResponse update(UUID id, UpdatePlayerBastionRequest request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        BastionViewer viewer = access.requireManager(bastion, userId);
        PlayerBastionAccess.requireNotArchived(bastion);
        PlayerBastionAccess.requireVersion(bastion, request.version());

        bastion.setName(request.name().trim());
        replaceMembers(bastion, request.members());

        return mapper.toResponse(bastionRepository.saveAndFlush(bastion), viewer);
    }

    /** Удаляет бастион вместе с планом и журналами. Только владелец. */
    @Transactional
    public void delete(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        access.requireOwner(bastion, userId);

        bastionRepository.deleteBastion(bastion.getId());
    }

    /**
     * Добавляет свой бастион в игру, участник которой владелец. Бастион может быть только в
     * одной игре; вести его там начинает мастер этой игры.
     */
    @Transactional
    public PlayerBastionResponse joinGame(UUID id, JoinGameRequest request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        access.requireOwner(bastion, userId);
        PlayerBastionAccess.requireNotArchived(bastion);
        if (bastion.getGameId() != null) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Бастион уже в другой игре: сначала уберите его оттуда");
        }
        GameMembership membership = access.requireParticipant(request.gameId(), userId);

        bastion.setGameId(request.gameId());
        bastion.setMasterId(membership.masterId());

        PlayerBastion saved = bastionRepository.saveAndFlush(bastion);
        return mapper.toResponse(saved, BastionViewer.inGame(saved, membership, userId));
    }

    /**
     * Убирает бастион из игры: владелец уходит с ним сам или мастер убирает его из своей игры.
     * Бастион остаётся у владельца со всеми сооружениями, планом и журналами.
     */
    @Transactional
    public PlayerBastionResponse leaveGame(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        BastionViewer viewer = access.requireViewer(bastion, userId);
        if (bastion.getGameId() == null) {
            throw new ApiException(HttpStatus.CONFLICT, "Бастион не добавлен в игру");
        }
        if (!viewer.owner() && !viewer.manager()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Убрать бастион из игры могут его владелец и мастер игры");
        }

        bastion.setGameId(null);
        bastion.setMasterId(null);

        PlayerBastion saved = bastionRepository.saveAndFlush(bastion);
        return mapper.toResponse(saved, BastionViewer.outsideGame(saved, userId));
    }

    /**
     * Запускает бастион: закладка закончена, начинаются ходы. Стартовый выбор сооружений
     * после этого закрыт. Нужны персонажи, и у каждого — оба стартовых базовых сооружения.
     */
    @Transactional
    public PlayerBastionResponse activate(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        BastionViewer viewer = access.requireManager(bastion, userId);
        if (bastion.getStatus() != PlayerBastionStatus.SETUP) {
            throw new ApiException(HttpStatus.CONFLICT, "Запустить можно только бастион в закладке");
        }
        facilityService.requireSetupComplete(bastion);

        bastion.setStatus(PlayerBastionStatus.ACTIVE);
        return mapper.toResponse(bastionRepository.saveAndFlush(bastion), viewer);
    }

    /** Архивирует бастион: игра закончилась, бастион остаётся только для просмотра. */
    @Transactional
    public PlayerBastionResponse archive(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        BastionViewer viewer = access.requireManager(bastion, userId);

        bastion.setStatus(PlayerBastionStatus.ARCHIVED);
        return mapper.toResponse(bastionRepository.saveAndFlush(bastion), viewer);
    }

    /**
     * Заменяет состав бастиона. Существующие записи обновляются на месте: к ним привязаны
     * сооружения персонажей. Удалённый из состава игрок теряет и сооружения своего персонажа.
     * Новым в составе может быть только владелец или, когда бастион в игре, игрок этой игры
     * с одобренной заявкой.
     */
    private void replaceMembers(PlayerBastion bastion, List<PlayerBastionMemberRequest> requested) {
        Set<UUID> seen = new HashSet<>();
        for (PlayerBastionMemberRequest member : requested) {
            if (!seen.add(member.userId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Игрок указан в бастионе дважды");
            }
        }

        Map<UUID, PlayerBastionMember> existing = bastion.getMembers().stream()
                .collect(Collectors.toMap(PlayerBastionMember::getUserId, Function.identity()));
        Map<UUID, GameMembers.Player> players = gamePlayers(bastion, requested, existing);
        for (PlayerBastionMemberRequest member : requested) {
            boolean known = existing.containsKey(member.userId()) || bastion.getOwnerId().equals(member.userId());
            if (!known && !players.containsKey(member.userId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, bastion.getGameId() == null
                        ? "Дать доступ другим игрокам можно, когда бастион добавлен в игру"
                        : "Доступ к бастиону можно дать только игроку этой игры с одобренной заявкой");
            }
        }

        bastion.getMembers().removeIf(member -> !seen.contains(member.getUserId()));

        for (PlayerBastionMemberRequest request : requested) {
            PlayerBastionMember member = existing.get(request.userId());
            if (member == null) {
                member = new PlayerBastionMember();
                member.setBastion(bastion);
                member.setUserId(request.userId());
                bastion.getMembers().add(member);
            } else if (member.getCharacterLevel() != request.characterLevel()) {
                facilityService.requireLevelFits(member, request.characterLevel());
            }
            member.setCharacterName(characterName(request, member, players.get(request.userId())));
            member.setCharacterLevel(request.characterLevel());
            member.setCharacterSheetId(request.characterSheetId());
        }
    }

    /**
     * Игроки игры бастиона — только если в составе есть новые лица: состав игры стоит похода
     * в каталог.
     */
    private Map<UUID, GameMembers.Player> gamePlayers(PlayerBastion bastion,
                                                     List<PlayerBastionMemberRequest> requested,
                                                     Map<UUID, PlayerBastionMember> existing) {
        boolean hasNewcomers = requested.stream()
                .anyMatch(member -> !existing.containsKey(member.userId()));
        if (!hasNewcomers || bastion.getGameId() == null) {
            return Map.of();
        }
        return membershipClient.members(bastion.getGameId()).players().stream()
                .collect(Collectors.toMap(GameMembers.Player::userId, Function.identity(), (first, second) -> first));
    }

    private static String characterName(PlayerBastionMemberRequest request,
                                        PlayerBastionMember member,
                                        GameMembers.Player player) {
        if (StringUtils.hasText(request.characterName())) {
            return request.characterName().trim();
        }
        if (StringUtils.hasText(member.getCharacterName())) {
            return member.getCharacterName();
        }
        if (player != null && StringUtils.hasText(player.characterName())) {
            return player.characterName().trim();
        }
        return DEFAULT_CHARACTER_NAME;
    }
}
