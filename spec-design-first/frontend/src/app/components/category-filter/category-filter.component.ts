import { Component, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-category-filter',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './category-filter.component.html',
  styleUrl: './category-filter.component.css'
})
export class CategoryFilterComponent {
  filterValue = '';

  @Output() filterChanged = new EventEmitter<string>();

  onFilterInput(): void {
    this.filterChanged.emit(this.filterValue.trim());
  }

  clearFilter(): void {
    this.filterValue = '';
    this.filterChanged.emit('');
  }
}
