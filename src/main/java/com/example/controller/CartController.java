package com.example.controller;

import com.example.dto.CartItemDTO;
import com.example.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:8081")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestParam Long userId,
                                       @RequestParam Long productId,
                                       @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            cartService.addToCart(userId, productId, quantity);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "添加成功");
            result.put("count", cartService.getCartCount(userId));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<CartItemDTO>> getCartList(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.getCartList(userId));
    }

    /**
     * 更新数量
     */
    @PutMapping("/quantity")
    public ResponseEntity<?> updateQuantity(@RequestParam Long cartItemId,
                                            @RequestParam Long userId,
                                            @RequestParam Integer quantity) {
        try {
            cartService.updateQuantity(cartItemId, userId, quantity);
            return ResponseEntity.ok("更新成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 更新选中状态
     */
    @PutMapping("/selected")
    public ResponseEntity<?> updateSelected(@RequestParam Long cartItemId,
                                            @RequestParam Long userId,
                                            @RequestParam Boolean selected) {
        try {
            cartService.updateSelected(cartItemId, userId, selected);
            return ResponseEntity.ok("更新成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 全选/全不选
     */
    @PutMapping("/select-all")
    public ResponseEntity<?> selectAll(@RequestParam Long userId, @RequestParam Boolean selected) {
        try {
            cartService.selectAll(userId, selected);
            return ResponseEntity.ok("更新成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除购物车商品
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Long cartItemId, @RequestParam Long userId) {
        try {
            cartService.removeFromCart(cartItemId, userId);
            return ResponseEntity.ok("删除成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestParam Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok("清空成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 获取购物车数量
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCartCount(@RequestParam Long userId) {
        Map<String, Long> result = new HashMap<>();
        result.put("count", cartService.getCartCount(userId));
        return ResponseEntity.ok(result);
    }

    /**
     * 获取选中的商品（用于下单）
     */
    @GetMapping("/selected")
    public ResponseEntity<List<CartItemDTO>> getSelectedItems(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.getSelectedItems(userId));
    }
}