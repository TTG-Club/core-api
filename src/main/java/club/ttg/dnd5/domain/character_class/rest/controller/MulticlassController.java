package club.ttg.dnd5.domain.character_class.rest.controller;

import club.ttg.dnd5.domain.character_class.rest.dto.ClassDetailedResponse;
import club.ttg.dnd5.domain.character_class.service.ClassService;
import club.ttg.dnd5.domain.common.rest.dto.MulticlassRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/multiclass")
@Tag(name = "Мультиклассирование", description = "API для управления мультиклассами")
public class MulticlassController {
    private final ClassService classService;

    @PostMapping
    public ClassDetailedResponse getClassByUrl(@RequestBody MulticlassRequest request) {
        return classService.getMulticlass(request);
    }
}
