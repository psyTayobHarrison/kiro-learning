package com.budgettracker.service;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.dto.ExpenseRequest;
import com.budgettracker.dto.ExpenseResponse;
import com.budgettracker.exception.ExpenseNotFoundException;
import com.budgettracker.model.Expense;
import com.budgettracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void createExpense_withValidData_returnsExpenseWithIdAndCreatedAt() {
        // Arrange
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("42.50"),
                "Groceries",
                LocalDate.of(2024, 1, 15),
                "Weekly shopping"
        );

        Expense savedExpense = new Expense();
        savedExpense.setId(1L);
        savedExpense.setAmount(new BigDecimal("42.50"));
        savedExpense.setCategory("Groceries");
        savedExpense.setDate(LocalDate.of(2024, 1, 15));
        savedExpense.setDescription("Weekly shopping");
        savedExpense.setCreatedAt(Instant.parse("2024-01-15T10:30:00Z"));

        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        // Act
        ExpenseResponse response = expenseService.createExpense(request);

        // Assert
        assertNotNull(response.getId());
        assertEquals(1L, response.getId());
        assertNotNull(response.getCreatedAt());
        assertEquals(new BigDecimal("42.50"), response.getAmount());
        assertEquals("Groceries", response.getCategory());
        assertEquals(LocalDate.of(2024, 1, 15), response.getDate());
        assertEquals("Weekly shopping", response.getDescription());
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void listExpenses_withNullCategory_returnsAllExpensesSortedByDateDesc() {
        // Arrange
        Expense expense1 = createExpense(1L, "50.00", "Food", LocalDate.of(2024, 1, 20));
        Expense expense2 = createExpense(2L, "30.00", "Transport", LocalDate.of(2024, 1, 15));

        when(expenseRepository.findAllByOrderByDateDesc()).thenReturn(List.of(expense1, expense2));

        // Act
        List<ExpenseResponse> result = expenseService.listExpenses(null);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(expenseRepository).findAllByOrderByDateDesc();
        verify(expenseRepository, never()).findByCategoryOrderByDateDesc(any());
    }

    @Test
    void listExpenses_withBlankCategory_returnsAllExpenses() {
        // Arrange
        Expense expense1 = createExpense(1L, "50.00", "Food", LocalDate.of(2024, 1, 20));

        when(expenseRepository.findAllByOrderByDateDesc()).thenReturn(List.of(expense1));

        // Act
        List<ExpenseResponse> result = expenseService.listExpenses("   ");

        // Assert
        assertEquals(1, result.size());
        verify(expenseRepository).findAllByOrderByDateDesc();
    }

    @Test
    void listExpenses_withCategory_returnsFilteredExpenses() {
        // Arrange
        Expense foodExpense = createExpense(1L, "50.00", "Food", LocalDate.of(2024, 1, 20));

        when(expenseRepository.findByCategoryOrderByDateDesc("Food")).thenReturn(List.of(foodExpense));

        // Act
        List<ExpenseResponse> result = expenseService.listExpenses("Food");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getCategory());
        verify(expenseRepository).findByCategoryOrderByDateDesc("Food");
        verify(expenseRepository, never()).findAllByOrderByDateDesc();
    }

    @Test
    void updateExpense_withExistingId_preservesCreatedAtAndUpdatesFields() {
        // Arrange
        Instant originalCreatedAt = Instant.parse("2024-01-10T08:00:00Z");

        Expense existing = new Expense();
        existing.setId(1L);
        existing.setAmount(new BigDecimal("20.00"));
        existing.setCategory("Transport");
        existing.setDate(LocalDate.of(2024, 1, 10));
        existing.setDescription("Bus fare");
        existing.setCreatedAt(originalCreatedAt);

        ExpenseRequest updateRequest = new ExpenseRequest(
                new BigDecimal("75.00"),
                "Food",
                LocalDate.of(2024, 1, 20),
                "Restaurant dinner"
        );

        Expense updatedExpense = new Expense();
        updatedExpense.setId(1L);
        updatedExpense.setAmount(new BigDecimal("75.00"));
        updatedExpense.setCategory("Food");
        updatedExpense.setDate(LocalDate.of(2024, 1, 20));
        updatedExpense.setDescription("Restaurant dinner");
        updatedExpense.setCreatedAt(originalCreatedAt);

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(expenseRepository.save(any(Expense.class))).thenReturn(updatedExpense);

        // Act
        ExpenseResponse response = expenseService.updateExpense(1L, updateRequest);

        // Assert
        assertEquals(1L, response.getId());
        assertEquals(originalCreatedAt, response.getCreatedAt());
        assertEquals(new BigDecimal("75.00"), response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals(LocalDate.of(2024, 1, 20), response.getDate());
        assertEquals("Restaurant dinner", response.getDescription());
    }

    @Test
    void updateExpense_withNonExistentId_throwsExpenseNotFoundException() {
        // Arrange
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("50.00"),
                "Food",
                LocalDate.of(2024, 1, 15),
                "Lunch"
        );

        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ExpenseNotFoundException.class, () ->
                expenseService.updateExpense(999L, request));
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void deleteExpense_withNonExistentId_throwsExpenseNotFoundException() {
        // Arrange
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ExpenseNotFoundException.class, () ->
                expenseService.deleteExpense(999L));
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void deleteExpense_withExistingId_deletesExpense() {
        // Arrange
        Expense existing = createExpense(1L, "25.00", "Food", LocalDate.of(2024, 1, 15));

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Act
        expenseService.deleteExpense(1L);

        // Assert
        verify(expenseRepository).delete(existing);
    }

    @Test
    void getSummary_returnsSummariesWithAmountsRoundedToTwoDecimalPlaces() {
        // Arrange
        CategorySummary groceries = new CategorySummary("Groceries", new BigDecimal("285.456"));
        CategorySummary transport = new CategorySummary("Transport", new BigDecimal("150.123"));

        when(expenseRepository.findCategorySummaries()).thenReturn(List.of(groceries, transport));

        // Act
        List<CategorySummary> result = expenseService.getSummary();

        // Assert
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("285.46"), result.get(0).getTotalAmount());
        assertEquals(new BigDecimal("150.12"), result.get(1).getTotalAmount());
    }

    // Helper method to create an Expense with common fields
    private Expense createExpense(Long id, String amount, String category, LocalDate date) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setAmount(new BigDecimal(amount));
        expense.setCategory(category);
        expense.setDate(date);
        expense.setDescription("Test expense");
        expense.setCreatedAt(Instant.now());
        return expense;
    }
}
