package com.budgettracker.property;

import com.budgettracker.dto.ExpenseRequest;
import com.budgettracker.dto.ExpenseResponse;
import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property 1: Create-then-retrieve round trip
 *
 * For any valid expense request (amount between 0.01 and 999,999,999.99, non-empty category
 * of 1-50 chars, valid ISO 8601 date, optional description), creating the expense via POST
 * and then retrieving all expenses via GET SHALL return a list containing an expense whose
 * amount, category, date, and description fields exactly match the original request, plus a
 * non-null unique id and a non-null created_at timestamp.
 *
 * Validates: Requirements 1.1, 1.5, 2.1
 *
 * Feature: budget-tracker, Property 1: Create-then-retrieve round trip
 */
@JqwikSpringSupport
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateRetrieveRoundTripProperties {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * **Validates: Requirements 1.1, 1.5, 2.1**
     */
    @Property(tries = 100)
    @Label("Create-then-retrieve round trip: POST then GET returns matching expense with id and createdAt")
    void createThenRetrieveRoundTrip(@ForAll("validExpenseRequests") ExpenseRequest request) {
        // POST the expense
        ResponseEntity<ExpenseResponse> postResponse = restTemplate.postForEntity(
                "/expenses", request, ExpenseResponse.class);

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ExpenseResponse created = postResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();

        // GET all expenses
        ResponseEntity<List<ExpenseResponse>> getResponse = restTemplate.exchange(
                "/expenses", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ExpenseResponse>>() {});

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ExpenseResponse> expenses = getResponse.getBody();
        assertThat(expenses).isNotNull();

        // Find the created expense in the list
        ExpenseResponse found = expenses.stream()
                .filter(e -> e.getId().equals(created.getId()))
                .findFirst()
                .orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getAmount().compareTo(request.getAmount())).isEqualTo(0);
        assertThat(found.getCategory()).isEqualTo(request.getCategory());
        assertThat(found.getDate()).isEqualTo(request.getDate());
        assertThat(found.getDescription()).isEqualTo(request.getDescription());
        assertThat(found.getId()).isNotNull();
        assertThat(found.getCreatedAt()).isNotNull();

        // Clean up: delete the created expense
        restTemplate.delete("/expenses/" + created.getId());
    }

    @Provide
    Arbitrary<ExpenseRequest> validExpenseRequests() {
        Arbitrary<BigDecimal> amounts = Arbitraries.bigDecimals()
                .between(new BigDecimal("0.01"), new BigDecimal("999999999.99"))
                .ofScale(2);

        Arbitrary<String> categories = Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50);

        Arbitrary<LocalDate> dates = Arbitraries.of(
                LocalDate.of(2000, 1, 1).datesUntil(LocalDate.of(2030, 12, 31)).toArray(LocalDate[]::new)
        );

        Arbitrary<String> descriptions = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.strings()
                        .alpha()
                        .ofMinLength(1)
                        .ofMaxLength(255)
        );

        return Combinators.combine(amounts, categories, dates, descriptions)
                .as(ExpenseRequest::new);
    }
}
