export interface Expense {
  id?: number;
  amount: number;
  category: string;
  date: string;
  description?: string;
  createdAt?: string;
}

export interface CategorySummary {
  category: string;
  total: number;
}
