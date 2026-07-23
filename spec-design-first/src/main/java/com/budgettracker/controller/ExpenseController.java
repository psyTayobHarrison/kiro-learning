package com.budgettracker.controller;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.dto.CategorySummaryDTO;
import com.budgettracker.dto.ExpenseCreateDTO;
import com.budgettracker.dto.ExpenseDTO;
import com.budgettracker.model.Expense;
import com.budgettracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseCreateDTO dto) {
        Expense expense = expenseService.createExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toExpenseDTO(expense));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses(@RequestParam(required = false) String category) {
        List<Expense> expenses = expenseService.getExpenses(category);
        List<ExpenseDTO> dtos = expenses.stream().map(this::toExpenseDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id,
                                                    @Valid @RequestBody ExpenseCreateDTO dto) {
        Expense expense = expenseService.updateExpense(id, dto);
        return ResponseEntity.ok(toExpenseDTO(expense));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CategorySummaryDTO>> getSummary() {
        List<CategorySummary> summaries = expenseService.getSummary();
        List<CategorySummaryDTO> dtos = summaries.stream().map(this::toCategorySummaryDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    private ExpenseDTO toExpenseDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory());
        dto.setDate(expense.getDate());
        dto.setDescription(expense.getDescription());
        dto.setCreatedAt(expense.getCreatedAt());
        return dto;
    }

    private CategorySummaryDTO toCategorySummaryDTO(CategorySummary summary) {
        CategorySummaryDTO dto = new CategorySummaryDTO();
        dto.setCategory(summary.getCategory());
        dto.setTotal(summary.getTotal());
        return dto;
    }
}
