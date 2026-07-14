package club.ttg.dnd5.domain.tool.tracker.rest.mapper;

import club.ttg.dnd5.domain.tool.tracker.model.InitiativeParticipant;
import club.ttg.dnd5.domain.tool.tracker.model.InitiativeTracker;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.ParticipantResponse;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerDetailedResponse;
import club.ttg.dnd5.domain.tool.tracker.rest.dto.TrackerShortResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "spring")
public interface InitiativeTrackerMapper {

    /**
     * Ответ на создание — единственный, в котором отдаётся секретный ключ доступа
     * {@code accessKey} (аноним сохраняет его на клиенте).
     */
    @Mapping(source = "tracker.id", target = "id")
    @Mapping(source = "tracker.name", target = "name")
    @Mapping(source = "tracker.status", target = "status")
    @Mapping(source = "tracker.status.name", target = "statusName")
    @Mapping(source = "tracker.round", target = "round")
    @Mapping(source = "tracker.rerollEachRound", target = "rerollEachRound")
    @Mapping(source = "tracker.currentParticipantId", target = "currentParticipantId")
    @Mapping(source = "tracker.accessKey", target = "accessKey")
    @Mapping(source = "tracker.createdAt", target = "createdAt")
    @Mapping(source = "tracker.updatedAt", target = "updatedAt")
    @Mapping(source = "participants", target = "participants")
    TrackerDetailedResponse toCreatedResponse(InitiativeTracker tracker, List<InitiativeParticipant> participants);

    @Mapping(source = "tracker.id", target = "id")
    @Mapping(source = "tracker.name", target = "name")
    @Mapping(source = "tracker.status", target = "status")
    @Mapping(source = "tracker.status.name", target = "statusName")
    @Mapping(source = "tracker.round", target = "round")
    @Mapping(source = "tracker.rerollEachRound", target = "rerollEachRound")
    @Mapping(source = "tracker.currentParticipantId", target = "currentParticipantId")
    @Mapping(target = "accessKey", ignore = true)
    @Mapping(source = "tracker.createdAt", target = "createdAt")
    @Mapping(source = "tracker.updatedAt", target = "updatedAt")
    @Mapping(source = "participants", target = "participants")
    TrackerDetailedResponse toDetailedResponse(InitiativeTracker tracker, List<InitiativeParticipant> participants);

    @Mapping(source = "status.name", target = "statusName")
    TrackerShortResponse toShortResponse(InitiativeTracker tracker);

    List<TrackerShortResponse> toShortResponseList(Collection<InitiativeTracker> trackers);

    @Mapping(source = "type.name", target = "typeName")
    ParticipantResponse toParticipantResponse(InitiativeParticipant participant);

    List<ParticipantResponse> toParticipantResponseList(Collection<InitiativeParticipant> participants);
}
