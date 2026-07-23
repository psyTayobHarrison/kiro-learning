export interface Expense {
  id: number;
  amount: number;
  category: string;
  date: string;        // ISO 8601 date string (YYYY-MM-DD)
  description?: string;
  createdAt: string;   // ISO 8601 timestamp
}

export interface ExpenseRequest {
  amount: number;
  category: string;
  date: string;
  description?: string;
}

export interface CategorySummary {
  category: string;
  totalAmount: number;
}

export interface ErrorResponse {
  status: number;
  message: string;
  errors?: string[];
}
