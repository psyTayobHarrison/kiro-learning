import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Expense } from '../../models/expense.model';

@Component({
  selector: 'app-expense-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './expense-list.component.html',
  styleUrl: './expense-list.component.css'
})
export class ExpenseListComponent {
  @Input() expenses: Expense[] = [];

  @Output() editExpense = new EventEmitter<Expense>();
  @Output() deleteExpense = new EventEmitter<number>();

  onEdit(expense: Expense): void {
    this.editExpense.emit(expense);
  }

  onDelete(id: number): void {
    this.deleteExpense.emit(id);
  }
}
