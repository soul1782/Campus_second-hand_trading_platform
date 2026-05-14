#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校园二手交易平台 - 按 Task 拆分成 3 个 Feature Branch
PR1: feature/student-register   (注册, Closes #9)
PR2: feature/user-login         (登录, Closes #11)
PR3: feature/password-security  (密码安全, 新建 Issue)
"""

import subprocess
import os
import sys

REPO_ROOT = os.path.dirname(os.path.abspath(__file__))
os.chdir(REPO_ROOT)

def run(cmd, check=True):
    """执行 shell 命令"""
    print(f"  > {' '.join(cmd)}")
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0 and check:
        print(f"[ERROR] {result.stderr}")
        raise RuntimeError(f"Command failed: {' '.join(cmd)}")
    return result

def write_file(path, content):
    """写入文本文件（UTF-8）"""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"  [write] {path}")

def restore_from_backup(path):
    """从 backup-main 分支恢复文件"""
    run(["git", "checkout", "backup-main", "--", path], check=False)

# ============================================================
# 0. 获取最初 commit hash
# ============================================================
print("=" * 50)
print("  校园二手平台 - Feature Branch 拆分脚本")
print("=" * 50)
print()

print("[Step 0] 查找最初 commit ...")
result = run(["git", "rev-list", "--max-parents=0", "HEAD"])
INITIAL_COMMIT = result.stdout.strip()
print(f"[OK] 最初 commit: {INITIAL_COMMIT}")

# ============================================================
# 1. 备份当前 main
# ============================================================
print("\n[Step 1] 备份当前 main 到 backup-main ...")
run(["git", "checkout", "main"])
run(["git", "branch", "-D", "backup-main"], check=False)
run(["git", "checkout", "-b", "backup-main"])
run(["git", "push", "origin", "backup-main", "--force"], check=False)
print("[OK] 备份完成: backup-main")

# ============================================================
# 2. 回退 main
# ============================================================
print(f"\n[Step 2] 回退 main 到 {INITIAL_COMMIT} ...")
run(["git", "checkout", "main"])
run(["git", "reset", "--hard", INITIAL_COMMIT])
print("[OK] main 已回退")

# ============================================================
# 裁剪后的混合文件内容
# ============================================================

AUTH_CONTROLLER_PR1 = '''package com.example.controller;

import com.example.dto.RegisterRequest;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8081")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(request);
            return ResponseEntity.ok("注册成功，请等待认证");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
'''

USER_SERVICE_PR1 = '''package com.example.service;

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
'''

ROUTER_PR1 = '''import { createRouter, createWebHistory } from 'vue-router'
import Register from '../views/Register.vue'

const routes = [
  {
    path: '/register',
    name: 'Register',
    component: Register
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
'''

AUTH_CONTROLLER_PR2 = '''package com.example.controller;

import com.example.dto.AuthResponse;
import com.example.dto.LoginRequest;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8081")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
'''

USER_SERVICE_PR2 = '''package com.example.service;

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
'''

ROUTER_PR2 = '''import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'

const HomePlaceholder = { template: '<div style="padding:50px;text-align:center">Home</div>' }

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    name: 'Home',
    component: HomePlaceholder,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
'''

ROUTER_PR3 = '''import { createRouter, createWebHistory } from 'vue-router'
import My from '../views/My.vue'
import Settings from '../views/Settings.vue'

const LoginPlaceholder = { template: '<div>Login Placeholder</div>' }
const HomePlaceholder = { template: '<div>Home Placeholder</div>' }

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginPlaceholder
  },
  {
    path: '/',
    name: 'Home',
    component: HomePlaceholder,
    meta: { requiresAuth: true }
  },
  {
    path: '/my',
    name: 'My',
    component: My,
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: Settings,
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
'''

# ============================================================
# 3. 创建 PR1: feature/student-register
# ============================================================
print("\n[Step 3] 创建 feature/student-register ...")
run(["git", "branch", "-D", "feature/student-register"], check=False)
run(["git", "checkout", "-b", "feature/student-register"])

# 从 backup 恢复独占文件
for path in [
    "frontend/src/views/Register.vue",
    "src/main/java/com/example/dto/RegisterRequest.java",
    "src/main/java/com/example/entity/Whitelist.java",
    "src/main/java/com/example/repository/WhitelistRepository.java",
    "src/main/java/com/example/entity/User.java",
    "src/main/java/com/example/repository/UserRepository.java",
    "src/main/java/com/example/config/SecurityConfig.java",
    "src/main/java/com/example/dto/AuthResponse.java",
    "database.sql",
    "pom.xml",
    "src/main/resources/application.properties",
    "frontend/src/main.js",
    "frontend/src/App.vue",
    "frontend/package.json",
    "frontend/vue.config.js",
    "frontend/public/index.html",
    "src/main/java/com/example/Main.java",
]:
    restore_from_backup(path)

# 覆写裁剪后的混合文件
write_file("src/main/java/com/example/controller/AuthController.java", AUTH_CONTROLLER_PR1)
write_file("src/main/java/com/example/service/UserService.java", USER_SERVICE_PR1)
write_file("frontend/src/router/index.js", ROUTER_PR1)

run(["git", "add", "-A"])
run(["git", "commit", "-m", "feat: 实现学生学号注册与校园身份认证 [Copilot辅助]\n\n- 添加注册页面 Register.vue\n- 实现注册接口 /api/auth/register\n- 对接校园白名单验证学号与真实姓名合法性\n- 密码使用 BCrypt 加密存储\n- 用户名/邮箱/学号重复校验\n- 注册成功后跳转登录页\n\nCloses #9"])
run(["git", "push", "origin", "feature/student-register", "--force"], check=False)
print("[OK] feature/student-register 已推送")

# ============================================================
# 4. 创建 PR2: feature/user-login
# ============================================================
print("\n[Step 4] 创建 feature/user-login ...")
run(["git", "checkout", INITIAL_COMMIT])
run(["git", "branch", "-D", "feature/user-login"], check=False)
run(["git", "checkout", "-b", "feature/user-login"])

for path in [
    "frontend/src/views/Login.vue",
    "src/main/java/com/example/dto/LoginRequest.java",
    "src/main/java/com/example/dto/AuthResponse.java",
    "src/main/java/com/example/entity/User.java",
    "src/main/java/com/example/repository/UserRepository.java",
    "src/main/java/com/example/config/SecurityConfig.java",
    "database.sql",
    "pom.xml",
    "src/main/resources/application.properties",
    "frontend/src/main.js",
    "frontend/src/App.vue",
    "frontend/package.json",
    "frontend/vue.config.js",
    "frontend/public/index.html",
    "src/main/java/com/example/Main.java",
]:
    restore_from_backup(path)

write_file("src/main/java/com/example/controller/AuthController.java", AUTH_CONTROLLER_PR2)
write_file("src/main/java/com/example/service/UserService.java", USER_SERVICE_PR2)
write_file("frontend/src/router/index.js", ROUTER_PR2)

run(["git", "add", "-A"])
run(["git", "commit", "-m", "feat: 实现用户登录与前端路由守卫 [Copilot辅助]\n\n- 添加登录页面 Login.vue\n- 实现登录接口 /api/auth/login\n- 支持用户名或邮箱登录\n- 登录成功后返回 token 并保存到 localStorage\n- 添加前端路由守卫拦截未登录请求\n- 更新用户最后登录时间\n\nCloses #11"])
run(["git", "push", "origin", "feature/user-login", "--force"], check=False)
print("[OK] feature/user-login 已推送")

# ============================================================
# 5. 创建 PR3: feature/password-security
# ============================================================
print("\n[Step 5] 创建 feature/password-security ...")
run(["git", "checkout", INITIAL_COMMIT])
run(["git", "branch", "-D", "feature/password-security"], check=False)
run(["git", "checkout", "-b", "feature/password-security"])

for path in [
    "frontend/src/views/Settings.vue",
    "frontend/src/views/My.vue",
    "src/main/java/com/example/controller/UserController.java",
    "src/main/java/com/example/dto/UpdatePasswordRequest.java",
    "src/main/java/com/example/dto/UpdateProfileRequest.java",
    "src/main/java/com/example/dto/UserProfileDTO.java",
    "src/main/java/com/example/entity/Wallet.java",
    "src/main/java/com/example/repository/WalletRepository.java",
    "src/main/java/com/example/service/WalletService.java",
    "src/main/java/com/example/service/OrderService.java",
    "src/main/java/com/example/repository/OrderRepository.java",
    "src/main/java/com/example/entity/Order.java",
    "src/main/java/com/example/entity/User.java",
    "src/main/java/com/example/repository/UserRepository.java",
    "src/main/java/com/example/config/SecurityConfig.java",
    "database.sql",
    "pom.xml",
    "src/main/resources/application.properties",
    "frontend/src/main.js",
    "frontend/src/App.vue",
    "frontend/package.json",
    "frontend/vue.config.js",
    "frontend/public/index.html",
    "src/main/java/com/example/Main.java",
]:
    restore_from_backup(path)

write_file("frontend/src/router/index.js", ROUTER_PR3)

run(["git", "add", "-A"])
run(["git", "commit", "-m", "feat: 实现账号设置与密码安全功能 [Copilot辅助]\n\n- 添加账号设置页 Settings.vue（修改密码 + 修改资料）\n- 添加我的页面 My.vue（个人信息、钱包、订单统计）\n- 实现密码修改接口 /api/user/{id}/password\n- 旧密码校验、新密码一致性校验\n- 实现资料更新接口 /api/user/{id}/profile\n- 前端密码长度校验、用户名/邮箱修改防重复校验\n\nRelates password-security"])
run(["git", "push", "origin", "feature/password-security", "--force"], check=False)
print("[OK] feature/password-security 已推送")

# ============================================================
# 6. 收尾
# ============================================================
print("\n[Step 6] 切回 main ...")
run(["git", "checkout", "main"])

print("\n" + "=" * 50)
print("  脚本执行完成!")
print("=" * 50)
print()
print("已创建的远程分支:")
print("  1. feature/student-register  (PR1 - 注册, Closes #9)")
print("  2. feature/user-login        (PR2 - 登录, Closes #11)")
print("  3. feature/password-security (PR3 - 密码安全, 新建 Issue)")
print("  4. backup-main              (原始 main 备份)")
print()
print("下一步操作:")
print('  1. git push origin main --force   (推送回退后的 main)')
print('  2. 去 GitHub 依次为 3 个分支创建 PR (按 PR1->PR2->PR3 顺序合并)')
print()
