package club.ttg.dnd5.controller.book;

import club.ttg.dnd5.dto.book.SourceBookDTO;
import club.ttg.dnd5.service.book.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/")
@RequiredArgsConstructor
@Tag(name = "Книги", description = "Контроллер для управления книгами и их поиском")
public class BookController {

    private final BookService bookService;

    /**
     * Создание новой книги.
     *
     * @param sourceBookDTO данные новой книги
     * @return созданная книга
     */
    @PostMapping("/book")
    @Operation(summary = "Создать книгу", description = "Позволяет создать новую книгу.")
    public ResponseEntity<SourceBookDTO> createBook(@RequestBody SourceBookDTO sourceBookDTO) {
        SourceBookDTO createdBook = bookService.createBook(sourceBookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**
     * Поиск книг по типу.
     *
     * @param typeName имя типа книги
     * @return список книг соответствующего типа
     */
    @GetMapping("/book/search/type")
    @Operation(summary = "Получить книги по типу", description = "Возвращает список книг определённого типа.")
    public ResponseEntity<List<SourceBookDTO>> getBooksByType(
            @Parameter(description = "Имя типа книги для поиска", example = "Базовые") @RequestParam String typeName) {
        List<SourceBookDTO> books = bookService.getBooksByType(typeName);
        return ResponseEntity.ok(books);
    }

    /**
     * Получение книги по акрониму источника.
     *
     * @param sourceAcronym акроним источника книги
     * @return данные книги или 404, если книга не найдена
     */
    @GetMapping("/book/acronym")
    @Operation(summary = "Получить книгу по акрониму источника", description = "Возвращает книгу по указанному акрониму источника.")
    public ResponseEntity<SourceBookDTO> getBookBySourceAcronym(
            @Parameter(description = "Акроним источника книги", example = "PHB") @RequestParam String sourceAcronym) {
        return bookService.getBookBySourceAcronym(sourceAcronym)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Получение всех типов книг.
     *
     * @return список всех доступных типов книг
     */
    @GetMapping("/dictionary/book-types")
    @Operation(summary = "Получить все типы книг", description = "Возвращает список всех типов книг.")
    public ResponseEntity<List<String>> getAllBookTypes() {
        List<String> bookTypes = bookService.getAllBookTypes();
        return ResponseEntity.ok(bookTypes);
    }

    /**
     * Поиск книг по тегу.
     *
     * @param tagName имя тега
     * @return список книг с указанным тегом
     */
    @GetMapping("/book/search/tag")
    @Operation(summary = "Получить книги по тегу", description = "Возвращает список книг, связанных с указанным тегом.")
    public ResponseEntity<List<SourceBookDTO>> getBooksByTag(
            @Parameter(description = "Имя тега для поиска", example = "Официальные") @RequestParam String tagName) {
        List<SourceBookDTO> books = bookService.getBooksByTag(tagName);
        return ResponseEntity.ok(books);
    }
}