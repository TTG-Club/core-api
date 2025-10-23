package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.repository.SourceRepository;
import club.ttg.dnd5.domain.source.rest.dto.SourceDetailResponse;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.source.rest.mapper.SourceMapper;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SourceService {
    private final SourceRepository sourceRepository;
    private final SourceMapper sourceMapper;

    public List<ShortResponse> search(String searchLine) {
        if (StringUtils.hasText(searchLine)) {
            var invertedSearchLine = SwitchLayoutUtils.switchLayout(searchLine);
            return sourceRepository.findBySearchLine(searchLine, invertedSearchLine, Sort.by("name"))
                    .stream()
                    .map(sourceMapper::toShort)
                    .toList();
        }
        return sourceRepository.findAll()
                .stream()
                .map(sourceMapper::toShort)
                .toList();

    }

    public Source findByUrl(String url) {
        return sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует" , url))
                );
    }

    public SourceDetailResponse findDetailByUrl(String url) {
        return sourceMapper.toDetail(sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует" , url)))
        );
    }

    public Optional<Source> findByUrOptional(String url) {
        return sourceRepository.findByUrl(url);
    }

    @Transactional
    public String save(final SourceRequest request) {
        if (sourceRepository.existsById(request.getAcronym())) {
            throw new EntityExistException("Книга с таким акронимом уже существует");
        }
        return sourceRepository.save(sourceMapper.toEntity(request)).getUrl();
    }

    @Transactional
    public String update(final SourceRequest request) {
        var source = sourceRepository.findByUrl(request.getUrl())
                .orElseThrow(() -> new EntityNotFoundException(
                                String.format("Источник с url %s не существует" , request.getUrl())));
        sourceMapper.toEntity(request, source);
        return sourceRepository.save(source).getUrl();
    }

    public SourceRequest findFormByUrl(final String url) {
        return sourceMapper.toRequest(sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                String.format("Источник с url %s не существует" , url))));
    }

    public SourceDetailResponse preview(final SourceRequest request) {
        return sourceMapper.toDetail(sourceMapper.toEntity(request));
    }
}
