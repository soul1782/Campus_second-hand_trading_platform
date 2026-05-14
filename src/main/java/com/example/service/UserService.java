package com.example.service;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.entity.User;
import com.example.repository.UserRepository;
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
    private PasswordEncoder passwordEncoder;

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

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = "dummy_token_" + user.getUserId();

        return new AuthResponse(token, user.getUserId(), user.getUsername(), user.getEmail());
    }
}
