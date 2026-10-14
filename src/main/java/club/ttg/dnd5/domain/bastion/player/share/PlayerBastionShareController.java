package club.ttg.dnd5.domain.bastion.player.share;

import club.ttg.dnd5.domain.bastion.player.plan.PlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Бастион по ссылке «поделиться». Отдельный контроллер, потому что основной закрыт
 * {@code @Secured("USER")} на уровне класса, а ссылка должна открываться кем угодно, включая
 * анонима. Ручек записи по ссылке нет: только чтение обеспечено сервером, а не клиентом.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/player-bastions/shared")
@Tag(name = "Бастион по ссылке", description = "Чтение бастиона по ссылке и запрос права на правку")
public class PlayerBastionShareController {
    private final PlayerBastionShareService service;

    @Operation(summary = "Бастион по ссылке",
            description = "Только чтение, без авторизации. Неизвестная или отозванная ссылка — 404")
    @GetMapping("/{token}")
    public SharedPlayerBastionResponse findShared(@PathVariable String token) {
        return service.findShared(token);
    }

    @Operation(summary = "План бастиона по ссылке", description = "Только чтение, без авторизации")
    @GetMapping("/{token}/plan")
    public PlanResponse findSharedPlan(@PathVariable String token) {
        return service.findSharedPlan(token);
    }

    @Operation(summary = "Попросить право на правку", description = "Запрос увидит владелец бастиона")
    @Secured("USER")
    @PostMapping("/{token}/edit-request")
    public SharedPlayerBastionResponse.Access requestEdit(@PathVariable String token) {
        return service.requestEdit(token);
    }
}
