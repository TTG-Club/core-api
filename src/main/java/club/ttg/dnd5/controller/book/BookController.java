package club.ttg.dnd5.controller.book;

import club.ttg.dnd5.dto.create.CreateSourceDTO;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2/")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // 1. Create Book
    @PostMapping
    public Book createBook(@RequestBody CreateSourceDTO createSourceDTO) {
        return bookService.createBook(createSourceDTO);
    }

    @GetMapping("book/search/book/type")
    public ResponseEntity<List<Book>> getBooksByType(@RequestParam String typeName) {
        List<Book> books = bookService.getBooksByType(typeName);
        return ResponseEntity.ok(books);
    }

    @GetMapping("book/acronym")
    public ResponseEntity<Book> getBookBySourceAcronym(@RequestParam String sourceAcronym) {
        Optional<Book> book = bookService.getBookBySourceAcronym(sourceAcronym);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("dictionary/book-types")
    public ResponseEntity<List<String>> getAllBookTypes() {
        List<String> bookTypes = bookService.getAllBookTypes();
        return ResponseEntity.ok(bookTypes);
    }
}