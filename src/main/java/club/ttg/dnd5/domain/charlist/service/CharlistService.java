package club.ttg.dnd5.domain.charlist.service;

import club.ttg.dnd5.domain.charlist.model.Charlist;
import club.ttg.dnd5.domain.charlist.model.CharlistVisibility;
import club.ttg.dnd5.domain.charlist.repository.CharlistRepository;
import club.ttg.dnd5.domain.charlist.rest.dto.CharlistRequest;
import club.ttg.dnd5.domain.charlist.rest.dto.CharlistResponse;
import club.ttg.dnd5.domain.charlist.rest.dto.CharlistShortResponse;
import club.ttg.dnd5.domain.charlist.rest.mapper.CharlistMapper;
import club.ttg.dnd5.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CharlistService {
    private final CharlistRepository charlistRepository;
    private final CharlistMapper charlistMapper;

    /**
     * Получить все чарлисты текущего пользователя.
     */
    @Transactional(readOnly = true)
    public List<CharlistShortResponse> getMyCharlists(UUID ownerId) {
        return charlistRepository.findAllByOwnerId(ownerId).stream()
                .map(charlistMapper::toShortResponse)
                .toList();
    }

    /**
     * Получить чарлист по ID (только владелец).
     */
    @Transactional(readOnly = true)
    public CharlistResponse getCharlistById(UUID charlistId, UUID ownerId) {
        Charlist charlist = charlistRepository.findById(charlistId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Чарлист не найден"));

        if (!charlist.getOwnerId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Нет доступа к чарлисту");
        }

        return charlistMapper.toResponse(charlist);
    }

    /**
     * Получить чарлист по share-токену (доступ по ссылке).
     */
    @Transactional(readOnly = true)
    public CharlistResponse getCharlistByShareToken(String shareToken) {
        Charlist charlist = charlistRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Чарлист не найден"));

        if (charlist.getVisibility() == CharlistVisibility.PRIVATE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Чарлист приватный");
        }

        return charlistMapper.toResponse(charlist);
    }

    /**
     * Получить все публичные чарлисты.
     */
    @Transactional(readOnly = true)
    public List<CharlistShortResponse> getPublicCharlists() {
        return charlistRepository.findAllByVisibility(CharlistVisibility.PUBLIC).stream()
                .map(charlistMapper::toShortResponse)
                .toList();
    }

    /**
     * Создать новый чарлист.
     */
    @Transactional
    public CharlistResponse createCharlist(CharlistRequest request, UUID ownerId) {
        Charlist charlist = new Charlist();
        charlist.setOwnerId(ownerId);
        charlist.setCharacterName(request.getCharacterName());
        charlist.setCharacterLevel(request.getCharacterLevel());
        charlist.setCharacterClass(request.getCharacterClass());
        charlist.setData(request.getData());
        charlist.setVisibility(
                request.getVisibility() != null ? request.getVisibility() : CharlistVisibility.PRIVATE
        );

        if (charlist.getVisibility() == CharlistVisibility.LINK) {
            charlist.setShareToken(UUID.randomUUID().toString());
        }

        charlist = charlistRepository.save(charlist);
        return charlistMapper.toResponse(charlist);
    }

    /**
     * Обновить чарлист.
     */
    @Transactional
    public CharlistResponse updateCharlist(UUID charlistId, CharlistRequest request, UUID ownerId) {
        Charlist charlist = charlistRepository.findById(charlistId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Чарлист не найден"));

        if (!charlist.getOwnerId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Нет доступа к чарлисту");
        }

        charlist.setCharacterName(request.getCharacterName());
        charlist.setCharacterLevel(request.getCharacterLevel());
        charlist.setCharacterClass(request.getCharacterClass());
        charlist.setData(request.getData());

        if (request.getVisibility() != null) {
            CharlistVisibility newVisibility = request.getVisibility();
            // Генерируем share-токен при переключении на доступ по ссылке
            if (newVisibility == CharlistVisibility.LINK && charlist.getShareToken() == null) {
                charlist.setShareToken(UUID.randomUUID().toString());
            }
            charlist.setVisibility(newVisibility);
        }

        charlist = charlistRepository.save(charlist);
        return charlistMapper.toResponse(charlist);
    }

    /**
     * Удалить чарлист.
     */
    @Transactional
    public void deleteCharlist(UUID charlistId, UUID ownerId) {
        Charlist charlist = charlistRepository.findById(charlistId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Чарлист не найден"));

        if (!charlist.getOwnerId().equals(ownerId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Нет доступа к чарлисту");
        }

        charlistRepository.delete(charlist);
    }
}
