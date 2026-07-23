package com.budgettracker.service;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.dto.ExpenseRequest;
import com.budgettracker.dto.ExpenseResponse;
import com.budgettracker.exception.ExpenseNotFoundException;
import com.budgettracker.model.Expense;
import com.budgettracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        Expense expense = mapToEntity(request);
        Expense saved = expenseRepository.save(expense);
        return mapToResponse(saved);
    }

    public List<ExpenseResponse> listExpenses(String category) {
        List<Expense> expenses;
        if (category != null && !category.isBlank()) {
            expenses = expenseRepository.findByCategoryOrderByDateDesc(category);
        } else {
            expenses = expenseRepository.findAllByOrderByDateDesc();
        }
        return expenses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CategorySummary> getSummary() {
        List<CategorySummary> summaries = expenseRepository.findCategorySummaries();
        for (CategorySummary summary : summaries) {
            summary.setTotalAmount(
                    summary.getTotalAmount().setScale(2, RoundingMode.HALF_UP)
            );
        }
        return summaries;
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));

        existing.setAmount(request.getAmount());
        existing.setCategory(request.getCategory());
        existing.setDate(request.getDate());
        existing.setDescription(request.getDescription());

        Expense updated = expenseRepository.save(existing);
        return mapToResponse(updated);
    }

    public void deleteExpense(Long id) {
        Expense existing = expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
        expenseRepository.delete(existing);
    }

    private Expense mapToEntity(ExpenseRequest request) {
        Expense expense = new Expense();
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setDescription(request.getDescription());
        return expense;
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getDescription(),
                expense.getCreatedAt()
        );
    }
}
