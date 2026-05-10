package com.example.repository;

import com.example.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findByStatusOrderByCreatedAtDesc(Byte status);
    List<Product> findByCategoryIdAndStatusOrderByCreatedAtDesc(Integer categoryId, Byte status);
    List<Product> findByTitleContainingAndStatusOrderByCreatedAtDesc(String title, Byte status);
    List<Product> findByCategoryIdAndTitleContainingAndStatusOrderByCreatedAtDesc(Integer categoryId, String title, Byte status);
}
