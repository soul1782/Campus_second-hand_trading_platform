package com.example.repository;

import com.example.entity.Whitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhitelistRepository extends JpaRepository<Whitelist, Long> {

    Optional<Whitelist> findByStudentIdAndRealName(String studentId, String realName);

    boolean existsByStudentId(String studentId);
}
