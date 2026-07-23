package com.budgettracker.repository;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategoryOrderByDateDesc(String category);

    List<Expense> findAllByOrderByDateDesc();

    @Query("SELECT new com.budgettracker.dto.CategorySummary(e.category, SUM(e.amount)) " +
           "FROM Expense e GROUP BY e.category")
    List<CategorySummary> findCategorySummaries();
}
