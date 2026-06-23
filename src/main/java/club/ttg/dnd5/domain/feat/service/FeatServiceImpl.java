package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.feat.model.FeatCategory;
import club.ttg.dnd5.domain.feat.rest.dto.FeatSelectResponse;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.dto.FeatShortResponse;
import club.ttg.dnd5.domain.feat.rest.mapper.FeatMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeatServiceImpl implements FeatService {
    private final FeatRepository featRepository;
    private final BackgroundRepository backgroundRepository;
    private final FeatQueryDslSearchService featQueryDslSearchService;
    private final SourceService sourceService;
    private final FeatMapper featMapper;

    @Override
    public FeatDetailResponse getFeat(final String featUrl) {
        return featMapper.toDetail(findByUrl(featUrl));
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addFeat(final FeatRequest dto) {
        if (featRepository.existsById(dto.getUrl())) {
            throw new EntityExistException("Feat exist by URL: " + dto.getUrl());
        }
        var book = sourceService.findByUrl(dto.getSource().getUrl());
        var feat = featMapper.toEntity(dto, book);
        return featRepository.save(feat).getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    public String updateFeat(final String featUrl, final FeatRequest request) {
        var existing = findByUrl(featUrl);
        var book = sourceService.findByUrl(request.getSource().getUrl());

        if (featUrl.equals(request.getUrl())) {
            // url не изменился — обновляем существующую строку (merge вместо delete+insert),
            // чтобы не задеть FK fk_background_on_feat. createdAt переносим, иначе isNew()
            // вернёт true и Spring Data попытается сделать INSERT (дубль ключа).
            featMapper.updateEntity(request, book, existing);
            return featRepository.save(existing).getUrl();
        }

        // url изменился — создаём черту с новым id, переводим на неё ссылки предысторий
        // и удаляем старую. Порядок важен: новая черта должна существовать до repoint,
        // а старую удаляем уже без ссылок.
        if (featRepository.existsById(request.getUrl())) {
            throw new EntityExistException("Feat exist by URL: " + request.getUrl());
        }
        var feat = featMapper.toEntity(request, book);
        featRepository.saveAndFlush(feat);
        backgroundRepository.repointFeat(featUrl, feat.getUrl());
        featRepository.deleteById(featUrl);
        return feat.getUrl();
    }

    @Secured("ADMIN")
    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String delete(final String featUrl) {
        var entity = findByUrl(featUrl);
        entity.setHiddenEntity(true);
        return featRepository.save(entity).getUrl();
    }

    @Override
    public boolean existOrThrow(final String url) {
        if (!featRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Черта с url %s не существует", url));
        }
        return true;
    }

    @Override
    public FeatRequest findFormByUrl(final String url) {
        return featMapper.toRequest(findByUrl(url));
    }

    private Feat findByUrl(String url) {
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Черта не найдена по URL: " + url));
    }

    public FeatDetailResponse preview(final FeatRequest request) {
        var book = sourceService.findByUrl(request.getSource().getUrl());
        return featMapper.toDetail(featMapper.toEntity(request, book));
    }

    @Override
    public Collection<FeatSelectResponse> getFeatsSelect(
            final String searchLine,
            final Set<FeatCategory> categories) {
        return featRepository.findBySearchLine(searchLine,
                        SwitchLayoutUtils.switchLayout(searchLine == null ? "" : searchLine),
                        Sort.by("name"))
                .stream()
                .filter(f -> CollectionUtils.isEmpty(categories) || categories.contains(f.getCategory()))
                .map(featMapper::toSelect)
                .toList();
    }
    @Override
    public Collection<FeatShortResponse> search(final club.ttg.dnd5.domain.feat.rest.dto.FeatQueryRequest request)
    {
        var predicate = FeatPredicateBuilder.build(request);
        return featQueryDslSearchService.search(predicate, request.getPage(), request.getPageSize())
                .stream()
                .map(featMapper::toShort)
                .toList();
    }

    @Override
    public Set<Feat> findAllById(final Set<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return Set.of();
        }
        return urls.stream()
                .map(featRepository::getReferenceById)
                .collect(Collectors.toSet());
    }
}
