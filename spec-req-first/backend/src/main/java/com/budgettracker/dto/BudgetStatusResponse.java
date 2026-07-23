package com.budgettracker.dto;

import java.math.BigDecimal;

public class BudgetStatusResponse {

    private Long id;
    private String category;
    private String month;
    private BigDecimal limitAmount;
    private BigDecimal actualSpend;
    private BigDecimal remainingAmount;

    public BudgetStatusResponse() {
    }

    public BudgetStatusResponse(Long id, String category, String month, BigDecimal limitAmount, BigDecimal actualSpend, BigDecimal remainingAmount) {
        this.id = id;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
        this.actualSpend = actualSpend;
        this.remainingAmount = remainingAmount;
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

    public BigDecimal getActualSpend() {
        return actualSpend;
    }

    public void setActualSpend(BigDecimal actualSpend) {
        this.actualSpend = actualSpend;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
}
