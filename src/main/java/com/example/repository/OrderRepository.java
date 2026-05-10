package com.example.repository;

import com.example.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerId(Long buyerId);
    List<Order> findByBuyerIdAndStatus(Long buyerId, Byte status);
    long countByBuyerIdAndStatus(Long buyerId, Byte status);
}
