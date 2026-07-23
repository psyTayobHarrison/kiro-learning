import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BudgetStatus } from '../../models/budget.model';

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './budget-list.component.html',
  styleUrl: './budget-list.component.css'
})
export class BudgetListComponent {
  @Input() budgetStatuses: BudgetStatus[] = [];

  @Output() editBudget = new EventEmitter<BudgetStatus>();
  @Output() deleteBudget = new EventEmitter<number>();

  getProgressWidth(status: BudgetStatus): number {
    if (status.limitAmount === 0) {
      return 0;
    }
    return Math.min((status.actual / status.limitAmount) * 100, 100);
  }

  isOverBudget(status: BudgetStatus): boolean {
    return status.remaining < 0;
  }

  onEdit(status: BudgetStatus): void {
    this.editBudget.emit(status);
  }

  onDelete(id: number): void {
    this.deleteBudget.emit(id);
  }
}
