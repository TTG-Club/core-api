package club.ttg.dnd5.domain.tool.sheet.service;

import club.ttg.dnd5.domain.tool.sheet.model.CharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.model.SavedCharacterSheet;
import club.ttg.dnd5.domain.tool.sheet.repository.CharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.repository.SavedCharacterSheetRepository;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.SavedCharacterSheetListResponse;
import club.ttg.dnd5.domain.tool.sheet.rest.dto.SavedCharacterSheetResponse;
import club.ttg.dnd5.domain.user.model.User;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.security.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Чужие листы персонажей, сохранённые по ссылке «поделиться»: список, сохранение и удаление.
 * Хранится только ссылка — документ остаётся у владельца листа, а сохранивший видит его на чтение
 * той же публичной ручкой. Своей копией листа заведует {@link CharacterSheetService}.
 */
@RequiredArgsConstructor
@Service
public class SavedCharacterSheetService {

    private static final int MAX_SAVED_SHEETS = 4;
    private static final String NOT_FOUND_MESSAGE = "Сохранённый лист персонажа не найден";

    private final SavedCharacterSheetRepository savedRepository;
    private final CharacterSheetRepository sheetRepository;

    /**
     * Сохранённые ссылки пользователя, новые первее, с лимитом и числом записей (для «N из M»
     * на клиенте). Листы догружаются одним запросом: у доступных отдаётся живое название и документ,
     * у остальных — снимок названия и {@code data = null}.
     */
    public SavedCharacterSheetListResponse findMine() {
        User user = SecurityUtils.getUser();
        List<SavedCharacterSheet> saved = savedRepository.findAllByUserIdOrderByCreatedAtDesc(user.getUuid());
        Map<UUID, CharacterSheet> sheets = sheetRepository
                .findAllById(saved.stream().map(SavedCharacterSheet::getSheetId).toList())
                .stream()
                .collect(Collectors.toMap(CharacterSheet::getId, Function.identity()));
        List<SavedCharacterSheetResponse> responses = saved.stream()
                .map(savedSheet -> toResponse(savedSheet, sheets.get(savedSheet.getSheetId())))
                .toList();
        return new SavedCharacterSheetListResponse(getSavedLimitFor(user), responses.size(), responses);
    }

    /**
     * Сохраняет чужой лист по токену ссылки. Неизвестный, отозванный или битый токен — 404, как и
     * при просмотре по ссылке. Свой лист сохранять незачем: он и так в списке. Повторное сохранение
     * того же листа не плодит записей — у существующей обновляются токен и название, поэтому
     * присланная заново ссылка просто «оживляет» карточку.
     */
    @Transactional
    public SavedCharacterSheetResponse save(String shareToken) {
        User user = SecurityUtils.getUser();
        UUID token = CharacterSheetService.parseShareToken(shareToken);
        CharacterSheet sheet = sheetRepository.findByShareTokenAndDeletedFalse(token)
                .orElseThrow(() -> new EntityNotFoundException(CharacterSheetService.SHARED_NOT_FOUND_MESSAGE));
        if (sheet.getUserId().equals(user.getUuid())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Это ваш лист персонажа — он уже есть в списке ваших персонажей");
        }
        SavedCharacterSheet savedSheet = savedRepository.findByUserIdAndSheetId(user.getUuid(), sheet.getId())
                .orElseGet(() -> newSavedSheet(user, sheet));
        savedSheet.setShareToken(token);
        savedSheet.setName(sheet.getName());
        // Флаш сразу: id новой записи генерируется при INSERT, без него в ответе был бы null,
        // а клиент по нему сразу убирает запись из списка.
        return toResponse(savedRepository.saveAndFlush(savedSheet), sheet);
    }

    /**
     * Убирает сохранённую ссылку. Чужая запись, как и несуществующая, — 404.
     */
    @Transactional
    public void delete(UUID savedId) {
        User user = SecurityUtils.getUser();
        SavedCharacterSheet savedSheet = savedRepository.findByIdAndUserId(savedId, user.getUuid())
                .orElseThrow(() -> new EntityNotFoundException(NOT_FOUND_MESSAGE));
        savedRepository.delete(savedSheet);
    }

    /**
     * Лимит сохранённых чужих листов. Пока константа; с появлением подписок здесь появится
     * расчёт по уровню подписки пользователя — как и у лимита своих листов.
     */
    private int getSavedLimitFor(User user) {
        return MAX_SAVED_SHEETS;
    }

    private void validateLimit(User user) {
        int limit = getSavedLimitFor(user);
        if (savedRepository.countByUserId(user.getUuid()) >= limit) {
            throw new ApiException(HttpStatus.BAD_REQUEST, String.format(
                    "Достигнут лимит сохранённых листов: %d. Уберите один из сохранённых", limit));
        }
    }

    /**
     * Заготовка новой записи. Лимит проверяется здесь, а не в начале сохранения: освежить уже
     * сохранённый лист новой ссылкой можно и при исчерпанном лимите — записей от этого не прибавится.
     */
    private SavedCharacterSheet newSavedSheet(User user, CharacterSheet sheet) {
        validateLimit(user);
        SavedCharacterSheet savedSheet = new SavedCharacterSheet();
        savedSheet.setUserId(user.getUuid());
        savedSheet.setSheetId(sheet.getId());
        return savedSheet;
    }

    /**
     * Доступность записи: лист есть, не удалён и открыт по тому же токену. Совпадение токена
     * обязательно — отзыв доступа владельцем должен гасить сохранённую ссылку, иначе выпущенная
     * позже ссылка для кого-то другого молча вернула бы доступ прежнему зрителю.
     */
    private SavedCharacterSheetResponse toResponse(SavedCharacterSheet savedSheet, CharacterSheet sheet) {
        boolean available = sheet != null
                && !sheet.isDeleted()
                && savedSheet.getShareToken().equals(sheet.getShareToken());
        return new SavedCharacterSheetResponse(
                savedSheet.getId(),
                savedSheet.getSheetId(),
                savedSheet.getShareToken(),
                available ? sheet.getName() : savedSheet.getName(),
                available ? sheet.getData() : null,
                available);
    }
}
