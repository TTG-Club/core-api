package club.ttg.dnd5.controller.character;

import club.ttg.dnd5.dto.character.ClassRequest;
import club.ttg.dnd5.dto.character.ClassResponse;
import club.ttg.dnd5.dto.engine.SearchRequest;
import club.ttg.dnd5.service.character.ClassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Tag(name = "Классы", description = "REST API классов персонажа")

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/class")
public class ClassController {
    private final ClassService classService;

    @GetMapping
    public Collection<ClassResponse> getClasses(@RequestBody final SearchRequest request) {
        return null;
    }

    @GetMapping("/{url}")
    public ClassResponse getClass(@PathVariable String url) {
        return classService.getClass(url);
    }

    @Secured("ADMIN")
    @PostMapping
    public ClassResponse addClass(@RequestBody final ClassRequest request) {
        return classService.addClass(request);
    }

    @Secured("ADMIN")
    @PutMapping("/{url}")
    public ClassResponse updateClass(
            @PathVariable final String url,
            @RequestBody final ClassRequest request) {
        return classService.updateClass(url, request);
    }
}
