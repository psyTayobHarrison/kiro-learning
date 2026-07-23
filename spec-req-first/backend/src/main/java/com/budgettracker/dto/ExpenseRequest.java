package com.budgettracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
    @DecimalMax(value = "999999999.99", message = "amount must be at most 999999999.99")
    private BigDecimal amount;

    @NotBlank(message = "category is required")
    @Size(min = 1, max = 50, message = "category must be between 1 and 50 characters")
    private String category;

    @NotNull(message = "date is required")
    private LocalDate date;

    @Size(max = 255, message = "description must be at most 255 characters")
    private String description;

    public ExpenseRequest() {
    }

    public ExpenseRequest(BigDecimal amount, String category, LocalDate date, String description) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
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
}
