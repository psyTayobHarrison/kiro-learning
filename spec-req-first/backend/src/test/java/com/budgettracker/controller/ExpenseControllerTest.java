package com.budgettracker.controller;

import com.budgettracker.dto.CategorySummary;
import com.budgettracker.dto.ExpenseRequest;
import com.budgettracker.dto.ExpenseResponse;
import com.budgettracker.exception.ExpenseNotFoundException;
import com.budgettracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    // 1. POST /expenses with valid body -> 201
    @Test
    void createExpense_withValidBody_returns201WithAllFields() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("42.50"), "Groceries", LocalDate.of(2024, 1, 15), "Weekly shopping");

        ExpenseResponse response = new ExpenseResponse(
                1L, new BigDecimal("42.50"), "Groceries", LocalDate.of(2024, 1, 15),
                "Weekly shopping", Instant.parse("2024-01-15T10:30:00Z"));

        when(expenseService.createExpense(any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(42.50))
                .andExpect(jsonPath("$.category").value("Groceries"))
                .andExpect(jsonPath("$.date").value("2024-01-15"))
                .andExpect(jsonPath("$.description").value("Weekly shopping"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    // 2. POST /expenses with missing amount -> 400
    @Test
    void createExpense_withMissingAmount_returns400WithFieldError() throws Exception {
        String body = "{\"category\":\"Food\",\"date\":\"2024-01-15\"}";

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@=~ /amount.*/)]").exists());
    }

    // 3. POST /expenses with missing category -> 400
    @Test
    void createExpense_withMissingCategory_returns400WithFieldError() throws Exception {
        String body = "{\"amount\":10.00,\"date\":\"2024-01-15\"}";

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@=~ /category.*/)]").exists());
    }

    // 4. POST /expenses with missing date -> 400
    @Test
    void createExpense_withMissingDate_returns400WithFieldError() throws Exception {
        String body = "{\"amount\":10.00,\"category\":\"Food\"}";

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@=~ /date.*/)]").exists());
    }

    // 5. POST /expenses with amount = 0 (below min) -> 400
    @Test
    void createExpense_withAmountBelowMin_returns400() throws Exception {
        String body = "{\"amount\":0,\"category\":\"Food\",\"date\":\"2024-01-15\"}";

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[?(@=~ /amount.*/)]").exists());
    }

    // 6. POST /expenses with amount above max -> 400
    @Test
    void createExpense_withAmountAboveMax_returns400() throws Exception {
        String body = "{\"amount\":9999999999.99,\"category\":\"Food\",\"date\":\"2024-01-15\"}";

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[?(@=~ /amount.*/)]").exists());
    }

    // 7. POST /expenses with category > 50 chars -> 400
    @Test
    void createExpense_withCategoryTooLong_returns400() throws Exception {
        String longCategory = "A".repeat(51);
        String body = "{\"amount\":10.00,\"category\":\"" + longCategory + "\",\"date\":\"2024-01-15\"}";

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[?(@=~ /category.*/)]").exists());
    }

    // 8. GET /expenses -> 200, returns list
    @Test
    void listExpenses_withNoFilter_returns200WithList() throws Exception {
        ExpenseResponse expense = new ExpenseResponse(
                1L, new BigDecimal("25.00"), "Food", LocalDate.of(2024, 2, 1),
                "Lunch", Instant.parse("2024-02-01T12:00:00Z"));

        when(expenseService.listExpenses(null)).thenReturn(List.of(expense));

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].category").value("Food"));
    }

    // 9. GET /expenses?category=Food -> 200, calls service with "Food"
    @Test
    void listExpenses_withCategoryFilter_returns200AndCallsServiceWithCategory() throws Exception {
        ExpenseResponse expense = new ExpenseResponse(
                1L, new BigDecimal("25.00"), "Food", LocalDate.of(2024, 2, 1),
                "Lunch", Instant.parse("2024-02-01T12:00:00Z"));

        when(expenseService.listExpenses("Food")).thenReturn(List.of(expense));

        mockMvc.perform(get("/expenses").param("category", "Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Food"));

        verify(expenseService).listExpenses("Food");
    }

    // 10. GET /expenses?category= (empty) -> 400
    @Test
    void listExpenses_withEmptyCategory_returns400WithMessage() throws Exception {
        mockMvc.perform(get("/expenses").param("category", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("category parameter must be non-empty"));
    }

    // 11. GET /expenses/summary -> 200, returns summaries
    @Test
    void getSummary_returns200WithSummaries() throws Exception {
        CategorySummary summary = new CategorySummary("Food", new BigDecimal("150.00"));

        when(expenseService.getSummary()).thenReturn(List.of(summary));

        mockMvc.perform(get("/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("Food"))
                .andExpect(jsonPath("$[0].totalAmount").value(150.00));
    }

    // 12. PUT /expenses/1 with valid body -> 200
    @Test
    void updateExpense_withValidBody_returns200() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("55.00"), "Transport", LocalDate.of(2024, 3, 1), "Taxi");

        ExpenseResponse response = new ExpenseResponse(
                1L, new BigDecimal("55.00"), "Transport", LocalDate.of(2024, 3, 1),
                "Taxi", Instant.parse("2024-01-15T10:30:00Z"));

        when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(55.00))
                .andExpect(jsonPath("$.category").value("Transport"));
    }

    // 13. PUT /expenses/999 when service throws ExpenseNotFoundException -> 404
    @Test
    void updateExpense_whenNotFound_returns404() throws Exception {
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("10.00"), "Food", LocalDate.of(2024, 1, 1), null);

        when(expenseService.updateExpense(eq(999L), any(ExpenseRequest.class)))
                .thenThrow(new ExpenseNotFoundException(999L));

        mockMvc.perform(put("/expenses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense not found with id: 999"));
    }

    // 14. PUT /expenses/1 with invalid body -> 400
    @Test
    void updateExpense_withInvalidBody_returns400() throws Exception {
        String body = "{\"category\":\"Food\",\"date\":\"2024-01-15\"}"; // missing amount

        mockMvc.perform(put("/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    // 15. DELETE /expenses/1 -> 204
    @Test
    void deleteExpense_withExistingId_returns204() throws Exception {
        doNothing().when(expenseService).deleteExpense(1L);

        mockMvc.perform(delete("/expenses/1"))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(1L);
    }

    // 16. DELETE /expenses/999 when service throws ExpenseNotFoundException -> 404
    @Test
    void deleteExpense_whenNotFound_returns404() throws Exception {
        doThrow(new ExpenseNotFoundException(999L)).when(expenseService).deleteExpense(999L);

        mockMvc.perform(delete("/expenses/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense not found with id: 999"));
    }
}
