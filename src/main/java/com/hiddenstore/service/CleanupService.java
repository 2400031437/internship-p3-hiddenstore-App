package com.hiddenstore.service;

import com.hiddenstore.model.Product;
import com.hiddenstore.model.UserOrder;
import com.hiddenstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Deletes records together with everything that depends on them (no foreign-key errors). */
@Service
@Transactional
@RequiredArgsConstructor
public class CleanupService {

    private final UserRepository users;
    private final UserOrderRepository orders;
    private final PaymentRepository payments;
    private final OrderItemRepository items;
    private final ProductRepository products;
    private final CategoryRepository categories;
    private final BrandRepository brands;

    public void deleteOrder(Long orderId) {
        payments.deleteByOrderId(orderId);
        orders.deleteById(orderId);
    }

    public void deleteUser(Long userId) {
        for (UserOrder o : orders.findByUserId(userId)) deleteOrder(o.getId());
        users.deleteById(userId);
    }

    public void deleteProduct(Long productId) {
        items.deleteByProductId(productId);
        products.deleteById(productId);
    }

    /** Deleting a category also deletes the products inside it. */
    public void deleteCategory(Long categoryId) {
        for (Product p : products.findByCategoryId(categoryId)) deleteProduct(p.getId());
        categories.deleteById(categoryId);
    }

    /** Deleting a brand also deletes the products of that brand. */
    public void deleteBrand(Long brandId) {
        for (Product p : products.findByBrandId(brandId)) deleteProduct(p.getId());
        brands.deleteById(brandId);
    }
}
