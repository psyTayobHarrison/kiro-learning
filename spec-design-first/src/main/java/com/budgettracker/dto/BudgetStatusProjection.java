package com.budgettracker.dto;

import java.math.BigDecimal;

/**
 * Spring Data interface-based projection for the native getBudgetStatusRows() query.
 * Column aliases in the SQL must match these getter names (case-insensitive).
 */
public interface BudgetStatusProjection {

    Long getId();

    String getCategory();

    String getMonth();

    BigDecimal getLimitAmount();

    BigDecimal getActual();
}
