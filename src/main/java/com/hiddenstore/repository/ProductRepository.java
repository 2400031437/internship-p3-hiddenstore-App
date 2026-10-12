package com.hiddenstore.repository;

import com.hiddenstore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByBrandId(Long brandId);

    List<Product> findByCategoryId(Long categoryId);

    @Query("select p from Product p left join p.brand b left join p.category c where " +
           "lower(p.title) like lower(concat('%', :q, '%')) or " +
           "lower(p.keywords) like lower(concat('%', :q, '%')) or " +
           "lower(p.description) like lower(concat('%', :q, '%')) or " +
           "lower(b.title) like lower(concat('%', :q, '%')) or " +
           "lower(c.title) like lower(concat('%', :q, '%'))")
    List<Product> search(@Param("q") String q);
}
