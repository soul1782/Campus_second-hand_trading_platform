package com.example.service;

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
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("学号已存在");
        }

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
        user.setVerifyStatus((byte) 1);
        user.setStatus((byte) 1);
        user.setPhone(request.getPhone());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
