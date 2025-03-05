package club.ttg.dnd5.domain.book.service;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.model.TypeBook;
import club.ttg.dnd5.domain.book.repository.BookRepository;
import club.ttg.dnd5.domain.book.rest.dto.BookDetailResponse;
import club.ttg.dnd5.domain.book.rest.mapper.BookMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public List<BookDetailResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(bookMapper::toDetailResponse).collect(Collectors.toList());
    }

    public Book findByUrl(String  url) {
        return bookRepository.findById(url)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Книга с url %s не существует" , url)));

    }

    // Получение всех типов книг
    public List<String> getAllBookTypes() {
        return Arrays.stream(TypeBook.values()).map(TypeBook::getName).toList();
    }
}
