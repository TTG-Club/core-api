package club.ttg.dnd5.domain.roadmap.service;

import club.ttg.dnd5.domain.common.service.RatingService;
import club.ttg.dnd5.domain.roadmap.model.Roadmap;
import club.ttg.dnd5.domain.roadmap.repository.RoadmapRepository;
import club.ttg.dnd5.domain.roadmap.rest.dto.RoadmapResponse;
import club.ttg.dnd5.domain.roadmap.rest.mapper.RoadmapMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class RoadmapService {
    private final RoadmapRepository roadmapRepository;
    private final RoadmapMapper roadmapMapper;
    private final RatingService ratingService;

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
                .collect(Collectors.toList());
    }

    public RoadmapResponse findOne(final String url) {
        return roadmapRepository.findById(url).map(roadmapMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Roadmap not found"));
    }

    @Transactional
    public String save(final Roadmap roadmap) {
        return roadmapRepository.save(roadmap).getUrl();
    }

    @Transactional
    public String update(final Roadmap roadmap) {
        var source = roadmapRepository.findById(roadmap.getUrl())
                .orElseThrow(() -> new EntityNotFoundException("Roadmap not found"));
        roadmapMapper.update(roadmap, source);
        return roadmapRepository.save(source).getUrl();
    }

    @Transactional
    public String remove(final String url) {
        roadmapRepository.deleteById(url);
        return url;
    }
}
