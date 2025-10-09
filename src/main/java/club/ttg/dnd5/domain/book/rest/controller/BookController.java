package club.ttg.dnd5.domain.book.rest.controller;

import club.ttg.dnd5.domain.book.rest.dto.BookRequest;
import club.ttg.dnd5.domain.book.service.BookService;

import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/v2/books")
@RequiredArgsConstructor
@Tag(name = "Книги", description = "Контроллер для управления книгами и их поиском")
public class BookController {
    private final BookService bookService;

    /**
     * Получение всех книг.
     * @return список книг
     */
    @PostMapping("/search")
    @Operation(summary = "Получить книги", description = "Возвращает список книги")
    public Collection<ShortResponse> getBooksByType() {
        return bookService.getAllBooks();
    }

    @PostMapping
    @Operation(summary = "Добавить книгу", description = "Добавление новой книги")
    public String create(BookRequest request) {
        return bookService.save(request);
    }

    @PutMapping
    @Operation(summary = "Обновить книгу", description = "Обновление книги")
    public String update(BookRequest request) {
        return bookService.update(request);
    }
}
