package club.ttg.dnd5.model.engie;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter

@Entity
@Table(name = "menu")
public class Menu {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Short id;

	@Basic(optional = false)
	@Column(nullable = false)
	private String name;
	private String icon;
	private String url;
	private boolean external;
	@JoinColumn(name = "only_dev")
	private boolean onlyDev;

	@Column(name = "order_by")
	private int order;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Menu parent;

	@OneToMany(mappedBy = "parent", orphanRemoval = false, cascade = CascadeType.REMOVE)
	private Collection<Menu> children;

	@JoinColumn(name = "on_index")
	private boolean onIndex;

	@JoinColumn(name = "index_order")
	private Integer indexOrder;
}
