package com.budgettracker.controller;

import com.budgettracker.dto.BudgetCreateDTO;
import com.budgettracker.dto.BudgetDTO;
import com.budgettracker.dto.BudgetStatusDTO;
import com.budgettracker.model.Budget;
import com.budgettracker.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetDTO> createBudget(@Valid @RequestBody BudgetCreateDTO dto) {
        Budget budget = budgetService.createBudget(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toBudgetDTO(budget));
    }

    @GetMapping
    public ResponseEntity<List<BudgetDTO>> getBudgets() {
        List<Budget> budgets = budgetService.getBudgets();
        List<BudgetDTO> dtos = budgets.stream().map(this::toBudgetDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> updateBudget(@PathVariable Long id,
                                                  @Valid @RequestBody BudgetCreateDTO dto) {
        Budget budget = budgetService.updateBudget(id, dto);
        return ResponseEntity.ok(toBudgetDTO(budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusDTO>> getBudgetStatus() {
        List<BudgetStatusDTO> statuses = budgetService.getBudgetStatus();
        return ResponseEntity.ok(statuses);
    }

    private BudgetDTO toBudgetDTO(Budget budget) {
        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        dto.setCategory(budget.getCategory());
        dto.setMonth(budget.getMonth());
        dto.setLimitAmount(budget.getLimitAmount());
        dto.setCreatedAt(budget.getCreatedAt());
        return dto;
    }
}
