package com.hiddenstore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;      // BCrypt hash, never plain text
    private String image;
    private String address;
    private String mobile;

    public String getImageUrl() {
        return (image == null || image.isBlank()) ? "/images/placeholder.svg" : "/uploads/" + image;
    }
}
