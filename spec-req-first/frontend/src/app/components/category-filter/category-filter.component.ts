import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-category-filter',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './category-filter.component.html',
  styleUrl: './category-filter.component.css'
})
export class CategoryFilterComponent {
  @Input() categories: string[] = [];
  @Output() categorySelected = new EventEmitter<string | null>();

  selectedCategory: string = '';

  onCategoryChange(): void {
    this.categorySelected.emit(this.selectedCategory || null);
  }
}
