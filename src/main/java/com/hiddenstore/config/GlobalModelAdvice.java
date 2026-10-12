package com.hiddenstore.config;

import com.hiddenstore.service.CartService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values needed by the navbar on every page. */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final CartService cartService;

    @ModelAttribute("loggedUserName")
    public String loggedUserName(HttpSession s) { return (String) s.getAttribute("userName"); }

    @ModelAttribute("loggedAdminName")
    public String loggedAdminName(HttpSession s) { return (String) s.getAttribute("adminName"); }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession s) { return cartService.count(s); }

    @ModelAttribute("cartTotal")
    public double cartTotal(HttpSession s) { return cartService.total(s); }
}
