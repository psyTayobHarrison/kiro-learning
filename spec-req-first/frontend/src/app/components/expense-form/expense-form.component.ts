import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ExpenseService } from '../../services/expense.service';
import { ExpenseRequest } from '../../models/expense.model';

@Component({
  selector: 'app-expense-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './expense-form.component.html',
  styleUrl: './expense-form.component.css'
})
export class ExpenseFormComponent {
  @Output() expenseCreated = new EventEmitter<void>();

  expenseForm: FormGroup;
  errorMessage = '';
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private expenseService: ExpenseService
  ) {
    this.expenseForm = this.fb.group({
      amount: [null, [Validators.required, Validators.min(0.01), Validators.max(999999999.99)]],
      category: ['', [Validators.required, Validators.maxLength(50)]],
      date: ['', [Validators.required]],
      description: ['', [Validators.maxLength(255)]]
    });
  }

  onSubmit(): void {
    if (this.expenseForm.invalid) {
      this.expenseForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const request: ExpenseRequest = {
      amount: this.expenseForm.value.amount,
      category: this.expenseForm.value.category.trim(),
      date: this.expenseForm.value.date,
      description: this.expenseForm.value.description?.trim() || undefined
    };

    this.expenseService.create(request).subscribe({
      next: () => {
        this.expenseForm.reset();
        this.submitting = false;
        this.expenseCreated.emit();
      },
      error: (err) => {
        this.submitting = false;
        if (err.error?.message) {
          this.errorMessage = err.error.message;
        } else if (err.error?.errors?.length) {
          this.errorMessage = err.error.errors.join(', ');
        } else {
          this.errorMessage = 'Something went wrong, please try again.';
        }
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.expenseForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }
}
