package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundShortResponse;
import club.ttg.dnd5.domain.background.rest.mapper.BackgroundMapper;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class BackgroundServiceImpl implements BackgroundService {
    private final BackgroundQueryDslSearchService backgroundQueryDslSearchService;
    private final BackgroundRepository backgroundRepository;
    private final FeatRepository featRepository;
    private final SourceService sourceService;
    private final BackgroundMapper backgroundMapper;

    @Override
    public BackgroundDetailResponse getBackground(final String backgroundUrl) {
        return backgroundMapper.toDetail(findByUrl(backgroundUrl));
    }

    @Override
    public Collection<BackgroundShortResponse> getBackgrounds(String searchLine, final SearchBody searchBody) {
        return backgroundQueryDslSearchService.search(searchLine, searchBody)
                .stream()
                .map(backgroundMapper::toShort)
                .toList();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String addBackground(final BackgroundRequest request) {
        checkUrlExist(request.getUrl());
        var feat = getFeat(request.getFeatUrl());
        var source = sourceService.findByUrl(request.getSource().getUrl());
        return backgroundRepository.save(backgroundMapper.toEntity(request, feat, source))
                .getUrl();
    }

    @Transactional
    @Override
    public String updateBackgrounds(final String url, final BackgroundRequest request) {
        var feat = getFeat(request.getFeatUrl());
        var source = sourceService.findByUrl(request.getSource().getUrl());
        if (!Objects.equals(url, request.getUrl())) {
            backgroundRepository.deleteById(url);
            backgroundRepository.flush();
        }
        return backgroundRepository.save(backgroundMapper.toEntity(request, feat, source))
                .getUrl();
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "countAllMaterials")
    public String deleteBackgrounds(final String url) {
        var entity = findByUrl(url);
        entity.setHiddenEntity(true);
        return backgroundRepository.save(entity).getUrl();
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
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Черта не найдена по URL: " + url));
    }

    private void checkUrlExist(String url) {
        if (backgroundRepository.existsById(url)) {
            throw new EntityExistException("Предыстория существует с url: " + url);
        }
    }

    private Background findByUrl(String url) {
        return backgroundRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Предыстория не найден по URL: " + url));
    }

    public BackgroundDetailResponse preview(final BackgroundRequest request) {
        var book = sourceService.findByUrl(request.getSource().getUrl());
        var feat = getFeat(request.getFeatUrl());
        return backgroundMapper.toDetail(backgroundMapper.toEntity(request, feat, book));
    }

    @Override
    public Collection<BackgroundRequest> getBackgroundsRaw() {
        return backgroundRepository.findAll().stream()
                .map(backgroundMapper::toRequest).toList();
    }
}
