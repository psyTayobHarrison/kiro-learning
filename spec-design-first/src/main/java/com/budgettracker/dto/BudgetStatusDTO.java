package com.budgettracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetStatusDTO {

    private Long id;
    private String category;
    private String month;
    private BigDecimal limitAmount;
    private BigDecimal actual;
    private BigDecimal remaining;  // limitAmount - actual; negative = over budget
}
