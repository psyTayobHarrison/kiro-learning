import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CategorySummary } from '../../models/expense.model';

@Component({
  selector: 'app-expense-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './expense-summary.component.html',
  styleUrls: ['./expense-summary.component.css']
})
export class ExpenseSummaryComponent {
  @Input() summary: CategorySummary[] = [];

  get grandTotal(): number {
    return this.summary.reduce((sum, item) => sum + item.total, 0);
  }
}
