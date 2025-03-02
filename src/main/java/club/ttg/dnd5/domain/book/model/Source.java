package club.ttg.dnd5.domain.book.model;

import club.ttg.dnd5.domain.common.model.Timestamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sources",
        indexes = {@Index(name = "idx_name", columnList = "id")}
)
public class Source extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_id", referencedColumnName = "sourceAcronym")
    private Book bookInfo; //null -> userId made book by userId
    private String userId;

    public String getSourceAcronym() {
        return (bookInfo != null) ? bookInfo.getSourceAcronym() : "";
    }
}
