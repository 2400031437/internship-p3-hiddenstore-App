package com.hiddenstore.repository;

import com.hiddenstore.model.UserOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserOrderRepository extends JpaRepository<UserOrder, Long> {
    List<UserOrder> findByUserIdOrderByOrderDateDesc(Long userId);
    List<UserOrder> findByUserIdAndStatusOrderByOrderDateDesc(Long userId, String status);
    long countByUserIdAndStatus(Long userId, String status);
    List<UserOrder> findAllByOrderByOrderDateDesc();
    List<UserOrder> findByUserId(Long userId);
}
