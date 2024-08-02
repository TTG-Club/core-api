package club.ttg.dnd5.model.user;

import club.ttg.dnd5.model.user.party.UserParty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String username;
	private String password;
	private String email;
	private LocalDateTime createDate;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private List<Role> roles;

	@Column(name = "enabled")
	private boolean enabled;

	@ManyToMany(mappedBy = "userList", fetch = FetchType.LAZY)
	private List<UserParty> userParties;

	public User() {
		this.createDate = LocalDateTime.now();
	}
}
