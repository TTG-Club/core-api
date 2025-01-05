package club.ttg.dnd5.repository.user;

import club.ttg.dnd5.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
	Optional<User> findByEmailOrUsername(String email, String username);

	@Query("SELECT count(u) FROM User u LEFT JOIN u.roles r WHERE r.name = :role")
	long countByRoles(@Param("role") String role);

	boolean existsByEmail(String email);
	boolean existsByUsername(String username);

}
