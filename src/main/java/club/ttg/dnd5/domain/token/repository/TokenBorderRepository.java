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

    /**
     * Transaction-scoped advisory lock.
     * NB: это SELECT, поэтому НЕ @Modifying.
     */
    @Query(value = "select pg_advisory_xact_lock(734562341)", nativeQuery = true)
    void lockTokenBorderReorder();

    /**
     * Увести одну запись в отрицательный буфер (чтобы освободить слот).
     */
    @Modifying
    @Query(value = "update token_border set order_index = :bufferOrder where id = :id", nativeQuery = true)
    void moveToBuffer(@Param("id") UUID id, @Param("bufferOrder") int bufferOrder);

    /**
     * Увести диапазон в отрицательные значения: k -> -k
     */
    @Modifying
    @Query(value = """
        update token_border
           set order_index = -order_index
         where order_index >= :from
           and order_index <= :to
        """, nativeQuery = true)
    void moveRangeToNegative(@Param("from") int from, @Param("to") int to);

    /**
     * Вернуть диапазон после moveRangeToNegative с сдвигом ВВЕРХ ( +1 ):
     * -k -> k + 1
     */
    @Modifying
    @Query(value = """
        update token_border
           set order_index = -order_index + 1
         where order_index <= -:from
           and order_index >= -:to
        """, nativeQuery = true)
    void restoreRangeShiftUp(@Param("from") int from, @Param("to") int to);

    /**
     * Вернуть диапазон после moveRangeToNegative с сдвигом ВНИЗ ( -1 ):
     * -k -> k - 1
     */
    @Modifying
    @Query(value = """
        update token_border
           set order_index = -order_index - 1
         where order_index <= -:from
           and order_index >= -:to
        """, nativeQuery = true)
    void restoreRangeShiftDown(@Param("from") int from, @Param("to") int to);
}
