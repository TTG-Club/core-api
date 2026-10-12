package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.RatingRepository;
import club.ttg.dnd5.domain.common.rest.mapper.RatingMapper;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    public Long countUserRating(String username) {
        return ratingRepository.countByUsername(username);
    }

    @Transactional
    public RatingResponse addOrUpdate(final RatingRequest request) {
        var username = getCurrentUsername();
        var rating = ratingRepository.findByUsernameAndSectionAndUrl(
                username,
                request.getSection(),
                request.getUrl())
            .orElse(ratingMapper.toRating(request, username));
        rating.setValue(request.getValue());
        ratingRepository.save(rating);
        return ratingMapper.toResponse(
                ratingRepository.getRating(request.getSection(), request.getUrl())
        );
    }

    public RatingResponse getRating(final String section, final String url) {
        var ratingStats = ratingRepository.getRating(section, url);
        return  ratingMapper.toResponse(ratingStats);
    }

    /**
     * Рейтинги нескольких записей раздела одним запросом: url → рейтинг. Записям без оценок — нулевой рейтинг.
     */
    public Map<String, RatingResponse> getRatings(final String section, final Collection<String> urls) {
        Map<String, RatingResponse> ratings = new HashMap<>();
        if (urls.isEmpty()) {
            return ratings;
        }
        ratingRepository.getRatings(section, urls)
                .forEach(stats -> ratings.put(stats.getUrl(), ratingMapper.toResponse(stats)));
        urls.forEach(url -> ratings.computeIfAbsent(url, ignored -> new RatingResponse()));
        return ratings;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // или throw, если требуется аутентификация
        }
        return authentication.getName(); // username (обычно email или логин)
    }
}
