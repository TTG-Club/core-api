package club.ttg.dnd5.domain.bastion.service;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import club.ttg.dnd5.domain.bastion.model.BastionOrder;
import club.ttg.dnd5.domain.bastion.repository.BastionFacilityRepository;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityDetailResponse;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityQueryRequest;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityRequest;
import club.ttg.dnd5.domain.bastion.rest.dto.BastionFacilityShortResponse;
import club.ttg.dnd5.domain.bastion.rest.mapper.BastionFacilityMapper;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BastionFacilityService {
    public static final String REVISION_ENTITY_TYPE = "bastion";

    private final BastionFacilityRepository facilityRepository;
    private final SourceService sourceService;
    private final BastionFacilityMapper facilityMapper;
    private final BastionFacilityQueryDslSearchService searchService;
    private final EntityRevisionService revisionService;

    public List<BastionFacilityShortResponse> search(final BastionFacilityQueryRequest request) {
        var predicate = BastionFacilityPredicateBuilder.build(request);
        return searchService.search(predicate, request.getPage(), request.getPageSize()).stream()
                .map(facilityMapper::toShort)
                .toList();
    }

    public BastionFacilityDetailResponse findDetailedByUrl(String url) {
        return facilityMapper.toDetail(findByUrl(url));
    }

    public BastionFacilityRequest findFormByUrl(String url) {
        return facilityMapper.toRequest(findByUrl(url));
    }

    public boolean existOrThrow(String url) {
        if (!facilityRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Сооружение бастиона с url %s не существует", url));
        }
        return true;
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public String save(BastionFacilityRequest request) {
        validateOrders(request);
        if (facilityRepository.existsById(request.getUrl())) {
            throw new EntityExistException(String.format("Сооружение бастиона с url %s уже существует",
                    request.getUrl()));
        }
        BastionFacility facility = facilityRepository.save(facilityMapper.toEntity(request, findSource(request)));
        revisionService.record(REVISION_ENTITY_TYPE, facility.getUrl(), RevisionOperation.CREATE,
                findFormByUrl(facility.getUrl()));
        return facility.getUrl();
    }

    @Transactional
    public String update(String url, BastionFacilityRequest request) {
        validateOrders(request);
        BastionFacility existing = findByUrl(url);
        Source source = findSource(request);

        if (url.equals(request.getUrl())) {
            facilityMapper.updateEntity(request, source, existing);
            facilityRepository.save(existing);
            revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.UPDATE, findFormByUrl(url));
            return url;
        }

        if (facilityRepository.existsById(request.getUrl())) {
            throw new EntityExistException(String.format("Сооружение бастиона с url %s уже существует",
                    request.getUrl()));
        }
        facilityRepository.deleteById(url);
        facilityRepository.flush();
        String savedUrl = facilityRepository.save(facilityMapper.toEntity(request, source)).getUrl();
        revisionService.record(REVISION_ENTITY_TYPE, savedUrl, RevisionOperation.UPDATE, findFormByUrl(savedUrl));
        return savedUrl;
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public void delete(String url) {
        BastionFacility existing = findByUrl(url);
        existing.setHiddenEntity(true);
        facilityRepository.save(existing);
        revisionService.record(REVISION_ENTITY_TYPE, url, RevisionOperation.DELETE, findFormByUrl(url));
    }

    public BastionFacilityDetailResponse preview(final BastionFacilityRequest request) {
        validateOrders(request);
        return facilityMapper.toDetail(facilityMapper.toEntity(request, findSource(request)));
    }

    /**
     * Приказ всему бастиону ({@link BastionOrder#isBastionWide()}) сооружению не отдаётся,
     * поэтому ни в списке приказов, ни в вариантах его быть не может.
     */
    private void validateOrders(BastionFacilityRequest request) {
        boolean inOrders = request.getOrders() != null
                && request.getOrders().stream().anyMatch(o -> o != null && o.isBastionWide());
        boolean inOptions = request.getOrderOptions() != null
                && request.getOrderOptions().stream()
                        .anyMatch(o -> o != null && o.getOrder() != null && o.getOrder().isBastionWide());
        if (inOrders || inOptions) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Приказ «Обслуживать» отдаётся всему бастиону и не может быть приказом сооружения");
        }
    }

    private BastionFacility findByUrl(String url) {
        return facilityRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Сооружение бастиона с url %s не найдено", url)));
    }

    private Source findSource(BastionFacilityRequest request) {
        return Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);
    }
}
