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
}
