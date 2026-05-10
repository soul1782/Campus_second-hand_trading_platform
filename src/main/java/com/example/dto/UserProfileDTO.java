package com.example.dto;

import java.math.BigDecimal;

public class UserProfileDTO {

    private Long userId;
    private String username;
    private String email;
    private String realName;
    private String avatarUrl;
    private String campus;
    private String department;
    private Byte identityType;
    private Byte verifyStatus;

    private BigDecimal walletBalance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenditure;

    private Long pendingPaymentCount;
    private Long pendingShipCount;
    private Long pendingReceiveCount;
    private Long completedCount;

    public UserProfileDTO() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Byte getIdentityType() {
        return identityType;
    }

    public void setIdentityType(Byte identityType) {
        this.identityType = identityType;
    }

    public Byte getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Byte verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(BigDecimal walletBalance) {
        this.walletBalance = walletBalance;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpenditure() {
        return totalExpenditure;
    }

    public void setTotalExpenditure(BigDecimal totalExpenditure) {
        this.totalExpenditure = totalExpenditure;
    }

    public Long getPendingPaymentCount() {
        return pendingPaymentCount;
    }

    public void setPendingPaymentCount(Long pendingPaymentCount) {
        this.pendingPaymentCount = pendingPaymentCount;
    }

    public Long getPendingShipCount() {
        return pendingShipCount;
    }

    public void setPendingShipCount(Long pendingShipCount) {
        this.pendingShipCount = pendingShipCount;
    }

    public Long getPendingReceiveCount() {
        return pendingReceiveCount;
    }

    public void setPendingReceiveCount(Long pendingReceiveCount) {
        this.pendingReceiveCount = pendingReceiveCount;
    }

    public Long getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(Long completedCount) {
        this.completedCount = completedCount;
    }
}
