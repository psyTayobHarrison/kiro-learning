import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExpenseFormComponent } from './components/expense-form/expense-form.component';
import { ExpenseListComponent } from './components/expense-list/expense-list.component';
import { ExpenseSummaryComponent } from './components/expense-summary/expense-summary.component';
import { ExpenseService } from './services/expense.service';
import { Expense, CategorySummary } from './models/expense.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ExpenseFormComponent, ExpenseListComponent, ExpenseSummaryComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  expenses: Expense[] = [];
  summary: CategorySummary[] = [];
  expenseToEdit: Expense | null = null;

  constructor(private expenseService: ExpenseService) {}

  ngOnInit(): void {
    this.loadExpenses();
    this.loadSummary();
  }

  loadExpenses(category?: string): void {
    this.expenseService.getExpenses(category).subscribe({
      next: (data) => this.expenses = data,
      error: (err) => console.error('Error loading expenses:', err)
    });
  }

  loadSummary(): void {
    this.expenseService.getSummary().subscribe({
      next: (data) => this.summary = data,
      error: (err) => console.error('Error loading summary:', err)
    });
  }

  onSaveExpense(expense: Expense): void {
    if (expense.id) {
      this.expenseService.updateExpense(expense.id, expense).subscribe({
        next: () => {
          this.loadExpenses();
          this.loadSummary();
          this.expenseToEdit = null;
        },
        error: (err) => console.error('Error updating expense:', err)
      });
    } else {
      this.expenseService.createExpense(expense).subscribe({
        next: () => {
          this.loadExpenses();
          this.loadSummary();
        },
        error: (err) => console.error('Error creating expense:', err)
      });
    }
  }

  onEditExpense(expense: Expense): void {
    this.expenseToEdit = { ...expense };
  }

  onDeleteExpense(id: number): void {
    this.expenseService.deleteExpense(id).subscribe({
      next: () => {
        this.loadExpenses();
        this.loadSummary();
      },
      error: (err) => console.error('Error deleting expense:', err)
    });
  }

  onFilterChange(category: string): void {
    this.loadExpenses(category || undefined);
  }

  onCancelEdit(): void {
    this.expenseToEdit = null;
  }
}
