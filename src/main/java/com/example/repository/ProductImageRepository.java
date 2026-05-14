package com.example.repository;
import com.example.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    // 按单个商品查
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    // ✅ 新增：按多个商品 ID 批量查（关键！）
    List<ProductImage> findByProductIdInOrderByProductIdAscSortOrderAsc(List<Long> productIds);



    void deleteByProductId(Long productId);
}