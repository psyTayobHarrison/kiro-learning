package com.budgettracker.controller;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.dto.ErrorResponse;
import com.budgettracker.dto.ExpenseRequest;
import com.budgettracker.dto.ExpenseResponse;
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
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<?> listExpenses(@RequestParam(required = false) String category) {
        if (category != null && category.isBlank()) {
            ErrorResponse error = new ErrorResponse(400, "category parameter must be non-empty", null);
            return ResponseEntity.badRequest().body(error);
        }
        List<ExpenseResponse> expenses = expenseService.listExpenses(category);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CategorySummary>> getSummary() {
        List<CategorySummary> summaries = expenseService.getSummary();
        return ResponseEntity.ok(summaries);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id, @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }
}
