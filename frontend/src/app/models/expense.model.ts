export interface Expense {
  id: number;
  amount: number;
  category: string;
  date: string;        // ISO date string YYYY-MM-DD
  description?: string;
  createdAt: string;   // ISO datetime string
}

export interface ExpenseCreate {
  amount: number;
  category: string;
  date: string;
  description?: string;
}

export interface CategorySummary {
  category: string;
  total: number;
}
