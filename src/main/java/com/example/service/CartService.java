package com.example.service;

import com.example.dto.CartItemDTO;
import com.example.entity.CartItem;
import com.example.entity.Product;
import com.example.entity.ProductImage;
import com.example.entity.User;
import com.example.repository.CartRepository;
import com.example.repository.ProductImageRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.base-url:http://localhost:8087}")
    private String baseUrl;

    /**
     * 添加商品到购物车
     */
    @Transactional
    public void addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在且在售
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架或已售出");
        }

        // 检查是否已存在
        CartItem existing = cartRepository.findByUserIdAndProductId(userId, productId).orElse(null);

        if (existing != null) {
            // 已存在，增加数量
            existing.setQuantity(existing.getQuantity() + quantity);
            existing.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(existing);
        } else {
            // 新建购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(quantity);
            cartItem.setSelected(true);
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cartItem);
        }
    }

    /**
     * 获取用户购物车列表（包含完整商品信息）
     */
    public List<CartItemDTO> getCartList(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return cartItems.stream().map(item -> {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            CartItemDTO dto = new CartItemDTO();
            dto.setCartItemId(item.getCartItemId());
            dto.setProductId(item.getProductId());
            dto.setQuantity(item.getQuantity());
            dto.setSelected(item.getSelected());

            if (product != null) {
                // 商品基本信息
                dto.setProductTitle(product.getTitle());
                dto.setPrice(product.getPrice());
                dto.setStock(product.getQuantity());

                // ✅ 交易相关字段（用于确认订单页面）
                dto.setTradeLocation(product.getTradeLocation());
                dto.setTradeMethod(product.getTradeMethod());
                dto.setConditionLevel(product.getConditionLevel());
                dto.setSellerId(product.getSellerId());

                // 获取卖家名称
                userRepository.findById(product.getSellerId()).ifPresent(seller -> {
                    dto.setSellerName(seller.getUsername());
                });

                // 获取商品封面图
                dto.setProductImage(getProductCoverImage(product.getProductId()));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取商品封面图
     */
    private String getProductCoverImage(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(productId);
        if (images != null && !images.isEmpty()) {
            String url = images.get(0).getImageUrl();
            if (url != null && url.startsWith("/")) {
                return baseUrl + url;
            }
            return url;
        }
        return "https://placehold.co/80x80/eee/999?text=No+Img";
    }

    /**
     * 更新购物车商品数量
     */
    @Transactional
    public void updateQuantity(Long cartItemId, Long userId, Integer quantity) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        if (quantity <= 0) {
            cartRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cartItem);
        }
    }

    /**
     * 更新选中状态
     */
    @Transactional
    public void updateSelected(Long cartItemId, Long userId, Boolean selected) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        cartItem.setSelected(selected);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cartItem);
    }

    /**
     * 全选/全不选
     */
    @Transactional
    public void selectAll(Long userId, Boolean selected) {
        List<CartItem> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (CartItem item : cartItems) {
            item.setSelected(selected);
            item.setUpdatedAt(LocalDateTime.now());
        }
        cartRepository.saveAll(cartItems);
    }

    /**
     * 删除购物车商品
     */
    @Transactional
    public void removeFromCart(Long cartItemId, Long userId) {
        CartItem cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (!cartItem.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        cartRepository.delete(cartItem);
    }

    /**
     * 清空购物车
     */
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    /**
     * 获取购物车商品数量
     */
    public long getCartCount(Long userId) {
        return cartRepository.countByUserId(userId);
    }

    /**
     * 获取选中的购物车商品（用于下单）
     */
    public List<CartItemDTO> getSelectedItems(Long userId) {
        List<CartItem> cartItems = cartRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return cartItems.stream()
                .filter(CartItem::getSelected)
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    CartItemDTO dto = new CartItemDTO();
                    dto.setCartItemId(item.getCartItemId());
                    dto.setProductId(item.getProductId());
                    dto.setQuantity(item.getQuantity());
                    dto.setSelected(item.getSelected());

                    if (product != null) {
                        dto.setProductTitle(product.getTitle());
                        dto.setPrice(product.getPrice());
                        dto.setStock(product.getQuantity());
                        dto.setTradeLocation(product.getTradeLocation());
                        dto.setTradeMethod(product.getTradeMethod());
                        dto.setConditionLevel(product.getConditionLevel());
                        dto.setSellerId(product.getSellerId());

                        userRepository.findById(product.getSellerId()).ifPresent(seller -> {
                            dto.setSellerName(seller.getUsername());
                        });

                        dto.setProductImage(getProductCoverImage(product.getProductId()));
                    }

                    return dto;
                }).collect(Collectors.toList());
    }
}