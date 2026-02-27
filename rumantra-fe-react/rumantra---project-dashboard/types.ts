export enum PhaseStatus {
  COMPLETED = 'Completed',
  IN_PROGRESS = 'In Progress',
  IN_REVIEW = 'In Review',
  LOCKED = 'Locked',
}

export interface Deliverable {
  id: string;
  title: string;
  type: 'PDF' | 'IMAGE' | 'DOC';
  url: string;
  previewUrl?: string;
  dateUploaded: string;
  version: number;
}

export interface ProjectPhase {
  id: string;
  title: string;
  description: string;
  status: PhaseStatus;
  dueDate: string;
  deliverables: Deliverable[];
  paymentStatus: 'Paid' | 'Pending' | 'Upcoming';
  amount: string;
}

export interface Message {
  id: string;
  senderId: string;
  senderName: string;
  text: string;
  timestamp: string;
  isMe: boolean;
}

export interface Architect {
  id: string;
  name: string;
  firmName: string;
  avatarUrl: string;
}

export interface Project {
  id: string;
  name: string;
  location: string;
  totalCost: string;
  architect: Architect;
  phases: ProjectPhase[];
  progress: number;
}