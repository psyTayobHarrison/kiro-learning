package com.budgettracker.repository;

import com.budgettracker.dto.BudgetStatusProjection;
import com.budgettracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * Returns budget rows joined with their actual spend from the expenses table.
     * Uses a native PostgreSQL query with a LEFT JOIN so budgets with no matching
     * expenses still appear with actual = 0 (via COALESCE).
     *
     * The column aliases (id, category, month, limitAmount, actual) must match
     * the getter names of BudgetStatusProjection for Spring Data to map them.
     */
    @Query(value = """
            SELECT b.id          AS id,
                   b.category    AS category,
                   b.month       AS month,
                   b.limit_amount AS limitAmount,
                   COALESCE(SUM(e.amount), 0) AS actual
            FROM budgets b
            LEFT JOIN expenses e
              ON e.category = b.category
             AND TO_CHAR(e.date, 'YYYY-MM') = b.month
            GROUP BY b.id, b.category, b.month, b.limit_amount
            """,
            nativeQuery = true)
    List<BudgetStatusProjection> getBudgetStatusRows();
}
