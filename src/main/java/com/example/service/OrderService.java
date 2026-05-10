package com.example.service;

import com.example.entity.Order;
import com.example.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> listByBuyer(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId);
    }

    public long countByStatus(Long buyerId, Byte status) {
        return orderRepository.countByBuyerIdAndStatus(buyerId, status);
    }
}
