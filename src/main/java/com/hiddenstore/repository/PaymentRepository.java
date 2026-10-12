package com.hiddenstore.repository;

import com.hiddenstore.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByOrderByPaymentDateDesc();
    @Modifying
    @Query("delete from Payment p where p.order.id = :oid")
    void deleteByOrderId(@Param("oid") Long orderId);
}
