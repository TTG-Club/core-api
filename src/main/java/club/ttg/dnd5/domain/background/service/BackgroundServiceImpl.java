package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundSelectResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.background.rest.mapper.BackgroundMapper;
import club.ttg.dnd5.domain.common.model.mechanics.SpellGrant;
import club.ttg.dnd5.domain.common.service.GrantedSpellResolver;
import club.ttg.dnd5.domain.feat.model.mechanics.FeatMechanics;
import club.ttg.dnd5.domain.feat.rest.dto.FeatGrantedSpellResponse;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.item.service.EquipmentNameResolver;
import club.ttg.dnd5.domain.revision.model.RevisionOperation;
import club.ttg.dnd5.domain.revision.service.EntityRevisionService;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BackgroundServiceImpl implements BackgroundService {
    public static final String REVISION_ENTITY_TYPE = "background";

    private final BackgroundQueryDslSearchService backgroundQueryDslSearchService;
    private final BackgroundRepository backgroundRepository;
    private final FeatRepository featRepository;
    private final SourceService sourceService;
    private final BackgroundMapper backgroundMapper;
    private final EntityRevisionService revisionService;
    private final EquipmentNameResolver equipmentNameResolver;
    private final GrantedSpellResolver grantedSpellResolver;


    @Override
    public BackgroundDetailResponse getBackground(final String backgroundUrl) {
        BackgroundDetailResponse response = backgroundMapper.toDetail(findByUrl(backgroundUrl));
        equipmentNameResolver.resolveNames(response.getStartingEquipment());
        response.setGrantedSpells(resolveGrantedSpells(response));
        return response;
    }

    /**
     * Дополняет выдаваемые предысторией заклинания данными справочника: в дарах они лежат
     * ссылками, а потребителю нужны круг и школа — лист персонажа без круга не положит
     * заклинание в книгу. Ровно так же поступает деталь черты: дары у них одной модели.
     *
     * <p>Ненайденное заклинание пропускается, а не роняет ответ: это свободный JSONB,
     * набранный руками в редакторе, и опечатка в url не должна ронять страницу целиком.</p>
     *
     * @param response деталь предыстории с разобранными дарами.
     * @return выдаваемые заклинания с данными справочника; null — предыстория их не выдаёт.
     */
    private Collection<FeatGrantedSpellResponse> resolveGrantedSpells(final BackgroundDetailResponse response) {
        var granted = Optional.ofNullable(response.getMechanics())
                .map(FeatMechanics::getSpells)
                .map(SpellGrant::getSpells)
                .orElse(List.of());

        if (CollectionUtils.isEmpty(granted)) {
            return null;
        }

        var spellsByUrl = grantedSpellResolver.shortSpellsByUrl(granted);

        var result = granted.stream()
                .filter(Objects::nonNull)
                .filter(ref -> spellsByUrl.containsKey(ref.getUrl()))
                .map(ref -> new FeatGrantedSpellResponse(spellsByUrl.get(ref.getUrl()),
                        ref.getRequiredLevel()))
                .toList();

        return result.isEmpty() ? null : result;
    }



    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addBackground(final BackgroundRequest request) {
        checkUrlExist(request.getUrl());
        var feat = getFeatReference(request.getFeatUrl());
        var source = sourceService.findReferenceByUrl(request.getSource().getUrl());
        Background saved = backgroundRepository.save(backgroundMapper.toEntity(request, feat, source));
        revisionService.record(REVISION_ENTITY_TYPE, saved.getUrl(), RevisionOperation.CREATE,
                backgroundMapper.toRequest(saved));
        return saved.getUrl();
    }

    @Transactional
    @Override
    public String updateBackgrounds(final String url, final BackgroundRequest request) {
        var feat = getFeatReference(request.getFeatUrl());
        var source = sourceService.findReferenceByUrl(request.getSource().getUrl());

        if (url.equals(request.getUrl())) {
            var existing = findByUrl(url);
            backgroundMapper.updateEntity(request, feat, source, existing);
            Background saved = backgroundRepository.save(existing);
            revisionService.record(REVISION_ENTITY_TYPE, saved.getUrl(), RevisionOperation.UPDATE,
                    backgroundMapper.toRequest(saved));
            return saved.getUrl();
        }

        findByUrl(url);
        checkUrlExist(request.getUrl());
        backgroundRepository.deleteById(url);
        backgroundRepository.flush();
        Background saved = backgroundRepository.save(backgroundMapper.toEntity(request, feat, source));
        revisionService.record(REVISION_ENTITY_TYPE, saved.getUrl(), RevisionOperation.UPDATE,
                backgroundMapper.toRequest(saved));
        return saved.getUrl();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String deleteBackgrounds(final String url) {
        var entity = findByUrl(url);
        entity.setHiddenEntity(true);
        Background saved = backgroundRepository.save(entity);
        revisionService.record(REVISION_ENTITY_TYPE, saved.getUrl(), RevisionOperation.DELETE,
                backgroundMapper.toRequest(saved));
        return saved.getUrl();
    }

    @Override
    public boolean exists(final String backgroundUrl) {
        return backgroundRepository.existsById(backgroundUrl);
    }

    @Override
    public BackgroundRequest findFormByUrl(final String url) {
        return backgroundMapper.toRequest(findByUrl(url));
    }

    private Feat getFeat(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Черта не найдена по URL: " + url));
    }

    private Feat getFeatReference(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (!featRepository.existsById(url)) {
            throw new EntityNotFoundException("Черта не найдена по URL: " + url);
        }
        return featRepository.getReferenceById(url);
    }

    private void checkUrlExist(String url) {
        if (backgroundRepository.existsById(url)) {
            throw new EntityExistException(String.format("Предыстория с url %s уже существует", url));
        }
    }

    private Background findByUrl(String url) {
        return backgroundRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Предыстория не найден по URL: " + url));
    }

    public BackgroundDetailResponse preview(final BackgroundRequest request) {
        var book = sourceService.findByUrl(request.getSource().getUrl());
        var feat = getFeat(request.getFeatUrl());
        BackgroundDetailResponse response = backgroundMapper.toDetail(backgroundMapper.toEntity(request, feat, book));
        equipmentNameResolver.resolveNames(response.getStartingEquipment());
        response.setGrantedSpells(resolveGrantedSpells(response));
        return response;
    }

    @Override
    public Collection<BackgroundSelectResponse> getBackgroundsSelect(final @Valid @Size String searchLine) {
        return backgroundRepository.findBySearchLine(searchLine,
                        SwitchLayoutUtils.switchLayout(searchLine == null ? "" : searchLine),
                        Sort.by("name"))
                .stream()
                .peek(b -> b.setDescription(null))
                .map(backgroundMapper::toSelect)
                .toList();
    }

    @Override
    public Collection<BackgroundShortResponse> search(final club.ttg.dnd5.domain.background.rest.dto.BackgroundQueryRequest request)
    {
        var predicate = BackgroundPredicateBuilder.build(request);
        return backgroundQueryDslSearchService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .map(backgroundMapper::toShort)
                .toList();
    }
}
