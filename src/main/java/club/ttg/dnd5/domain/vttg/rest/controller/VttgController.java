package club.ttg.dnd5.domain.vttg.rest.controller;

import club.ttg.dnd5.domain.vttg.rest.dto.VttgChangesResponse;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgChangesStatus;
import club.ttg.dnd5.domain.vttg.service.VttgChangesService;
import club.ttg.dnd5.domain.vttg.service.VttgModuleArchive;
import club.ttg.dnd5.domain.vttg.service.VttgModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

@Tag(name = "VTTG", description = "Экспорт контента TTG Club в модули VTTG")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/vttg")
@Secured({"VTTG", "ADMIN"})
public class VttgController {
    private final VttgModuleService moduleService;
    private final VttgChangesService changesService;

    @Operation(summary = "Проверить наличие изменений для VTTG (лёгкий индикатор)",
            description = "Возвращает число добавленных/изменённых/скрытых сущностей в окне (since, now]. "
                    + "Без полезной нагрузки — для отображения индикатора «есть обновления».")
    @GetMapping("/changes/status")
    public VttgChangesStatus getChangesStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
            @RequestParam(required = false) String srdVersion,
            @RequestParam(required = false) Set<String> types) {
        return changesService.status(since, srdVersion, types);
    }

    @Operation(summary = "Получить дельту изменений сущностей для VTTG",
            description = "Добавленные/изменённые видимые сущности (upserts) в окне (since, now]. "
                    + "Скрытые сущности (мягкое удаление на стороне источника) не возвращаются. "
                    + "После успешного применения клиент сохраняет until как новый курсор since. "
                    + "Для первичной загрузки используйте /module или since по умолчанию (с начала времён).")
    @GetMapping("/changes")
    public VttgChangesResponse getChanges(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
            @RequestParam(required = false) String srdVersion,
            @RequestParam(required = false) Set<String> types) {
        return changesService.changes(since, srdVersion, types);
    }

    @Operation(summary = "Скачать общий модуль заклинаний и существ для VTTG")
    @GetMapping(value = "/module", produces = "application/zip")
    public ResponseEntity<byte[]> getModule(@RequestParam(required = false) String srdVersion) {
        return archive(moduleService.buildAllModule(srdVersion));
    }

    @Operation(summary = "Скачать модуль заклинаний для VTTG")
    @GetMapping(value = "/spells/module", produces = "application/zip")
    public ResponseEntity<byte[]> getSpellModule(@RequestParam(required = false) String srdVersion) {
        return archive(moduleService.buildSpellModule(srdVersion));
    }

    @Operation(summary = "Скачать модуль существ для VTTG")
    @GetMapping(value = "/creatures/module", produces = "application/zip")
    public ResponseEntity<byte[]> getCreatureModule(@RequestParam(required = false) String srdVersion) {
        return archive(moduleService.buildCreatureModule(srdVersion));
    }

    private ResponseEntity<byte[]> archive(VttgModuleArchive archive) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(archive.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(archive.content().length)
                .body(archive.content());
    }
}
