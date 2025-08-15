package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingResponse;
import club.ttg.dnd5.domain.common.service.RatingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Рейтинг", description = "API для рейтинга")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v2/rating")
public class RatingController {
    private final RatingService ratingService;
    @GetMapping
    public RatingResponse getRating(String section, String url) {
        return ratingService.getRating(section, url);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public RatingResponse addRating(@RequestBody RatingRequest rating) {
        return ratingService.addOrUpdate(rating);
    }
}
