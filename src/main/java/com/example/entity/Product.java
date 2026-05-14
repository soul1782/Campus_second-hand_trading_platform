package com.example.entity; // 请根据你的实际包名修改

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    // 1. 基础ID与卖家信息
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // 2. 分类与状态 (注意：数据库中是 category_id, status)
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    // 状态: 0-待审核 1-在售 2-已售出 3-已下架 4-审核不通过
    @Column(name = "status", nullable = false)
    private Byte status;

    // 3. 核心信息 (标题、描述、价格)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 原价 (数据库定义中有 original_price)
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // 4. 属性与规格 (品牌、新旧程度、数量)
    @Column(name = "brand", length = 100)
    private String brand;

    // 新旧程度: 1-全新 2-几乎全新 3-轻微使用 4-明显使用 5-较旧
    @Column(name = "condition_level", nullable = false)
    private Byte conditionLevel;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // 5. 交易设置 (校区、地点、方式、议价)
    @Column(name = "campus", nullable = false, length = 100)
    private String campus;

    // 交易地点 (之前遗漏的关键字段)
    @Column(name = "trade_location", length = 255)
    private String tradeLocation;

    // 交易方式: 1-仅自提 2-仅快递 3-两者皆可
    @Column(name = "trade_method", nullable = false)
    private Byte tradeMethod;

    // 是否议价: 0-不议价 1-可议价
    @Column(name = "is_bargain", nullable = false)
    private Byte isBargain;

    // 6. 统计与时间
    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== Getter 和 Setter ====================

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Byte getConditionLevel() {
        return conditionLevel;
    }

    public void setConditionLevel(Byte conditionLevel) {
        this.conditionLevel = conditionLevel;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getTradeLocation() {
        return tradeLocation;
    }

    public void setTradeLocation(String tradeLocation) {
        this.tradeLocation = tradeLocation;
    }

    public Byte getTradeMethod() {
        return tradeMethod;
    }

    public void setTradeMethod(Byte tradeMethod) {
        this.tradeMethod = tradeMethod;
    }

    public Byte getIsBargain() {
        return isBargain;
    }

    public void setIsBargain(Byte isBargain) {
        this.isBargain = isBargain;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}