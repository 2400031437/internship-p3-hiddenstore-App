package com.hiddenstore.controller;

import com.hiddenstore.model.*;
import com.hiddenstore.repository.*;
import com.hiddenstore.service.CleanupService;
import com.hiddenstore.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Admin area. Login is enforced for every /admin/** URL by an interceptor (see WebConfig). */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminRepository adminRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final UserOrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final OrderItemRepository itemRepo;
    private final BCryptPasswordEncoder encoder;
    private final FileStorageService files;
    private final CleanupService cleanup;

    private String back(RedirectAttributes ra, String err, String to) {
        ra.addFlashAttribute("err", err);
        return "redirect:" + to;
    }

    // ---------- Admin register / login / logout ----------
    @GetMapping("/register")
    public String registerForm() { return "admin/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email,
                           @RequestParam String password, @RequestParam("confirm_password") String confirm,
                           RedirectAttributes ra) {
        username = username.trim(); email = email.trim();
        if (!password.equals(confirm)) return back(ra, "Passwords do not match.", "/admin/register");
        if (adminRepo.existsByUsername(username) || adminRepo.existsByEmail(email))
            return back(ra, "Admin username or email already exists.", "/admin/register");
        Admin a = new Admin();
        a.setUsername(username); a.setEmail(email); a.setPassword(encoder.encode(password));
        adminRepo.save(a);
        ra.addFlashAttribute("msg", "You have successfully registered. Please login.");
        return "redirect:/admin/login";
    }

    @GetMapping("/login")
    public String loginForm() { return "admin/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        Admin a = adminRepo.findByUsername(username.trim()).orElse(null);
        if (a == null || !encoder.matches(password, a.getPassword()))
            return back(ra, "Invalid credentials.", "/admin/login");
        session.setAttribute("adminId", a.getId());
        session.setAttribute("adminName", a.getUsername());
        ra.addFlashAttribute("msg", "Successfully logged in.");
        return "redirect:/admin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute("adminId");
        session.removeAttribute("adminName");
        ra.addFlashAttribute("msg", "Admin logged out.");
        return "redirect:/admin/login";
    }

    // ---------- Dashboard ----------
    @GetMapping({"", "/"})
    public String dashboard(Model m) {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("Products", productRepo.count());
        stats.put("Categories", categoryRepo.count());
        stats.put("Brands", brandRepo.count());
        stats.put("Users", userRepo.count());
        stats.put("Orders", orderRepo.count());
        stats.put("Payments", paymentRepo.count());
        m.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    // ---------- Products: insert / view / edit / delete ----------
    @GetMapping("/products")
    public String products(Model m) {
        Map<Product, Long> sold = new LinkedHashMap<>();
        for (Product p : productRepo.findAll()) sold.put(p, itemRepo.totalSold(p.getId()));
        m.addAttribute("soldMap", sold);
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model m) {
        m.addAttribute("product", new Product());
        m.addAttribute("categories", categoryRepo.findAll());
        m.addAttribute("brands", brandRepo.findAll());
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model m, RedirectAttributes ra) {
        Product p = productRepo.findById(id).orElse(null);
        if (p == null) return back(ra, "Product not found.", "/admin/products");
        m.addAttribute("product", p);
        m.addAttribute("categories", categoryRepo.findAll());
        m.addAttribute("brands", brandRepo.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@RequestParam(required = false) Long id,
                              @RequestParam String title, @RequestParam(defaultValue = "") String description,
                              @RequestParam(defaultValue = "") String keywords,
                              @RequestParam Long categoryId, @RequestParam Long brandId, @RequestParam double price,
                              @RequestParam(value = "image1", required = false) MultipartFile image1,
                              @RequestParam(value = "image2", required = false) MultipartFile image2,
                              @RequestParam(value = "image3", required = false) MultipartFile image3,
                              RedirectAttributes ra) {
        Product p = (id == null) ? new Product() : productRepo.findById(id).orElseThrow();
        p.setTitle(title.trim()); p.setDescription(description); p.setKeywords(keywords); p.setPrice(price);
        p.setCategory(categoryRepo.findById(categoryId).orElseThrow());
        p.setBrand(brandRepo.findById(brandId).orElseThrow());
        String f1 = files.save(image1), f2 = files.save(image2), f3 = files.save(image3);
        if (f1 != null) p.setImage1(f1);
        if (f2 != null) p.setImage2(f2);
        if (f3 != null) p.setImage3(f3);
        productRepo.save(p);
        ra.addFlashAttribute("msg", id == null ? "Successfully inserted the product." : "Product has been updated successfully.");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        cleanup.deleteProduct(id);
        ra.addFlashAttribute("msg", "Product has been deleted successfully.");
        return "redirect:/admin/products";
    }

    // ---------- Categories ----------
    @GetMapping("/categories")
    public String categories(@RequestParam(required = false) Long edit, Model m) {
        m.addAttribute("kind", "Category");
        m.addAttribute("base", "/admin/categories");
        m.addAttribute("items", categoryRepo.findAll());
        m.addAttribute("editing", edit == null ? null : categoryRepo.findById(edit).orElse(null));
        return "admin/simple-list";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) Long id, @RequestParam String title, RedirectAttributes ra) {
        Category c = (id == null) ? new Category() : categoryRepo.findById(id).orElseThrow();
        c.setTitle(title.trim());
        categoryRepo.save(c);
        ra.addFlashAttribute("msg", id == null ? "Category has been inserted successfully." : "Category has been updated successfully.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        cleanup.deleteCategory(id);
        ra.addFlashAttribute("msg", "Category deleted successfully (and the products inside it).");
        return "redirect:/admin/categories";
    }

    // ---------- Brands ----------
    @GetMapping("/brands")
    public String brands(@RequestParam(required = false) Long edit, Model m) {
        m.addAttribute("kind", "Brand");
        m.addAttribute("base", "/admin/brands");
        m.addAttribute("items", brandRepo.findAll());
        m.addAttribute("editing", edit == null ? null : brandRepo.findById(edit).orElse(null));
        return "admin/simple-list";
    }

    @PostMapping("/brands/save")
    public String saveBrand(@RequestParam(required = false) Long id, @RequestParam String title, RedirectAttributes ra) {
        Brand b = (id == null) ? new Brand() : brandRepo.findById(id).orElseThrow();
        b.setTitle(title.trim());
        brandRepo.save(b);
        ra.addFlashAttribute("msg", id == null ? "Brand has been inserted successfully." : "Brand has been updated successfully.");
        return "redirect:/admin/brands";
    }

    @PostMapping("/brands/{id}/delete")
    public String deleteBrand(@PathVariable Long id, RedirectAttributes ra) {
        cleanup.deleteBrand(id);
        ra.addFlashAttribute("msg", "Brand deleted successfully (and the products of that brand).");
        return "redirect:/admin/brands";
    }

    // ---------- Orders / Payments / Users ----------
    @GetMapping("/orders")
    public String orders(Model m) {
        m.addAttribute("orders", orderRepo.findAllByOrderByOrderDateDesc());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/delete")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes ra) {
        cleanup.deleteOrder(id);
        ra.addFlashAttribute("msg", "Order deleted successfully.");
        return "redirect:/admin/orders";
    }

    @GetMapping("/payments")
    public String payments(Model m) {
        m.addAttribute("payments", paymentRepo.findAllByOrderByPaymentDateDesc());
        return "admin/payments";
    }

    @GetMapping("/users")
    public String users(Model m) {
        m.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        cleanup.deleteUser(id);
        ra.addFlashAttribute("msg", "User deleted successfully.");
        return "redirect:/admin/users";
    }
}
