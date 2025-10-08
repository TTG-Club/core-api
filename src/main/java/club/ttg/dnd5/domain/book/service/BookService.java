package club.ttg.dnd5.domain.book.service;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.repository.BookRepository;
import club.ttg.dnd5.domain.book.rest.dto.BookRequest;
import club.ttg.dnd5.domain.book.rest.mapper.BookMapper;
import club.ttg.dnd5.domain.common.rest.dto.ShortResponse;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public List<ShortResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toShort)
                .toList();
    }

    public Book findByUrl(String url) {
        return bookRepository.findByUrl(url)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Книга с url %s не существует" , url)));

    }

    public Optional<Book> findByUrOptional(String url) {
        return bookRepository.findByUrl(url);
    }

    @Transactional
    public String save(final BookRequest request) {
        bookRepository.existsById(request.getAcronym());
        return bookRepository.save(bookMapper.toEntity(request)).getUrl();
    }

    @Transactional
    public String update(final BookRequest request) {
        findByUrl(request.getUrl());
        return null;
    }
}
