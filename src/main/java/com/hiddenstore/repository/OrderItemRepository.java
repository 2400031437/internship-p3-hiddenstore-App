package com.hiddenstore.repository;

import com.hiddenstore.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Modifying
    @Query("delete from OrderItem oi where oi.product.id = :pid")
    void deleteByProductId(@Param("pid") Long productId);

    /** Total quantity sold (only paid/complete orders) */
    @Query("select coalesce(sum(oi.quantity), 0) from OrderItem oi " +
           "where oi.product.id = :pid and oi.order.status = 'complete'")
    long totalSold(@Param("pid") Long productId);
}
