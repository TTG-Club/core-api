package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.source.model.Source;
import club.ttg.dnd5.domain.source.service.SourceService;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.repository.GlossaryRepository;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryDetailedResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryShortResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
import club.ttg.dnd5.domain.glossary.rest.mapper.GlossaryMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlossaryService {
    private final GlossaryRepository glossaryRepository;
    private final SourceService sourceService;
    private final GlossaryMapper glossaryMapper;
    private final GlossaryQueryDslSearchService glossaryQueryDslSearchService;
    private final ObjectMapper objectMapper;

    public List<GlossaryShortResponse> search(final @Valid @Size(min = 2) String searchLine, final String filters) {
        var searchBody = SearchBody.parse(filters, objectMapper);
        return glossaryQueryDslSearchService.search(searchLine, searchBody).stream()
                .map(glossaryMapper::toShort)
                .collect(Collectors.toList());
    }

    public List<GlossaryShortResponse> search(String searchLine, SearchBody searchBody) {
        return glossaryQueryDslSearchService.search(searchLine, searchBody).stream()
                .map(glossaryMapper::toShort)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public String save(GlossaryRequest glossaryRequest) {
        if (glossaryRepository.existsById(glossaryRequest.getUrl())) {
            throw new EntityExistException(String.format("Glossary with url %s already exists", glossaryRequest.getUrl()));
        }

        Source source = Optional.ofNullable(glossaryRequest.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);

        Glossary glossary = glossaryMapper.toEntity(glossaryRequest, source);
        glossary = glossaryRepository.save(glossary);

        return glossary.getUrl();
    }

    @Transactional
    public String update(String url, GlossaryRequest request) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        Source source = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(sourceService::findByUrl)
                .orElse(null);

        Glossary updatedGlossary = glossaryMapper.toEntity(request, source);
        updatedGlossary.setUrl(url);
        glossaryRepository.delete(existingGlossary);
        glossaryRepository.flush();
        glossaryRepository.save(updatedGlossary);

        return updatedGlossary.getUrl();
    }

    @Transactional
    @CacheEvict(cacheNames = "countAllMaterials")
    public void delete(String url) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        existingGlossary.setHiddenEntity(true);
        glossaryRepository.save(existingGlossary);
    }

    public GlossaryDetailedResponse findByUrl(String url) {
        Glossary glossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        return glossaryMapper.toDetail(glossary);
    }

    public boolean existOrThrow(String url) {
        if (!glossaryRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Glossary with url %s does not exist", url));
        }
        return true;
    }

    public GlossaryDetailedResponse findDetailedByUrl(String url) {
        Glossary glossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        return glossaryMapper.toDetail(glossary);
    }

    public GlossaryRequest findFormByUrl(String url) {
        Glossary glossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        return glossaryMapper.toRequest(glossary);
    }

    public GlossaryDetailedResponse preview(final GlossaryRequest request) {
        var book = sourceService.findByUrl(request.getSource().getUrl());
        return glossaryMapper.toDetail(glossaryMapper.toEntity(request, book));
    }
}
