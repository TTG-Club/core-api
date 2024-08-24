package club.ttg.dnd5.model.engine;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "menu")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String icon;
    private String url;
    @JoinColumn(name = "only_dev")
    private boolean onlyDev;
    @Column(name = "order_num")  // Переименовал, чтобы избежать конфликта с SQL-зарезервированным словом.
    private int order;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Menu parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> children;
    @JoinColumn(name = "on_index")
    private boolean onIndex;
    @JoinColumn(name = "index_order")
    private Integer indexOrder;
}
