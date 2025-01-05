package club.ttg.dnd5.controller.engine;

import club.ttg.dnd5.dto.user.UserDto;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Пользователи", description = "REST API пользователей сайта")

@Slf4j
@RestController
@RequestMapping("/api/v2/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping
    public UserDto getUser() {
        return SecurityUtils.getUserDto();
    }

}
