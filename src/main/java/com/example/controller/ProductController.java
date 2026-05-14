package com.example.controller;
import com.example.dto.ProductCreateRequest;
import com.example.dto.ProductDetailDTO;
import com.example.dto.ProductListDTO;
import com.example.dto.ProductUpdateDTO;
import com.example.entity.Product;
import com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:8081")
public class ProductController {
    @Autowired
    private ProductService productService;

    // ==========================================
    // 1. 查询操作
    // ==========================================

    /**
     * 商品列表与搜索
     */
    @GetMapping
    public ResponseEntity<Page<ProductListDTO>> listProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Byte conditionLevel,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<ProductListDTO> products = productService.searchProducts(
                categoryId, keyword, minPrice, maxPrice, conditionLevel, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * 获取我发布的商品
     */
    @GetMapping("/my")
    public ResponseEntity<List<ProductListDTO>> getMyProducts(@RequestParam Long sellerId) {
        List<ProductListDTO> products = productService.getMyProducts(sellerId);
        return ResponseEntity.ok(products);
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDTO> getProductDetail(@PathVariable Long id) {
        ProductDetailDTO detail = productService.getProductDetail(id);
        return ResponseEntity.ok(detail);
    }

    // ==========================================
    // 2. 增删改操作
    // ==========================================

    /**
     * 发布商品
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductCreateRequest request) {
        Product created = productService.createProductWithImages(request);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO dto) {
        Product updatedProduct = productService.updateProduct(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * 上下架切换 (PATCH)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id, @RequestParam Byte status) {
        productService.changeProductStatus(id, status);
        return ResponseEntity.ok().build();
    }

    /**
     * ✅ 删除商品 (DELETE) - 物理删除
     * 注意：确保 Service 层有对应的 deleteProduct 方法
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}