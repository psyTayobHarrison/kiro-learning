package com.budgettracker.exception;

public class DuplicateBudgetException extends RuntimeException {

    public DuplicateBudgetException(String category, String month) {
        super("A budget already exists for category '" + category + "' and month '" + month + "'");
    }
}
