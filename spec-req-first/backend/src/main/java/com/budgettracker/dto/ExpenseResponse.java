package com.budgettracker.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ExpenseResponse {

    private Long id;
    private BigDecimal amount;
    private String category;
    private LocalDate date;
    private String description;
    private Instant createdAt;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, BigDecimal amount, String category, LocalDate date, String description, Instant createdAt) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
