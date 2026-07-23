package com.budgettracker.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class BudgetResponse {

    private Long id;
    private String category;
    private String month;
    private BigDecimal limitAmount;
    private Instant createdAt;

    public BudgetResponse() {
    }

    public BudgetResponse(Long id, String category, String month, BigDecimal limitAmount, Instant createdAt) {
        this.id = id;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(BigDecimal limitAmount) {
        this.limitAmount = limitAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
