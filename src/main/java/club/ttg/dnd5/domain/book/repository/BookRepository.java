package club.ttg.dnd5.domain.book.repository;

import club.ttg.dnd5.domain.book.model.Book;
import club.ttg.dnd5.domain.book.model.TypeBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {
    // Метод для поиска книги по sourceAcronym
    Optional<Book> findBySourceAcronym(String sourceAcronym);
    Optional<Book> findByUrl(String url);
    // Метод для поиска книг по типу
    List<Book> findByType(TypeBook type);

    // Метод для поиска книг по тегу
//    @Query("SELECT b FROM Book b JOIN b.tags t WHERE t = :tag")
//    List<Book> findByTags(@Param("tag") Tag tag);
}