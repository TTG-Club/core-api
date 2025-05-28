package club.ttg.dnd5.domain.common.rest.controller;

import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
import club.ttg.dnd5.domain.common.service.RatingService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/rating")
public class RatingController {
    private final RatingService ratingService;
    @GetMapping
    public byte getRating(String section, String url) {
        return ratingService.getRating(section, url);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public byte addRating(@RequestBody RatingRequest rating) {
        return ratingService.addOrUpdate(rating);
    }
}
