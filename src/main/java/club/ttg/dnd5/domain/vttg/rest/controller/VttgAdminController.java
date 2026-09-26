package club.ttg.dnd5.domain.vttg.rest.controller;

import club.ttg.dnd5.domain.vttg.rest.dto.VttgCompendiumVersionRequest;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgCompendiumVersionResponse;
import club.ttg.dnd5.domain.vttg.rest.dto.VttgRebuildStatus;
import club.ttg.dnd5.domain.vttg.service.VttgCompendiumRebuildService;
import club.ttg.dnd5.domain.vttg.service.VttgCompendiumVersionService;
import club.ttg.dnd5.exception.ErrorResponseDto;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "VTTG: администрирование", description = "Версия формата выгрузки компендиума VTTG")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/vttg/admin")
@Secured("ADMIN")
public class VttgAdminController {
    private final VttgCompendiumVersionService versionService;
    private final VttgCompendiumRebuildService rebuildService;

    @Operation(summary = "Текущая версия компендиума VTTG и состояние пересборки")
    @GetMapping("/compendium-version")
    public VttgCompendiumVersionResponse getCompendiumVersion() {
        return response(versionService.get(), rebuildService.status());
    }

    @Operation(summary = "Поднять версию компендиума VTTG",
            description = "Версия должна быть строго больше текущей (иначе 409). Клиенты VTTG, увидев "
                    + "новую schemaVersion, перекачают компендиум целиком. После записи сбрасывается кэш "
                    + "полного дампа и в фоне запускается пересборка выгрузки; ответ приходит сразу.")
    @PutMapping("/compendium-version")
    public VttgCompendiumVersionResponse updateCompendiumVersion(
            @Valid @RequestBody VttgCompendiumVersionRequest request) {
        VttgCompendiumVersionService.Snapshot updated =
                versionService.raise(request.version(), SecurityUtils.getUser().getUsername());
        rebuildService.evictFullExport();
        return response(updated, rebuildService.request());
    }

    /** Тело не JSON или версия не число — 400, а не общий 500. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleUnreadableBody() {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDto(HttpStatus.BAD_REQUEST, "Версия должна быть целым числом"));
    }

    private static VttgCompendiumVersionResponse response(VttgCompendiumVersionService.Snapshot version,
                                                          VttgRebuildStatus rebuild) {
        return new VttgCompendiumVersionResponse(version.version(), version.updatedAt(), version.updatedBy(), rebuild);
    }
}
