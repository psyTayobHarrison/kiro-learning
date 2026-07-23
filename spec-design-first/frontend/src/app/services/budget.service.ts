import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Budget, BudgetCreate, BudgetStatus } from '../models/budget.model';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private apiUrl = 'http://localhost:8080/budgets';

  constructor(private http: HttpClient) {}

  getBudgets(): Observable<Budget[]> {
    return this.http.get<Budget[]>(this.apiUrl);
  }

  createBudget(budget: BudgetCreate): Observable<Budget> {
    return this.http.post<Budget>(this.apiUrl, budget);
  }

  updateBudget(id: number, budget: BudgetCreate): Observable<Budget> {
    return this.http.put<Budget>(`${this.apiUrl}/${id}`, budget);
  }

  deleteBudget(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getBudgetStatus(): Observable<BudgetStatus[]> {
    return this.http.get<BudgetStatus[]>(`${this.apiUrl}/status`);
  }
}
