export enum UserRole {
  CLIENT = 'CLIENT',
  ARCHITECT = 'ARCHITECT'
}

export interface Project {
  id: string;
  title: string;
  location: string;
  lotSize: number; // in m2
  buildingType: string; // e.g., "Student Housing", "Villa"
  budget: string; // Used as the primary display budget (usually Design Budget)
  totalBudget?: string; // Overall construction + design budget
  designBudget?: string; // Specific architect fee
  description: string;
  deliverables?: string[]; // List of specific requested documents
  createdAt: string;
  status: 'OPEN' | 'CLOSED' | 'AWARDED';
  biddingDuration?: number; // Added based on usage
  winnerProposalId?: string; // Added based on usage
}

export interface Proposal {
  id: string;
  projectId: string;
  architectName: string;
  firmName: string;
  estimatedCost: number; // IDR
  durationMonths: number;
  conceptDescription: string;
  materialsStrategy: string;
  submittedAt: string;
  pdfUrl?: string; // Base64 data URI for the PDF file
  pdfFileName?: string;
  pdfExtractedText?: string;
  coverImage?: string; // Base64 data URI for cover image
  facadeImage?: string; // Base64 data URI for facade concept image
  interiorImages?: string[]; // Array of Base64 strings for interior concepts
  layoutImages?: string[]; // Array of Base64 strings for 2D layouts
}

export interface ComparisonResult {
  architectName: string;
  costScore: number; // 0-100
  timeScore: number; // 0-100
  designScore: number; // 0-100 based on AI sentiment
  summary: string;
  pros: string[];
  cons: string[];
}

export interface AnalysisResponse {
  comparison: ComparisonResult[];
  recommendation: string;
  topOptionsSummary: string;
}