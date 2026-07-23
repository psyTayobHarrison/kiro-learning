import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ExpenseFormComponent } from './components/expense-form/expense-form.component';
import { ExpenseListComponent } from './components/expense-list/expense-list.component';
import { CategoryFilterComponent } from './components/category-filter/category-filter.component';
import { SpendingSummaryComponent } from './components/spending-summary/spending-summary.component';
import { ExpenseService } from './services/expense.service';
import { Expense, CategorySummary } from './models/expense.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    ExpenseFormComponent,
    ExpenseListComponent,
    CategoryFilterComponent,
    SpendingSummaryComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'Budget Tracker';
  expenses: Expense[] = [];
  summaries: CategorySummary[] = [];
  categories: string[] = [];
  selectedCategory: string | null = null;

  constructor(private expenseService: ExpenseService) {}

  ngOnInit(): void {
    this.loadExpenses();
    this.loadSummary();
  }

  loadExpenses(): void {
    const source$ = this.selectedCategory
      ? this.expenseService.getByCategory(this.selectedCategory)
      : this.expenseService.getAll();

    source$.subscribe({
      next: (expenses) => {
        this.expenses = expenses;
        this.categories = [...new Set(expenses.map(e => e.category))].sort();
      }
    });
  }

  loadSummary(): void {
    this.expenseService.getSummary().subscribe({
      next: (summaries) => {
        this.summaries = summaries;
      }
    });
  }

  onExpenseCreated(): void {
    this.selectedCategory = null;
    this.loadExpenses();
    this.loadSummary();
  }

  onExpenseDeleted(): void {
    this.loadExpenses();
    this.loadSummary();
  }

  onExpenseUpdated(): void {
    this.loadExpenses();
    this.loadSummary();
  }

  onCategorySelected(category: string | null): void {
    this.selectedCategory = category;
    this.loadExpenses();
  }
}
