package club.ttg.dnd5.model.engine;

import club.ttg.dnd5.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter

@Entity
@Table(name = "youtube_videos")
public class YoutubeVideo {
	@Id
	@Column(columnDefinition="varchar(12)")
	private String id;
	private String name;
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	private boolean active;
	@Column(name = "`order`")
	private int order;
	@JoinColumn(updatable = false)
	private LocalDateTime created;

	public void setCreated(LocalDateTime created) {
		this.created = LocalDateTime.now();
	}

	public YoutubeVideo() {
		this.created = LocalDateTime.now();
	}
}
