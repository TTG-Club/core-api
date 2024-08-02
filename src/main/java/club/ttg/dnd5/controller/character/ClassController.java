package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.ClassRequest;
import club.ttg.dnd5.dto.character.ClassResponse;
import club.ttg.dnd5.service.character.ClassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Tag(name = "Классы", description = "REST API классов персонажа")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/class")
public class ClassController {
    private final ClassService classService;

    @GetMapping
    public Collection<ClassResponse> getClasses() {
        return null;
    }

    @GetMapping("/{url}")
    public ClassResponse getClass(final String url) {
        return null;
    }

    @PostMapping
    public ClassResponse addClass(@RequestBody ClassRequest request) {
        return null;
    }

    @PutMapping
    public ClassResponse updateClass(@RequestBody ClassRequest request) {
        return null;
    }
}
