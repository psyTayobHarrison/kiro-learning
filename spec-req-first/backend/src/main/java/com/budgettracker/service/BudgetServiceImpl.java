package com.budgettracker.service;

import com.budgettracker.dto.BudgetRequest;
import com.budgettracker.dto.BudgetResponse;
import com.budgettracker.dto.BudgetStatusResponse;
import com.budgettracker.exception.BudgetNotFoundException;
import com.budgettracker.exception.DuplicateBudgetException;
import com.budgettracker.model.Budget;
import com.budgettracker.repository.BudgetRepository;
import com.budgettracker.repository.ExpenseRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository, ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    public BudgetResponse createBudget(BudgetRequest request) {
        YearMonth month = YearMonth.parse(request.getMonth());

        Optional<Budget> existing = budgetRepository.findByCategoryIgnoreCaseAndMonth(
                request.getCategory(), month);
        if (existing.isPresent()) {
            throw new DuplicateBudgetException(request.getCategory(), request.getMonth());
        }

        Budget budget = new Budget();
        budget.setCategory(request.getCategory());
        budget.setMonth(month);
        budget.setLimitAmount(request.getLimitAmount());

        try {
            Budget saved = budgetRepository.save(budget);
            return mapToResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateBudgetException(request.getCategory(), request.getMonth());
        }
    }

    @Override
    public List<BudgetResponse> listBudgets() {
        return budgetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));

        YearMonth month = YearMonth.parse(request.getMonth());

        Optional<Budget> duplicate = budgetRepository.findByCategoryIgnoreCaseAndMonth(
                request.getCategory(), month);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateBudgetException(request.getCategory(), request.getMonth());
        }

        budget.setCategory(request.getCategory());
        budget.setMonth(month);
        budget.setLimitAmount(request.getLimitAmount());

        try {
            Budget updated = budgetRepository.save(budget);
            return mapToResponse(updated);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateBudgetException(request.getCategory(), request.getMonth());
        }
    }

    @Override
    public void deleteBudget(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));
        budgetRepository.delete(budget);
    }

    @Override
    public List<BudgetStatusResponse> getBudgetStatus() {
        List<Budget> budgets = budgetRepository.findAll();

        return budgets.stream().map(budget -> {
            int year = budget.getMonth().getYear();
            int monthValue = budget.getMonth().getMonthValue();

            BigDecimal actualSpend = expenseRepository.sumAmountByCategoryAndMonth(
                    budget.getCategory(), year, monthValue);

            BigDecimal remainingAmount = budget.getLimitAmount().subtract(actualSpend)
                    .setScale(2, RoundingMode.HALF_UP);

            return new BudgetStatusResponse(
                    budget.getId(),
                    budget.getCategory(),
                    budget.getMonth().toString(),
                    budget.getLimitAmount(),
                    actualSpend.setScale(2, RoundingMode.HALF_UP),
                    remainingAmount
            );
        }).collect(Collectors.toList());
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getMonth().toString(),
                budget.getLimitAmount(),
                budget.getCreatedAt()
        );
    }
}
