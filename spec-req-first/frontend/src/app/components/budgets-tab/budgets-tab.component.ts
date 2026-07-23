import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BudgetFormComponent } from '../budget-form/budget-form.component';
import { BudgetListComponent } from '../budget-list/budget-list.component';
import { BudgetService } from '../../services/budget.service';
import { BudgetStatus } from '../../models/budget.model';

@Component({
  selector: 'app-budgets-tab',
  standalone: true,
  imports: [CommonModule, BudgetFormComponent, BudgetListComponent],
  templateUrl: './budgets-tab.component.html',
  styleUrl: './budgets-tab.component.css'
})
export class BudgetsTabComponent implements OnInit {
  budgets: BudgetStatus[] = [];
  editBudget: BudgetStatus | null = null;
  error = '';

  constructor(private budgetService: BudgetService) {}

  ngOnInit(): void {
    this.loadBudgetStatus();
  }

  loadBudgetStatus(): void {
    this.error = '';
    this.budgetService.getStatus().subscribe({
      next: (data) => {
        this.budgets = data;
      },
      error: () => {
        this.error = 'Failed to load budget data.';
      }
    });
  }

  onBudgetCreated(): void {
    this.loadBudgetStatus();
  }

  onBudgetUpdated(): void {
    this.editBudget = null;
    this.loadBudgetStatus();
  }

  onBudgetDeleted(): void {
    this.loadBudgetStatus();
  }

  onBudgetEdit(budget: BudgetStatus): void {
    this.editBudget = budget;
  }
}
