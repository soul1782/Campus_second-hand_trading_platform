package com.example.config;

import com.example.entity.Order;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.entity.Wallet;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import com.example.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            User testUser = userRepository.findById(1L).orElse(null);
            if (testUser == null) {
                testUser = new User();
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");
                testUser.setPasswordHash(passwordEncoder.encode("123456"));
                testUser.setStudentId("2024001");
                testUser.setRealName("张三");
                testUser.setIdentityType((byte) 1);
                testUser.setCampus("主校区");
                testUser.setDepartment("计算机学院");
                testUser.setVerifyStatus((byte) 1);
                testUser.setStatus((byte) 1);
                testUser.setCreatedAt(LocalDateTime.now());
                testUser.setUpdatedAt(LocalDateTime.now());
                testUser = userRepository.save(testUser);
            }

            Long userId = testUser.getUserId();

            if (walletRepository.findByUserId(userId).isEmpty()) {
                Wallet wallet = new Wallet();
                wallet.setUserId(userId);
                wallet.setBalance(new BigDecimal("1250.50"));
                wallet.setFrozenAmount(new BigDecimal("200.00"));
                wallet.setTotalIncome(new BigDecimal("3500.00"));
                wallet.setTotalExpenditure(new BigDecimal("2250.50"));
                wallet.setUpdatedAt(LocalDateTime.now());
                walletRepository.save(wallet);
            }

            if (orderRepository.count() == 0) {
                orderRepository.save(createOrder(userId, userId, 1L, "CT202605100001", new BigDecimal("4200"), (byte) 0));
                orderRepository.save(createOrder(userId, userId, 2L, "CT202605100002", new BigDecimal("35"), (byte) 1));
                orderRepository.save(createOrder(userId, userId, 3L, "CT202605100003", new BigDecimal("89"), (byte) 2));
                orderRepository.save(createOrder(userId, userId, 4L, "CT202605100004", new BigDecimal("850"), (byte) 3));
                orderRepository.save(createOrder(userId, userId, 5L, "CT202605100005", new BigDecimal("499"), (byte) 3));
            }

            productRepository.save(createProduct(userId, 1, "iPad Pro 2021", new BigDecimal("4200"), (byte) 2, "主校区"));
            productRepository.save(createProduct(userId, 2, "考研英语红宝书", new BigDecimal("35"), (byte) 3, "主校区"));
            productRepository.save(createProduct(userId, 3, "小米台灯 1S", new BigDecimal("89"), (byte) 2, "主校区"));
            productRepository.save(createProduct(userId, 4, "捷安特山地车", new BigDecimal("850"), (byte) 5, "主校区"));
            productRepository.save(createProduct(userId, 6, "Nike Dunk 低帮", new BigDecimal("499"), (byte) 4, "主校区"));
            productRepository.save(createProduct(userId, 1, "Switch 游戏卡", new BigDecimal("210"), (byte) 2, "主校区"));
            productRepository.save(createProduct(userId, 1, "AirPods Pro 2", new BigDecimal("998"), (byte) 3, "主校区"));
            productRepository.save(createProduct(userId, 1, "MacBook Air M1", new BigDecimal("4500"), (byte) 4, "主校区"));
            productRepository.save(createProduct(userId, 1, "佳能 G7X Mark III", new BigDecimal("3200"), (byte) 2, "主校区"));
        }
    }

    private Order createOrder(Long buyerId, Long sellerId, Long productId, String orderNo, BigDecimal totalAmount, Byte status) {
        Order o = new Order();
        o.setBuyerId(buyerId);
        o.setSellerId(sellerId);
        o.setProductId(productId);
        o.setOrderNo(orderNo);
        o.setQuantity(1);
        o.setUnitPrice(totalAmount);
        o.setTotalAmount(totalAmount);
        o.setStatus(status);
        o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(LocalDateTime.now());
        return o;
    }

    private Product createProduct(Long sellerId, Integer categoryId, String title, BigDecimal price, Byte conditionLevel, String campus) {
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setCategoryId(categoryId);
        p.setTitle(title);
        p.setDescription(title + " 描述信息");
        p.setPrice(price);
        p.setOriginalPrice(price.multiply(new BigDecimal("1.2")));
        p.setConditionLevel(conditionLevel);
        p.setQuantity(1);
        p.setCampus(campus);
        p.setTradeLocation("图书馆门口");
        p.setTradeMethod((byte) 3);
        p.setIsBargain((byte) 1);
        p.setStatus((byte) 1);
        p.setViewCount(0);
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        return p;
    }
}
