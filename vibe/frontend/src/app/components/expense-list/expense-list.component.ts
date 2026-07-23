import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Expense } from '../../models/expense.model';

@Component({
  selector: 'app-expense-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './expense-list.component.html',
  styleUrls: ['./expense-list.component.css']
})
export class ExpenseListComponent {
  @Input() expenses: Expense[] = [];
  @Output() edit = new EventEmitter<Expense>();
  @Output() delete = new EventEmitter<number>();
  @Output() filterChange = new EventEmitter<string>();

  filterCategory = '';

  onFilterChange(): void {
    this.filterChange.emit(this.filterCategory);
  }

  onEdit(expense: Expense): void {
    this.edit.emit(expense);
  }

  onDelete(id: number): void {
    this.delete.emit(id);
  }
}
