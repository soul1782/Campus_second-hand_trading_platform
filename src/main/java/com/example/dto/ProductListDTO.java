package com.example.dto;

import java.math.BigDecimal;

public class ProductListDTO {

    private Long id;
    private String title;
    private BigDecimal price;
    private String condition;
    private String campus;
    private String category;
    private String icon;

    public ProductListDTO() {
    }

    public ProductListDTO(Long id, String title, BigDecimal price, String condition, String campus, String category, String icon) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.condition = condition;
        this.campus = campus;
        this.category = category;
        this.icon = icon;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
