package club.ttg.dnd5.repository;

import club.ttg.dnd5.model.base.Tag;
import club.ttg.dnd5.model.base.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);
    List<Tag> findByTagType(TagType tagType);
}