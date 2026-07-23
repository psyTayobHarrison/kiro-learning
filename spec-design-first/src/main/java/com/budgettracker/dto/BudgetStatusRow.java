package com.budgettracker.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * JPQL constructor-mapped projection used by BudgetRepository.getBudgetStatusRows().
 */
@Data
public class BudgetStatusRow {

    private Long id;
    private String category;
    private String month;
    private BigDecimal limitAmount;
    private BigDecimal actual;

    public BudgetStatusRow(Long id, String category, String month,
                           BigDecimal limitAmount, BigDecimal actual) {
        this.id = id;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
        this.actual = actual;
    }
}
