package club.ttg.dnd5.model.user;

import club.ttg.dnd5.model.base.TimestampedEntity;
import club.ttg.dnd5.model.user.party.UserParty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends TimestampedEntity implements UserDetails {
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

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		String[] userRoles = this.getRoles().stream()
				.map(Role::getName)
				.toArray(String[]::new);

		return AuthorityUtils.createAuthorityList(userRoles);
	}
}
