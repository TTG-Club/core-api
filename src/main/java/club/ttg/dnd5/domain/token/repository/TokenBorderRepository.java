package club.ttg.dnd5.domain.token.repository;

import club.ttg.dnd5.domain.token.model.TokenBorder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TokenBorderRepository extends JpaRepository<TokenBorder, UUID>
{
    @Query(value = "select coalesce(max(tb.order_index), 0) from token_border tb", nativeQuery = true)
    int findMaxOrder();

    @Modifying
    @Query(value = "update token_border set order_index = :order where id = :id", nativeQuery = true)
    void updateOrder(@Param("id") UUID id, @Param("order") int order);

    @Modifying
    @Query(value = """
        update token_border
           set order_index = order_index - 1
         where order_index > :deletedOrder
        """, nativeQuery = true)
    void shiftAfterDelete(@Param("deletedOrder") int deletedOrder);

    @Query(value = "select 1 where pg_advisory_xact_lock(734562341) is not null", nativeQuery = true)
    Long lockTokenBorderReorder();

    /**
     * Переместить запись в "буфер" (отрицательные значения),
     * чтобы избежать коллизий unique индекса при массовых shift-апдейтах.
     */
    @Modifying
    @Query(value = "update token_border set order_index = :bufferOrder where id = :id", nativeQuery = true)
    void moveToBuffer(@Param("id") UUID id, @Param("bufferOrder") int bufferOrder);

    @Modifying
    @Query(value = """
    update token_border
       set order_index = -(order_index)
     where order_index >= :from
       and order_index <= :to
    """, nativeQuery = true)
    void moveRangeToNegative(@Param("from") int from, @Param("to") int to);

    @Modifying
    @Query(value = """
    update token_border
       set order_index = -order_index + 1
     where order_index <= -:from
       and order_index >= -:to
    """, nativeQuery = true)
    void restoreRangeShiftUp(@Param("from") int from, @Param("to") int to);

    @Modifying
    @Query(value = """
    update token_border
       set order_index = -order_index - 1
     where order_index <= -:from
       and order_index >= -:to
    """, nativeQuery = true)
    void restoreRangeShiftDown(@Param("from") int from, @Param("to") int to);

}
