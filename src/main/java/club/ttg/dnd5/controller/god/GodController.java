package club.ttg.dnd5.controller.god;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Боги ", description = "REST API боги")

@RestController
@NoArgsConstructor
@RequestMapping("/api/v2/god")
public class GodController {
}
