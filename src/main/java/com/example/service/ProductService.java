package com.example.service;

import com.example.dto.ProductCreateRequest;
import com.example.dto.ProductDetailDTO;
import com.example.dto.ProductListDTO;
import com.example.dto.ProductUpdateDTO;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.entity.ProductImage;
import com.example.entity.User;
import com.example.repository.CategoryRepository;
import com.example.repository.ProductImageRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.*;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.base-url:http://localhost:8087}")
    private String baseUrl;

    private static final Byte STATUS_ON_SALE = 1; // 在售状态

    private static final Map<Byte, String> CONDITION_MAP = Map.of(
            (byte) 1, "全新",
            (byte) 2, "99新",
            (byte) 3, "95新",
            (byte) 4, "9成新",
            (byte) 5, "8成新",
            (byte) 6, "7成新及以下" // 补全你前端定义的第6级
    );

    private static final Map<Integer, String> ICON_MAP = Map.of(
            1, "digital", 2, "books", 3, "life",
            4, "transport", 5, "beauty", 6, "clothes", 7, "box"
    );

    /**
     * 增强版搜索与筛选 (使用 Specification)
     */
    public Page<ProductListDTO> searchProducts(Integer categoryId, String keyword,
                                               BigDecimal minPrice, BigDecimal maxPrice,
                                               Byte conditionLevel,Pageable pageable) {

        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 状态：仅查询在售商品
            predicates.add(criteriaBuilder.equal(root.get("status"), STATUS_ON_SALE));

            // 2. 分类筛选
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }

            // 3. 关键词筛选 (标题模糊匹配)
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + keyword + "%"));
            }

            // 4. 价格区间筛选
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 5. 新旧程度筛选
            if (conditionLevel != null && conditionLevel >= 1 && conditionLevel <= 6) {
                predicates.add(criteriaBuilder.equal(root.get("conditionLevel"), conditionLevel));
            }

            predicates.add(criteriaBuilder.equal(root.get("status"), STATUS_ON_SALE));

            if(categoryId != null){
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }

            if(keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + keyword + "%"));
            }

            if(minPrice != null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if(maxPrice != null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if(conditionLevel != null && conditionLevel >= 1 && conditionLevel <= 6) {
                predicates.add(criteriaBuilder.equal(root.get("conditionLevel"), conditionLevel));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. 关键修改：将 Pageable 传入 findAll，直接返回 Page<Product>
        // 这样才能利用数据库的分页能力（如 LIMIT/OFFSET），而不是查出所有数据
        org.springframework.data.domain.Page<Product> productPage =
                productRepository.findAll(spec, pageable);

        // 3. 将查出的实体 Page 转换为 DTO Page
        // 注意：这里不能直接用 convertToDTOList，因为那是转 List 的
        // 我们需要构造一个新的 PageImpl
        List<ProductListDTO> dtoList = convertToDTOList(productPage.getContent());

        return new PageImpl<>(dtoList, pageable, productPage.getTotalElements());
    }

    /**
     * 获取商品详情（含图片、分类名、成色文字）
     */
    public ProductDetailDTO getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品未找到"));

        // 增加浏览量
        product.setViewCount(product.getViewCount() == null ? 1 : product.getViewCount() + 1);
        productRepository.save(product);

        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getProductId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setDescription(product.getDescription());
        dto.setCondition(CONDITION_MAP.getOrDefault(product.getConditionLevel(), "未知").trim());
        dto.setCampus(product.getCampus());
        dto.setTradeLocation(product.getTradeLocation());
        dto.setTradeMethod(product.getTradeMethod());
        dto.setIsBargain(product.getIsBargain());
        dto.setViewCount(product.getViewCount());
        dto.setSellerId(product.getSellerId());
        dto.setCreatedAt(product.getCreatedAt()); // ✅ 修复 createdAt 为 null

        // 1. ✅ 修复 category 为 null：关联查询分类表
        Category category = categoryRepository.findById(product.getCategoryId()).orElse(null);
        dto.setCategory(category != null ? category.getCategoryName() : "未知分类");

        // 2. ✅ 修复 images 为 null：关联查询图片表并补全 URL
        List<ProductImage> productImages = productImageRepository.findByProductIdOrderBySortOrderAsc(product.getProductId());
        List<String> imageUrls = productImages.stream()
                .map(ProductImage::getImageUrl)
                .map(url -> {
                    if (url != null && url.startsWith("/") && !url.contains("http")) {
                        return "http://localhost:8087" + url; // 你的后端端口
                    }
                    return url;
                })
                .collect(Collectors.toList());

        if (imageUrls.isEmpty()) {
            imageUrls.add("https://placehold.co/800x800/eee/999?text=No+Image");
        }
        dto.setImages(imageUrls);

        // 3. ✅ 修复卖家信息
        User seller = userRepository.findById(product.getSellerId()).orElse(null);
        if (seller != null) {
            dto.setSellerName(seller.getUsername());
            dto.setSellerAvatar(seller.getAvatarUrl()); // 若 DB 中该用户没传头像，此处自然为 null
        } else {
            dto.setSellerName("匿名用户");
            dto.setSellerAvatar(null);
        }

        return dto;
    }

    // 辅助方法：根据 ID 获取分类名称
    private String getCategoryName(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .map(Category::getCategoryName)
                .orElse("未知分类");
    }

    /**
     * 发布商品
     */
    @Transactional
    public Product createProductWithImages(ProductCreateRequest req) {
        // 1. 保存商品主体
        Product product = new Product();
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setCategoryId(req.getCategoryId());
        product.setConditionLevel(req.getConditionLevel());
        product.setBrand(req.getBrand());
        product.setQuantity(req.getQuantity() != null ? req.getQuantity() : 1);
        product.setCampus(req.getCampus());
        product.setTradeLocation(req.getTradeLocation());
        product.setTradeMethod(req.getTradeMethod() != null ? req.getTradeMethod() : (byte) 3);
        product.setIsBargain(req.getIsBargain() != null ? req.getIsBargain() : (byte) 1);
        product.setSellerId(req.getSellerId());
        product.setStatus(STATUS_ON_SALE); // 1-在售
        product.setViewCount(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getProductId();

        // 2. ✅ 批量插入图片记录（如果存在）
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            for (int i = 0; i < req.getImageUrls().size(); i++) {
                ProductImage img = new ProductImage();
                img.setProductId(productId);
                img.setImageUrl(req.getImageUrls().get(i));
                img.setSortOrder(i);  // 排序：首张为封面
                img.setIsCover(i == 0 ? (byte) 1 : (byte) 0);  // 首张设为封面
                img.setCreatedAt(LocalDateTime.now());
                images.add(img);
            }
            // 批量保存，效率高
            productImageRepository.saveAll(images);
        }

        return savedProduct;
    }

    /**
     * 更新商品信息
     */
    @Transactional
    public Product updateProduct(Long id, ProductUpdateDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品未找到"));

        // 更新基础字段
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setConditionLevel(dto.getConditionLevel());
        product.setCampus(dto.getCampus());
        product.setTradeLocation(dto.getTradeLocation());
        product.setTradeMethod(dto.getTradeMethod());
        product.setIsBargain(dto.getIsBargain());
        product.setUpdatedAt(LocalDateTime.now());

        // ✅ 替换图片：先删后增
        if (dto.getImageUrls() != null) {
            productImageRepository.deleteByProductId(id);
            if (!dto.getImageUrls().isEmpty()) {
                List<ProductImage> newImages = new ArrayList<>();
                for (int i = 0; i < dto.getImageUrls().size(); i++) {
                    ProductImage img = new ProductImage();
                    img.setProductId(id);
                    img.setImageUrl(dto.getImageUrls().get(i));
                    img.setSortOrder(i);
                    img.setIsCover(i == 0 ? (byte) 1 : (byte) 0);
                    img.setCreatedAt(LocalDateTime.now());
                    newImages.add(img);
                }
                productImageRepository.saveAll(newImages);
            }
        }
        return productRepository.save(product);
    }

    /**
     * 切换上下架状态
     * @param status 1-在售 3-已下架
     */
    @Transactional
    public void changeProductStatus(Long id, Byte status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品未找到"));
        if (status != 1 && status != 3) {
            throw new RuntimeException("状态参数无效");
        }
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }


    /**
     * 下架商品
     */
    @Transactional
    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品未找到"));
        product.setStatus((byte) 3); // 3-已下架
        // 修复：使用 LocalDateTime
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    /**
     * 彻底删除商品（含关联图片）
     */
    @Transactional
    public void deleteProduct(Long id) {
        // 1. 先删图片记录（避免外键约束报错，如果配置了 CASCADE 可省略，但显式删除更安全）
        productImageRepository.deleteByProductId(id);

        // 2. 再删商品主体
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品未找到"));
        productRepository.delete(product);
    }

    /**
     * 获取我发布的商品列表
     */
    public List<ProductListDTO> getMyProducts(Long sellerId) {
        // 这里我们查询所有状态的商品，方便管理下架商品
        List<Product> products = productRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
        return convertToDTOList(products);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将 Product 实体列表转换为 DTO 列表
     */
    private List<ProductListDTO> convertToDTOList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 获取所有分类信息用于映射
        List<Category> categories = categoryRepository.findByStatusOrderBySortOrderAsc((byte) 1);
        Map<Integer, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName));
        Map<Integer, String> categoryIconMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> ICON_MAP.getOrDefault(c.getCategoryId(), "box")));

        // 2. 获取所有商品的图片（优化：批量查询，避免 N+1 问题）
        List<Long> productIds = products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());

        // 查询所有相关图片
        List<ProductImage> allImages = productImageRepository
                .findByProductIdInOrderByProductIdAscSortOrderAsc(productIds);

        // 按 productId 分组，方便快速查找
        Map<Long, List<ProductImage>> imagesMap = allImages.stream()
                .collect(Collectors.groupingBy(ProductImage::getProductId));

        // 3. 组装 DTO
        return products.stream().map(p -> {
            ProductListDTO dto = new ProductListDTO();
            dto.setId(p.getProductId());
            dto.setTitle(p.getTitle());
            dto.setPrice(p.getPrice());

            // ✅ 核心修复 1：必须设置 status，否则前端无法判断上下架状态
            dto.setStatus(p.getStatus());

            dto.setCondition(CONDITION_MAP.getOrDefault(p.getConditionLevel(), "未知").trim());
            dto.setCampus(p.getCampus());

            // 处理分类名称，防止 null
            String catName = categoryNameMap.getOrDefault(p.getCategoryId(), "其他");
            dto.setCategory(catName);
            dto.setIcon(categoryIconMap.getOrDefault(p.getCategoryId(), "box"));

            dto.setTradeLocation(p.getTradeLocation());

            // ✅ 核心修复 2：处理图片并拼接 baseUrl
            List<ProductImage> productImages = imagesMap.getOrDefault(p.getProductId(), Collections.emptyList());
            List<String> imageUrls = productImages.stream()
                    .map(ProductImage::getImageUrl)
                    .map(url -> {
                        // 如果是相对路径且不为空，拼接后端地址
                        if (url != null && url.startsWith("/") && !url.contains("http")) {
                            return baseUrl + url;
                        }
                        return url;
                    })
                    .collect(Collectors.toList());

            // 如果没有图片，使用在线默认图兜底
            if (imageUrls.isEmpty()) {
                imageUrls.add("https://placehold.co/400x400/eee/999?text=No+Image");
            }
            dto.setImages(imageUrls);

            return dto;
        }).collect(Collectors.toList());
    }
}