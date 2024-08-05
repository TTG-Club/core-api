package club.ttg.dnd5.controller.character;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Виды", description = "REST API видов (бывшие расы) персонажа")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/type")
public class SpecieController {

}
