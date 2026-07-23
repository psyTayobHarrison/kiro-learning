import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { CategorySummary } from '../../models/expense.model';

@Component({
  selector: 'app-spending-summary',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './spending-summary.component.html',
  styleUrl: './spending-summary.component.css'
})
export class SpendingSummaryComponent implements OnChanges {
  @Input() summaries: CategorySummary[] = [];

  sortedSummaries: CategorySummary[] = [];

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['summaries']) {
      this.sortedSummaries = [...this.summaries].sort((a, b) =>
        a.category.localeCompare(b.category)
      );
    }
  }
}
