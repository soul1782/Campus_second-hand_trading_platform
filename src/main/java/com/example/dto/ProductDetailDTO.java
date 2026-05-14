package com.example.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ProductDetailDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String description;
    private String condition; // 已转换的成色文字，如 "9成新"
    private String campus;
    private String category;
    private String tradeLocation;
    private Byte tradeMethod;
    private Byte isBargain;
    private Integer viewCount;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private List<String> images;
    private LocalDateTime createdAt;



    // ==================== Getter & Setter ====================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTradeLocation() { return tradeLocation; }
    public void setTradeLocation(String tradeLocation) { this.tradeLocation = tradeLocation; }
    public Byte getTradeMethod() { return tradeMethod; }
    public void setTradeMethod(Byte tradeMethod) { this.tradeMethod = tradeMethod; }
    public Byte getIsBargain() { return isBargain; }
    public void setIsBargain(Byte isBargain) { this.isBargain = isBargain; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public String getSellerAvatar() {return sellerAvatar;}
    public void setSellerAvatar(String sellerAvatar) {this.sellerAvatar = sellerAvatar;}
    public String getSellerName() {return sellerName;}
    public void setSellerName(String sellerName) {this.sellerName = sellerName;}
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}