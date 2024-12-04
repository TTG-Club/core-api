package club.ttg.dnd5.repository.book;

import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.book.Book;
import club.ttg.dnd5.model.book.TypeBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {
    // Метод для поиска книги по sourceAcronym
    Optional<Book> findBySourceAcronym(String sourceAcronym);

    // Метод для поиска книг по типу
    List<Book> findByType(TypeBook type);

    // Метод для поиска книг по тегу
    @Query("SELECT b FROM Book b JOIN b.tags t WHERE t = :tag")
    List<Book> findByTags(@Param("tag") Tag tag);
}