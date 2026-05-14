<#
.SYNOPSIS
    校园二手交易平台 - 按 Task 拆分成 3 个 Feature Branch
.DESCRIPTION
    PR1: feature/student-register   (注册, Closes #9)
    PR2: feature/user-login         (登录, Closes #11)
    PR3: feature/password-security  (密码安全, 新建 Issue)
#>

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $RepoRoot

# ============================================================
# 0. 获取最初 commit hash
# ============================================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  校园二手平台 - Feature Branch 拆分脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "最近的 git 提交历史:" -ForegroundColor Yellow
git log --oneline -20
Write-Host ""

$INITIAL_COMMIT = Read-Host "请输入 main 最初状态的 commit hash (用于回退)"
if ([string]::IsNullOrWhiteSpace($INITIAL_COMMIT)) {
    Write-Host "[ERROR] 未输入 commit hash, 脚本终止" -ForegroundColor Red
    exit 1
}

Write-Host "`n[Step 1] 备份当前 main 到 backup-main ..." -ForegroundColor Green
git checkout main
git branch -D backup-main 2>$null
git checkout -b backup-main
git push origin backup-main --force 2>$null
Write-Host "[OK] 备份完成: backup-main" -ForegroundColor Green

Write-Host "`n[Step 2] 回退 main 到 $INITIAL_COMMIT ..." -ForegroundColor Green
git checkout main
git reset --hard $INITIAL_COMMIT
Write-Host "[OK] main 已回退" -ForegroundColor Green

# ============================================================
# 3. 创建 PR1: feature/student-register
# ============================================================
Write-Host "`n[Step 3] 创建 feature/student-register ..." -ForegroundColor Green

git branch -D feature/student-register 2>$null
git checkout -b feature/student-register

git add frontend/src/views/Register.vue
git add src/main/java/com/example/dto/RegisterRequest.java
git add src/main/java/com/example/entity/Whitelist.java
git add src/main/java/com/example/repository/WhitelistRepository.java
git add src/main/java/com/example/entity/User.java
git add src/main/java/com/example/repository/UserRepository.java
git add src/main/java/com/example/config/SecurityConfig.java
git add src/main/java/com/example/dto/AuthResponse.java
git add database.sql
git add pom.xml
git add src/main/resources/application.properties
git add frontend/src/main.js
git add frontend/src/App.vue
git add frontend/package.json
git add frontend/vue.config.js
git add frontend/public/index.html
git add src/main/java/com/example/Main.java

Write-Host "  写入 AuthController.java (仅 register) ..." -ForegroundColor Gray
$authCtrl1 = @'
package com.example.controller;

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
'@
Set-Content -Path "src/main/java/com/example/controller/AuthController.java" -Value $authCtrl1 -Encoding UTF8

Write-Host "  写入 UserService.java (仅 registerUser) ..." -ForegroundColor Gray
$userSvc1 = @'
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
'@
Set-Content -Path "src/main/java/com/example/service/UserService.java" -Value $userSvc1 -Encoding UTF8

Write-Host "  写入 router/index.js (仅注册路由) ..." -ForegroundColor Gray
$router1 = @'
import { createRouter, createWebHistory } from 'vue-router'
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
'@
Set-Content -Path "frontend/src/router/index.js" -Value $router1 -Encoding UTF8

git add src/main/java/com/example/controller/AuthController.java
git add src/main/java/com/example/service/UserService.java
git add frontend/src/router/index.js

git commit -m "feat: 实现学生学号注册与校园身份认证 [Copilot辅助]

- 添加注册页面 Register.vue
- 实现注册接口 /api/auth/register
- 对接校园白名单验证学号与真实姓名合法性
- 密码使用 BCrypt 加密存储
- 用户名/邮箱/学号重复校验
- 注册成功后跳转登录页

Closes #9"

git push origin feature/student-register --force 2>$null
Write-Host "[OK] feature/student-register 已推送" -ForegroundColor Green

# ============================================================
# 4. 创建 PR2: feature/user-login
# ============================================================
Write-Host "`n[Step 4] 创建 feature/user-login ..." -ForegroundColor Green

git checkout $INITIAL_COMMIT --detach
git branch -D feature/user-login 2>$null
git checkout -b feature/user-login

git add frontend/src/views/Login.vue
git add src/main/java/com/example/dto/LoginRequest.java
git add src/main/java/com/example/dto/AuthResponse.java
git add src/main/java/com/example/entity/User.java
git add src/main/java/com/example/repository/UserRepository.java
git add src/main/java/com/example/config/SecurityConfig.java
git add database.sql
git add pom.xml
git add src/main/resources/application.properties
git add frontend/src/main.js
git add frontend/src/App.vue
git add frontend/package.json
git add frontend/vue.config.js
git add frontend/public/index.html
git add src/main/java/com/example/Main.java

Write-Host "  写入 AuthController.java (仅 login) ..." -ForegroundColor Gray
$authCtrl2 = @'
package com.example.controller;

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
'@
Set-Content -Path "src/main/java/com/example/controller/AuthController.java" -Value $authCtrl2 -Encoding UTF8

Write-Host "  写入 UserService.java (仅 loginUser) ..." -ForegroundColor Gray
$userSvc2 = @'
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
'@
Set-Content -Path "src/main/java/com/example/service/UserService.java" -Value $userSvc2 -Encoding UTF8

Write-Host "  写入 router/index.js (登录路由+守卫) ..." -ForegroundColor Gray
$router2 = @'
import { createRouter, createWebHistory } from 'vue-router'
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
'@
Set-Content -Path "frontend/src/router/index.js" -Value $router2 -Encoding UTF8

git add src/main/java/com/example/controller/AuthController.java
git add src/main/java/com/example/service/UserService.java
git add frontend/src/router/index.js

git commit -m "feat: 实现用户登录与前端路由守卫 [Copilot辅助]

- 添加登录页面 Login.vue
- 实现登录接口 /api/auth/login
- 支持用户名或邮箱登录
- 登录成功后返回 token 并保存到 localStorage
- 添加前端路由守卫拦截未登录请求
- 更新用户最后登录时间

Closes #11"

git push origin feature/user-login --force 2>$null
Write-Host "[OK] feature/user-login 已推送" -ForegroundColor Green

# ============================================================
# 5. 创建 PR3: feature/password-security
# ============================================================
Write-Host "`n[Step 5] 创建 feature/password-security ..." -ForegroundColor Green

git checkout $INITIAL_COMMIT --detach
git branch -D feature/password-security 2>$null
git checkout -b feature/password-security

git add frontend/src/views/Settings.vue
git add frontend/src/views/My.vue
git add src/main/java/com/example/controller/UserController.java
git add src/main/java/com/example/dto/UpdatePasswordRequest.java
git add src/main/java/com/example/dto/UpdateProfileRequest.java
git add src/main/java/com/example/dto/UserProfileDTO.java
git add src/main/java/com/example/entity/Wallet.java
git add src/main/java/com/example/repository/WalletRepository.java
git add src/main/java/com/example/service/WalletService.java
git add src/main/java/com/example/service/OrderService.java
git add src/main/java/com/example/repository/OrderRepository.java
git add src/main/java/com/example/entity/Order.java
git add src/main/java/com/example/entity/User.java
git add src/main/java/com/example/repository/UserRepository.java
git add src/main/java/com/example/config/SecurityConfig.java
git add database.sql
git add pom.xml
git add src/main/resources/application.properties
git add frontend/src/main.js
git add frontend/src/App.vue
git add frontend/package.json
git add frontend/vue.config.js
git add frontend/public/index.html
git add src/main/java/com/example/Main.java

Write-Host "  写入 router/index.js (设置+我的路由+守卫) ..." -ForegroundColor Gray
$router3 = @'
import { createRouter, createWebHistory } from 'vue-router'
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
'@
Set-Content -Path "frontend/src/router/index.js" -Value $router3 -Encoding UTF8

git add frontend/src/router/index.js

git commit -m "feat: 实现账号设置与密码安全功能 [Copilot辅助]

- 添加账号设置页 Settings.vue（修改密码 + 修改资料）
- 添加我的页面 My.vue（个人信息、钱包、订单统计）
- 实现密码修改接口 /api/user/{id}/password
- 旧密码校验、新密码一致性校验
- 实现资料更新接口 /api/user/{id}/profile
- 前端密码长度校验、用户名/邮箱修改防重复校验

Relates password-security"

git push origin feature/password-security --force 2>$null
Write-Host "[OK] feature/password-security 已推送" -ForegroundColor Green

# ============================================================
# 6. 切回 main
# ============================================================
Write-Host "`n[Step 6] 切回 main ..." -ForegroundColor Green
git checkout main

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  脚本执行完成!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "已创建的远程分支:" -ForegroundColor Green
Write-Host "  1. feature/student-register  (PR1 - 注册, Closes #9)" -ForegroundColor White
Write-Host "  2. feature/user-login        (PR2 - 登录, Closes #11)" -ForegroundColor White
Write-Host "  3. feature/password-security (PR3 - 密码安全, 新建 Issue)" -ForegroundColor White
Write-Host "  4. backup-main              (原始 main 备份)" -ForegroundColor White
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "  1. git push origin main --force   (推送回退后的 main)" -ForegroundColor White
Write-Host "  2. 去 GitHub 依次为 3 个分支创建 PR (按 PR1->PR2->PR3 顺序合并)" -ForegroundColor White
Write-Host ""
