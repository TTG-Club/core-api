package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.mapper.BackgroundMapper;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.background.model.Background;
import club.ttg.dnd5.domain.background.repository.BackgroundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class BackgroundServiceImpl implements BackgroundService {
    private final BackgroundRepository backgroundRepository;
    private final FeatRepository featRepository;
    private final BackgroundMapper backgroundMapper;

    @Override
    public BackgroundDetailResponse getBackground(final String backgroundUrl) {
        return backgroundMapper.toDetailDto(findByUrl(backgroundUrl));
    }

    @Override
    public Collection<ShortResponse> getBackgrounds() {
        return backgroundRepository.findAll()
                .stream()
                .map(backgroundMapper::toShortDto)
                .toList();
    }

    @Transactional
    @Override
    public BackgroundDetailResponse addBackground(final BackgroundRequest dto) {
        checkUrlExist(dto.getUrl());
        var feat = getFeat(dto.getFeatUrl());
        var background = backgroundRepository.save(backgroundMapper.toEntity(dto, feat));
        return backgroundMapper.toDetailDto(background);
    }

    @Transactional
    @Override
    public BackgroundDetailResponse updateBackgrounds(final String url, final BackgroundRequest dto) {
        checkUrlExist(dto.getUrl());
        if (!url.equals(dto.getUrl())) {
            backgroundRepository.deleteById(url);
        }
        var feat = getFeat(dto.getFeatUrl());
        var background = backgroundRepository.save(backgroundMapper.toEntity(dto, feat));
        return backgroundMapper.toDetailDto(background);
    }

    @Transactional
    @Override
    public ShortResponse deleteBackgrounds(final String url) {
        var entity = findByUrl(url);
        entity.setHiddenEntity(true);
        return backgroundMapper.toShortDto(backgroundRepository.save(entity));
    }

    @Override
    public boolean exists(final String backgroundUrl) {
        return backgroundRepository.existsById(backgroundUrl);
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
}
