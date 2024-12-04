package club.ttg.dnd5.service.book;

import club.ttg.dnd5.dto.create.CreateSourceDTO;
import club.ttg.dnd5.exception.EntityNotFoundException;
import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.TypeBook;
import club.ttg.dnd5.repository.TagRepository;
import club.ttg.dnd5.repository.book.BookRepository;
import club.ttg.dnd5.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final TagRepository tagRepository;
    private final TagService tagService;

    // Создание новой книги
    public Book createBook(CreateSourceDTO createSourceDTO) {
        Book book = convertingCreateSourceToEntity(createSourceDTO);

        // Преобразуем строки тегов в объекты Tag
        Set<Tag> tagSet = book.getTags().stream()
                .map(Tag::getName)
                .map(tagName -> tagRepository.findByNameIgnoreCase(tagName)
                        .orElseGet(() -> tagService.createTag(tagName))) // Создаем тег, если его нет
                .collect(Collectors.toSet());

        // Устанавливаем теги для книги
        book.setTags(tagSet);

        // Сохраняем книгу
        return bookRepository.save(book);
    }

    // Получение книги по её sourceAcronym
    public Optional<Book> getBookBySourceAcronym(String sourceAcronym) {
        return bookRepository.findBySourceAcronym(sourceAcronym);
    }

    // Поиск книги по типу
    public List<Book> getBooksByType(String type) {
        TypeBook bookType = TypeBook.valueOf(type.toUpperCase());
        return bookRepository.findByType(bookType);
    }

    // Получение всех типов книг
    public List<String> getAllBookTypes() {
        return Arrays.stream(TypeBook.values())
                .map(TypeBook::getName)
                .collect(Collectors.toList());
    }

    // Добавление тегов к книге
    public Book addTagsToBook(String sourceAcronym, Set<String> tags) {
        Book book = bookRepository.findBySourceAcronym(sourceAcronym)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        Set<Tag> tagSet = tags.stream()
                .map(tagName -> tagRepository.findByNameIgnoreCase(tagName)
                        .orElseGet(() -> tagService.createTag(tagName))) // Создаем тег, если его нет
                .collect(Collectors.toSet());

        book.getTags().addAll(tagSet);
        return bookRepository.save(book);
    }

    // Получение всех книг с определённым тегом
    public List<Book> getBooksByTag(String tagName) {
        Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found"));

        return bookRepository.findByTags(tag);
    }

    private static Book convertingCreateSourceToEntity(CreateSourceDTO createSourceDTO) {
        // Извлекаем данные из DTO
        String sourceAcronym = createSourceDTO.getName().getName();  // Получаем имя как источник
        String name = createSourceDTO.getName().getName();
        String altName = createSourceDTO.getName().getAlternative();
        String englishName = createSourceDTO.getName().getEnglish();
        String description = createSourceDTO.getDescription();
        String type = createSourceDTO.getType();
        Integer year = createSourceDTO.getYear() != null ? Integer.valueOf(createSourceDTO.getYear()) : null;

        // Создаем новую книгу
        Book book = new Book(sourceAcronym);
        book.setName(name);
        book.setAltName(altName);
        book.setEnglishName(englishName);
        book.setDescription(description);
        book.setType(TypeBook.valueOf(type.toUpperCase()));  // Предполагаем, что в DTO передается имя типа в строчном регистре
        book.setYear(year);
        return book;
    }
}
