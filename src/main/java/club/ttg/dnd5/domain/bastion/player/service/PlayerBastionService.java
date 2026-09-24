package club.ttg.dnd5.domain.bastion.player.service;

import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembers;
import club.ttg.dnd5.domain.bastion.player.client.GameMembershipClient.GameMembership;
import club.ttg.dnd5.domain.bastion.player.client.GameRole;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionMember;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionStatus;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.CreatePlayerBastionRequest;
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Бастионы игроков в играх каталога.
 *
 * <p>Права берутся из актуального состава игры в find-game-api ({@link PlayerBastionAccess}):
 * <ul>
 *     <li>смотреть бастионы игры может любой её участник — мастер и игроки с одобренной заявкой;</li>
 *     <li>создавать, переименовывать, менять состав и архивировать — только мастер;</li>
 *     <li>выбирать сооружения своего персонажа — его игрок, пока бастион в закладке.</li>
 * </ul>
 * Игрок, чью заявку отозвали, теряет доступ сразу (с точностью до кеша клиента), даже если
 * он ещё числится в составе бастиона.</p>
 */
@Service
@RequiredArgsConstructor
public class PlayerBastionService {
    private static final String DEFAULT_CHARACTER_NAME = "Персонаж";

    private final PlayerBastionRepository bastionRepository;
    private final GameMembershipClient membershipClient;
    private final PlayerBastionAccess access;
    private final PlayerBastionResponseMapper mapper;
    private final PlayerBastionFacilityService facilityService;

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
                bastionRepository.findAllByGameIdOrderByCreatedAtAsc(gameId), membership, userId);

        return new PlayerBastionGameResponse(gameId, membership.title(), membership.role(), master, players, bastions);
    }

    @Transactional(readOnly = true)
    public PlayerBastionResponse findById(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        GameMembership membership = access.requireParticipant(bastion.getGameId(), userId);
        return mapper.toResponse(bastion, membership, userId);
    }

    @Transactional
    public PlayerBastionResponse create(CreatePlayerBastionRequest request) {
        UUID userId = access.currentUserId();
        GameMembership membership = access.requireMaster(request.gameId(), userId);

        PlayerBastion bastion = new PlayerBastion();
        bastion.setGameId(request.gameId());
        bastion.setMasterId(membership.masterId());
        bastion.setName(request.name().trim());
        replaceMembers(bastion, Optional.ofNullable(request.members()).orElse(List.of()), request.gameId());

        return mapper.toResponse(bastionRepository.save(bastion), membership, userId);
    }

    @Transactional
    public PlayerBastionResponse update(UUID id, UpdatePlayerBastionRequest request) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        GameMembership membership = access.requireMaster(bastion.getGameId(), userId);
        PlayerBastionAccess.requireNotArchived(bastion);
        PlayerBastionAccess.requireVersion(bastion, request.version());

        bastion.setName(request.name().trim());
        replaceMembers(bastion, request.members(), bastion.getGameId());

        return mapper.toResponse(bastionRepository.saveAndFlush(bastion), membership, userId);
    }

    /**
     * Запускает бастион: закладка закончена, начинаются ходы. Стартовый выбор сооружений
     * после этого закрыт. Нужны персонажи, и у каждого — оба стартовых базовых сооружения.
     */
    @Transactional
    public PlayerBastionResponse activate(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        GameMembership membership = access.requireMaster(bastion.getGameId(), userId);
        if (bastion.getStatus() != PlayerBastionStatus.SETUP) {
            throw new ApiException(HttpStatus.CONFLICT, "Запустить можно только бастион в закладке");
        }
        facilityService.requireSetupComplete(bastion);

        bastion.setStatus(PlayerBastionStatus.ACTIVE);
        return mapper.toResponse(bastionRepository.saveAndFlush(bastion), membership, userId);
    }

    /** Архивирует бастион: игра закончилась, бастион остаётся только для просмотра. */
    @Transactional
    public PlayerBastionResponse archive(UUID id) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(id);
        GameMembership membership = access.requireMaster(bastion.getGameId(), userId);

        bastion.setStatus(PlayerBastionStatus.ARCHIVED);
        return mapper.toResponse(bastionRepository.saveAndFlush(bastion), membership, userId);
    }

    /**
     * Заменяет состав бастиона. Существующие записи обновляются на месте: к ним привязаны
     * сооружения персонажей. Удалённый из состава игрок теряет и сооружения своего персонажа.
     */
    private void replaceMembers(PlayerBastion bastion, List<PlayerBastionMemberRequest> requested, UUID gameId) {
        Set<UUID> seen = new HashSet<>();
        for (PlayerBastionMemberRequest member : requested) {
            if (!seen.add(member.userId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Игрок указан в бастионе дважды");
            }
        }

        Map<UUID, GameMembers.Player> players = requested.isEmpty()
                ? Map.of()
                : membershipClient.members(gameId).players().stream()
                        .collect(Collectors.toMap(GameMembers.Player::userId, Function.identity(), (first, second) -> first));
        for (PlayerBastionMemberRequest member : requested) {
            if (!players.containsKey(member.userId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Доступ к бастиону можно дать только игроку этой игры с одобренной заявкой");
            }
        }

        Map<UUID, PlayerBastionMember> existing = bastion.getMembers().stream()
                .collect(Collectors.toMap(PlayerBastionMember::getUserId, Function.identity()));
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
            member.setCharacterName(characterName(request, players.get(request.userId())));
            member.setCharacterLevel(request.characterLevel());
            member.setCharacterSheetId(request.characterSheetId());
        }
    }

    private static String characterName(PlayerBastionMemberRequest request, GameMembers.Player player) {
        if (StringUtils.hasText(request.characterName())) {
            return request.characterName().trim();
        }
        if (player != null && StringUtils.hasText(player.characterName())) {
            return player.characterName().trim();
        }
        return DEFAULT_CHARACTER_NAME;
    }
}
