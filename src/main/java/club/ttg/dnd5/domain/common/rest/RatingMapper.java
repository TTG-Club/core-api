package club.ttg.dnd5.domain.common.rest;

import club.ttg.dnd5.domain.common.model.Rating;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    Rating toRating(RatingRequest request);
}
