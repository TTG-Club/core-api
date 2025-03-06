package club.ttg.dnd5.domain.tool.generator.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Генераторы", description = "REST API генерации случайного торговца")

@RestController
@RequiredArgsConstructor
public class TraderController {
}
