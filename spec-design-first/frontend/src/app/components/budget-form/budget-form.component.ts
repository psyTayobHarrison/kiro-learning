import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Budget, BudgetCreate } from '../../models/budget.model';
import { BudgetService } from '../../services/budget.service';

@Component({
  selector: 'app-budget-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './budget-form.component.html',
  styleUrl: './budget-form.component.css'
})
export class BudgetFormComponent implements OnChanges {
  @Input() budgetToEdit: Budget | null = null;
  @Output() budgetSaved = new EventEmitter<void>();

  budgetForm: FormGroup;
  errorMessage: string | null = null;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private budgetService: BudgetService
  ) {
    this.budgetForm = this.fb.group({
      category: ['', [Validators.required]],
      month: ['', [Validators.required]],
      limitAmount: [null, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['budgetToEdit']) {
      const budget = changes['budgetToEdit'].currentValue as Budget | null;
      if (budget) {
        this.isEditMode = true;
        this.budgetForm.patchValue({
          category: budget.category,
          month: budget.month,
          limitAmount: budget.limitAmount
        });
        this.errorMessage = null;
      } else {
        this.isEditMode = false;
      }
    }
  }

  onSubmit(): void {
    if (this.budgetForm.invalid) {
      this.budgetForm.markAllAsTouched();
      return;
    }

    const formValue = this.budgetForm.value;
    const payload: BudgetCreate = {
      category: formValue.category,
      month: formValue.month,
      limitAmount: formValue.limitAmount
    };

    if (this.isEditMode && this.budgetToEdit) {
      this.budgetService.updateBudget(this.budgetToEdit.id, payload).subscribe({
        next: () => {
          this.clearForm();
          this.budgetSaved.emit();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || err.message || 'An error occurred while updating the budget.';
        }
      });
    } else {
      this.budgetService.createBudget(payload).subscribe({
        next: () => {
          this.clearForm();
          this.budgetSaved.emit();
        },
        error: (err) => {
          this.errorMessage = err.error?.message || err.message || 'An error occurred while creating the budget.';
        }
      });
    }
  }

  cancelEdit(): void {
    this.clearForm();
  }

  private clearForm(): void {
    this.budgetForm.reset();
    this.isEditMode = false;
    this.budgetToEdit = null;
    this.errorMessage = null;
  }
}
