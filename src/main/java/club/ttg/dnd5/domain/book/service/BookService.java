package club.ttg.dnd5.domain.book.service;

import club.ttg.dnd5.domain.book.rest.mapper.BookMapper;
import club.ttg.dnd5.domain.book.rest.dto.BookDetailResponse;
import club.ttg.dnd5.domain.book.model.TypeBook;
import club.ttg.dnd5.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public List<BookDetailResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(bookMapper::toDetailResponse).collect(Collectors.toList());
    }

    // Получение всех типов книг
    public List<String> getAllBookTypes() {
        return Arrays.stream(TypeBook.values()).map(TypeBook::getName).toList();
    }
}
