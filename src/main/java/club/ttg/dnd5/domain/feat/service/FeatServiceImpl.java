package club.ttg.dnd5.domain.feat.service;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatDetailResponse;
import club.ttg.dnd5.domain.feat.rest.dto.FeatRequest;
import club.ttg.dnd5.domain.feat.rest.mapper.FeatMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.feat.model.Feat;
import club.ttg.dnd5.domain.feat.repository.FeatRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class FeatServiceImpl implements FeatService {
    private final FeatRepository featRepository;
    private final FeatMapper featMapper;

    @Override
    public FeatDetailResponse getFeat(final String featUrl) {
        return featMapper.toDetailDto(findByUrl(featUrl));
    }

    @Override
    public Collection<ShortResponse> getFeats() {
        return featRepository.findAll()
                .stream()
                .map(featMapper::toShortDto)
                .toList();
    }

    @Transactional
    @Override
    public FeatDetailResponse addFeat(final FeatRequest dto) {
        if (featRepository.existsById(dto.getUrl())) {
            throw new EntityExistException("Feature exist");
        }
        var feat = featMapper.toEntity(dto);
        return featMapper.toDetailDto(featRepository.save(feat));
    }

    @Transactional
    @Override
    public FeatDetailResponse updateFeat(final String featUrl, final FeatRequest dto) {
        var entity = findByUrl(featUrl);
        if (!featUrl.equalsIgnoreCase(dto.getUrl())) {
            featRepository.deleteById(featUrl);
        }
        var feat = featMapper.toEntity(dto);
        return featMapper.toDetailDto(featRepository.save(entity));
    }

    @Transactional
    @Override
    public ShortResponse delete(final String featUrl) {
        var entity = findByUrl(featUrl);
        entity.setHiddenEntity(true);
        return featMapper.toShortDto(featRepository.save(entity));
    }

    @Override
    public boolean exists(final String featUrl) {
        return featRepository.existsById(featUrl);
    }

    private Feat findByUrl(String url) {
        return featRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException("Class not found with URL: " + url));
    }
}
