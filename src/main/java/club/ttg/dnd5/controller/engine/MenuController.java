package club.ttg.dnd5.controller.engine;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Tag(name = "Меню сайта", description = "REST API для меню сайта")
@RestController
@RequestMapping("/api/v2/menu")
public class MenuController {

}
