package club.ttg.dnd5.domain.common.service;

import club.ttg.dnd5.domain.common.repository.RatingRepository;
import club.ttg.dnd5.domain.common.rest.RatingMapper;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    @Transactional
    public byte addOrUpdate(final RatingRequest rating) {
        ratingRepository.save(ratingMapper.toRating(rating));
        var avg = ratingRepository.getRating(rating.getSection(), rating.getUrl());
        return  avg == null ? 0 : avg.byteValue();
    }

    public byte getRating(final String section, final String url) {
        var avg = ratingRepository.getRating(section, url);
        return  avg == null ? 0 : avg.byteValue();
    }
}
