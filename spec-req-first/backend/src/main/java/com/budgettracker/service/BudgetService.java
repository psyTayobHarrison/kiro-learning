package com.budgettracker.service;

import com.budgettracker.dto.BudgetRequest;
import com.budgettracker.dto.BudgetResponse;
import com.budgettracker.dto.BudgetStatusResponse;

import java.util.List;

public interface BudgetService {

    BudgetResponse createBudget(BudgetRequest request);

    List<BudgetResponse> listBudgets();

    BudgetResponse updateBudget(Long id, BudgetRequest request);

    void deleteBudget(Long id);

    List<BudgetStatusResponse> getBudgetStatus();
}
