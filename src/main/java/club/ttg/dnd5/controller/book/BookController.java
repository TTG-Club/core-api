package club.ttg.dnd5.controller.book;

import club.ttg.dnd5.dto.book.SourceBookDTO;
import club.ttg.dnd5.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // 1. Create Book
    @PostMapping("/book")
    public ResponseEntity<SourceBookDTO> createBook(@RequestBody SourceBookDTO sourceBookDTO) {
        SourceBookDTO createdBook = bookService.createBook(sourceBookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    // 2. Get Books by Type
    @GetMapping("/book/search/type")
    public ResponseEntity<List<SourceBookDTO>> getBooksByType(@RequestParam String typeName) {
        List<SourceBookDTO> books = bookService.getBooksByType(typeName);
        return ResponseEntity.ok(books);
    }

    // 3. Get Book by Source Acronym
    @GetMapping("/book/acronym")
    public ResponseEntity<SourceBookDTO> getBookBySourceAcronym(@RequestParam String sourceAcronym) {
        return bookService.getBookBySourceAcronym(sourceAcronym)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4. Get All Book Types
    @GetMapping("/dictionary/book-types")
    public ResponseEntity<List<String>> getAllBookTypes() {
        List<String> bookTypes = bookService.getAllBookTypes();
        return ResponseEntity.ok(bookTypes);
    }

    // 5. Get Books by Tag
    @GetMapping("/book/search/tag")
    public ResponseEntity<List<SourceBookDTO>> getBooksByTag(@RequestParam String tagName) {
        List<SourceBookDTO> books = bookService.getBooksByTag(tagName);
        return ResponseEntity.ok(books);
    }
}