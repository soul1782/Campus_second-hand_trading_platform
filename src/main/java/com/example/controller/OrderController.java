package com.example.controller;

import com.example.dto.OrderCreateDTO;
import com.example.dto.OrderListDTO;
import com.example.entity.Order;
import com.example.service.TradeOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:8081")
public class OrderController {

    @Autowired
    private TradeOrderService tradeOrderService;

    // ==================== 订单操作 ====================

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderCreateDTO dto) {
        try {
            Order order = tradeOrderService.createOrder(dto);
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getOrderId());
            result.put("orderNo", order.getOrderNo());
            result.put("totalAmount", order.getTotalAmount());
            result.put("message", "订单创建成功，请支付");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 模拟支付
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable Long orderId, @RequestParam Long buyerId) {
        try {
            tradeOrderService.payOrder(orderId, buyerId);
            return ResponseEntity.ok("支付成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 卖家发货
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<?> shipOrder(@PathVariable Long orderId, @RequestParam Long sellerId) {
        try {
            tradeOrderService.shipOrder(orderId, sellerId);
            return ResponseEntity.ok("发货成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 买家确认收货
     */
    @PostMapping("/{orderId}/receive")
    public ResponseEntity<?> receiveOrder(@PathVariable Long orderId, @RequestParam Long buyerId) {
        try {
            tradeOrderService.receiveOrder(orderId, buyerId);
            return ResponseEntity.ok("确认收货成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        try {
            tradeOrderService.cancelOrder(orderId, userId);
            return ResponseEntity.ok("订单已取消");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== 退款功能 ====================

    /**
     * 申请退款
     */
    @PostMapping("/{orderId}/refund/apply")
    public ResponseEntity<?> applyRefund(@PathVariable Long orderId,
                                         @RequestParam Long userId,
                                         @RequestParam String reason) {
        try {
            tradeOrderService.applyRefund(orderId, userId, reason);
            return ResponseEntity.ok("退款申请已提交");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 取消退款申请
     */
    @PostMapping("/{orderId}/refund/cancel")
    public ResponseEntity<?> cancelRefund(@PathVariable Long orderId, @RequestParam Long userId) {
        try {
            tradeOrderService.cancelRefund(orderId, userId);
            return ResponseEntity.ok("已取消退款申请");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 处理退款（管理员接口）
     */
    @PostMapping("/{orderId}/refund/process")
    public ResponseEntity<?> processRefund(@PathVariable Long orderId,
                                           @RequestParam Long adminId,
                                           @RequestParam boolean approve) {
        try {
            tradeOrderService.processRefund(orderId, adminId, approve);
            String message = approve ? "已同意退款" : "已拒绝退款";
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==================== 订单查询 ====================

    /**
     * 买家订单列表
     */
    @GetMapping("/buyer")
    public ResponseEntity<Page<OrderListDTO>> getBuyerOrders(
            @RequestParam Long buyerId,
            @RequestParam(required = false) Byte status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderListDTO> orders = tradeOrderService.getBuyerOrders(buyerId, status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * 卖家订单列表（作为卖家收到的订单）
     */
    @GetMapping("/seller")
    public ResponseEntity<Page<OrderListDTO>> getSellerOrders(
            @RequestParam Long sellerId,
            @RequestParam(required = false) Byte status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderListDTO> orders = tradeOrderService.getSellerOrders(sellerId, status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * 订单详情
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId) {
        try {
            Order order = tradeOrderService.getOrderDetail(orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}