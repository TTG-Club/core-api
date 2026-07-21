package club.ttg.dnd5.domain.spellbook.rest.controller;

import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookAddSpellsRequest;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookDetailedResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookListResponse;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookRequest;
import club.ttg.dnd5.domain.spellbook.rest.dto.SpellbookSpellUpdateRequest;
import club.ttg.dnd5.domain.spellbook.service.SpellbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Доступ: только зарегистрированный пользователь. Менять книгу может лишь владелец; остальным
 * она доступна на чтение по ссылке — ключ {@code shareKey} отдаётся владельцу вместе с книгой.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/spellbooks")
@Tag(name = "Книга заклинаний",
        description = "REST API личных книг заклинаний: состав книги по уровням и подготовленные заклинания")
public class SpellbookController {

    private final SpellbookService spellbookService;

    @Operation(summary = "Создание книги: без подписки — не больше трёх, с подпиской — без ограничения. "
            + "Можно сразу передать заклинания в поле spells")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public SpellbookDetailedResponse create(@RequestBody(required = false) @Valid final SpellbookRequest request) {
        return spellbookService.create(request);
    }

    @Operation(summary = "Книги пользователя двумя списками: свои (own) и доступные по ссылке (shared), "
            + "с числом заклинаний и подготовленных")
    @GetMapping
    public SpellbookListResponse findMine() {
        return spellbookService.findMine();
    }

    @Operation(summary = "Книга с заклинаниями, разбитыми на группы по уровню. Своя книга — на чтение "
            + "и изменение (owner=true, отдаётся shareKey), добавленная по ссылке — только на чтение")
    @GetMapping("/{id}")
    public SpellbookDetailedResponse findById(@PathVariable final UUID id) {
        return spellbookService.findById(id);
    }

    @Operation(summary = "Переименование книги")
    @PutMapping("/{id}")
    public SpellbookDetailedResponse update(@PathVariable final UUID id,
                                            @RequestBody @Valid final SpellbookRequest request) {
        return spellbookService.update(id, request);
    }

    @Operation(summary = "Удаление книги вместе с её заклинаниями (освобождает место в лимите без подписки)")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable final UUID id) {
        spellbookService.delete(id);
    }

    @Operation(summary = "Добавление заклинаний в книгу из раздела заклинаний; уже добавленные пропускаются")
    @PostMapping("/{id}/spells")
    public SpellbookDetailedResponse addSpells(@PathVariable final UUID id,
                                               @RequestBody @Valid final SpellbookAddSpellsRequest request) {
        return spellbookService.addSpells(id, request);
    }

    @Operation(summary = "Отметка «подготовлено» у заклинания книги")
    @PutMapping("/{id}/spells/{spellUrl}")
    public SpellbookDetailedResponse updateSpell(@PathVariable final UUID id,
                                                 @PathVariable final String spellUrl,
                                                 @RequestBody @Valid final SpellbookSpellUpdateRequest request) {
        return spellbookService.updateSpell(id, spellUrl, request);
    }

    @Operation(summary = "Удаление заклинания из книги")
    @DeleteMapping("/{id}/spells/{spellUrl}")
    public SpellbookDetailedResponse deleteSpell(@PathVariable final UUID id,
                                                 @PathVariable final String spellUrl) {
        return spellbookService.deleteSpell(id, spellUrl);
    }

    @Operation(summary = "Просмотр книги по ссылке без добавления к себе: ключ shareKey владелец "
            + "берёт из своей книги")
    @GetMapping("/link/{shareKey}")
    public SpellbookDetailedResponse findByShareKey(@PathVariable final UUID shareKey) {
        return spellbookService.findByShareKey(shareKey);
    }

    @Operation(summary = "Добавление книги по ссылке в свой список доступных; повторное добавление "
            + "ничего не меняет")
    @PostMapping("/link/{shareKey}")
    public SpellbookDetailedResponse addShared(@PathVariable final UUID shareKey) {
        return spellbookService.addShared(shareKey);
    }

    @Operation(summary = "Убрать доступную по ссылке книгу из своего отображения. Книга владельца "
            + "не удаляется — вернуть её можно по той же ссылке")
    @DeleteMapping("/shared/{id}")
    public void deleteShared(@PathVariable final UUID id) {
        spellbookService.deleteShared(id);
    }
}
