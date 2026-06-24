package club.ttg.dnd5.domain.achievement.service;

import club.ttg.dnd5.domain.achievement.model.Achievement;
import club.ttg.dnd5.domain.achievement.model.UserAchievement;
import club.ttg.dnd5.domain.achievement.repository.AchievementRepository;
import club.ttg.dnd5.domain.achievement.repository.UserAchievementRepository;
import club.ttg.dnd5.domain.achievement.rest.dto.AchievementRequest;
import club.ttg.dnd5.domain.achievement.rest.dto.AchievementResponse;
import club.ttg.dnd5.domain.achievement.rest.dto.UserAchievementResponse;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    /**
     * Выдаёт пользователю набор достижений по их кодам. Несуществующие в каталоге
     * и уже имеющиеся — пропускаются. Идемпотентно.
     *
     * @return фактически выданные достижения с описанием из каталога
     */
    @Transactional
    public List<UserAchievementResponse> grant(String username, Collection<String> codes,
                                               UUID sourceCode, String grantedBy) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        Instant now = Instant.now();
        List<UserAchievementResponse> granted = new ArrayList<>();
        for (String code : codes) {
            Achievement achievement = achievementRepository.findById(code).orElse(null);
            if (achievement == null || userAchievementRepository.existsByUsernameAndAchievementCode(username, code)) {
                continue;
            }
            UserAchievement entity = new UserAchievement();
            entity.setUsername(username);
            entity.setAchievementCode(code);
            entity.setGrantedAt(now);
            entity.setSourceCode(sourceCode);
            entity.setGrantedBy(grantedBy);
            userAchievementRepository.save(entity);
            granted.add(toUserResponse(achievement, now));
        }
        return granted;
    }

    /**
     * Автоматическая выдача: все достижения, привязанные к ключу события. Точку
     * вызова определяет доменная логика (например, после создания первого персонажа).
     */
    @Transactional
    public List<UserAchievementResponse> grantByTrigger(String username, String triggerKey) {
        if (!StringUtils.hasText(triggerKey)) {
            return List.of();
        }
        List<String> codes = achievementRepository.findByTriggerKey(triggerKey).stream()
                .map(Achievement::getCode)
                .toList();
        return grant(username, codes, null, null);
    }

    @Transactional(readOnly = true)
    public List<UserAchievementResponse> currentUserAchievements() {
        Map<String, Achievement> catalog = catalogMap();
        return userAchievementRepository.findByUsernameOrderByGrantedAtDesc(currentUsername()).stream()
                .map(ua -> {
                    Achievement achievement = catalog.get(ua.getAchievementCode());
                    return achievement == null
                            ? new UserAchievementResponse(ua.getAchievementCode(), ua.getAchievementCode(),
                                    null, null, ua.getGrantedAt())
                            : toUserResponse(achievement, ua.getGrantedAt());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> catalog() {
        return achievementRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AchievementResponse createOrUpdate(String code, AchievementRequest request) {
        if (!StringUtils.hasText(code)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Код достижения обязателен");
        }
        Achievement achievement = achievementRepository.findById(code).orElseGet(() -> {
            Achievement created = new Achievement();
            created.setCode(code);
            return created;
        });
        achievement.setTitle(request.title());
        achievement.setDescription(request.description());
        achievement.setIcon(request.icon());
        achievement.setHidden(request.hidden());
        achievement.setTriggerKey(StringUtils.hasText(request.triggerKey()) ? request.triggerKey() : null);
        return toResponse(achievementRepository.save(achievement));
    }

    private Map<String, Achievement> catalogMap() {
        Map<String, Achievement> map = new LinkedHashMap<>();
        achievementRepository.findAll().forEach(achievement -> map.put(achievement.getCode(), achievement));
        return map;
    }

    private AchievementResponse toResponse(Achievement achievement) {
        return new AchievementResponse(
                achievement.getCode(),
                achievement.getTitle(),
                achievement.getDescription(),
                achievement.getIcon(),
                achievement.isHidden(),
                achievement.getTriggerKey());
    }

    private UserAchievementResponse toUserResponse(Achievement achievement, Instant grantedAt) {
        return new UserAchievementResponse(
                achievement.getCode(),
                achievement.getTitle(),
                achievement.getDescription(),
                achievement.getIcon(),
                grantedAt);
    }

    private String currentUsername() {
        String username = SecurityUtils.getUser().getUsername();
        if (!StringUtils.hasText(username)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return username;
    }
}
