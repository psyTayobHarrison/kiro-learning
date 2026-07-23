import { Component, EventEmitter, Input, Output, OnChanges, OnInit, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { BudgetService } from '../../services/budget.service';
import { BudgetRequest, BudgetStatus } from '../../models/budget.model';

@Component({
  selector: 'app-budget-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './budget-form.component.html',
  styleUrl: './budget-form.component.css'
})
export class BudgetFormComponent implements OnChanges, OnInit, OnDestroy {
  @Input() editBudget: BudgetStatus | null = null;
  @Output() budgetCreated = new EventEmitter<void>();
  @Output() budgetUpdated = new EventEmitter<void>();

  budgetForm: FormGroup;
  apiError = '';
  isSubmitting = false;
  submitted = false;

  private valueChangesSubscription: Subscription | null = null;

  constructor(
    private fb: FormBuilder,
    private budgetService: BudgetService
  ) {
    this.budgetForm = this.fb.group({
      category: ['', [Validators.required, Validators.maxLength(50)]],
      month: ['', [Validators.required]],
      limitAmount: [null, [Validators.required, Validators.min(0.01)]]
    });
  }

  ngOnInit(): void {
    this.valueChangesSubscription = this.budgetForm.valueChanges.subscribe(() => {
      this.apiError = '';
    });
  }

  ngOnDestroy(): void {
    this.valueChangesSubscription?.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['editBudget'] && this.editBudget) {
      this.budgetForm.patchValue({
        category: this.editBudget.category,
        month: this.editBudget.month,
        limitAmount: this.editBudget.limitAmount
      });
      this.submitted = false;
      this.apiError = '';
    }
  }

  get isEditMode(): boolean {
    return this.editBudget !== null;
  }

  onSubmit(): void {
    this.submitted = true;

    if (this.budgetForm.invalid) {
      this.budgetForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.apiError = '';

    const request: BudgetRequest = {
      category: this.budgetForm.value.category.trim(),
      month: this.budgetForm.value.month,
      limitAmount: this.budgetForm.value.limitAmount
    };

    if (this.isEditMode) {
      this.budgetService.update(this.editBudget!.id, request).subscribe({
        next: () => {
          this.isSubmitting = false;
          this.budgetUpdated.emit();
        },
        error: (err) => {
          this.isSubmitting = false;
          this.apiError = this.extractErrorMessage(err);
        }
      });
    } else {
      this.budgetService.create(request).subscribe({
        next: () => {
          this.budgetForm.reset();
          this.submitted = false;
          this.isSubmitting = false;
          this.budgetCreated.emit();
        },
        error: (err) => {
          this.isSubmitting = false;
          this.apiError = this.extractErrorMessage(err);
        }
      });
    }
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.budgetForm.get(fieldName);
    return !!(field && field.invalid && (field.touched || this.submitted));
  }

  private extractErrorMessage(err: any): string {
    if (err.error?.message) {
      return err.error.message;
    } else if (err.error?.errors?.length) {
      return err.error.errors.join(', ');
    }
    return 'Something went wrong, please try again.';
  }
}
