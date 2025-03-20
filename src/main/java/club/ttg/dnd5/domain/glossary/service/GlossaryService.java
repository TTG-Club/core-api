package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.repository.GlossaryRepository;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GlossaryService {
    private final GlossaryRepository glossaryRepository;

    public List<Glossary> search(String searchLine) {
        return Optional.ofNullable(searchLine)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(line -> glossaryRepository.findBySearchLine(line, Sort.by(Sort.Order.asc("name"))))
                .orElseGet(() -> findAll());
    }

    public List<Glossary> findAll() {
        return glossaryRepository.findAll(Sort.by(Sort.Order.asc("name")));
    }

    @Transactional
    public Glossary save(Glossary glossary) {
        if (glossaryRepository.existsById(glossary.getUrl())) {
            throw new EntityExistException(String.format("Glossary with url %s already exists", glossary.getUrl()));
        }
        return glossaryRepository.save(glossary);
    }

    @Transactional
    public Glossary update(String url, Glossary glossary) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        existingGlossary.setName(glossary.getName());
        existingGlossary.setEnglish(glossary.getEnglish());
        existingGlossary.setAlternative(glossary.getAlternative());
        existingGlossary.setTags(glossary.getTags());
        existingGlossary.setDescription(glossary.getDescription());
        existingGlossary.setImageUrl(glossary.getImageUrl());

        return glossaryRepository.save(existingGlossary);
    }

    @Transactional
    public void delete(String url) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        existingGlossary.setHiddenEntity(true);
        glossaryRepository.save(existingGlossary);
    }
}
