package club.ttg.dnd5.controller.item.magic;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Магические предметы и артефакты", description = "REST API для магических предметов")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/magic-item")
public class MagicItemController {
}
