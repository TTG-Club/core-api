package club.ttg.dnd5.domain.common.repository;

import club.ttg.dnd5.domain.common.model.Gallery;
import club.ttg.dnd5.domain.common.model.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface GalleryRepository extends JpaRepository<Gallery, Long> {
    Collection<Gallery> findAllByUrlAndType(String url, SectionType type);

    void deleteByUrlAndType(String url, SectionType sectionType);
}
