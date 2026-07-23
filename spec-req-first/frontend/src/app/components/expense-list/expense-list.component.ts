import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ExpenseService } from '../../services/expense.service';
import { Expense, ExpenseRequest } from '../../models/expense.model';

@Component({
  selector: 'app-expense-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './expense-list.component.html',
  styleUrl: './expense-list.component.css'
})
export class ExpenseListComponent {
  @Input() expenses: Expense[] = [];
  @Output() expenseDeleted = new EventEmitter<void>();
  @Output() expenseUpdated = new EventEmitter<void>();

  editingExpenseId: number | null = null;
  editForm: FormGroup;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private expenseService: ExpenseService
  ) {
    this.editForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(0.01), Validators.max(999999999.99)]],
      category: ['', [Validators.required, Validators.maxLength(50)]],
      date: ['', [Validators.required]],
      description: ['', [Validators.maxLength(255)]]
    });
  }

  deleteExpense(id: number): void {
    this.expenseService.delete(id).subscribe({
      next: () => {
        this.expenseDeleted.emit();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to delete expense.';
      }
    });
  }

  startEdit(expense: Expense): void {
    this.editingExpenseId = expense.id;
    this.errorMessage = '';
    this.editForm.setValue({
      amount: expense.amount,
      category: expense.category,
      date: expense.date,
      description: expense.description || ''
    });
  }

  cancelEdit(): void {
    this.editingExpenseId = null;
    this.editForm.reset();
    this.errorMessage = '';
  }

  saveEdit(id: number): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const request: ExpenseRequest = {
      amount: this.editForm.value.amount,
      category: this.editForm.value.category.trim(),
      date: this.editForm.value.date,
      description: this.editForm.value.description?.trim() || undefined
    };

    this.expenseService.update(id, request).subscribe({
      next: () => {
        this.editingExpenseId = null;
        this.editForm.reset();
        this.expenseUpdated.emit();
      },
      error: (err) => {
        if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else if (err.error?.errors?.length) {
          this.errorMessage = err.error.errors.join(', ');
        } else {
          this.errorMessage = 'Failed to update expense.';
        }
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.editForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }
}
