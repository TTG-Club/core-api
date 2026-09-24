package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.model.FacilityOrderOption;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityRequest;
import club.ttg.dnd5.domain.bastion.rest.mapper.BastionFacilityMapper;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class BastionFacilityServiceTest {
    private final BastionFacilityRepository repository = mock(BastionFacilityRepository.class);
    private final BastionFacilityService service = new BastionFacilityService(
            repository,
            mock(SourceService.class),
            mock(BastionFacilityMapper.class),
            mock(BastionFacilityQueryDslSearchService.class),
            mock(EntityRevisionService.class));

    /** «Обслуживать» отдаётся всему бастиону — сооружение его не принимает. */
    @Test
    void maintainIsRejectedInFacilityOrders() {
        BastionFacilityRequest request = new BastionFacilityRequest();
        request.setUrl("library");
        request.setOrders(List.of(BastionOrder.RESEARCH, BastionOrder.MAINTAIN));

        assertThrows(ApiException.class, () -> service.save(request));
        verifyNoInteractions(repository);
    }

    @Test
    void maintainIsRejectedInOrderOptions() {
        FacilityOrderOption option = new FacilityOrderOption();
        option.setOrder(BastionOrder.MAINTAIN);
        BastionFacilityRequest request = new BastionFacilityRequest();
        request.setUrl("library");
        request.setOrders(List.of(BastionOrder.RESEARCH));
        request.setOrderOptions(List.of(option));

        assertThrows(ApiException.class, () -> service.update("library", request));
        verifyNoInteractions(repository);
    }
}
