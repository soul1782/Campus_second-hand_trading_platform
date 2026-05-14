package com.example.dto;
import java.math.BigDecimal;
import java.util.List;

public class ProductUpdateDTO {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer categoryId;
    private Byte conditionLevel;
    private String campus;
    private String tradeLocation;
    private Byte tradeMethod;
    private Byte isBargain;
    private List<String> imageUrls; // 前端传入的全量图片URL列表

    // ==================== Getter & Setter (IDE一键生成) ====================
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Byte getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(Byte conditionLevel) { this.conditionLevel = conditionLevel; }
    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }
    public String getTradeLocation() { return tradeLocation; }
    public void setTradeLocation(String tradeLocation) { this.tradeLocation = tradeLocation; }
    public Byte getTradeMethod() { return tradeMethod; }
    public void setTradeMethod(Byte tradeMethod) { this.tradeMethod = tradeMethod; }
    public Byte getIsBargain() { return isBargain; }
    public void setIsBargain(Byte isBargain) { this.isBargain = isBargain; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}