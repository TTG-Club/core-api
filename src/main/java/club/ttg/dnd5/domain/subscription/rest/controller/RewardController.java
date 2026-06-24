package club.ttg.dnd5.domain.subscription.rest.controller;

import club.ttg.dnd5.domain.subscription.model.RewardPerk;
import club.ttg.dnd5.domain.subscription.rest.dto.GrantPerksRequest;
import club.ttg.dnd5.domain.subscription.rest.dto.RewardResourceResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.UpdateRewardResourceRequest;
import club.ttg.dnd5.domain.subscription.rest.dto.UserRewardResponse;
import club.ttg.dnd5.domain.subscription.service.RewardService;
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

@Tag(name = "Награды")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardController {
    private final RewardService rewardService;

    @Secured("USER")
    @Operation(summary = "Награды текущего пользователя со ссылками и статусом готовности")
    @GetMapping("/me")
    public List<UserRewardResponse> myRewards() {
        return rewardService.currentUserRewards();
    }

    @Secured("USER")
    @Operation(summary = "Список увековеченных в приложении (перк APP_CREDITS)")
    @GetMapping("/supporters")
    public List<String> supporters() {
        return rewardService.supporters();
    }

    @Secured("ADMIN")
    @Operation(summary = "Конфиг контента наград")
    @GetMapping("/resources")
    public List<RewardResourceResponse> resources() {
        return rewardService.resources();
    }

    @Secured("ADMIN")
    @Operation(summary = "Обновить контент награды (ссылку/статус готовности)")
    @PutMapping("/resources/{perk}")
    public RewardResourceResponse updateResource(@PathVariable RewardPerk perk,
                                                 @Valid @RequestBody UpdateRewardResourceRequest request) {
        return rewardService.updateResource(perk, request);
    }

    @Secured("ADMIN")
    @Operation(summary = "Выдать перки пользователю напрямую (минуя коды)")
    @PostMapping("/grant/{username}")
    public List<RewardPerk> grant(@PathVariable String username,
                                  @Valid @RequestBody GrantPerksRequest request) {
        return rewardService.grantPerks(username, request.perks(), null);
    }
}
