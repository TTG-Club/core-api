package club.ttg.dnd5.domain.achievement.rest.controller;

import club.ttg.dnd5.domain.achievement.rest.dto.GrantAchievementsInternalRequest;
import club.ttg.dnd5.domain.achievement.rest.dto.UserAchievementResponse;
import club.ttg.dnd5.domain.achievement.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Внутренняя ручка выдачи достижений для других сервисов (subscriber-service).
 * Защита — общий секрет {@code X-Service-Token} в {@code InternalServiceTokenFilter},
 * а не JWT; на уровне Spring Security путь {@code /api/internal/**} открыт.
 */
@Tag(name = "Internal: Достижения")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/achievements")
public class InternalAchievementController {
    private final AchievementService achievementService;

    @Operation(summary = "Выдать достижения пользователю (межсервисный вызов)")
    @PostMapping("/grant")
    public List<UserAchievementResponse> grant(@Valid @RequestBody GrantAchievementsInternalRequest request) {
        return achievementService.grant(request.username(), request.achievements(), request.sourceCode(), null);
    }
}
