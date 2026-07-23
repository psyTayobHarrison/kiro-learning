import { Component, Input } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { CategorySummary } from '../../models/expense.model';

@Component({
  selector: 'app-summary-view',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './summary-view.component.html',
  styleUrl: './summary-view.component.css'
})
export class SummaryViewComponent {
  @Input() summary: CategorySummary[] = [];
}
