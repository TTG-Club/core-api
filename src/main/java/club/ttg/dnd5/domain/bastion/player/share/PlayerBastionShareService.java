package club.ttg.dnd5.domain.bastion.player.share;

import club.ttg.dnd5.domain.bastion.player.model.PlayerBastion;
import club.ttg.dnd5.domain.bastion.player.model.PlayerBastionEditor;
import club.ttg.dnd5.domain.bastion.player.plan.PlanResponse;
import club.ttg.dnd5.domain.bastion.player.plan.PlayerBastionPlanService;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionEditorRepository;
import club.ttg.dnd5.domain.bastion.player.repository.PlayerBastionRepository;
import club.ttg.dnd5.domain.bastion.player.rest.dto.PlayerBastionResponse;
import club.ttg.dnd5.domain.bastion.player.service.BastionViewer;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionAccess;
import club.ttg.dnd5.domain.bastion.player.service.PlayerBastionResponseMapper;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Доступ к бастиону по ссылке, как у листа персонажа.
 *
 * <p>Владелец включает ссылку; по ней бастион и его план открываются кому угодно, но только на
 * чтение — ручек записи по ссылке нет. Вошедший пользователь может попросить право на правку, а
 * владелец разрешает, отклоняет или отзывает его. Редактор правит бастион наравне с игроками, а
 * вне игры ведёт его вместе с владельцем ({@link BastionViewer}). Отзыв ссылки снимает все
 * запросы и права.</p>
 */
@Service
@RequiredArgsConstructor
public class PlayerBastionShareService {
    private final PlayerBastionRepository bastionRepository;
    private final PlayerBastionEditorRepository editorRepository;
    private final PlayerBastionAccess access;
    private final PlayerBastionResponseMapper mapper;
    private final PlayerBastionPlanService planService;
    private final UserService userService;

    /** Включает ссылку; если она уже есть, возвращает ту же. */
    @Transactional
    public PlayerBastionResponse share(UUID bastionId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireOwner(bastion, userId);
        if (bastion.getShareToken() == null) {
            bastion.setShareToken(UUID.randomUUID());
            bastionRepository.save(bastion);
        }
        return mapper.toResponse(bastion, viewer);
    }

    /** Отзывает ссылку вместе со всеми запросами и правами на правку. */
    @Transactional
    public PlayerBastionResponse revoke(UUID bastionId) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = access.findBastion(bastionId);
        BastionViewer viewer = access.requireOwner(bastion, userId);
        bastion.setShareToken(null);
        bastionRepository.save(bastion);
        editorRepository.deleteAllByBastionId(bastionId);
        return mapper.toResponse(bastion, viewer);
    }

    /** Бастион по ссылке; неизвестная или отозванная ссылка — 404. */
    @Transactional(readOnly = true)
    public SharedPlayerBastionResponse findShared(String token) {
        PlayerBastion bastion = findByToken(token);
        UUID userId = userService.getCurrentUserId().orElse(null);
        return new SharedPlayerBastionResponse(
                mapper.toResponse(bastion, BastionViewer.byLink(bastion, userId)),
                accessOf(bastion, userId));
    }

    /** План бастиона по ссылке: только чтение. */
    @Transactional(readOnly = true)
    public PlanResponse findSharedPlan(String token) {
        return planService.findByLink(findByToken(token));
    }

    /**
     * Просит право на правку бастиона, открытого по ссылке. Повторный запрос ничего не меняет;
     * тот, кто и так может править, получает {@link SharedPlayerBastionResponse.Access#GRANTED}.
     */
    @Transactional
    public SharedPlayerBastionResponse.Access requestEdit(String token) {
        UUID userId = access.currentUserId();
        PlayerBastion bastion = findByToken(token);
        SharedPlayerBastionResponse.Access current = accessOf(bastion, userId);
        if (current != SharedPlayerBastionResponse.Access.NONE) {
            return current;
        }

        PlayerBastionEditor editor = new PlayerBastionEditor();
        editor.setBastionId(bastion.getId());
        editor.setUserId(userId);
        editor.setStatus(PlayerBastionEditor.Status.PENDING);
        editorRepository.save(editor);
        return SharedPlayerBastionResponse.Access.PENDING;
    }

    /** Запросы на правку и выданные права — для владельца. */
    @Transactional(readOnly = true)
    public List<PlayerBastionEditorResponse> findEditors(UUID bastionId) {
        requireOwned(bastionId);
        return editorRepository.findAllByBastionIdOrderByCreatedAtAsc(bastionId).stream()
                .map(PlayerBastionEditorResponse::of)
                .toList();
    }

    /** Владелец разрешает править бастион. */
    @Transactional
    public List<PlayerBastionEditorResponse> approve(UUID bastionId, UUID editorId) {
        requireOwned(bastionId);
        PlayerBastionEditor editor = findEditor(bastionId, editorId);
        if (editor.getStatus() != PlayerBastionEditor.Status.APPROVED) {
            editor.setStatus(PlayerBastionEditor.Status.APPROVED);
            editor.setDecidedAt(Instant.now());
            editorRepository.save(editor);
        }
        return findEditors(bastionId);
    }

    /** Владелец отклоняет запрос или отзывает выданное право. */
    @Transactional
    public List<PlayerBastionEditorResponse> remove(UUID bastionId, UUID editorId) {
        requireOwned(bastionId);
        editorRepository.delete(findEditor(bastionId, editorId));
        return findEditors(bastionId);
    }

    private SharedPlayerBastionResponse.Access accessOf(PlayerBastion bastion, UUID userId) {
        if (userId == null) {
            return SharedPlayerBastionResponse.Access.ANONYMOUS;
        }
        if (canOpen(bastion, userId)) {
            return SharedPlayerBastionResponse.Access.GRANTED;
        }
        return editorRepository.findByBastionIdAndUserId(bastion.getId(), userId)
                .map(editor -> editor.getStatus() == PlayerBastionEditor.Status.APPROVED
                        ? SharedPlayerBastionResponse.Access.GRANTED
                        : SharedPlayerBastionResponse.Access.PENDING)
                .orElse(SharedPlayerBastionResponse.Access.NONE);
    }

    /** Может ли пользователь открыть бастион и без ссылки: владелец, мастер или игрок с доступом. */
    private boolean canOpen(PlayerBastion bastion, UUID userId) {
        try {
            return access.requireViewer(bastion, userId).visible();
        } catch (ApiException forbidden) {
            return false;
        }
    }

    private PlayerBastion findByToken(String token) {
        UUID shareToken;
        try {
            shareToken = UUID.fromString(token);
        } catch (IllegalArgumentException invalid) {
            throw new EntityNotFoundException("Бастион по ссылке не найден");
        }
        return bastionRepository.findWithMembersByShareToken(shareToken)
                .orElseThrow(() -> new EntityNotFoundException("Бастион по ссылке не найден"));
    }

    private void requireOwned(UUID bastionId) {
        access.requireOwner(access.findBastion(bastionId), access.currentUserId());
    }

    private PlayerBastionEditor findEditor(UUID bastionId, UUID editorId) {
        return editorRepository.findById(editorId)
                .filter(editor -> editor.getBastionId().equals(bastionId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Запрос на правку не найден"));
    }
}
