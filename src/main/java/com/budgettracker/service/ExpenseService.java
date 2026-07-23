package com.budgettracker.service;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.dto.ExpenseCreateDTO;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.model.Expense;
import com.budgettracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public Expense createExpense(ExpenseCreateDTO dto) {
        Expense entity = new Expense();
        entity.setAmount(dto.getAmount());
        entity.setCategory(dto.getCategory());
        entity.setDate(dto.getDate());
        entity.setDescription(dto.getDescription());
        return repository.save(entity);
    }

    public List<Expense> getExpenses(String category) {
        if (category != null && !category.isBlank()) {
            return repository.findByCategory(category);
        }
        return repository.findAll();
    }

    public Expense updateExpense(Long id, ExpenseCreateDTO dto) {
        Expense existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        existing.setAmount(dto.getAmount());
        existing.setCategory(dto.getCategory());
        existing.setDate(dto.getDate());
        existing.setDescription(dto.getDescription());
        return repository.save(existing);
    }

    public void deleteExpense(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found: " + id);
        }
        repository.deleteById(id);
    }

    public List<CategorySummary> getSummary() {
        return repository.getCategoryTotals();
    }
}
