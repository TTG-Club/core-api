package club.ttg.dnd5.domain.book.service;

import club.ttg.dnd5.domain.book.rest.mapper.BookMapper;
import club.ttg.dnd5.domain.book.repository.BookRepository;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public List<ShortResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toShortResponse)
                .toList();
    }
}
