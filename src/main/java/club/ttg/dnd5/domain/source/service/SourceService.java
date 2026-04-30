package club.ttg.dnd5.domain.source.service;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.repository.SourceRepository;
import club.ttg.dnd5.domain.source.rest.dto.PublisherDto;
import club.ttg.dnd5.domain.source.rest.dto.SourceDetailResponse;
import club.ttg.dnd5.domain.source.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.source.rest.dto.SourceShortResponse;
import club.ttg.dnd5.domain.source.rest.mapper.SourceMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SourceService {
    private final SourceRepository sourceRepository;
    private final SourceMapper sourceMapper;

    @Transactional(readOnly = true)
    public List<Source> findAll() {
        return sourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SourceShortResponse> search(String searchLine) {
        if (StringUtils.hasText(searchLine)) {
            var invertedSearchLine = SwitchLayoutUtils.switchLayout(searchLine);
            return sourceRepository.findBySearchLine(searchLine, invertedSearchLine, Sort.by("name"))
                    .stream()
                    .map(sourceMapper::toShort)
                    .toList();
        }
        return sourceRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        (Source s) -> Optional.ofNullable(s.getPublisher())
                                .map(PublisherDto::getDate)
                                .orElse(null),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))                .map(sourceMapper::toShort)
                .toList();

    }

    @Transactional(readOnly = true)
    public Source findByUrl(String url) {
        return sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", url))
                );
    }

    @Transactional(readOnly = true)
    public Source findReferenceByUrl(String url) {
        String acronym = sourceRepository.findAcronymByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", url))
                );
        return sourceRepository.getReferenceById(acronym);
    }

    @Transactional(readOnly = true)
    public SourceDetailResponse findDetailByUrl(String url) {
        return sourceMapper.toDetail(sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с акронимом %s не существует", url)))
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
        if (sourceRepository.existsByUrl(request.getUrl())) {
            throw new EntityExistException("Книга с таким url уже существует");
        }

        return sourceRepository.save(sourceMapper.toEntity(request)).getUrl();
    }

    @Transactional
    public String update(String url, final SourceRequest request) {
        var source = sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", request.getUrl())));
        sourceMapper.toEntity(request, source);
        return sourceRepository.save(source).getUrl();
    }

    @Transactional(readOnly = true)
    public SourceRequest findFormByUrl(final String url) {
        return sourceMapper.toRequest(sourceRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Источник с url %s не существует", url))));
    }

    public SourceDetailResponse preview(final SourceRequest request) {
        return sourceMapper.toDetail(sourceMapper.toEntity(request));
    }
}
