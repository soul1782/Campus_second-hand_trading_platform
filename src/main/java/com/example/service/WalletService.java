package com.example.service;

import com.example.entity.Wallet;
import com.example.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public Wallet getByUserId(Long userId) {
        return walletRepository.findByUserId(userId).orElse(null);
    }
}
