package com.example.service;

import com.example.dto.ProductListDTO;
import com.example.entity.Category;
import com.example.entity.Product;
import com.example.repository.CategoryRepository;
import com.example.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final Map<Byte, String> CONDITION_MAP = Map.of(
            (byte) 1, "全新",
            (byte) 2, "99新",
            (byte) 3, "95新",
            (byte) 4, "9新",
            (byte) 5, "85新"
    );

    private static final Map<Integer, String> ICON_MAP = Map.of(
            1, "digital",
            2, "books",
            3, "life",
            4, "transport",
            5, "beauty",
            6, "clothes",
            7, "box"
    );

    public List<ProductListDTO> listProducts(Integer categoryId, String keyword) {
        List<Product> products;
        Byte status = 1;

        if (categoryId != null && keyword != null && !keyword.isBlank()) {
            products = productRepository.findByCategoryIdAndTitleContainingAndStatusOrderByCreatedAtDesc(categoryId, keyword, status);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(categoryId, status);
        } else if (keyword != null && !keyword.isBlank()) {
            products = productRepository.findByTitleContainingAndStatusOrderByCreatedAtDesc(keyword, status);
        } else {
            products = productRepository.findByStatusOrderByCreatedAtDesc(status);
        }

        List<Category> categories = categoryRepository.findByStatusOrderBySortOrderAsc((byte) 1);
        Map<Integer, String> categoryNameMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, Category::getCategoryName));
        Map<Integer, String> categoryIconMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> ICON_MAP.getOrDefault(c.getCategoryId(), "box")));

        return products.stream().map(p -> {
            ProductListDTO dto = new ProductListDTO();
            dto.setId(p.getProductId());
            dto.setTitle(p.getTitle());
            dto.setPrice(p.getPrice());
            dto.setCondition(CONDITION_MAP.getOrDefault(p.getConditionLevel(), "未知"));
            dto.setCampus(p.getCampus());
            dto.setCategory(categoryNameMap.getOrDefault(p.getCategoryId(), ""));
            dto.setIcon(categoryIconMap.getOrDefault(p.getCategoryId(), "box"));
            return dto;
        }).collect(Collectors.toList());
    }
}
