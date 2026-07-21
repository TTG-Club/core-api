package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterRequest;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterResponse;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameByLoginResponse;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNameResponse;
import club.ttg.dnd5.domain.user.rest.dto.DisplayNamesLookupRequest;
import club.ttg.dnd5.domain.user.rest.dto.UpdateDisplayNameRequest;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import club.ttg.dnd5.domain.user.service.DisplayNameService;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Пользователи")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final SourceSavedFilterService sourceSavedFilterService;
    private final DisplayNameService displayNameService;

    @Secured("USER")
    @Operation(summary = "Получение профиля пользователя")
    @GetMapping("/profile")
    public UserDto getUser() {
        return SecurityUtils.getUserDto();
    }

    @Secured("USER")
    @Operation(summary = "Отображаемое имя текущего пользователя (создаётся лениво)")
    @GetMapping("/profile/display-name")
    public DisplayNameResponse getDisplayName() {
        return displayNameService.getOrCreateForCurrentUser();
    }

    @Secured("USER")
    @Operation(summary = "Смена отображаемого имени")
    @PatchMapping("/profile/display-name")
    public DisplayNameResponse updateDisplayName(@Valid @RequestBody UpdateDisplayNameRequest request) {
        return displayNameService.updateForCurrentUser(request.displayName());
    }

    @Operation(summary = "Отображаемые имена по логинам (публично, для рейтингов)")
    @PostMapping("/display-names")
    public List<DisplayNameByLoginResponse> resolveDisplayNames(
            @Valid @RequestBody DisplayNamesLookupRequest request) {
        return displayNameService.resolveByLogins(request.logins());
    }

    @Secured("ADMIN")
    @Operation(summary = "Поиск пользователей по отображаемому имени (подсказки в админке)")
    @GetMapping("/display-names/search")
    public List<DisplayNameByLoginResponse> searchDisplayNames(
            @RequestParam(required = false) String query) {
        return displayNameService.searchByDisplayName(query);
    }

    @Secured("USER")
    @Operation(summary = "Получение профиля пользователя для бокового меню")
    @GetMapping("/profile/short")
    public UserProfileShortResponse getUserProfileShort() {
        return userService.getUserProfileShort();
    }

    @Operation(summary = "Получить сохраненный фильтр по умолчанию")
    @GetMapping("/profile/saved-filter")
    public SourceSavedFilterResponse SourceSavedFilter() {
        return sourceSavedFilterService.getSavedFilterResponse();
    }

    @Operation(summary = "Создать сохраненный фильтр")
    @PostMapping("/profile/saved-filter")
    public SourceSavedFilterResponse saveSourceFilter(@RequestBody SourceSavedFilterRequest sourceFilter) {
        return sourceSavedFilterService.createFilter(sourceFilter);
    }

    @Operation(summary = "Обновить сохраненный фильтр")
    @PutMapping("/profile/saved-filter")
    public SourceSavedFilterResponse updateSourceFilter(@RequestBody SourceSavedFilterRequest sourceFilter) {
        return sourceSavedFilterService.updateFilter(sourceFilter);
    }

    @Secured("USER")
    @Operation(summary = "Получение списка ролей текущего пользователя")
    @GetMapping("/roles")
    public List<String> getRoles() {
        UserDto userDto = SecurityUtils.getUserDto();
        return userDto.getRoles();
    }
}
