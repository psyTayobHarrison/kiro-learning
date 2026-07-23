import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BudgetService } from '../../services/budget.service';
import { BudgetStatus } from '../../models/budget.model';

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './budget-list.component.html',
  styleUrl: './budget-list.component.css'
})
export class BudgetListComponent {
  @Input() budgets: BudgetStatus[] = [];
  @Output() budgetDeleted = new EventEmitter<void>();
  @Output() budgetEdit = new EventEmitter<BudgetStatus>();

  deleteErrors = new Map<number, string>();

  constructor(private budgetService: BudgetService) {}

  getProgressPercentage(budget: BudgetStatus): number {
    if (budget.limitAmount <= 0) {
      return 100;
    }
    return Math.min((budget.actualSpend / budget.limitAmount) * 100, 100);
  }

  isOverBudget(budget: BudgetStatus): boolean {
    return budget.remainingAmount < 0;
  }

  deleteBudget(id: number): void {
    this.deleteErrors.delete(id);
    this.budgetService.delete(id).subscribe({
      next: () => {
        this.budgetDeleted.emit();
      },
      error: (err) => {
        const message = err.error?.message || 'Failed to delete budget.';
        this.deleteErrors.set(id, message);
      }
    });
  }

  editBudget(budget: BudgetStatus): void {
    this.budgetEdit.emit(budget);
  }

  getDeleteError(id: number): string | undefined {
    return this.deleteErrors.get(id);
  }
}
