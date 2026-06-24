package club.ttg.dnd5.domain.subscription.service;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.model.RewardResource;
import club.ttg.dnd5.domain.subscription.model.RewardTier;
import club.ttg.dnd5.domain.subscription.model.UserReward;
import club.ttg.dnd5.domain.subscription.repository.RewardResourceRepository;
import club.ttg.dnd5.domain.subscription.repository.UserRewardRepository;
import club.ttg.dnd5.domain.subscription.rest.dto.RewardResourceResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.UpdateRewardResourceRequest;
import club.ttg.dnd5.domain.subscription.rest.dto.UserRewardResponse;
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
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final UserRewardRepository rewardRepository;
    private final RewardResourceRepository resourceRepository;

    /**
     * Выдаёт пользователю все перки тира. Уже имеющиеся перки пропускаются.
     *
     * @return фактически выданные перки
     */
    @Transactional
    public List<RewardPerk> grantTier(String username, RewardTier tier, UUID sourceCode) {
        return grantPerks(username, tier.perks(), sourceCode);
    }

    /**
     * Выдаёт пользователю произвольный набор перков. Уже имеющиеся перки пропускаются.
     * Идемпотентно — повторная выдача не дублирует награды.
     *
     * @return фактически выданные перки
     */
    @Transactional
    public List<RewardPerk> grantPerks(String username, Collection<RewardPerk> perks, UUID sourceCode) {
        if (perks == null || perks.isEmpty()) {
            return List.of();
        }
        Instant now = Instant.now();
        List<RewardPerk> granted = new ArrayList<>();
        // EnumSet сортирует по объявлению перков — детерминированный порядок выдачи
        Set<RewardPerk> ordered = EnumSet.copyOf(perks);
        for (RewardPerk perk : ordered) {
            if (rewardRepository.existsByUsernameAndPerk(username, perk)) {
                continue;
            }
            UserReward reward = new UserReward();
            reward.setUsername(username);
            reward.setPerk(perk);
            reward.setGrantedAt(now);
            reward.setSourceCode(sourceCode);
            rewardRepository.save(reward);
            granted.add(perk);
        }
        return granted;
    }

    @Transactional(readOnly = true)
    public List<UserRewardResponse> currentUserRewards() {
        Map<RewardPerk, RewardResource> resources = resourceMap();
        return rewardRepository.findByUsernameOrderByGrantedAtDesc(currentUsername()).stream()
                .map(reward -> toResponse(reward, resources.get(reward.getPerk())))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasPerk(String username, RewardPerk perk) {
        return StringUtils.hasText(username) && rewardRepository.existsByUsernameAndPerk(username, perk);
    }

    @Transactional(readOnly = true)
    public List<RewardResourceResponse> resources() {
        return resourceRepository.findAll().stream()
                .map(this::toResourceResponse)
                .toList();
    }

    @Transactional
    public RewardResourceResponse updateResource(RewardPerk perk, UpdateRewardResourceRequest request) {
        RewardResource resource = resourceRepository.findById(perk).orElseGet(() -> {
            RewardResource created = new RewardResource();
            created.setPerk(perk);
            return created;
        });
        resource.setTitle(request.title());
        resource.setUrl(request.url());
        resource.setAvailability(request.availability());
        resource.setNote(request.note());
        return toResourceResponse(resourceRepository.save(resource));
    }

    /** Пользователи, увековеченные в приложении (перк APP_CREDITS). */
    @Transactional(readOnly = true)
    public List<String> supporters() {
        return rewardRepository.findByPerkOrderByGrantedAtAsc(RewardPerk.APP_CREDITS).stream()
                .map(UserReward::getUsername)
                .distinct()
                .toList();
    }

    private Map<RewardPerk, RewardResource> resourceMap() {
        Map<RewardPerk, RewardResource> map = new EnumMap<>(RewardPerk.class);
        resourceRepository.findAll().forEach(resource -> map.put(resource.getPerk(), resource));
        return map;
    }

    private UserRewardResponse toResponse(UserReward reward, RewardResource resource) {
        return new UserRewardResponse(
                reward.getPerk(),
                reward.getGrantedAt(),
                resource == null ? null : resource.getTitle(),
                resource == null ? null : resource.getUrl(),
                resource == null ? null : resource.getAvailability(),
                resource == null ? null : resource.getNote());
    }

    private RewardResourceResponse toResourceResponse(RewardResource resource) {
        return new RewardResourceResponse(
                resource.getPerk(),
                resource.getTitle(),
                resource.getUrl(),
                resource.getAvailability(),
                resource.getNote(),
                resource.getUpdatedAt());
    }

    private String currentUsername() {
        String username = SecurityUtils.getUser().getUsername();
        if (!StringUtils.hasText(username)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }
        return username;
    }
}
