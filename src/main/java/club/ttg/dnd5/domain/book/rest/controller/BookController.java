package club.ttg.dnd5.domain.book.rest.controller;

import club.ttg.dnd5.domain.book.rest.dto.BookDetailResponse;
import club.ttg.dnd5.domain.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/books")
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
    @PostMapping("/create")
    @Operation(summary = "Создать книгу", description = "Позволяет создать новую книгу.")
    public ResponseEntity<BookDetailResponse> createBook(@RequestBody BookDetailResponse sourceBookDTO) {
        BookDetailResponse createdBook = bookService.createBook(sourceBookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**

     * Получение всех книг.
     *
     *
     * @return список книг
     */
    @PostMapping("/search")
    @Operation(summary = "Получить книги", description = "Возвращает список книги")
    public ResponseEntity<List<BookDetailResponse>> getBooksByType() {
        List<BookDetailResponse> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Поиск книг по типу.
     *
     * @param typeName имя типа книги
     * @return список книг соответствующего типа
     */
    @PostMapping("/search/type")
    @Operation(summary = "Получить книги по типу", description = "Возвращает список книг определённого типа.")
    public ResponseEntity<List<BookDetailResponse>> getBooksByType(
            @Parameter(description = "Имя типа книги для поиска", example = "Базовые") @RequestParam String typeName) {
        List<BookDetailResponse> books = bookService.getBooksByType(typeName);
        return ResponseEntity.ok(books);
    }

    @PostMapping("/by-book-tag-type")
    @Operation(summary = "Получить книги по типу тега", description = "Возвращает книги, связанные с тегами типа TAG_BOOK")
    public ResponseEntity<List<BookDetailResponse>> getBooksByBookTagType() {
        List<BookDetailResponse> books = bookService.getBooksByBookTagType();
        return ResponseEntity.ok(books);
    }

    /**
     * Получение книги по URL.
     *
     * @param url акроним источника книги
     * @return данные книги или 404, если книга не найдена
     */
    @GetMapping("/{url}")
    @Operation(summary = "Получить книгу по акрониму источника", description = "Возвращает книгу по указанному url.")
    public ResponseEntity<BookDetailResponse> getBookByUrl(
            @Parameter(description = "URL книги", example = "players-handbook") @PathVariable String url) {
        return bookService.getBookByUrl(url)
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
    @GetMapping("/search/by-tag")
    @Operation(summary = "Получить книги по тегу", description = "Возвращает список книг, связанных с указанным тегом.")
    public ResponseEntity<List<BookDetailResponse>> getBooksByTag(
            @Parameter(description = "Имя тега для поиска", example = "Официальные") @RequestParam String tagName) {
        List<BookDetailResponse> books = bookService.getBooksByTag(tagName);
        return ResponseEntity.ok(books);
    }
}
