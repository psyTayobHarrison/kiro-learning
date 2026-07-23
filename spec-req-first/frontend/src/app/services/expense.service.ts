import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Expense, ExpenseRequest, CategorySummary } from '../models/expense.model';

@Injectable({ providedIn: 'root' })
export class ExpenseService {
  private apiUrl = '/api/expenses';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Expense[]> {
    return this.http.get<Expense[]>(this.apiUrl);
  }

  getByCategory(category: string): Observable<Expense[]> {
    const params = new HttpParams().set('category', category);
    return this.http.get<Expense[]>(this.apiUrl, { params });
  }

  getSummary(): Observable<CategorySummary[]> {
    return this.http.get<CategorySummary[]>(`${this.apiUrl}/summary`);
  }

  create(expense: ExpenseRequest): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, expense);
  }

  update(id: number, expense: ExpenseRequest): Observable<Expense> {
    return this.http.put<Expense>(`${this.apiUrl}/${id}`, expense);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
