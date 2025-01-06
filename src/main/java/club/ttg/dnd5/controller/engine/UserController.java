package club.ttg.dnd5.controller.engine;

import club.ttg.dnd5.dto.user.UserDto;
import club.ttg.dnd5.security.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Пользователи", description = "REST API пользователей сайта")

@Slf4j
@Secured("USER")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/profile")
    public UserDto getUser() {
        UserDto userDto = SecurityUtils.getUserDto();

        if (userDto == null) {
            log.info("no user");
        }

        return userDto;
    }

    @GetMapping("/roles")
    public List<String> getRoles() {
        UserDto userDto = SecurityUtils.getUserDto();

        return userDto.getRoles();
    }

}
