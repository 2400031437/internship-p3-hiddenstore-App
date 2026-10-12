package com.hiddenstore.controller;

import com.hiddenstore.dto.CartLine;
import com.hiddenstore.model.*;
import com.hiddenstore.repository.*;
import com.hiddenstore.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Public shop: home, brands, categories, search, product details, cart, checkout. */
@Controller
@RequiredArgsConstructor
public class StoreController {

    private final ProductRepository productRepo;
    private final BrandRepository brandRepo;
    private final CategoryRepository categoryRepo;
    private final UserRepository userRepo;
    private final UserOrderRepository orderRepo;
    private final CartService cartService;

    private void sidebar(Model m) {
        m.addAttribute("brands", brandRepo.findAll());
        m.addAttribute("categories", categoryRepo.findAll());
    }

    // ---------- Home: random order, brand filter, category filter ----------
    @GetMapping("/")
    public String home(@RequestParam(name = "brand_id", required = false) Long brandId,
                       @RequestParam(name = "category_id", required = false) Long categoryId,
                       Model m) {
        List<Product> list;
        String notice = null;
        if (brandId != null) {
            list = productRepo.findByBrandId(brandId);
            if (list.isEmpty()) notice = "Sorry, this brand is not available for service right now.";
        } else if (categoryId != null) {
            list = productRepo.findByCategoryId(categoryId);
            if (list.isEmpty()) notice = "No stock for this category.";
        } else {
            list = new ArrayList<>(productRepo.findAll());
            Collections.shuffle(list);             // random order on every visit
        }
        sidebar(m);
        m.addAttribute("products", list);
        m.addAttribute("notice", notice);
        m.addAttribute("heading", "Our Products");
        m.addAttribute("showHero", brandId == null && categoryId == null);
        return "index";
    }

    // ---------- All products ----------
    @GetMapping("/products")
    public String all(Model m) {
        sidebar(m);
        m.addAttribute("products", productRepo.findAll());
        m.addAttribute("heading", "All Products");
        return "index";
    }

    // ---------- Search by keyword ----------
    @GetMapping("/search")
    public String search(@RequestParam(name = "q", defaultValue = "") String q, Model m) {
        q = q.trim();
        if (q.isEmpty()) return "redirect:/";
        List<Product> list = productRepo.search(q);
        sidebar(m);
        m.addAttribute("products", list);
        m.addAttribute("heading", "Search results for \"" + q + "\"");
        m.addAttribute("notice", list.isEmpty() ? "No results match. No products found for this keyword." : null);
        return "index";
    }

    // ---------- Product details + related products ----------
    @GetMapping("/product/{id}")
    public String details(@PathVariable Long id, Model m, RedirectAttributes ra) {
        Optional<Product> p = productRepo.findById(id);
        if (p.isEmpty()) {
            ra.addFlashAttribute("err", "Product not found.");
            return "redirect:/";
        }
        List<Product> related = new ArrayList<>(productRepo.findByCategoryId(p.get().getCategory().getId()));
        related.removeIf(r -> r.getId().equals(id));
        sidebar(m);
        m.addAttribute("product", p.get());
        m.addAttribute("related", related);
        return "product-details";
    }

    // ---------- Cart ----------
    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session, HttpServletRequest req, RedirectAttributes ra) {
        cartService.add(session, id);
        ra.addFlashAttribute("msg", "Item is added to cart.");
        String ref = req.getHeader("Referer");
        String host = req.getScheme() + "://" + req.getServerName();
        return "redirect:" + (ref != null && ref.startsWith(host) ? ref : "/");
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model m) {
        m.addAttribute("lines", cartService.lines(session));
        return "cart";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Map<String, String> params, HttpSession session, RedirectAttributes ra) {
        params.forEach((k, v) -> {
            if (k.startsWith("qty_")) {
                try {
                    cartService.setQuantity(session, Long.parseLong(k.substring(4)), Integer.parseInt(v.trim()));
                } catch (NumberFormatException ignored) { }
            }
        });
        ra.addFlashAttribute("msg", "Cart updated.");
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        cartService.remove(session, id);
        ra.addFlashAttribute("msg", "Item removed from cart.");
        return "redirect:/cart";
    }

    // ---------- Checkout: login required, then payment page ----------
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model m, RedirectAttributes ra) {
        if (cartService.count(session) == 0) {
            ra.addFlashAttribute("err", "Your cart is empty.");
            return "redirect:/cart";
        }
        if (session.getAttribute("userId") == null) {
            session.setAttribute("redirectAfterLogin", "/checkout");
            ra.addFlashAttribute("err", "Please login to continue with checkout.");
            return "redirect:/user/login";
        }
        m.addAttribute("lines", cartService.lines(session));
        return "payment";
    }

    @PostMapping("/checkout/pay-offline")
    @Transactional
    public String payOffline(HttpSession session, RedirectAttributes ra) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            session.setAttribute("redirectAfterLogin", "/checkout");
            return "redirect:/user/login";
        }
        List<CartLine> lines = cartService.lines(session);
        if (lines.isEmpty()) return "redirect:/cart";

        UserOrder order = new UserOrder();
        order.setUser(userRepo.findById(userId).orElseThrow());
        order.setInvoiceNumber(ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L));
        order.setAmountDue(lines.stream().mapToDouble(CartLine::getSubtotal).sum());
        order.setTotalProducts(lines.stream().mapToInt(CartLine::getQuantity).sum());
        order.setStatus("pending");
        for (CartLine l : lines) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(l.getProduct());
            item.setQuantity(l.getQuantity());
            order.getItems().add(item);
        }
        orderRepo.save(order);
        cartService.clear(session);
        ra.addFlashAttribute("msg", "Orders are submitted successfully.");
        return "redirect:/user/profile";
    }
}
