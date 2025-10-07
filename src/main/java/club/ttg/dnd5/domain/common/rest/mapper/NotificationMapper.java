package club.ttg.dnd5.domain.common.rest.mapper;

import club.ttg.dnd5.domain.common.model.notification.Notification;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationRequest;
import club.ttg.dnd5.domain.common.rest.dto.notification.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(source = "persona.name", target = "persona")
    @Mapping(source = "persona.image", target = "image")
    @Mapping(source = "description", target = "text")
    NotificationResponse toResponse(Notification notification);

    Notification toEntity(NotificationRequest request);
}
