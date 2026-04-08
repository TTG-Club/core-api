package club.ttg.dnd5.domain.common.rest.mapper;

import club.ttg.dnd5.domain.common.model.notification.Notification;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationDetailResponse;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface NotificationMapper {
    @Mapping(source = "persona.name", target = "persona")
    @Mapping(source = "persona.image", target = "image")
    @Mapping(source = "description", target = "text")
    NotificationResponse toResponse(Notification notification);

    @Mapping(target = "personaId", expression = "java(notification.getPersona() != null ? notification.getPersona().getId().toString() : null)")
    @Mapping(source = "description", target = "text")
    @Mapping(source = "type.name", target = "typeName")
    NotificationDetailResponse toDetailResponse(Notification notification);

    @Mapping(source = "text", target = "description")
    Notification toEntity(NotificationRequest request);

    @Mapping(source = "text", target = "description")
    void updateEntity(NotificationRequest request, @org.mapstruct.MappingTarget Notification notification);
}
