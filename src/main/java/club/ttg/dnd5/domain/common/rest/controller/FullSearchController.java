package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Tag(name = "Поиск", description = "API для поиска по всем разделам")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v2/search")
public class FullSearchController {

    @GetMapping
    public Collection<ShortResponse> search(String query) {
        return null;
    }
}
