package club.ttg.dnd5.domain.token.repository;

import club.ttg.dnd5.domain.token.model.TokenBorder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TokenBorderRepository extends JpaRepository<TokenBorder, UUID> {

    @Query("select coalesce(max(tb.order), 0) from TokenBorder tb")
    int findMaxOrder();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TokenBorder tb
           set tb.order = tb.order + 1
         where tb.order >= :fromInclusive
           and tb.order <= :toInclusive
        """)
    void shiftUp(int fromInclusive, int toInclusive);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TokenBorder tb
           set tb.order = tb.order - 1
         where tb.order >= :fromInclusive
           and tb.order <= :toInclusive
        """)
    void shiftDown(int fromInclusive, int toInclusive);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update TokenBorder tb
           set tb.order = tb.order - 1
         where tb.order > :deletedOrder
        """)
    void decrementOrdersAfter(int deletedOrder);
}
