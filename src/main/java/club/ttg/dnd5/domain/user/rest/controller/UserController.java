package club.ttg.dnd5.domain.user.rest.controller;

import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterRequest;
import club.ttg.dnd5.domain.source.rest.dto.filter.SourceSavedFilterResponse;
import club.ttg.dnd5.domain.source.service.SourceSavedFilterService;
import club.ttg.dnd5.domain.user.rest.dto.UserDto;
import club.ttg.dnd5.domain.user.rest.dto.UserProfileShortResponse;
import club.ttg.dnd5.domain.user.service.UserService;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Secured("USER")
    @Operation(summary = "Получение профиля пользователя")
    @GetMapping("/profile")
    public UserDto getUser() {
        return SecurityUtils.getUserDto();
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
