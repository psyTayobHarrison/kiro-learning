package com.budgettracker.service;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.model.Expense;
import com.budgettracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense createExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Expense> getAllExpenses(String category) {
        if (category != null && !category.isBlank()) {
            return expenseRepository.findByCategoryIgnoreCase(category);
        }
        return expenseRepository.findAll();
    }

    public Expense updateExpense(Long id, Expense updatedExpense) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        existing.setAmount(updatedExpense.getAmount());
        existing.setCategory(updatedExpense.getCategory());
        existing.setDate(updatedExpense.getDate());
        existing.setDescription(updatedExpense.getDescription());

        return expenseRepository.save(existing);
    }

    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    public List<CategorySummary> getSummary() {
        List<Expense> all = expenseRepository.findAll();

        Map<String, BigDecimal> grouped = all.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));

        return grouped.entrySet().stream()
                .map(entry -> new CategorySummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
