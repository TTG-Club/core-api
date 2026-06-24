package club.ttg.dnd5.domain.achievement.rest.controller;

import club.ttg.dnd5.domain.achievement.rest.dto.AchievementRequest;
import club.ttg.dnd5.domain.achievement.rest.dto.AchievementResponse;
import club.ttg.dnd5.domain.achievement.rest.dto.GrantAchievementsRequest;
import club.ttg.dnd5.domain.achievement.rest.dto.UserAchievementResponse;
import club.ttg.dnd5.domain.achievement.service.AchievementService;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Достижения")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/achievements")
public class AchievementController {
    private final AchievementService achievementService;

    @Secured("USER")
    @Operation(summary = "Достижения текущего пользователя")
    @GetMapping("/me")
    public List<UserAchievementResponse> myAchievements() {
        return achievementService.currentUserAchievements();
    }

    @Secured("ADMIN")
    @Operation(summary = "Каталог достижений")
    @GetMapping("/catalog")
    public List<AchievementResponse> catalog() {
        return achievementService.catalog();
    }

    @Secured("ADMIN")
    @Operation(summary = "Создать/обновить достижение в каталоге")
    @PutMapping("/catalog/{code}")
    public AchievementResponse upsert(@PathVariable String code,
                                      @Valid @RequestBody AchievementRequest request) {
        return achievementService.createOrUpdate(code, request);
    }

    @Secured("ADMIN")
    @Operation(summary = "Выдать достижения пользователю напрямую (минуя коды)")
    @PostMapping("/grant/{username}")
    public List<UserAchievementResponse> grant(@PathVariable String username,
                                               @Valid @RequestBody GrantAchievementsRequest request) {
        return achievementService.grant(username, request.achievementCodes(), null,
                SecurityUtils.getUser().getUsername());
    }
}
