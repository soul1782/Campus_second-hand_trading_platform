package com.example.controller;

import com.example.dto.UpdatePasswordRequest;
import com.example.dto.UpdateProfileRequest;
import com.example.dto.UserProfileDTO;
import com.example.entity.User;
import com.example.entity.Wallet;
import com.example.repository.UserRepository;
import com.example.service.OrderService;
import com.example.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:8081")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        User user = userOpt.get();
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCampus(user.getCampus());
        dto.setDepartment(user.getDepartment());
        dto.setIdentityType(user.getIdentityType());
        dto.setVerifyStatus(user.getVerifyStatus());

        Wallet wallet = walletService.getByUserId(userId);
        if (wallet != null) {
            dto.setWalletBalance(wallet.getBalance());
            dto.setFrozenAmount(wallet.getFrozenAmount());
            dto.setTotalIncome(wallet.getTotalIncome());
            dto.setTotalExpenditure(wallet.getTotalExpenditure());
        }

        dto.setPendingPaymentCount(orderService.countByStatus(userId, (byte) 0));
        dto.setPendingShipCount(orderService.countByStatus(userId, (byte) 1));
        dto.setPendingReceiveCount(orderService.countByStatus(userId, (byte) 2));
        dto.setCompletedCount(orderService.countByStatus(userId, (byte) 3));

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, @RequestBody UpdateProfileRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        User user = userOpt.get();

        // 检查用户名是否被其他用户占用
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body("用户名已被占用");
            }
            user.setUsername(request.getUsername());
        }

        // 检查邮箱是否被其他用户占用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body("邮箱已被占用");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("资料更新成功");
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody UpdatePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("用户不存在");
        }

        User user = userOpt.get();

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body("旧密码错误");
        }

        // 更新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok("密码修改成功");
    }
}
