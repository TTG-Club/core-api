package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.repository.GlossaryRepository;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryRequest;
import club.ttg.dnd5.domain.glossary.rest.mapper.GlossaryMapper;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
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

    public List<GlossaryResponse> search(String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> glossaryRepository.findBySearchLine(line, Sort.by(Sort.Order.asc("name"))))
                .orElseGet(() -> findAll())
                .stream()
                .map(glossaryMapper::toGlossaryDetailedResponse)
                .collect(Collectors.toList());
    }

    public List<Glossary> findAll() {
        return glossaryRepository.findAll(Sort.by(Sort.Order.asc("name")));
    }

    @Transactional
    public GlossaryResponse save(GlossaryRequest glossaryRequest) {
        if (glossaryRepository.existsById(glossaryRequest.getUrl())) {
            throw new EntityExistException(String.format("Glossary with url %s already exists", glossaryRequest.getUrl()));
        }
        Glossary glossary = glossaryMapper.toEntity(glossaryRequest);

        glossary = glossaryRepository.save(glossary);
        return glossaryMapper.toResponse(glossary);
    }

    @Transactional
    public GlossaryResponse update(String url, GlossaryRequest request) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        glossaryMapper.updateEntity(existingGlossary, request);

        existingGlossary = glossaryRepository.save(existingGlossary);

        return glossaryMapper.toResponse(existingGlossary);
    }

    @Transactional
    public void delete(String url) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        existingGlossary.setHiddenEntity(true);
        glossaryRepository.save(existingGlossary);
    }

    public Glossary findByUrl(String url) {
        return glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));
    }

    public boolean existOrThrow(String url) {
        if (!glossaryRepository.existsById(url)) {
            throw new EntityNotFoundException(String.format("Glossary with url %s does not exist", url));
        }
        return true;
    }

    public GlossaryResponse findDetailedByUrl(String url) {
        return glossaryMapper.toGlossaryDetailedResponse(findByUrl(url));
    }

    public GlossaryRequest findFormByUrl(String url) {
        return glossaryMapper.toGlossaryRequest(findByUrl(url));
    }
}
