package club.ttg.dnd5.controller.item;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Доспехи", description = "REST API доспехи")

@RestController
@NoArgsConstructor
@RequestMapping("/api/v2/armor")
public class ArmorController {
}
