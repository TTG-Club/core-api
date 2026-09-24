package club.ttg.dnd5.domain.bastion.repository;

import club.ttg.dnd5.domain.bastion.model.BastionFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BastionFacilityRepository extends JpaRepository<BastionFacility, String> {
    @Query(value = """
        select distinct f.source
        from bastion_facility f
        where f.source is not null
        order by f.source
        """, nativeQuery = true)
    List<String> findAllUsedSourceCodes();

    @Query(value = """
        select distinct f.srd_version
        from bastion_facility f
        where f.srd_version is not null
        order by f.srd_version
        """, nativeQuery = true)
    List<String> findDistinctSrdVersions();
}
