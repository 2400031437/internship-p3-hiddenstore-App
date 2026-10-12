package com.hiddenstore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(length = 2000)
    private String description;
    private String keywords;
    @ManyToOne(optional = false)
    private Category category;
    @ManyToOne(optional = false)
    private Brand brand;
    private String image1;
    private String image2;
    private String image3;
    private double price;
    private LocalDateTime createdAt = LocalDateTime.now();

    private static String url(String f) {
        return (f == null || f.isBlank()) ? "/images/placeholder.svg" : "/uploads/" + f;
    }
    public String getImage1Url() { return url(image1); }
    public String getImage2Url() { return url(image2); }
    public String getImage3Url() { return url(image3); }
}
