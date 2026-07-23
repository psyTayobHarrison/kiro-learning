package com.budgettracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetCreateDTO {

    @NotBlank
    private String category;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$",
             message = "month must be in YYYY-MM format")
    private String month;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal limitAmount;
}
