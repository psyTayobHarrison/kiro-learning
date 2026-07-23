import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Budget, BudgetRequest, BudgetStatus } from '../models/budget.model';

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private apiUrl = '/api/budgets';

  constructor(private http: HttpClient) {}

  getStatus(): Observable<BudgetStatus[]> {
    return this.http.get<BudgetStatus[]>(`${this.apiUrl}/status`);
  }

  getAll(): Observable<Budget[]> {
    return this.http.get<Budget[]>(this.apiUrl);
  }

  create(request: BudgetRequest): Observable<Budget> {
    return this.http.post<Budget>(this.apiUrl, request);
  }

  update(id: number, request: BudgetRequest): Observable<Budget> {
    return this.http.put<Budget>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
