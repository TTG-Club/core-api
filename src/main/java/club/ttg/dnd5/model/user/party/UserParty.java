package club.ttg.dnd5.model.user.party;

import club.ttg.dnd5.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "user_party")
public class UserParty {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "owner_id")
	private Long ownerId;

	@Column(name = "group_name")
	private String groupName;

	@Column(name = "description")
	private String description;

	@ManyToMany
	@JoinTable(
		name = "user_membership",
		joinColumns = @JoinColumn(name = "user_party_id"),
		inverseJoinColumns = @JoinColumn(name = "user_id")
	)

	private List<User> userList = new ArrayList<>();

	@ManyToMany
	@JoinTable(
		name = "user_wait_list",
		joinColumns = @JoinColumn(name = "user_party_id"),
		inverseJoinColumns = @JoinColumn(name = "user_id")
	)
	private List<User> userWaitList = new ArrayList<>();

	@OneToOne(mappedBy = "userParty", cascade = CascadeType.REMOVE)
	private Invitation invitation;

	@Column(name = "creation_date")
	private LocalDateTime creationDate;

	@Column(name = "last_update_date")
	private LocalDateTime lastUpdateDate;
}
