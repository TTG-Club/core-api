package club.ttg.dnd5.domain.background.service;

import club.ttg.dnd5.domain.background.rest.dto.BackgroundDetailResponse;
import club.ttg.dnd5.domain.background.rest.dto.BackgroundRequest;
import club.ttg.dnd5.domain.background.rest.mapper.BackgroundMapper;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
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
        var background = backgroundRepository.save(backgroundMapper.toEntity(dto));
        return backgroundMapper.toDetailDto(background);
    }

    @Transactional
    @Override
    public BackgroundDetailResponse updateBackgrounds(final String url, final BackgroundRequest dto) {
        checkUrlExist(dto.getUrl());
        if (!url.equals(dto.getUrl())) {
            backgroundRepository.deleteById(url);
        }
        var background = backgroundRepository.save(backgroundMapper.toEntity(dto));
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

    private void checkUrlExist(String url) {
        if (backgroundRepository.existsById(url)) {
            throw new EntityExistException("Background exist by url");
        }
    }

    private Background findByUrl(String url) {
        return backgroundRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with URL: " + url));
    }
}
