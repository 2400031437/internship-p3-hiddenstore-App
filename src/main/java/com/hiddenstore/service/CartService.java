package com.hiddenstore.service;

import com.hiddenstore.dto.CartLine;
import com.hiddenstore.model.Product;
import com.hiddenstore.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Shopping cart stored in the HTTP session: productId -> quantity (works for guests too). */
@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductRepository products;

    @SuppressWarnings("unchecked")
    public Map<Long, Integer> cart(HttpSession session) {
        Object c = session.getAttribute("cart");
        if (c == null) {
            c = new LinkedHashMap<Long, Integer>();
            session.setAttribute("cart", c);
        }
        return (Map<Long, Integer>) c;
    }

    public void add(HttpSession session, Long productId) {
        if (products.existsById(productId)) cart(session).merge(productId, 1, Integer::sum);
    }

    public void setQuantity(HttpSession session, Long productId, int qty) {
        if (qty <= 0) remove(session, productId);
        else if (cart(session).containsKey(productId)) cart(session).put(productId, qty);
    }

    public void remove(HttpSession session, Long productId) { cart(session).remove(productId); }

    public void clear(HttpSession session) { cart(session).clear(); }

    public int count(HttpSession session) { return cart(session).size(); }

    public List<CartLine> lines(HttpSession session) {
        List<CartLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> e : new ArrayList<>(cart(session).entrySet())) {
            Optional<Product> p = products.findById(e.getKey());
            if (p.isPresent()) lines.add(new CartLine(p.get(), e.getValue()));
            else cart(session).remove(e.getKey());   // product was deleted by admin
        }
        return lines;
    }

    public double total(HttpSession session) {
        return lines(session).stream().mapToDouble(CartLine::getSubtotal).sum();
    }
}
