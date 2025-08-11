package club.ttg.dnd5.domain.glossary.service;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.service.BookService;
import club.ttg.dnd5.domain.common.rest.dto.SourceRequest;
import club.ttg.dnd5.domain.filter.model.SearchBody;
import club.ttg.dnd5.domain.glossary.model.Glossary;
import club.ttg.dnd5.domain.glossary.repository.GlossaryRepository;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryDetailedResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.GlossaryShortResponse;
import club.ttg.dnd5.domain.glossary.rest.dto.create.GlossaryRequest;
import club.ttg.dnd5.domain.glossary.rest.mapper.GlossaryMapper;
import club.ttg.dnd5.domain.spell.rest.dto.SpellShortResponse;
import club.ttg.dnd5.exception.EntityExistException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.util.SwitchLayoutUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlossaryService {
    private final GlossaryRepository glossaryRepository;

    private final BookService bookService;

    private final GlossaryMapper glossaryMapper;

    private final GlossaryQueryDslSearchService glossaryQueryDslSearchService;

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

        Book book = Optional.ofNullable(glossaryRequest.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        Glossary glossary = glossaryMapper.toEntity(glossaryRequest, book);
        glossary = glossaryRepository.save(glossary);

        return glossary.getUrl();
    }

    @Transactional
    public String update(String url, GlossaryRequest request) {
        Glossary existingGlossary = glossaryRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Glossary with url %s not found", url)));

        Book book = Optional.ofNullable(request.getSource())
                .map(SourceRequest::getUrl)
                .map(bookService::findByUrl)
                .orElse(null);

        Glossary updatedGlossary = glossaryMapper.toEntity(request, book);
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
}
