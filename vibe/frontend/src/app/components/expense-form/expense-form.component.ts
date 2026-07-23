import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Expense } from '../../models/expense.model';

@Component({
  selector: 'app-expense-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './expense-form.component.html',
  styleUrls: ['./expense-form.component.css']
})
export class ExpenseFormComponent implements OnChanges {
  @Input() expenseToEdit: Expense | null = null;
  @Output() save = new EventEmitter<Expense>();
  @Output() cancelEdit = new EventEmitter<void>();

  expense: Expense = this.getEmptyExpense();
  isEditing = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['expenseToEdit'] && this.expenseToEdit) {
      this.expense = { ...this.expenseToEdit };
      this.isEditing = true;
    }
  }

  onSubmit(): void {
    if (this.expense.amount && this.expense.category && this.expense.date) {
      this.save.emit({ ...this.expense });
      this.resetForm();
    }
  }

  onCancel(): void {
    this.resetForm();
    this.cancelEdit.emit();
  }

  private resetForm(): void {
    this.expense = this.getEmptyExpense();
    this.isEditing = false;
  }

  private getEmptyExpense(): Expense {
    return {
      amount: 0,
      category: '',
      date: '',
      description: ''
    };
  }
}
