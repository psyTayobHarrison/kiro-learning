export interface Budget {
  id: number;
  category: string;
  month: string;        // "YYYY-MM"
  limitAmount: number;
  createdAt: string;    // ISO datetime string
}

export interface BudgetCreate {
  category: string;
  month: string;        // "YYYY-MM"
  limitAmount: number;
}

export interface BudgetStatus {
  id: number;
  category: string;
  month: string;        // "YYYY-MM"
  limitAmount: number;
  actual: number;
  remaining: number;    // negative = over budget
}
