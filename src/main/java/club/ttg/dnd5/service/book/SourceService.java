package club.ttg.dnd5.service.book;

import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.repository.book.BookRepository;
import club.ttg.dnd5.repository.book.SourceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SourceService {
    private final SourceRepository sourceRepository;
    private final BookRepository bookRepository;

    public List<String> getAllBookAcronym() {
        return bookRepository.findAll().stream().map(Book::getSourceAcronym).toList();
    }
}
