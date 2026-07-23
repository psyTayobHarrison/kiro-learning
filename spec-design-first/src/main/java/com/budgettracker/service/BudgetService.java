package com.budgettracker.service;

import com.budgettracker.dto.BudgetCreateDTO;
import com.budgettracker.dto.BudgetStatusDTO;
import com.budgettracker.dto.BudgetStatusProjection;
import com.budgettracker.exception.ResourceNotFoundException;
import com.budgettracker.model.Budget;
import com.budgettracker.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository repository;

    public BudgetService(BudgetRepository repository) {
        this.repository = repository;
    }

    public Budget createBudget(BudgetCreateDTO dto) {
        Budget entity = new Budget();
        entity.setCategory(dto.getCategory());
        entity.setMonth(dto.getMonth());
        entity.setLimitAmount(dto.getLimitAmount());
        return repository.save(entity);
    }

    public List<Budget> getBudgets() {
        return repository.findAll();
    }

    public Budget updateBudget(Long id, BudgetCreateDTO dto) {
        Budget existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
        existing.setCategory(dto.getCategory());
        existing.setMonth(dto.getMonth());
        existing.setLimitAmount(dto.getLimitAmount());
        return repository.save(existing);
    }

    public void deleteBudget(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Budget not found: " + id);
        }
        repository.deleteById(id);
    }

    public List<BudgetStatusDTO> getBudgetStatus() {
        List<BudgetStatusProjection> rows = repository.getBudgetStatusRows();
        return rows.stream().map(projection -> {
            java.math.BigDecimal limitAmount = projection.getLimitAmount();
            java.math.BigDecimal actual = projection.getActual();
            java.math.BigDecimal remaining = limitAmount.subtract(actual);
            return new BudgetStatusDTO(
                    projection.getId(),
                    projection.getCategory(),
                    projection.getMonth(),
                    limitAmount,
                    actual,
                    remaining
            );
        }).toList();
    }
}
