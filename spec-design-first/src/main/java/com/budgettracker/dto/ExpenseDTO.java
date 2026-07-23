package com.budgettracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDTO {

    private Long id;
    private BigDecimal amount;
    private String category;
    private LocalDate date;
    private String description;
    private LocalDateTime createdAt;
}
