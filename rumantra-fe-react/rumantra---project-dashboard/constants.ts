import { Project, PhaseStatus } from './types';

export const CURRENT_PROJECT: Project = {
  id: 'prj_001',
  name: 'Pererenan Villa',
  location: 'Badung, Bali',
  totalCost: 'IDR 40,000,000',
  progress: 35,
  architect: {
    id: 'arch_001',
    name: 'Sarah Andhani',
    firmName: 'Studio Tropis',
    avatarUrl: 'https://picsum.photos/seed/architect/200/200',
  },
  phases: [
    {
      id: 'ph_001',
      title: 'Contract & Down Payment',
      description: 'Signing of agreement and initial deposit.',
      status: PhaseStatus.COMPLETED,
      dueDate: '2023-10-01',
      deliverables: [{ id: 'd1', title: 'Signed_Contract.pdf', type: 'PDF', url: '#', dateUploaded: '2023-10-01', version: 1 }],
      paymentStatus: 'Paid',
      amount: 'IDR 12,000,000'
    },
    {
      id: 'ph_002',
      title: 'Kick Off Meeting',
      description: 'Site analysis, moodboard finalization, and brief confirmation.',
      status: PhaseStatus.COMPLETED,
      dueDate: '2023-10-15',
      deliverables: [{ id: 'd2', title: 'Meeting_Minutes.pdf', type: 'PDF', url: '#', dateUploaded: '2023-10-15', version: 1 }],
      paymentStatus: 'Paid',
      amount: 'Included'
    },
    {
      id: 'ph_003',
      title: 'Phase 1 - 2D Drawing',
      description: 'Floor plans, elevations, and sections.',
      status: PhaseStatus.IN_REVIEW,
      dueDate: '2023-11-01',
      deliverables: [
        { 
          id: 'd3', 
          title: 'Ground_Floor_Plan_v2.pdf', 
          type: 'PDF', 
          url: '#', 
          // Updated to a 2D Floor Plan layout image
          previewUrl: 'https://images.unsplash.com/photo-1593450985552-3eb41a547285?auto=format&fit=crop&q=80&w=2000',
          dateUploaded: '2023-10-30', 
          version: 2 
        },
        { id: 'd4', title: 'Elevations_North_South.pdf', type: 'PDF', url: '#', dateUploaded: '2023-10-30', version: 1 }
      ],
      paymentStatus: 'Pending',
      amount: 'IDR 10,000,000'
    },
    {
      id: 'ph_004',
      title: 'Phase 2 - 3D Modelling',
      description: 'Interior and exterior 3D renders.',
      status: PhaseStatus.LOCKED,
      dueDate: '2023-11-20',
      deliverables: [],
      paymentStatus: 'Upcoming',
      amount: 'IDR 10,000,000'
    },
    {
      id: 'ph_005',
      title: 'Phase 3 - DED',
      description: 'Detailed Engineering Drawings for construction.',
      status: PhaseStatus.LOCKED,
      dueDate: '2023-12-10',
      deliverables: [],
      paymentStatus: 'Upcoming',
      amount: 'IDR 8,000,000'
    }
  ]
};

export const MOCK_MESSAGES = [
  { id: 'm1', senderId: 'arch_001', senderName: 'Sarah', text: 'Hi! I have uploaded the revised 2D drawings for Phase 1 based on your feedback regarding the kitchen layout.', timestamp: '10:30 AM', isMe: false },
  { id: 'm2', senderId: 'user_001', senderName: 'Me', text: 'Thanks Sarah. I will take a look at them today.', timestamp: '10:35 AM', isMe: true },
  { id: 'm3', senderId: 'arch_001', senderName: 'Sarah', text: 'Great, let me know if we can proceed to Phase 2.', timestamp: '10:36 AM', isMe: false },
];