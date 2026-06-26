package club.ttg.dnd5.domain.roadmap.service;

import club.ttg.dnd5.domain.common.service.RatingService;
import club.ttg.dnd5.domain.roadmap.model.Roadmap;
import club.ttg.dnd5.domain.roadmap.repository.RoadmapRepository;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapRequest;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapResponse;
import club.ttg.dnd5.domain.roadmap.rest.mapper.RoadmapMapper;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoadmapService {
    public static final String REVISION_ENTITY_TYPE = "roadmap";

    private final RoadmapRepository roadmapRepository;
    private final RoadmapMapper roadmapMapper;
    private final RatingService ratingService;
    private final EntityRevisionService revisionService;

    public Collection<RoadmapResponse> findAll(final boolean visible) {
        List<Roadmap> roadmaps;
        if (visible) {
            roadmaps = roadmapRepository.findAll();
        } else {
            roadmaps = roadmapRepository.findAllByVisible(true);
        }
        return roadmaps.stream()
                .map(roadmapMapper::toResponse)
                .peek(r -> r.setRate(ratingService.getRating("ROADMAP", r.getUrl())))
                .sorted(
                        Comparator.comparingLong((RoadmapResponse r) -> r.getRate().getTotal())
                                .thenComparingDouble(r -> -r.getRate().getValue())
                                .reversed()
                )
                .collect(Collectors.toList());
    }

    public RoadmapResponse findOne(final String url) {
        return roadmapRepository.findById(url).map(roadmapMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap not found"));
    }

    @Transactional
    public String save(final RoadmapRequest roadmap) {
        String url = roadmapRepository.save(roadmapMapper.toEntity(roadmap)).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.CREATE, findFormByUrl(url));
        return url;
    }

    @Transactional
    public String update(final String url, final RoadmapRequest roadmap) {
        var entity = roadmapRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap not found"));
        if (url.equals(roadmap.getUrl())) {
            roadmapMapper.update(entity, roadmap);
            String savedUrl = roadmapRepository.save(entity).getUrl();
            revisionService.record(REVISION_ENTITY_TYPE, savedUrl, RevisionOperation.UPDATE, findFormByUrl(savedUrl));
            return savedUrl;
        }

        if (roadmapRepository.existsById(roadmap.getUrl())) {
            throw new EntityExistException("Roadmap already exists");
        }
        roadmapRepository.deleteById(url);
        roadmapRepository.flush();
        String savedUrl = roadmapRepository.save(roadmapMapper.toEntity(roadmap)).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, savedUrl, RevisionOperation.UPDATE, findFormByUrl(savedUrl));
        return savedUrl;
    }

    @Transactional
    public String remove(final String url) {
        // Жёсткое удаление — снимаем снимок до удаления, чтобы версия осталась в истории.
        RoadmapRequest snapshot = findFormByUrl(url);
        roadmapRepository.deleteById(url);
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.DELETE, snapshot);
        return url;
    }

    public RoadmapRequest findFormByUrl(final String url) {
        return roadmapMapper.toRequest(roadmapRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap not found")));
    }

    public RoadmapResponse preview(final RoadmapRequest request) {
        return roadmapMapper.toResponse(roadmapMapper.toEntity(request));
    }
}
