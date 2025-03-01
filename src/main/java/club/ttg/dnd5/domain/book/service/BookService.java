package club.ttg.dnd5.domain.book.service;

import club.ttg.dnd5.domain.common.rest.dto.NameDto;
import club.ttg.dnd5.dto.base.TranslationDto;
import club.ttg.dnd5.domain.book.rest.dto.BookDetailResponse;
import club.ttg.dnd5.exception.ApiException;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.domain.common.model.Tag;
import club.ttg.dnd5.domain.common.model.TagType;
import club.ttg.dnd5.domain.common.model.Translation;
import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.model.TypeBook;
import club.ttg.dnd5.domain.common.repository.TagRepository;
import club.ttg.dnd5.domain.book.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final TagRepository tagRepository;

    // Создание новой книги
    @Transactional
    public BookDetailResponse createBook(BookDetailResponse sourceBookDTO) {
        Book book = convertingCreateSourceToEntity(sourceBookDTO);
        Book savedBook = bookRepository.save(book);
        return convertingEntityToSourceDTO(savedBook);
    }


    public List<BookDetailResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(this::convertingEntityToSourceDTO).collect(Collectors.toList());
    }

    // Получение книги по её sourceAcronym
    public Optional<BookDetailResponse> getBookBySourceAcronym(String sourceAcronym) {
        return bookRepository.findBySourceAcronym(sourceAcronym).map(this::convertingEntityToSourceDTO);
    }

    // Получение книги по её url
    public Optional<BookDetailResponse> getBookByUrl(String url) {
        return bookRepository.findByUrl(url).map(this::convertingEntityToSourceDTO);
    }

    // Поиск книги по типу
    public List<BookDetailResponse> getBooksByType(String type) {
        TypeBook bookType = TypeBook.valueOf(type.toUpperCase());
        return bookRepository.findByType(bookType).stream().map(this::convertingEntityToSourceDTO).toList();
    }

    // Получение всех типов книг
    public List<String> getAllBookTypes() {
        return Arrays.stream(TypeBook.values()).map(TypeBook::getName).toList();
    }

    // Получение всех книг с определённым тегом
    public List<BookDetailResponse> getBooksByTag(String tagName) {
        Tag tag = tagRepository.findByNameIgnoreCase(tagName).orElseThrow(() -> new EntityNotFoundException("Tag not found"));

//        return bookRepository.findByTags(tag).stream().map(this::convertingEntityToSourceDTO).toList();
        return List.of();
    }


    public List<BookDetailResponse> getBooksByBookTagType() {
        List<Tag> tags = tagRepository.findByTagType(TagType.TAG_BOOK);

        Set<Book> books = Set.of();
//                tags.stream().flatMap(tag -> tag.getBooks().stream()).collect(Collectors.toSet());

        return books.stream().map(this::convertingEntityToSourceDTO).toList();
    }

    private Book convertingCreateSourceToEntity(BookDetailResponse sourceBookDto) {
        NameDto name = sourceBookDto.getName();
        if (StringUtils.isBlank(sourceBookDto.getUrl())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Отсутствует обязательное поле `url`");
        }
        if (StringUtils.isBlank(sourceBookDto.getUrl())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Должен быть указан url");
        }
        return Book.builder()
                .bookDate(LocalDate.ofYearDay(sourceBookDto.getYear(), 1))
                .name(name.getName())
                .englishName(name.getEnglish())
                .authors(sourceBookDto.getAuthor())
                .image(sourceBookDto.getImage())
                .description(sourceBookDto.getDescription())
                .type(TypeBook.parse(sourceBookDto.getType()))
                .translation(convertingTranslation(sourceBookDto.getTranslation()))
                .url(sourceBookDto.getUrl())
                .build();
    }

    private Set<Tag> generatingTags(Set<String> tags) {
        return tags.stream().map(tagName -> new Tag(tagName, TagType.TAG_BOOK)).collect(Collectors.toSet());
    }

    private BookDetailResponse convertingEntityToSourceDTO(Book book) {
        if (book == null || StringUtils.isBlank(book.getSourceAcronym())) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Сущность книги невалидная");
        }

        return BookDetailResponse.builder()
                .year(book.getBookDate().getYear())
                .name(NameDto.builder()
                        .name(book.getName())
                        .english(book.getEnglishName())
                        .build())
                .author(new HashSet<>(book.getAuthors()))
                .image(book.getImage())
                .url(book.getUrl())
                .description(book.getDescription())
                .type(book.getType().getName())
                .translation(convertingTranslationToDTO(book.getTranslation())).build();
    }

    private Translation convertingTranslation(TranslationDto translationDTO) {
        if (translationDTO != null) {
            return Translation.builder().translationDate(translationDTO.getTranslationDate()).authors(translationDTO.getAuthor()).build();
        } else {
            return null;
        }
    }

    private TranslationDto convertingTranslationToDTO(Translation translation) {
        if (translation == null) {
            return null;
        }
        if (translation.getAuthors().isEmpty() && translation.getTranslationDate() == null) {
            return null;
        } else {
            return TranslationDto.builder().translationDate(translation.getTranslationDate()).author(new HashSet<>(translation.getAuthors())).build();
        }
    }
}
