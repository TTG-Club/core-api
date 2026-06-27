package club.ttg.dnd5.domain.subscription.rest.controller;

import club.ttg.dnd5.domain.subscription.rest.dto.CreateCodesRequest;
import club.ttg.dnd5.domain.subscription.rest.dto.MyRedemptionResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.RedeemCodeRequest;
import club.ttg.dnd5.domain.subscription.rest.dto.RedeemResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.RedemptionCodeResponse;
import club.ttg.dnd5.domain.subscription.rest.dto.SubscriptionResponse;
import club.ttg.dnd5.domain.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Подписки")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Secured("ADMIN")
    @Operation(summary = "Выпустить пачку кодов с одинаковыми наградами и периодом")
    @PostMapping("/codes")
    @ResponseStatus(HttpStatus.CREATED)
    public List<RedemptionCodeResponse> createCodes(@Valid @RequestBody CreateCodesRequest request) {
        return subscriptionService.createCodes(request);
    }

    @Secured("ADMIN")
    @Operation(summary = "Получить все выпущенные коды (для админки)")
    @GetMapping("/codes")
    public List<RedemptionCodeResponse> allCodes() {
        return subscriptionService.allCodes();
    }

    @Secured("ADMIN")
    @Operation(summary = "Деактивировать выпущенный код (его нельзя будет погасить)")
    @PostMapping("/codes/{id}/deactivate")
    public RedemptionCodeResponse deactivateCode(@PathVariable UUID id) {
        return subscriptionService.setCodeDisabled(id, true);
    }

    @Secured("ADMIN")
    @Operation(summary = "Снова активировать ранее деактивированный код")
    @PostMapping("/codes/{id}/reactivate")
    public RedemptionCodeResponse reactivateCode(@PathVariable UUID id) {
        return subscriptionService.setCodeDisabled(id, false);
    }

    @Secured("USER")
    @Operation(summary = "Погасить код: выдать награды и зарегистрировать подписку")
    @PostMapping("/redeem")
    public RedeemResponse redeem(@Valid @RequestBody RedeemCodeRequest request) {
        return subscriptionService.redeem(request.code());
    }

    @Secured("USER")
    @Operation(summary = "Активировать подписку текущего пользователя")
    @PostMapping("/{id}/activate")
    public SubscriptionResponse activate(@PathVariable UUID id) {
        return subscriptionService.activate(id);
    }

    @Secured("USER")
    @Operation(summary = "Получить подписки текущего пользователя")
    @GetMapping("/my")
    public List<SubscriptionResponse> mySubscriptions() {
        return subscriptionService.currentUserSubscriptions();
    }

    @Secured("USER")
    @Operation(summary = "Коды, погашенные текущим пользователем, с наградами и ссылками")
    @GetMapping("/my-codes")
    public List<MyRedemptionResponse> myCodes() {
        return subscriptionService.currentUserRedemptions();
    }

    @Secured("ADMIN")
    @Operation(summary = "Получить все подписки (для админки)")
    @GetMapping("/all")
    public List<SubscriptionResponse> allSubscriptions() {
        return subscriptionService.allSubscriptions();
    }
}
