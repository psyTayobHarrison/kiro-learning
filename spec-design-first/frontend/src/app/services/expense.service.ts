import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Expense, ExpenseCreate, CategorySummary } from '../models/expense.model';

@Injectable({ providedIn: 'root' })
export class ExpenseService {
  private apiUrl = 'http://localhost:8080/expenses';

  constructor(private http: HttpClient) {}

  getExpenses(category?: string): Observable<Expense[]> {
    let params = new HttpParams();
    if (category) {
      params = params.set('category', category);
    }
    return this.http.get<Expense[]>(this.apiUrl, { params });
  }

  createExpense(expense: ExpenseCreate): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, expense);
  }

  updateExpense(id: number, expense: ExpenseCreate): Observable<Expense> {
    return this.http.put<Expense>(`${this.apiUrl}/${id}`, expense);
  }

  deleteExpense(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getSummary(): Observable<CategorySummary[]> {
    return this.http.get<CategorySummary[]>(`${this.apiUrl}/summary`);
  }
}
