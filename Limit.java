package com.example.limits.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "limits")
public class Limit {
    @Id
    private String userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal reserved = BigDecimal.ZERO;

    private LocalDateTime updatedAt;

    public Limit() {}

    public Limit(String userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
        this.reserved = BigDecimal.ZERO;
        this.updatedAt = LocalDateTime.now();
    }

    // getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getReserved() { return reserved; }
    public void setReserved(BigDecimal reserved) { this.reserved = reserved; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}\n