package club.ttg.dnd5.domain.articles.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Статьи", description = "REST API глоссарий")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/article")
public class AticlesController {
}
