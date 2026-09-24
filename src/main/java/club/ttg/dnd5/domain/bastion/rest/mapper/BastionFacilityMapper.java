package club.ttg.dnd5.domain.bastion.rest.mapper;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilityCategory;
import club.ttg.dnd5.domain.bastion.model.FacilityPrerequisite;
import club.ttg.dnd5.domain.bastion.model.FacilitySpace;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityDetailResponse;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityRequest;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityShortResponse;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionLabel;
import club.ttg.dnd5.domain.bastion.rest.dto.FacilitySpaceResponse;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.dto.base.mapping.BaseMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring", uses = {BaseMapping.class})
public interface BastionFacilityMapper {

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    BastionFacilityShortResponse toShort(BastionFacility facility);

    @BaseMapping.BaseShortResponseNameMapping
    @BaseMapping.BaseSourceMapping
    BastionFacilityDetailResponse toDetail(BastionFacility facility);

    @BaseMapping.BaseRequestNameMapping
    @BaseMapping.BaseSourceRequestMapping
    BastionFacilityRequest toRequest(BastionFacility facility);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(source = "request.url", target = "url")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(target = "source", source = "source")
    BastionFacility toEntity(BastionFacilityRequest request, Source source);

    @BaseMapping.BaseEntityNameMapping
    @Mapping(target = "url", ignore = true)
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.original", target = "original")
    @Mapping(source = "request.source.page", target = "sourcePage")
    @Mapping(source = "request.srdVersion", target = "srdVersion")
    @Mapping(target = "source", source = "source")
    void updateEntity(BastionFacilityRequest request, Source source, @MappingTarget BastionFacility facility);

    default BastionLabel toLabel(FacilityCategory category) {
        return category == null ? null : new BastionLabel(category.name(), category.getName());
    }

    default BastionLabel toLabel(FacilityPrerequisite prerequisite) {
        return prerequisite == null ? null : new BastionLabel(prerequisite.name(), prerequisite.getName());
    }

    default BastionLabel toLabel(BastionOrder order) {
        return order == null ? null : new BastionLabel(order.name(), order.getName());
    }

    default FacilitySpaceResponse toSpace(FacilitySpace space) {
        return space == null ? null : new FacilitySpaceResponse(space.name(), space.getName(), space.getSquares());
    }
}
