package club.ttg.dnd5.controller.book;


import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Источники", description = "REST API для источников и книги")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/source")
public class SourceController {

}
