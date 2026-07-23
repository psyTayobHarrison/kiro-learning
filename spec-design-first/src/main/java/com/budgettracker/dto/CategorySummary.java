package com.budgettracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Projection class used by JPQL SELECT NEW queries for category aggregation.
 * This is referenced directly in the repository query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummary {

    private String category;
    private BigDecimal total;
}
