package com.budgettracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class BudgetRequest {

    @NotBlank(message = "category is required")
    @Size(max = 50, message = "category must be at most 50 characters")
    private String category;

    @NotNull(message = "month is required")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "month must be in YYYY-MM format")
    private String month;

    @NotNull(message = "limitAmount is required")
    @DecimalMin(value = "0.01", message = "limitAmount must be at least 0.01")
    private BigDecimal limitAmount;

    public BudgetRequest() {
    }

    public BudgetRequest(String category, String month, BigDecimal limitAmount) {
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
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
}
