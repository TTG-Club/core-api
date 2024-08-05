package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.TraitResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Tag(name = "Черты ", description = "REST API черты персонажа")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/trait")
public class TraitController {
    @GetMapping
    public Collection<TraitResponse> getTraits() {
        return null;
    }
}
