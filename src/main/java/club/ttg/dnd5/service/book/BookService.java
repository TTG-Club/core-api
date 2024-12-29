package club.ttg.dnd5.service.book;

import club.ttg.dnd5.dto.base.NameBasedDTO;
import club.ttg.dnd5.dto.base.TranslationDTO;
import club.ttg.dnd5.dto.book.SourceBookDTO;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.base.TagType;
import club.ttg.dnd5.model.base.Translation;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.TypeBook;
import club.ttg.dnd5.repository.TagRepository;
import club.ttg.dnd5.repository.book.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final TagRepository tagRepository;

    // Создание новой книги
    @Transactional
    public SourceBookDTO createBook(SourceBookDTO sourceBookDTO) {
        Book book = convertingCreateSourceToEntity(sourceBookDTO);
        Book savedBook = bookRepository.save(book);
        return convertingEntityToSourceDTO(savedBook);
    }

    public List<SourceBookDTO> getAllBooks() {
        return bookRepository.findAll().stream().map(this::convertingEntityToSourceDTO).collect(Collectors.toList());
    }

    // Получение книги по её sourceAcronym
    public Optional<SourceBookDTO> getBookBySourceAcronym(String sourceAcronym) {
        return bookRepository.findBySourceAcronym(sourceAcronym).map(this::convertingEntityToSourceDTO);
    }

    // Поиск книги по типу
    public List<SourceBookDTO> getBooksByType(String type) {
        TypeBook bookType = TypeBook.valueOf(type.toUpperCase());
        return bookRepository.findByType(bookType).stream().map(this::convertingEntityToSourceDTO).toList();
    }

    // Получение всех типов книг
    public List<String> getAllBookTypes() {
        return Arrays.stream(TypeBook.values()).map(TypeBook::getName).toList();
    }

    // Получение всех книг с определённым тегом
    public List<SourceBookDTO> getBooksByTag(String tagName) {
        Tag tag = tagRepository.findByNameIgnoreCase(tagName).orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        return bookRepository.findByTags(tag).stream().map(this::convertingEntityToSourceDTO).toList();
    }

    public List<SourceBookDTO> getBooksByBookTagType() {
        List<Tag> tags = tagRepository.findByTagType(TagType.TAG_BOOK);

        Set<Book> books = tags.stream().flatMap(tag -> tag.getBooks().stream()).collect(Collectors.toSet());

        return books.stream().map(this::convertingEntityToSourceDTO).toList();
    }

    private Book convertingCreateSourceToEntity(SourceBookDTO sourceBookDTO) {
        NameBasedDTO name = sourceBookDTO.getName();
        if (StringUtils.isBlank(name.getShortName())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Акроним у книги должен быть, это является ID, в бдшке");
        }
        return Book.builder().bookDate(sourceBookDTO.getYear()).sourceAcronym(name.getShortName()).name(name.getName()).englishName(name.getEnglish()).authors(sourceBookDTO.getAuthor()).image(sourceBookDTO.getImage()).description(sourceBookDTO.getDescription()).tags(generatingTags(sourceBookDTO.getTags())).type(TypeBook.parse(sourceBookDTO.getType())).translation(convertingTranslation(sourceBookDTO.getTranslation())).build();
    }

    private Set<Tag> generatingTags(Set<String> tags) {
        return tags.stream().map(tagName -> new Tag(tagName, TagType.TAG_BOOK)).collect(Collectors.toSet());
    }

    private SourceBookDTO convertingEntityToSourceDTO(Book book) {
        if (book == null || StringUtils.isBlank(book.getSourceAcronym())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Сущность книги невалидная");
        }

        return SourceBookDTO.builder().year(book.getBookDate()).name(NameBasedDTO.builder().shortName(book.getSourceAcronym()).name(book.getName()).english(book.getEnglishName()).build()).author(new HashSet<>(book.getAuthors())).image(book.getImage()).description(book.getDescription()).tags(book.getTags().stream().map(Tag::getName).collect(Collectors.toSet())).type(book.getType().getName()).translation(convertingTranslationToDTO(book.getTranslation())).build();
    }

    private Translation convertingTranslation(TranslationDTO translationDTO) {
        if (translationDTO != null) {
            return Translation.builder().translationDate(translationDTO.getTranslationDate()).authors(translationDTO.getAuthor()).build();
        } else {
            return null;
        }
    }

    private TranslationDTO convertingTranslationToDTO(Translation translation) {
        if (translation == null) {
            return null;
        }
        if (translation.getAuthors().isEmpty() && translation.getTranslationDate() == null) {
            return null;
        } else {
            return TranslationDTO.builder().translationDate(translation.getTranslationDate()).author(new HashSet<>(translation.getAuthors())).build();
        }
    }
}
