package club.ttg.dnd5.domain.workshop.service;

import club.ttg.dnd5.domain.common.model.SectionType;
import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopPairDto;
import club.ttg.dnd5.domain.workshop.rest.dto.WorkshopDto;
import club.ttg.dnd5.domain.workshop.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkshopService {
    private final WorkshopRepository workshopRepository;

    public List<WorkshopDto> getWorkshopUserStatistics(String username) {
        List<WorkshopPairDto> test = workshopRepository.test(username);
        return test.stream()
                .map(p -> WorkshopDto.builder()
                        .type(SectionType.valueOf(p.getSectionType()))
                        .counters(WorkshopDto.Counters.builder()
                                .created(p.getCount()).build())
                        .build())
                .toList();
    }
}
