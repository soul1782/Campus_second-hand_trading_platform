package com.example.dto;

import java.math.BigDecimal;

public class CartItemDTO {

    private Long cartItemId;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private Integer stock;
    private Boolean selected;
    private BigDecimal totalPrice;

    // ========== 交易相关字段 ==========
    private String tradeLocation;   // 交易地点
    private Byte tradeMethod;        // 交易方式 (1-仅自提 2-仅快递 3-两者皆可)
    private Long sellerId;           // 卖家ID
    private String sellerName;       // 卖家名称
    private Byte conditionLevel;     // 新旧程度

    // ==================== Getter & Setter ====================
    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductTitle() { return productTitle; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) {
        this.price = price;
        calculateTotalPrice();
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateTotalPrice();
    }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) {
        this.selected = selected;
        calculateTotalPrice();
    }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    // ========== 交易相关 Getter/Setter ==========
    public String getTradeLocation() { return tradeLocation; }
    public void setTradeLocation(String tradeLocation) { this.tradeLocation = tradeLocation; }

    public Byte getTradeMethod() { return tradeMethod; }
    public void setTradeMethod(Byte tradeMethod) { this.tradeMethod = tradeMethod; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public Byte getConditionLevel() { return conditionLevel; }
    public void setConditionLevel(Byte conditionLevel) { this.conditionLevel = conditionLevel; }

    // 辅助方法：计算总价
    private void calculateTotalPrice() {
        if (selected != null && selected && price != null && quantity != null) {
            this.totalPrice = price.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }
}