import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SummaryViewComponent } from './summary-view.component';
import { CategorySummary } from '../../models/expense.model';

describe('SummaryViewComponent', () => {
  let component: SummaryViewComponent;
  let fixture: ComponentFixture<SummaryViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SummaryViewComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SummaryViewComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should show empty-state message when summary is empty', () => {
    component.summary = [];
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeTruthy();
    expect(emptyState.textContent).toContain('No expenses recorded');
  });

  it('should not show table when summary is empty', () => {
    component.summary = [];
    fixture.detectChanges();

    const table = fixture.nativeElement.querySelector('.summary-table');
    expect(table).toBeNull();
  });

  it('should display summary table with data', () => {
    const mockSummary: CategorySummary[] = [
      { category: 'Food', total: 150.5 },
      { category: 'Transport', total: 75.25 }
    ];
    component.summary = mockSummary;
    fixture.detectChanges();

    const table = fixture.nativeElement.querySelector('.summary-table');
    expect(table).toBeTruthy();

    const rows = fixture.nativeElement.querySelectorAll('.summary-table tbody tr');
    expect(rows.length).toBe(2);
  });

  it('should display category names correctly', () => {
    const mockSummary: CategorySummary[] = [
      { category: 'Food', total: 150.5 },
      { category: 'Transport', total: 75.25 }
    ];
    component.summary = mockSummary;
    fixture.detectChanges();

    const cells = fixture.nativeElement.querySelectorAll('.summary-table tbody td:first-child');
    expect(cells[0].textContent.trim()).toBe('Food');
    expect(cells[1].textContent.trim()).toBe('Transport');
  });

  it('should format totals to 2 decimal places', () => {
    const mockSummary: CategorySummary[] = [
      { category: 'Food', total: 150.5 },
      { category: 'Transport', total: 75 }
    ];
    component.summary = mockSummary;
    fixture.detectChanges();

    const cells = fixture.nativeElement.querySelectorAll('.summary-table tbody td:last-child');
    expect(cells[0].textContent.trim()).toBe('150.50');
    expect(cells[1].textContent.trim()).toBe('75.00');
  });

  it('should display table headers correctly', () => {
    const mockSummary: CategorySummary[] = [
      { category: 'Food', total: 100 }
    ];
    component.summary = mockSummary;
    fixture.detectChanges();

    const headers = fixture.nativeElement.querySelectorAll('.summary-table th');
    expect(headers[0].textContent.trim()).toBe('Category');
    expect(headers[1].textContent.trim()).toBe('Total');
  });

  it('should not show empty-state when summary has data', () => {
    component.summary = [{ category: 'Food', total: 50 }];
    fixture.detectChanges();

    const emptyState = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyState).toBeNull();
  });

  it('should update display when summary input changes', () => {
    component.summary = [];
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.empty-state')).toBeTruthy();

    component.summary = [{ category: 'Food', total: 100 }];
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.empty-state')).toBeNull();
    expect(fixture.nativeElement.querySelector('.summary-table')).toBeTruthy();
  });
});
