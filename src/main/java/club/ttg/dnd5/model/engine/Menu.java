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
    @Column(nullable = false, unique = true)
    private String url;
    @Column(nullable = false)
    private String name;
    private String icon;
    @JoinColumn(name = "only_dev")
    private boolean onlyDev;
    @Column(name = "order_num")  // Переименовал, чтобы избежать конфликта с SQL-зарезервированным словом.
    private int order;
    @JoinColumn(name = "on_index")
    private boolean onIndex;
    @JoinColumn(name = "index_order")
    private Integer indexOrder;
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Menu parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> children;
}
