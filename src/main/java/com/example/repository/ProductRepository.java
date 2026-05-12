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
    // 根据卖家ID和状态查询，按创建时间倒序
    List<Product> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, Byte status);
    // 如果需要查询所有状态（包括已下架），可以再加一个：
    List<Product> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    void deleteByProductId(Long productId);
}
