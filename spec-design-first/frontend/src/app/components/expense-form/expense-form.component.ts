import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Expense, ExpenseCreate } from '../../models/expense.model';
import { ExpenseService } from '../../services/expense.service';

@Component({
  selector: 'app-expense-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './expense-form.component.html',
  styleUrl: './expense-form.component.css'
})
export class ExpenseFormComponent implements OnChanges {
  @Input() expenseToEdit: Expense | null = null;
  @Output() expenseSaved = new EventEmitter<void>();

  expenseForm: FormGroup;
  errorMessage: string | null = null;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private expenseService: ExpenseService
  ) {
    this.expenseForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(0.01)]],
      category: ['', [Validators.required]],
      date: ['', [Validators.required]],
      description: ['']
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['expenseToEdit']) {
      const expense = changes['expenseToEdit'].currentValue as Expense | null;
      if (expense) {
        this.isEditMode = true;
        this.expenseForm.patchValue({
          amount: expense.amount,
          category: expense.category,
          date: expense.date,
          description: expense.description || ''
        });
        this.errorMessage = null;
      } else {
        this.isEditMode = false;
      }
    }
  }

  onSubmit(): void {
    if (this.expenseForm.invalid) {
      this.expenseForm.markAllAsTouched();
      return;
    }

    const formValue = this.expenseForm.value;
    const payload: ExpenseCreate = {
      amount: formValue.amount,
      category: formValue.category,
      date: formValue.date,
      description: formValue.description || undefined
    };

    if (this.isEditMode && this.expenseToEdit) {
      this.expenseService.updateExpense(this.expenseToEdit.id, payload).subscribe({
        next: () => {
          this.clearForm();
          this.expenseSaved.emit();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || err.message || 'An error occurred while updating the expense.';
        }
      });
    } else {
      this.expenseService.createExpense(payload).subscribe({
        next: () => {
          this.clearForm();
          this.expenseSaved.emit();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || err.message || 'An error occurred while creating the expense.';
        }
      });
    }
  }

  cancelEdit(): void {
    this.clearForm();
  }

  private clearForm(): void {
    this.expenseForm.reset();
    this.isEditMode = false;
    this.expenseToEdit = null;
    this.errorMessage = null;
  }
}
