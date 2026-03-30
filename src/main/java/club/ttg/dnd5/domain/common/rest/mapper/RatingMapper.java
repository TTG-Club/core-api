package club.ttg.dnd5.domain.common.rest.mapper;

import club.ttg.dnd5.domain.common.model.Rating;
import club.ttg.dnd5.domain.common.repository.RatingStats;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingRequest;
import club.ttg.dnd5.domain.common.rest.dto.rating.RatingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface RatingMapper {
    Rating toRating(RatingRequest request, String username);

    @Mapping(source = "value", target = "value", qualifiedByName = "round")
    RatingResponse toResponse(RatingStats ratingStats);

    @Named("round")
    default Double round(Double value) {
        return value == null ? 0 : Math.round(value * 100.0) / 100.0;
    }
}
