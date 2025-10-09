package club.ttg.dnd5.domain.user.model;

import club.ttg.dnd5.domain.user.model.party.UserParty;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails {
	@Id
	@UuidGenerator
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID uuid;

	@Column(unique = true, nullable = false)
	private String username;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_uuid"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private List<Role> roles;

	private boolean enabled;

	@ManyToMany(mappedBy = "userList", fetch = FetchType.LAZY)
	private List<UserParty> userParties;

	@Column(name = "created_at", updatable = false)
	@CreationTimestamp(source = SourceType.DB)
	private Instant createdAt;
	@Column(name = "updated_at")
	@UpdateTimestamp(source = SourceType.DB)
	private Instant updatedAt;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private UserPreferences preferences;

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		String[] userRoles = this.getRoles().stream()
				.map(Role::getName)
				.toArray(String[]::new);

		return AuthorityUtils.createAuthorityList(userRoles);
	}

}
