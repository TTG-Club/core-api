package club.ttg.dnd5.domain.workshop.service;

import club.ttg.dnd5.domain.workshop.repository.WorkshopRepository;
import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopResponse;
import club.ttg.dnd5.domain.workshop.rest.mapper.WorkshopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopService {
    private final WorkshopRepository workshopRepository;
    private final WorkshopMapper workshopMapper;

    public List<WorkshopResponse> getWorkshopUserSections(String username) {
        return workshopRepository.findWorkshopUserStatistics(username).stream()
                .map(workshopMapper::toResponse)
                .toList();
    }
}
