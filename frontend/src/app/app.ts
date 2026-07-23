import { Component, OnInit } from '@angular/core';
import { ExpenseService } from './services/expense.service';
import { Expense, CategorySummary } from './models/expense.model';
import { ExpenseFormComponent } from './components/expense-form/expense-form.component';
import { ExpenseListComponent } from './components/expense-list/expense-list.component';
import { CategoryFilterComponent } from './components/category-filter/category-filter.component';
import { SummaryViewComponent } from './components/summary-view/summary-view.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [ExpenseFormComponent, ExpenseListComponent, CategoryFilterComponent, SummaryViewComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  expenses: Expense[] = [];
  summary: CategorySummary[] = [];
  filterCategory = '';
  noMatchesFound = false;
  expenseToEdit: Expense | null = null;

  constructor(private expenseService: ExpenseService) {}

  ngOnInit(): void {
    this.loadExpenses();
    this.loadSummary();
  }

  loadExpenses(): void {
    const category = this.filterCategory || undefined;
    this.expenseService.getExpenses(category).subscribe(data => {
      this.expenses = data;
      this.noMatchesFound = this.filterCategory.length > 0 && data.length === 0;
    });
  }

  loadSummary(): void {
    this.expenseService.getSummary().subscribe(data => {
      this.summary = data;
    });
  }

  onFilterChanged(category: string): void {
    this.filterCategory = category;
    this.loadExpenses();
  }

  onEditExpense(expense: Expense): void {
    this.expenseToEdit = expense;
  }

  onExpenseSaved(): void {
    this.expenseToEdit = null;
    this.loadExpenses();
    this.loadSummary();
  }

  onDeleteExpense(id: number): void {
    this.expenseService.deleteExpense(id).subscribe(() => {
      this.loadExpenses();
      this.loadSummary();
    });
  }
}
