package com.hiddenstore.dto;

import com.hiddenstore.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CartLine {
    private final Product product;
    private final int quantity;

    public double getSubtotal() { return product.getPrice() * quantity; }
}
