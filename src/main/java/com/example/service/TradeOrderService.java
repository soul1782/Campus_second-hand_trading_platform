package com.example.service;

import com.example.dto.OrderCreateDTO;
import com.example.dto.OrderListDTO;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.entity.Wallet;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserRepository;
import com.example.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Set;

@Service
public class TradeOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Value("${app.base-url:http://localhost:8087}")
    private String baseUrl;

    private static final Random random = new Random();

    /**
     * 生成订单号：CT + yyyyMMdd + 6位随机数
     */
    private String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomNum = random.nextInt(1000000);
        return "CT" + dateStr + String.format("%06d", randomNum);
    }

    /**
     * 创建订单（立即购买）
     */
    @Transactional
    public Order createOrder(OrderCreateDTO dto) {
        // 1. 查询商品信息
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        // 2. 验证商品状态（必须在售）
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架或已售出，无法购买");
        }

        // 3. 验证不能购买自己的商品
        if (product.getSellerId().equals(dto.getBuyerId())) {
            throw new RuntimeException("不能购买自己发布的商品");
        }

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(dto.getBuyerId());
        order.setSellerId(product.getSellerId());
        order.setProductId(dto.getProductId());
        order.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        order.setUnitPrice(product.getPrice());
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        order.setRemark(dto.getRemark());
        order.setStatus((byte) 0); // 0-待付款
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 5. 如果有收货信息则保存
        if (dto.getReceiverName() != null) order.setReceiverName(dto.getReceiverName());
        if (dto.getReceiverPhone() != null) order.setReceiverPhone(dto.getReceiverPhone());
        if (dto.getReceiverAddress() != null) order.setReceiverAddress(dto.getReceiverAddress());

        return orderRepository.save(order);
    }

    /**
     * 模拟支付（实际项目中应调用微信支付等）
     */
    @Transactional
    public void payOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 验证订单状态
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常，无法支付");
        }

        // 验证是否是自己的订单
        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("无权操作此订单");
        }

        // 验证钱包余额
        Wallet wallet = walletRepository.findByUserId(buyerId)
                .orElseThrow(() -> new RuntimeException("钱包不存在"));

        if (wallet.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new RuntimeException("余额不足，请充值");
        }

        // 扣款（买家）
        wallet.setBalance(wallet.getBalance().subtract(order.getTotalAmount()));
        wallet.setTotalExpenditure(wallet.getTotalExpenditure().add(order.getTotalAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // 冻结金额到卖家（待收货后解冻并转入卖家账户）
        Wallet sellerWallet = walletRepository.findByUserId(order.getSellerId())
                .orElseThrow(() -> new RuntimeException("卖家钱包不存在"));
        sellerWallet.setFrozenAmount(sellerWallet.getFrozenAmount().add(order.getTotalAmount()));
        sellerWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(sellerWallet);

        // 更新订单状态
        order.setStatus((byte) 1); // 待发货
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 卖家发货
     */
    @Transactional
    public void shipOrder(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getSellerId().equals(sellerId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getStatus() != 1) {
            throw new RuntimeException("订单状态异常，无法发货");
        }

        order.setStatus((byte) 2); // 待收货
        order.setShippedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 买家确认收货
     */
    @Transactional
    public void receiveOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getStatus() != 2) {
            throw new RuntimeException("订单状态异常，无法确认收货");
        }

        // 将冻结金额转入卖家可用余额
        Wallet sellerWallet = walletRepository.findByUserId(order.getSellerId())
                .orElseThrow(() -> new RuntimeException("卖家钱包不存在"));
        sellerWallet.setFrozenAmount(sellerWallet.getFrozenAmount().subtract(order.getTotalAmount()));
        sellerWallet.setBalance(sellerWallet.getBalance().add(order.getTotalAmount()));
        sellerWallet.setTotalIncome(sellerWallet.getTotalIncome().add(order.getTotalAmount()));
        sellerWallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(sellerWallet);

        // 更新订单状态
        order.setStatus((byte) 3); // 已完成
        order.setReceivedAt(LocalDateTime.now());
        order.setCompletedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 更新商品状态为已售出
        Product product = productRepository.findById(order.getProductId()).orElse(null);
        if (product != null && product.getStatus() == 1) {
            product.setStatus((byte) 2); // 2-已售出
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }
    }

    /**
     * 取消订单（仅待付款状态可取消）
     */
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态异常，无法取消");
        }

        order.setStatus((byte) 4); // 已取消
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    // ==================== 退款功能 ====================

    /**
     * 申请退款（支持多种状态）
     * 退款方式：
     * - 待付款(0)：直接取消，无需审核
     * - 待发货(1)：申请退款，进入退款中状态（需审核）
     * - 待收货(2)：申请退款，进入退款中状态（需审核）
     * - 已完成(3)：申请退款，进入退款中状态（需审核）
     */
    @Transactional
    public void applyRefund(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("只有买家可以申请退款");
        }

        // 可退款的状态列表：待付款(0)、待发货(1)、待收货(2)、已完成(3)
        Set<Byte> refundableStatuses = Set.of((byte)0, (byte)1, (byte)2, (byte)3);

        if (!refundableStatuses.contains(order.getStatus())) {
            throw new RuntimeException("当前订单状态不支持退款");
        }

        // 检查是否已申请过退款
        if (order.getStatus() == 5) {
            throw new RuntimeException("退款申请已提交，请等待处理");
        }

        // 如果订单是待付款状态，直接取消订单（无需审核）
        if (order.getStatus() == 0) {
            order.setStatus((byte) 4); // 直接取消
            order.setCancelledAt(LocalDateTime.now());
            order.setRemark("用户申请取消订单：" + reason);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            return;
        }

        // 其他状态（待发货、待收货、已完成）需要审核
        order.setStatus((byte) 5); // 退款中
        order.setRemark("退款原因：" + reason);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 处理退款（卖家确认/平台处理）
     * 同意退款：退款给买家，恢复商品状态
     * 拒绝退款：恢复到原订单状态
     */
    @Transactional
    public void processRefund(Long orderId, Long adminId, boolean approve) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (order.getStatus() != 5) {
            throw new RuntimeException("该订单不在退款流程中");
        }

        if (approve) {
            // ========== 同意退款 ==========
            Wallet buyerWallet = walletRepository.findByUserId(order.getBuyerId())
                    .orElseThrow(() -> new RuntimeException("买家钱包不存在"));
            Wallet sellerWallet = walletRepository.findByUserId(order.getSellerId())
                    .orElseThrow(() -> new RuntimeException("卖家钱包不存在"));

            boolean isPaid = order.getPaidAt() != null;

            if (isPaid) {
                // 已支付的订单，需要退款
                if (order.getReceivedAt() != null) {
                    // 已完成订单，钱在卖家账户
                    if (sellerWallet.getBalance().compareTo(order.getTotalAmount()) < 0) {
                        throw new RuntimeException("卖家余额不足，无法退款");
                    }
                    sellerWallet.setBalance(sellerWallet.getBalance().subtract(order.getTotalAmount()));
                    sellerWallet.setTotalExpenditure(sellerWallet.getTotalExpenditure().add(order.getTotalAmount()));
                    buyerWallet.setBalance(buyerWallet.getBalance().add(order.getTotalAmount()));
                    buyerWallet.setTotalIncome(buyerWallet.getTotalIncome().add(order.getTotalAmount()));
                } else if (order.getShippedAt() != null || order.getPaidAt() != null) {
                    // 已发货或已支付未发货，钱在卖家冻结账户
                    sellerWallet.setFrozenAmount(sellerWallet.getFrozenAmount().subtract(order.getTotalAmount()));
                    buyerWallet.setBalance(buyerWallet.getBalance().add(order.getTotalAmount()));
                    buyerWallet.setTotalIncome(buyerWallet.getTotalIncome().add(order.getTotalAmount()));
                }
            }

            sellerWallet.setUpdatedAt(LocalDateTime.now());
            buyerWallet.setUpdatedAt(LocalDateTime.now());
            walletRepository.save(sellerWallet);
            walletRepository.save(buyerWallet);

            order.setStatus((byte) 4); // 已取消
            order.setCancelledAt(LocalDateTime.now());

            // 恢复商品状态为在售
            Product product = productRepository.findById(order.getProductId()).orElse(null);
            if (product != null && product.getStatus() == 2) {
                product.setStatus((byte) 1);
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
            }

            String currentRemark = order.getRemark() != null ? order.getRemark() : "";
            order.setRemark(currentRemark + "；退款申请已同意");
        } else {
            // ========== 拒绝退款 - 恢复到原始状态 ==========
            Byte originalStatus;
            if (order.getReceivedAt() != null) {
                originalStatus = 3; // 已完成
            } else if (order.getShippedAt() != null) {
                originalStatus = 2; // 待收货
            } else if (order.getPaidAt() != null) {
                originalStatus = 1; // 待发货
            } else {
                originalStatus = 0; // 待付款
            }

            order.setStatus(originalStatus);
            String currentRemark = order.getRemark() != null ? order.getRemark() : "";
            order.setRemark(currentRemark + "；退款申请被驳回");
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    /**
     * 取消退款申请（买家主动取消）
     */
    @Transactional
    public void cancelRefund(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        if (order.getStatus() != 5) {
            throw new RuntimeException("当前不在退款流程中");
        }

        // 恢复到原始状态
        Byte originalStatus;
        if (order.getReceivedAt() != null) {
            originalStatus = 3; // 已完成
        } else if (order.getShippedAt() != null) {
            originalStatus = 2; // 待收货
        } else if (order.getPaidAt() != null) {
            originalStatus = 1; // 待发货
        } else {
            originalStatus = 0; // 待付款
        }

        order.setStatus(originalStatus);
        String currentRemark = order.getRemark() != null ? order.getRemark() : "";
        order.setRemark(currentRemark + "；用户取消了退款申请");
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    // ==================== 查询功能 ====================

    /**
     * 获取买家订单列表（分页）
     */
    public Page<OrderListDTO> getBuyerOrders(Long buyerId, Byte status, Pageable pageable) {
        Page<Order> orderPage;
        if (status != null && status >= 0 && status <= 5) {
            orderPage = orderRepository.findByBuyerIdAndStatusOrderByCreatedAtDesc(buyerId, status, pageable);
        } else {
            orderPage = orderRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId, pageable);
        }
        return convertToDTOPage(orderPage);
    }

    /**
     * 获取卖家订单列表（分页）
     */
    public Page<OrderListDTO> getSellerOrders(Long sellerId, Byte status, Pageable pageable) {
        Page<Order> orderPage;
        if (status != null && status >= 0 && status <= 5) {
            orderPage = orderRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status, pageable);
        } else {
            orderPage = orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        }
        return convertToDTOPage(orderPage);
    }

    /**
     * 获取订单详情
     */
    public Order getOrderDetail(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    /**
     * 统计买家各状态订单数量
     */
    public long countBuyerOrdersByStatus(Long buyerId, Byte status) {
        return orderRepository.countByBuyerIdAndStatus(buyerId, status);
    }

    /**
     * 统计卖家各状态订单数量
     */
    public long countSellerOrdersByStatus(Long sellerId, Byte status) {
        return orderRepository.countBySellerIdAndStatus(sellerId, status);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 转换订单实体到DTO
     */
    private Page<OrderListDTO> convertToDTOPage(Page<Order> orderPage) {
        return orderPage.map(order -> {
            OrderListDTO dto = new OrderListDTO();
            dto.setOrderId(order.getOrderId());
            dto.setOrderNo(order.getOrderNo());
            dto.setProductId(order.getProductId());
            dto.setQuantity(order.getQuantity());
            dto.setUnitPrice(order.getUnitPrice());
            dto.setTotalAmount(order.getTotalAmount());
            dto.setStatus(order.getStatus());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setPaidAt(order.getPaidAt());
            dto.setShippedAt(order.getShippedAt());
            dto.setReceivedAt(order.getReceivedAt());
            dto.setCompletedAt(order.getCompletedAt());

            // 获取商品信息
            productRepository.findById(order.getProductId()).ifPresent(product -> {
                dto.setProductTitle(product.getTitle());
                dto.setProductImage("https://placehold.co/80x80/eee/999?text=Img");
            });

            // 获取卖家信息
            userRepository.findById(order.getSellerId()).ifPresent(seller -> {
                dto.setSellerId(seller.getUserId());
                dto.setSellerName(seller.getUsername());
            });

            return dto;
        });
    }
}