package com.hiddenstore.config;

import com.hiddenstore.model.Brand;
import com.hiddenstore.model.Category;
import com.hiddenstore.model.Product;
import com.hiddenstore.repository.BrandRepository;
import com.hiddenstore.repository.CategoryRepository;
import com.hiddenstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Inserts demo brands, categories and 8 products on first start (like the demo video). */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BrandRepository brands;
    private final CategoryRepository categories;
    private final ProductRepository products;

    @Override
    public void run(String... args) {
        if (categories.count() > 0) return;

        Brand adidas = brand("Adidas"), amazon = brand("Amazon"), zomato = brand("Zomato"), nike = brand("Nike");
        brand("Flipkart"); // no products -> "brand not available" message

        Category juices = cat("Juices"), clothes = cat("Clothes"), shoes = cat("Shoes"),
                books = cat("Books"), food = cat("Food"), plants = cat("Plants");
        cat("Milk Products"); // no products -> "no stock" message

        product("Apple Juice", "Fresh and sweet apple juice.", "apple,juice,fruit", juices, zomato, 120);
        product("Mango Juice", "Thick and tasty mango juice.", "mango,juice,fruit", juices, zomato, 150);
        product("Frock", "Beautiful cotton frock for kids.", "frock,dress,kids", clothes, adidas, 900);
        product("Jeans", "Comfortable blue denim jeans.", "jeans,denim,pant", clothes, adidas, 1500);
        product("Shoes", "Lightweight running shoes.", "shoes,running,sports", shoes, nike, 3500);
        product("Book", "A bestselling novel.", "book,novel,reading,amazon", books, amazon, 500);
        product("Biryani", "Hyderabadi chicken biryani.", "biryani,food,rice", food, zomato, 250);
        product("Plant", "Green indoor plant for your home.", "plant,indoor,green", plants, nike, 300);
    }

    private Brand brand(String t) { Brand b = new Brand(); b.setTitle(t); return brands.save(b); }
    private Category cat(String t) { Category c = new Category(); c.setTitle(t); return categories.save(c); }

    private void product(String title, String desc, String keywords, Category c, Brand b, double price) {
        Product p = new Product();
        p.setTitle(title); p.setDescription(desc); p.setKeywords(keywords);
        p.setCategory(c); p.setBrand(b); p.setPrice(price);
        products.save(p);
    }
}
