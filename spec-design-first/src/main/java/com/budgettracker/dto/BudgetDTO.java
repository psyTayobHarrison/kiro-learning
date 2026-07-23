package com.budgettracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    private Long id;
    private String category;
    private String month;
    private BigDecimal limitAmount;
    private LocalDateTime createdAt;
}
