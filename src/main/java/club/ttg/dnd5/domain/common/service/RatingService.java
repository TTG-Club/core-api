package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.RatingRepository;
import club.ttg.dnd5.domain.common.rest.RatingMapper;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
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
    public byte addOrUpdate(final RatingRequest request) {
        var username = getCurrentUsername();
        var rating = ratingRepository.findByUsernameAndSectionAndUrl(
                username,
                request.getSection(),
                request.getUrl())
            .orElse(ratingMapper.toRating(request, username));
        rating.setValue(request.getValue());
        ratingRepository.save(rating);
        var avg = ratingRepository.getRating(request.getSection(), request.getUrl());
        return  avg == null ? 0 : avg.byteValue();
    }

    public byte getRating(final String section, final String url) {
        var avg = ratingRepository.getRating(section, url);
        return  avg == null ? 0 : avg.byteValue();
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // или throw, если требуется аутентификация
        }
        return authentication.getName(); // username (обычно email или логин)
    }
}
