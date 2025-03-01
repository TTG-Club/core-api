package club.ttg.dnd5.domain.user.model.party;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(
	name = "invitation",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = {"link", "code"})
	}
)
public class Invitation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String link;

	@Column(unique = true)
	private String code;

	@Column(unique = true)
	private String uniqueIdentifier;

	private Date generationDate;

	@OneToOne
	@JoinColumn(name = "user_party_id")
	private UserParty userParty;

	private Long expirationTime;

	public boolean isExpired() {
		// Calculate expiration date by adding expirationTime milliseconds to generationDate
		Date expirationDate = new Date(generationDate.getTime() + expirationTime);

		// Check if current date is after the expiration date
		return new Date().after(expirationDate);
	}
}
