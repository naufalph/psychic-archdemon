
export type UserRole = 'homeowner' | 'architect';

export interface Project {
  id: string;
  title: string;
  description: string;
  location: string;
  budget: string;
  status: 'Open' | 'In Progress' | 'Completed' | 'Draft' | 'Contracting';
  proposalsCount: number;
  postedDate: string;
  type: 'Renovation' | 'New Build' | 'Interior' | 'Landscape';
  imageUrl?: string;
  winningProposalId?: string; // New field
  // New fields for comparison context
  expectedDuration?: string;
  deliverables?: string[];
  landArea?: string;
  landFrontage?: string;
  landDepth?: string;
  lotType?: string;
  locationMapLink?: string;
}

export interface Proposal {
  id: string;
  architectName: string;
  architectAvatar?: string;
  projectId: string;
  projectTitle: string;
  bidAmount: string;
  estimatedDuration: string;
  status: 'Pending' | 'Accepted' | 'Rejected';
  submittedDate: string;
  // Extended fields for comparison
  architectRating?: number;
  architectExperience?: string;
  coverLetter?: string;
  tags?: string[];
  features?: string[];
  // Visuals and docs
  proposalImages?: string[];
  proposalPdf?: string;
  
  // New Submission Fields
  architectType?: 'Freelancer' | 'Firm';
  iaiCertified?: boolean;
  revisions?: string;
  availabilityStart?: string;
}

export interface Stat {
  label: string;
  value: string;
  trend?: string;
  trendUp?: boolean;
}

export interface ChatMessage {
  id: string;
  senderId: string;
  senderName: string;
  text: string;
  timestamp: string;
  isMe: boolean;
  attachments?: { type: 'image' | 'file'; url: string; name: string }[];
}

export interface Conversation {
  id: string;
  participantName: string;
  participantAvatar?: string;
  participantRole: UserRole;
  lastMessage: string;
  unreadCount: number;
  projectId?: string;
}

// New types for Project Creation Wizard
export interface ProjectDraft {
  // Step 1
  title: string;
  description: string;
  location: string;
  
  // Step 2
  landArea: string;
  landFrontage: string;
  landDepth: string;
  buildArea: string;
  floors: string;
  lotType: string;
  locationMapLink?: string;
  
  // Step 1 (swapped)
  mainCategory: string; // Level 1
  category: string; // Level 2
  subCategory: string; // Level 3
  
  // Step 3
  designPreferences: string[];
  contactName: string;
  contactPhone: string;
  startDate: string;
  deliverables: string[];
}
