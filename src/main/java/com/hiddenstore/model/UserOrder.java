package com.hiddenstore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** One checkout = one order (which can contain many products). status: "pending" or "complete" */
@Entity
@Table(name = "user_orders")
@Getter @Setter @NoArgsConstructor
public class UserOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User user;
    private double amountDue;
    private long invoiceNumber;
    private int totalProducts;
    private LocalDateTime orderDate = LocalDateTime.now();
    private String status = "pending";

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}
