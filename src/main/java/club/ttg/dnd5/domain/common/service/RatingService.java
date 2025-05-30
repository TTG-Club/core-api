package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.RatingRepository;
import club.ttg.dnd5.domain.common.rest.RatingMapper;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

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

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // или throw, если требуется аутентификация
        }
        return authentication.getName(); // username (обычно email или логин)
    }
}
