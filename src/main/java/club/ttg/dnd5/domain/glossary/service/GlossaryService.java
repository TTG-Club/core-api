package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.repository.GlossaryRepository;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryDetailedResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryShortResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
import club.ttg.dnd5.domain.glossary.rest.mapper.GlossaryMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlossaryService {
    private final GlossaryRepository glossaryRepository;

    private final GlossaryMapper glossaryMapper;

    private static final Sort DEFAULT_GLOSSARY_SORT = Sort.by("name", "tags");

    public List<GlossaryShortResponse> search(String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> glossaryRepository.findBySearchLine(line, Sort.by(Sort.Order.asc("name"))))
                .orElseGet(() -> findAll(DEFAULT_GLOSSARY_SORT));
    }

    public List<GlossaryShortResponse> findAll(Sort sort) {
        List<Glossary> glossaries = glossaryRepository.findAll(sort);
        return glossaries.stream()
                .map(glossaryMapper::toShortResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public GlossaryDetailedResponse save(GlossaryRequest glossaryRequest) {
        if (glossaryRepository.existsById(glossaryRequest.getUrl())) {
            throw new EntityExistException(String.format("Glossary with url %s already exists", glossaryRequest.getUrl()));
        }
        Glossary glossary = glossaryMapper.toEntity(glossaryRequest);

        glossary = glossaryRepository.save(glossary);
        return glossaryMapper.toGlossaryDetailedResponse(glossary);
    }

    @Transactional
    public GlossaryDetailedResponse update(String url, GlossaryRequest request) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        Glossary updatedGlossary = glossaryMapper.toEntity(request);
        updatedGlossary.setUrl(url);
        glossaryRepository.delete(existingGlossary);
        glossaryRepository.save(updatedGlossary);

        return glossaryMapper.toGlossaryDetailedResponse(updatedGlossary);
    }

    @Transactional
    public void delete(String url) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        existingGlossary.setHiddenEntity(true);
        glossaryRepository.save(existingGlossary);
    }

    public GlossaryDetailedResponse findByUrl(String url) {
        Glossary glossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        return glossaryMapper.toGlossaryDetailedResponse(glossary);
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

        return glossaryMapper.toGlossaryDetailedResponse(glossary);
    }

    public GlossaryRequest findFormByUrl(String url) {
        Glossary glossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        return glossaryMapper.toGlossaryRequest(glossary);
    }
}
