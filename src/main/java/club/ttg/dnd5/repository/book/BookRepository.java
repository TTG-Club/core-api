package club.ttg.dnd5.repository.book;

import club.ttg.dnd5.model.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {

    // Custom query to find books by type with case-insensitive matching
    @Query("SELECT b FROM Book b WHERE LOWER(b.type.name) = LOWER(:typeName)")
    List<Book> findByTypeIgnoreCase(String typeName);
}