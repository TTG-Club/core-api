package club.ttg.dnd5.domain.moderation.service;

import club.ttg.dnd5.domain.moderation.model.ModerationEntity;
import club.ttg.dnd5.domain.moderation.model.StatusType;
import club.ttg.dnd5.domain.moderation.repository.ModerationRepository;
import club.ttg.dnd5.domain.moderation.rest.dto.ModerationRequest;
import club.ttg.dnd5.domain.moderation.rest.dto.ModerationResponse;
import club.ttg.dnd5.domain.moderation.rest.dto.ModerationShortResponse;
import club.ttg.dnd5.domain.moderation.rest.mapper.ModerationMapper;
import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.common.model.Timestamped;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerationService {
    private final ModerationRepository moderationRepository;
    private final ModerationMapper moderationMapper;

    public List<ModerationResponse> getAllPages(List<SectionType> sections, List<StatusType> statusTypes, Pageable pageable) {
        return moderationRepository.findByStatusTypeInAndSectionTypeIn(statusTypes, sections, pageable).stream()
                .map(moderationMapper::toResponse)
                .toList();
    }

    public List<Pair<SectionType, Integer>> getPageCount(List<StatusType> statusTypes) {
        return moderationRepository.findByStatusTypeIn(statusTypes).stream()
                .collect(Collectors.groupingBy(ModerationEntity::getSectionType))
                .entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue().size()))
                .toList();
    }

    public void update(ModerationRequest request, String url) {
        Optional<ModerationEntity> adminEntityOpt = moderationRepository.findByUrl(url);
        if (adminEntityOpt.isPresent()){
            ModerationEntity moderationEntity = adminEntityOpt.get();
            moderationEntity.setComment(request.getComment());
            moderationRepository.save(moderationEntity);
        }
    }

    public ModerationShortResponse getPageStatus(String url) {
        ModerationEntity page = moderationRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Страница с url %s не существует", url)));
        return moderationMapper.toShortResponse(page);
    }

    public void addAdminEntity(String url, SectionType sectionType, Timestamped timestamped) {
        ModerationEntity entity = moderationMapper.toEntity(timestamped);
        entity.setUrl(url);
        entity.setSectionType(sectionType);
        moderationRepository.save(entity);
    }

    public void updateAdminEntity(String url, StatusType statusType) {
        Optional<ModerationEntity> entityOpt = moderationRepository.findByUrl(url);
        if (entityOpt.isPresent()) {
            ModerationEntity moderationEntity = entityOpt.get();
            moderationEntity.setStatusType(statusType);
            moderationEntity.setUpdatedAt(Instant.now());
            moderationRepository.save(moderationEntity);
        }
    }
}
