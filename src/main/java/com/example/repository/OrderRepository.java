package com.example.repository;

import com.example.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerId(Long buyerId);
    List<Order> findByBuyerIdAndStatus(Long buyerId, Byte status);
    long countByBuyerIdAndStatus(Long buyerId, Byte status);

    // ========== 新增分页查询方法 ==========

    /**
     * 分页查询买家的订单（按创建时间倒序）
     */
    Page<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);

    /**
     * 分页查询卖家的订单（按创建时间倒序）
     */
    Page<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    /**
     * 分页查询买家订单并按状态筛选（按创建时间倒序）
     */
    Page<Order> findByBuyerIdAndStatusOrderByCreatedAtDesc(Long buyerId, Byte status, Pageable pageable);

    /**
     * 分页查询卖家订单并按状态筛选（按创建时间倒序）
     */
    Page<Order> findBySellerIdAndStatusOrderByCreatedAtDesc(Long sellerId, Byte status, Pageable pageable);

    /**
     * 统计卖家某状态的订单数量
     */
    long countBySellerIdAndStatus(Long sellerId, Byte status);

    /**
     * 根据订单号查询订单
     */
    Order findByOrderNo(String orderNo);
}