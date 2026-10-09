package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditor;
import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheetEditorStatus;
import club.ttg.dnd5.domain.tool.sheet.model.SavedCharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetEditorRepository;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.repository.SavedCharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditAccessListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditAccessResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditRequestCountResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditRequestResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditorListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.CharacterSheetEditorResponse;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByUserIdResponse;
import club.ttg.dnd5.domain.user.service.DisplayNameService;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Права на редактирование чужого листа: запрос, решение владельца и счётчик входящих запросов.
 * <p>
 * Право выдаётся только по запросу. Просить может лишь тот, кто уже сохранил лист по живой ссылке
 * «поделиться», поэтому искать пользователей по логину или почте не нужно и перебирать их некому.
 * Отказ оставляет запись со временем решения: повторный запрос возможен только после паузы, иначе
 * владельца можно было бы засыпать уведомлениями. Отзыв выданного права запись удаляет — это не
 * блокировка, и попросить право заново можно сразу.
 */
@RequiredArgsConstructor
@Service
public class CharacterSheetEditorService {

    /** Сколько редакторов может быть у одного листа одновременно. */
    static final int MAX_EDITORS = 5;

    /** Пауза после отказа, до которой повторный запрос не принимается. */
    static final Duration REQUEST_COOLDOWN = Duration.ofDays(1);

    private static final String SAVED_NOT_FOUND_MESSAGE = "Сохранённый лист персонажа не найден";
    private static final String EDITOR_NOT_FOUND_MESSAGE = "Запрос на редактирование не найден";
    private static final String UNNAMED_USER = "Пользователь без имени";

    private final CharacterSheetEditorRepository editorRepository;
    private final CharacterSheetRepository sheetRepository;
    private final SavedCharacterSheetRepository savedRepository;
    private final DisplayNameService displayNameService;

    /**
     * Запрос на редактирование листа, сохранённого по ссылке. Отозванная ссылка или удалённый лист —
     * 404, как и при просмотре. Повтор идемпотентен: ждущий запрос не дублируется, выданное право
     * остаётся. После отказа новый запрос принимается только по истечении {@link #REQUEST_COOLDOWN}.
     */
    @Transactional
    public CharacterSheetEditRequestResponse requestEdit(UUID savedId) {
        User user = SecurityUtils.getUser();
        SavedCharacterSheet savedSheet = savedRepository.findByIdAndUserId(savedId, user.getUuid())
                .orElseThrow(() -> new EntityNotFoundException(SAVED_NOT_FOUND_MESSAGE));
        CharacterSheet sheet = sheetRepository.findById(savedSheet.getSheetId())
                .filter(found -> !found.isDeleted() && savedSheet.getShareToken().equals(found.getShareToken()))
                .orElseThrow(() -> new EntityNotFoundException(CharacterSheetService.SHARED_NOT_FOUND_MESSAGE));
        CharacterSheetEditor editor = editorRepository.findBySheetIdAndUserId(sheet.getId(), user.getUuid())
                .orElseGet(() -> newRequest(sheet, user));
        if (editor.getStatus() == CharacterSheetEditorStatus.DECLINED) {
            requireCooldownPassed(editor);
            editor.setStatus(CharacterSheetEditorStatus.PENDING);
            editor.setDecidedAt(null);
        }
        return new CharacterSheetEditRequestResponse(editorRepository.save(editor).getStatus());
    }

    /**
     * Неотвеченные запросы и выданные права листа — только владельцу.
     */
    public CharacterSheetEditorListResponse findEditors(UUID sheetId) {
        return toListResponse(getOwnedActive(sheetId));
    }

    /**
     * Разрешает редактирование. Уже выданное право — без изменений; отклонённый запрос разрешить
     * нельзя — пусть пользователь попросит заново. Сверх {@link #MAX_EDITORS} — 409.
     */
    @Transactional
    public CharacterSheetEditorListResponse approve(UUID sheetId, UUID editorId) {
        CharacterSheet sheet = getOwnedActive(sheetId);
        CharacterSheetEditor editor = getEditor(sheet, editorId);
        if (editor.getStatus() == CharacterSheetEditorStatus.DECLINED) {
            throw new ApiException(HttpStatus.CONFLICT, "Запрос уже отклонён");
        }
        if (editor.getStatus() == CharacterSheetEditorStatus.PENDING) {
            if (editorRepository.countBySheetIdAndStatus(sheet.getId(), CharacterSheetEditorStatus.APPROVED)
                    >= MAX_EDITORS) {
                throw new ApiException(HttpStatus.CONFLICT, String.format(
                        "У листа уже %d редакторов — отзовите право у одного из них", MAX_EDITORS));
            }
            editor.setStatus(CharacterSheetEditorStatus.APPROVED);
            editor.setDecidedAt(Instant.now());
        }
        return toListResponse(sheet);
    }

    /**
     * Отклоняет запрос или отзывает выданное право — для владельца это одно действие «убрать».
     * Отклонённый запрос остаётся отказом со временем решения: от него отсчитывается пауза до
     * нового запроса. Отозванное право удаляется целиком: пользователь может попросить его снова.
     */
    @Transactional
    public CharacterSheetEditorListResponse remove(UUID sheetId, UUID editorId) {
        CharacterSheet sheet = getOwnedActive(sheetId);
        CharacterSheetEditor editor = getEditor(sheet, editorId);
        if (editor.getStatus() == CharacterSheetEditorStatus.APPROVED) {
            editorRepository.delete(editor);
        } else if (editor.getStatus() == CharacterSheetEditorStatus.PENDING) {
            editor.setStatus(CharacterSheetEditorStatus.DECLINED);
            editor.setDecidedAt(Instant.now());
        }
        return toListResponse(sheet);
    }

    /**
     * Неотвеченные запросы на активные листы текущего пользователя — для точки у шлема.
     */
    public CharacterSheetEditRequestCountResponse countIncoming() {
        User user = SecurityUtils.getUser();
        return new CharacterSheetEditRequestCountResponse(editorRepository.countPendingForOwner(user.getUuid()));
    }

    /**
     * Права на редактирование сохранённых листов текущего пользователя — без документов, чтобы
     * клиент мог часто спрашивать, не ответил ли владелец. Листы, по которым ничего не запрашивали,
     * в ответ не попадают.
     */
    public CharacterSheetEditAccessListResponse findMyEditAccess() {
        User user = SecurityUtils.getUser();
        List<SavedCharacterSheet> saved = savedRepository.findAllByUserIdOrderByCreatedAtDesc(user.getUuid());
        if (saved.isEmpty()) {
            return new CharacterSheetEditAccessListResponse(List.of());
        }
        List<UUID> sheetIds = saved.stream().map(SavedCharacterSheet::getSheetId).toList();
        Map<UUID, CharacterSheetEditorStatus> statuses = editorRepository
                .findAllByUserIdAndSheetIdIn(user.getUuid(), sheetIds)
                .stream()
                .collect(Collectors.toMap(CharacterSheetEditor::getSheetId, CharacterSheetEditor::getStatus));
        List<CharacterSheetEditAccessResponse> sheets = saved.stream()
                .filter(savedSheet -> statuses.containsKey(savedSheet.getSheetId()))
                .map(savedSheet -> new CharacterSheetEditAccessResponse(savedSheet.getId(),
                        savedSheet.getSheetId(), savedSheet.getName(), statuses.get(savedSheet.getSheetId())))
                .toList();
        return new CharacterSheetEditAccessListResponse(sheets);
    }

    private CharacterSheetEditor newRequest(CharacterSheet sheet, User user) {
        CharacterSheetEditor editor = new CharacterSheetEditor();
        editor.setSheetId(sheet.getId());
        editor.setUserId(user.getUuid());
        editor.setStatus(CharacterSheetEditorStatus.PENDING);
        return editor;
    }

    private void requireCooldownPassed(CharacterSheetEditor editor) {
        Instant decidedAt = editor.getDecidedAt();
        if (decidedAt != null && decidedAt.plus(REQUEST_COOLDOWN).isAfter(Instant.now())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Владелец отклонил запрос. Повторить его можно через сутки после отказа");
        }
    }

    private CharacterSheetEditor getEditor(CharacterSheet sheet, UUID editorId) {
        return editorRepository.findByIdAndSheetId(editorId, sheet.getId())
                .orElseThrow(() -> new EntityNotFoundException(EDITOR_NOT_FOUND_MESSAGE));
    }

    /**
     * Активный лист текущего пользователя. Раздавать права может только владелец: чужой лист — 403,
     * в том числе для его редакторов.
     */
    private CharacterSheet getOwnedActive(UUID sheetId) {
        User user = SecurityUtils.getUser();
        CharacterSheet sheet = sheetRepository.findById(sheetId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Лист персонажа с id %s не существует", sheetId)));
        if (!sheet.getUserId().equals(user.getUuid())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Доступ к листу персонажа запрещен");
        }
        return sheet;
    }

    /**
     * Ждущие запросы и выданные права с именами. Имена догружаются одним запросом; у пользователя
     * без отображаемого имени — общая подпись, логин наружу не отдаётся.
     */
    private CharacterSheetEditorListResponse toListResponse(CharacterSheet sheet) {
        List<CharacterSheetEditor> editors = editorRepository.findAllBySheetIdAndStatusInOrderByCreatedAtAsc(
                sheet.getId(), List.of(CharacterSheetEditorStatus.PENDING, CharacterSheetEditorStatus.APPROVED));
        Map<UUID, DisplayNameByUserIdResponse> names = displayNameService
                .resolveByUserIds(editors.stream().map(CharacterSheetEditor::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(DisplayNameByUserIdResponse::userId, Function.identity()));
        List<CharacterSheetEditorResponse> responses = editors.stream()
                .map(editor -> toResponse(editor, names.get(editor.getUserId())))
                .toList();
        return new CharacterSheetEditorListResponse(MAX_EDITORS, responses);
    }

    private static CharacterSheetEditorResponse toResponse(CharacterSheetEditor editor,
                                                           DisplayNameByUserIdResponse name) {
        return new CharacterSheetEditorResponse(
                editor.getId(),
                name != null ? name.displayName() : UNNAMED_USER,
                name != null ? name.avatarUrl() : null,
                editor.getStatus(),
                editor.getCreatedAt());
    }
}
