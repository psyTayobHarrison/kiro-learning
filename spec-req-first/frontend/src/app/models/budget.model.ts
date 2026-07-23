export interface Budget {
  id: number;
  category: string;
  month: string;         // YYYY-MM
  limitAmount: number;
  createdAt: string;     // ISO 8601 timestamp
}

export interface BudgetRequest {
  category: string;
  month: string;         // YYYY-MM
  limitAmount: number;
}

export interface BudgetStatus {
  id: number;
  category: string;
  month: string;         // YYYY-MM
  limitAmount: number;
  actualSpend: number;
  remainingAmount: number;
}
