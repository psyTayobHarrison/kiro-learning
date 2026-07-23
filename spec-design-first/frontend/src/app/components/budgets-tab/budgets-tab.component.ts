import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BudgetFormComponent } from '../budget-form/budget-form.component';
import { BudgetListComponent } from '../budget-list/budget-list.component';
import { BudgetService } from '../../services/budget.service';
import { Budget, BudgetStatus } from '../../models/budget.model';

@Component({
  selector: 'app-budgets-tab',
  standalone: true,
  imports: [CommonModule, BudgetFormComponent, BudgetListComponent],
  templateUrl: './budgets-tab.component.html',
  styleUrl: './budgets-tab.component.css'
})
export class BudgetsTabComponent implements OnInit {
  budgetStatuses: BudgetStatus[] = [];
  budgetToEdit: Budget | null = null;

  constructor(private budgetService: BudgetService) {}

  ngOnInit(): void {
    this.loadBudgetStatuses();
  }

  onBudgetSaved(): void {
    this.loadBudgetStatuses();
  }

  onEditBudget(status: BudgetStatus): void {
    this.budgetToEdit = {
      id: status.id,
      category: status.category,
      month: status.month,
      limitAmount: status.limitAmount,
      createdAt: ''
    };
  }

  onDeleteBudget(id: number): void {
    this.budgetService.deleteBudget(id).subscribe({
      next: () => {
        this.loadBudgetStatuses();
      }
    });
  }

  private loadBudgetStatuses(): void {
    this.budgetService.getBudgetStatus().subscribe({
      next: (statuses) => {
        this.budgetStatuses = statuses;
      }
    });
  }
}
