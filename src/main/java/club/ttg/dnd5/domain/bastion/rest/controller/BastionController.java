package club.ttg.dnd5.domain.bastion.rest.controller;

import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityDetailResponse;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityQueryRequest;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityRequest;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityShortResponse;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionRulesResponse;
import club.ttg.dnd5.domain.bastion.service.BastionFacilityFilterService;
import club.ttg.dnd5.domain.bastion.service.BastionFacilityService;
import club.ttg.dnd5.domain.bastion.service.BastionRulesService;
import club.ttg.dnd5.domain.filter.rest.dto.FilterMetadataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Бастионы", description = "REST API сооружений бастиона")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bastions")
public class BastionController {
    private final BastionFacilityService facilityService;
    private final BastionFacilityFilterService filterService;
    private final BastionRulesService rulesService;

    @Operation(summary = "Правила бастиона",
            description = "Справочники (приказы, пространства, требования) и общие числа правил бастиона")
    @GetMapping("/rules")
    public BastionRulesResponse getRules() {
        return rulesService.getRules();
    }

    @Operation(summary = "Поиск сооружений бастиона", description = "Поиск сооружений с GET-параметрами фильтрации")
    @GetMapping("/search")
    public List<BastionFacilityShortResponse> search(@ParameterObject BastionFacilityQueryRequest request) {
        return facilityService.search(request);
    }

    @Operation(summary = "Получить метаданные фильтров")
    @GetMapping("/filters")
    public FilterMetadataResponse getFilters(@RequestParam(required = false) Set<String> source) {
        return filterService.getFilterMetadata(source != null ? source : Set.of());
    }

    @Operation(summary = "Проверить сооружение по URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сооружение существует"),
            @ApiResponse(responseCode = "404", description = "Сооружение не существует")
    })
    @RequestMapping(path = "/{url}", method = RequestMethod.HEAD)
    public Boolean exist(@PathVariable String url) {
        return facilityService.existOrThrow(url);
    }

    @Operation(summary = "Получение сооружения бастиона")
    @GetMapping("/{url}")
    public BastionFacilityDetailResponse getByUrl(@PathVariable String url) {
        return facilityService.findDetailedByUrl(url);
    }

    @Operation(summary = "Форма сооружения бастиона для редактора")
    @GetMapping("/{url}/raw")
    public BastionFacilityRequest getFormByUrl(@PathVariable String url) {
        return facilityService.findFormByUrl(url);
    }

    @Secured({"ADMIN", "MODERATOR"})
    @Operation(summary = "Добавление сооружения бастиона")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@Valid @RequestBody BastionFacilityRequest request) {
        return facilityService.save(request);
    }

    @Secured({"ADMIN", "MODERATOR"})
    @Operation(summary = "Предпросмотр сооружения бастиона")
    @PostMapping("/preview")
    public BastionFacilityDetailResponse preview(@RequestBody BastionFacilityRequest request) {
        return facilityService.preview(request);
    }

    @Secured({"ADMIN", "MODERATOR"})
    @Operation(summary = "Обновление сооружения бастиона")
    @PutMapping("/{url}")
    public String update(@PathVariable String url, @Valid @RequestBody BastionFacilityRequest request) {
        return facilityService.update(url, request);
    }

    @Secured({"ADMIN", "MODERATOR"})
    @Operation(summary = "Скрывает сооружение бастиона")
    @DeleteMapping("/{url}")
    public void delete(@PathVariable String url) {
        facilityService.delete(url);
    }
}
