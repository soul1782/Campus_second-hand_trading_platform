package com.example.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductListDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String condition;
    private String campus;
    private String category;
    private String icon;

    // 新增：用于展示商品图片（列表页通常展示第一张或几张）
    private List<String> images;

    // 新增：用于展示卖家信息或交易地点
    private String tradeLocation;
    private String sellerName; // 假设需要展示卖家昵称
    private Byte status;
    // 无参构造函数
    public ProductListDTO() {}

    // 全参构造函数（可根据需要调整）
    public ProductListDTO(Long id, String title, BigDecimal price, String condition, String campus,
                          String category, String icon, List<String> images, String tradeLocation) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.condition = condition;
        this.campus = campus;
        this.category = category;
        this.icon = icon;
        this.images = images;
        this.tradeLocation = tradeLocation;
    }

    // Getter 和 Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getCampus() { return campus; }
    public void setCampus(String campus) { this.campus = campus; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getTradeLocation() { return tradeLocation; }
    public void setTradeLocation(String tradeLocation) { this.tradeLocation = tradeLocation; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public Byte getStatus() { return status; }
    public void setStatus(Byte status) { this.status = status; }
}