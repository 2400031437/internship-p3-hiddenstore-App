package com.hiddenstore.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Serve uploaded images from /uploads/** */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            String location = dir.toUri().toString();
            if (!location.endsWith("/")) location += "/";
            registry.addResourceHandler("/uploads/**").addResourceLocations(location);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create upload directory", e);
        }
    }

    /** Protect user pages and admin pages: redirect to login when not logged in */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
                if (req.getSession().getAttribute("userId") == null) {
                    res.sendRedirect(req.getContextPath() + "/user/login");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/user/profile", "/user/orders", "/user/orders/**", "/user/edit", "/user/delete");

        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
                if (req.getSession().getAttribute("adminId") == null) {
                    res.sendRedirect(req.getContextPath() + "/admin/login");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/admin/**").excludePathPatterns("/admin/login", "/admin/register", "/admin/logout");
    }
}
