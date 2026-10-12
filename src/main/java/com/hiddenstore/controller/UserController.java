package com.hiddenstore.controller;

import com.hiddenstore.model.*;
import com.hiddenstore.repository.*;
import com.hiddenstore.service.CartService;
import com.hiddenstore.service.CleanupService;
import com.hiddenstore.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/** User registration, login, profile, orders, payment confirmation, edit/delete account. */
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepo;
    private final UserOrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final BCryptPasswordEncoder encoder;
    private final FileStorageService files;
    private final CartService cartService;
    private final CleanupService cleanup;

    private Long uid(HttpSession s) { return (Long) s.getAttribute("userId"); }

    // ---------- Register ----------
    @GetMapping("/register")
    public String registerForm() { return "user/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email,
                           @RequestParam String password, @RequestParam("confirm_password") String confirm,
                           @RequestParam(defaultValue = "") String address, @RequestParam(defaultValue = "") String mobile,
                           @RequestParam(value = "image", required = false) MultipartFile image,
                           HttpSession session, RedirectAttributes ra) {
        username = username.trim(); email = email.trim();
        if (!password.equals(confirm)) return back(ra, "Passwords do not match.", "/user/register");
        if (password.length() < 4) return back(ra, "Password must be at least 4 characters.", "/user/register");
        if (userRepo.existsByUsername(username) || userRepo.existsByEmail(email))
            return back(ra, "Username or email already exists.", "/user/register");

        User u = new User();
        u.setUsername(username); u.setEmail(email);
        u.setPassword(encoder.encode(password));            // hashed, not stored as text
        u.setAddress(address); u.setMobile(mobile);
        u.setImage(files.save(image));
        userRepo.save(u);

        if (cartService.count(session) > 0) {
            session.setAttribute("redirectAfterLogin", "/checkout");
            ra.addFlashAttribute("msg", "Registration successful. You have some items in cart - please login to continue.");
        } else {
            ra.addFlashAttribute("msg", "Registration successful. Please login.");
        }
        return "redirect:/user/login";
    }

    private String back(RedirectAttributes ra, String err, String to) {
        ra.addFlashAttribute("err", err);
        return "redirect:" + to;
    }

    // ---------- Login / Logout ----------
    @GetMapping("/login")
    public String loginForm() { return "user/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        String name = username.trim();
        User u = userRepo.findByUsername(name).or(() -> userRepo.findByEmail(name)).orElse(null);
        if (u == null || !encoder.matches(password, u.getPassword()))
            return back(ra, "Invalid credentials.", "/user/login");

        session.setAttribute("userId", u.getId());
        session.setAttribute("userName", u.getUsername());
        ra.addFlashAttribute("msg", "Successfully logged in.");
        String next = (String) session.getAttribute("redirectAfterLogin");
        session.removeAttribute("redirectAfterLogin");
        return "redirect:" + (next != null ? next : "/user/profile");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute("userId");
        session.removeAttribute("userName");
        ra.addFlashAttribute("msg", "You have been logged out.");
        return "redirect:/";
    }

    // ---------- Profile ----------
    @GetMapping("/profile")
    public String profile(HttpSession session, Model m) {
        Long id = uid(session);
        m.addAttribute("user", userRepo.findById(id).orElseThrow());
        m.addAttribute("pendingCount", orderRepo.countByUserIdAndStatus(id, "pending"));
        return "user/profile";
    }

    // ---------- Orders (all, or ?status=pending) ----------
    @GetMapping("/orders")
    public String orders(@RequestParam(required = false) String status, HttpSession session, Model m) {
        Long id = uid(session);
        List<UserOrder> list = "pending".equals(status)
                ? orderRepo.findByUserIdAndStatusOrderByOrderDateDesc(id, "pending")
                : orderRepo.findByUserIdOrderByOrderDateDesc(id);
        m.addAttribute("orders", list);
        m.addAttribute("onlyPending", "pending".equals(status));
        return "user/orders";
    }

    // ---------- Confirm payment ----------
    @GetMapping("/orders/{id}/confirm")
    public String confirmForm(@PathVariable Long id, HttpSession session, Model m, RedirectAttributes ra) {
        UserOrder o = orderRepo.findById(id).orElse(null);
        if (o == null || !o.getUser().getId().equals(uid(session)) || !"pending".equals(o.getStatus()))
            return back(ra, "Order not found or already paid.", "/user/orders");
        m.addAttribute("order", o);
        return "user/confirm";
    }

    @PostMapping("/orders/{id}/confirm")
    @Transactional
    public String confirm(@PathVariable Long id, @RequestParam String paymentMode,
                          HttpSession session, RedirectAttributes ra) {
        UserOrder o = orderRepo.findById(id).orElse(null);
        if (o == null || !o.getUser().getId().equals(uid(session)) || !"pending".equals(o.getStatus()))
            return back(ra, "Order not found or already paid.", "/user/orders");

        Payment p = new Payment();
        p.setOrder(o);
        p.setInvoiceNumber(o.getInvoiceNumber());
        p.setAmount(o.getAmountDue());
        p.setPaymentMode(paymentMode);
        p.setPaymentDate(LocalDateTime.now());
        paymentRepo.save(p);
        o.setStatus("complete");
        orderRepo.save(o);
        ra.addFlashAttribute("msg", "Payment successfully updated.");
        return "redirect:/user/orders";
    }

    // ---------- Edit / delete account ----------
    @GetMapping("/edit")
    public String editForm(HttpSession session, Model m) {
        m.addAttribute("user", userRepo.findById(uid(session)).orElseThrow());
        return "user/edit";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam String username, @RequestParam String email,
                       @RequestParam(defaultValue = "") String address, @RequestParam(defaultValue = "") String mobile,
                       @RequestParam(value = "image", required = false) MultipartFile image,
                       HttpSession session, RedirectAttributes ra) {
        User u = userRepo.findById(uid(session)).orElseThrow();
        username = username.trim(); email = email.trim();
        boolean userTaken = userRepo.findByUsername(username).filter(x -> !x.getId().equals(u.getId())).isPresent();
        boolean mailTaken = userRepo.findByEmail(email).filter(x -> !x.getId().equals(u.getId())).isPresent();
        if (userTaken || mailTaken) return back(ra, "Username or email already used by someone else.", "/user/edit");

        u.setUsername(username); u.setEmail(email); u.setAddress(address); u.setMobile(mobile);
        String img = files.save(image);
        if (img != null) u.setImage(img);
        userRepo.save(u);
        session.setAttribute("userName", u.getUsername());
        ra.addFlashAttribute("msg", "Account updated successfully.");
        return "redirect:/user/profile";
    }

    @PostMapping("/delete")
    public String delete(HttpSession session, RedirectAttributes ra) {
        cleanup.deleteUser(uid(session));
        session.removeAttribute("userId");
        session.removeAttribute("userName");
        ra.addFlashAttribute("msg", "Your account has been deleted.");
        return "redirect:/";
    }
}
