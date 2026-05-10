package com.example.service;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.dto.RegisterRequest;
import com.example.entity.User;
import com.example.entity.Whitelist;
import com.example.repository.UserRepository;
import com.example.repository.WhitelistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WhitelistRepository whitelistRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(RegisterRequest request) {
        // 检查用户名、邮箱、学号是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("学号已存在");
        }

        // 检查白名单
        Optional<Whitelist> whitelistOpt = whitelistRepository.findByStudentIdAndRealName(request.getStudentId(), request.getRealName());
        if (whitelistOpt.isEmpty()) {
            throw new RuntimeException("您不在白名单中，无法注册");
        }
        Whitelist whitelist = whitelistOpt.get();
        if (!whitelist.getStatus().equals((byte) 1)) {
            throw new RuntimeException("白名单已失效，无法注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStudentId(request.getStudentId());
        user.setRealName(request.getRealName());
        user.setIdentityType(whitelist.getIdentityType().byteValue());
        user.setCampus(whitelist.getCampus());
        user.setDepartment(whitelist.getDepartment());
        user.setVerifyStatus((byte) 1); // 认证成功
        user.setStatus((byte) 1); // 正常
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public AuthResponse loginUser(LoginRequest request) {
        Optional<User> userOpt;
        if (request.getUsernameOrEmail().contains("@")) {
            userOpt = userRepository.findByEmail(request.getUsernameOrEmail());
        } else {
            userOpt = userRepository.findByUsername(request.getUsernameOrEmail());
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }

        if (!user.getStatus().equals((byte) 1)) {
            throw new RuntimeException("账户已被禁用或锁定");
        }

        if (user.getVerifyStatus() != 1) {
            throw new RuntimeException("账户未认证，请联系管理员");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 这里简化，实际应该生成JWT token
        String token = "dummy_token_" + user.getUserId(); // 占位符

        return new AuthResponse(token, user.getUserId(), user.getUsername(), user.getEmail());
    }
}
