
import { Project, Proposal, Stat, ChatMessage, Conversation } from './types';

// Consistent mock image for the prototype
const MOCK_IMAGE_URL = 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?q=80&w=2000&auto=format&fit=crop';

export const MOCK_PROJECTS: Project[] = [
  {
    id: '1',
    title: 'Pererenan Commercial Plot',
    description: 'Looking for an architect to design a commercial space on my vacant land. The commercial space covers only the front part, as the back is intended for a private villa.',
    location: 'Pererenan, Bali',
    budget: 'IDR 500 Million',
    status: 'Open',
    proposalsCount: 12,
    postedDate: '2 days ago',
    type: 'New Build',
    imageUrl: MOCK_IMAGE_URL,
    expectedDuration: '3 - 5 Months',
    deliverables: ['Design Concept', '3D Visualization', 'Construction Drawings', 'Permit Assistance'],
    lotType: 'Corner Lot (Hook)'
  },
  {
    id: '2',
    title: 'Urban Loft Warehouse Conversion',
    description: 'Total redesign of an industrial warehouse space into a luxury 3-bedroom family home. Requires high ceilings and mezzanine levels.',
    location: 'South Jakarta',
    budget: 'IDR 3.5B - IDR 6B',
    status: 'In Progress',
    proposalsCount: 8,
    postedDate: '2 weeks ago',
    type: 'Interior',
    imageUrl: MOCK_IMAGE_URL,
    expectedDuration: '6 - 9 Months',
    deliverables: ['Interior Design', 'MEP Drawings', 'Furniture Selection'],
    lotType: 'Not Applicable'
  },
  {
    id: '3',
    title: 'Eco-Friendly Mountain Retreat',
    description: 'New build project for a holiday home in the mountains. Must be net-zero energy and blend with the surrounding landscape.',
    location: 'Ubud, Bali',
    budget: 'IDR 18B - IDR 22B',
    status: 'Open',
    proposalsCount: 4,
    postedDate: '5 hours ago',
    type: 'New Build',
    imageUrl: MOCK_IMAGE_URL,
    expectedDuration: '12 - 18 Months',
    deliverables: ['Full Architectural Services', 'Landscape Design', 'Sustainability Analysis'],
    lotType: 'Flag Lot'
  }
];

export const MOCK_PROPOSALS: Proposal[] = [
  {
    id: 'p1',
    architectName: 'Sarah Chen',
    projectId: '1',
    projectTitle: 'Pererenan Commercial Plot',
    bidAmount: 'IDR 400 Million',
    estimatedDuration: '4 weeks',
    status: 'Pending',
    submittedDate: '1 day ago',
    architectRating: 4.9,
    architectExperience: '8 years',
    coverLetter: 'I specialize in Nordic minimalist extensions. My approach focuses on maximizing natural light while ensuring structural integrity. I have worked on 3 similar projects in Bandung.',
    tags: ['Sustainable', 'Nordic Style', 'Renovation Expert'],
    features: ['3D Visualization', 'Construction Drawings', 'Weekly Site Visits', 'Material Sourcing'],
    proposalImages: [
      MOCK_IMAGE_URL,
      MOCK_IMAGE_URL
    ],
    proposalPdf: 'project_proposal_schen.pdf',
    iaiCertified: true
  },
  {
    id: 'p2',
    architectName: 'Marcus Aurelius Design',
    projectId: '1',
    projectTitle: 'Pererenan Commercial Plot',
    bidAmount: 'IDR 750 Million',
    estimatedDuration: '6 weeks',
    status: 'Pending',
    submittedDate: '4 hours ago',
    architectRating: 4.7,
    architectExperience: '15 years',
    coverLetter: 'Our firm offers a premium full-service package. We propose a slightly longer timeline to ensure high-quality wood sourcing and precise joinery details that define a luxury aesthetic.',
    tags: ['Luxury', 'Full Service', 'Award Winning'],
    features: ['3D Visualization', 'Permit Assistance', 'Interior Concept', 'VR Walkthrough'],
    proposalImages: [
      MOCK_IMAGE_URL,
      MOCK_IMAGE_URL,
      MOCK_IMAGE_URL
    ],
    proposalPdf: 'mad_luxury_quote_v2.pdf',
    architectType: 'Firm',
    iaiCertified: true
  },
  {
    id: 'p3',
    architectName: 'Budi Santoso Architects',
    projectId: '1',
    projectTitle: 'Pererenan Commercial Plot',
    bidAmount: 'IDR 500 Million',
    estimatedDuration: '3 weeks',
    status: 'Pending',
    submittedDate: '2 days ago',
    architectRating: 4.5,
    architectExperience: '5 years',
    coverLetter: 'Efficient and cost-effective design solutions. We can deliver the main drawings quickly so you can start construction sooner.',
    tags: ['Fast Turnaround', 'Budget Friendly', 'Modern'],
    features: ['Design Concept', 'Construction Drawings'],
    proposalImages: [
      MOCK_IMAGE_URL
    ],
    proposalPdf: 'budi_santoso_estimate.pdf',
    iaiCertified: false
  }
];

export const MOCK_CHATS: Record<string, ChatMessage[]> = {
  'p1': [
    { id: 'm1', senderId: 'u1', senderName: 'Alex', text: 'Hi Sarah, I saw your proposal for the Pererenan plot. Could you clarify the permit handling?', timestamp: '10:30 AM', isMe: true },
    { id: 'm2', senderId: 'a1', senderName: 'Sarah Chen', text: 'Hi Alex! Yes, absolutely. My fee includes coordination with the local banjar and handling the initial IMB submission.', timestamp: '10:45 AM', isMe: false },
    { id: 'm3', senderId: 'a1', senderName: 'Sarah Chen', text: 'I have attached a similar permit timeline from my last project in the area.', timestamp: '10:46 AM', isMe: false, attachments: [{ type: 'file', name: 'Permit_Process_Bali.pdf', url: '#' }] },
    { id: 'm4', senderId: 'u1', senderName: 'Alex', text: 'That sounds perfect. I am also interested in the Nordic style you mentioned. Do you have more facade examples?', timestamp: '11:05 AM', isMe: true },
  ],
  'p2': [
    { id: 'm1', senderId: 'a2', senderName: 'Marcus', text: 'Alex, we would love to schedule a VR walkthrough of our initial concept for you.', timestamp: 'Yesterday', isMe: false },
  ]
};

export const MOCK_CONVERSATIONS: Conversation[] = [
  { id: 'p1', participantName: 'Sarah Chen', participantRole: 'architect', lastMessage: 'Do you have more facade examples?', unreadCount: 0, projectId: '1' },
  { id: 'p2', participantName: 'Marcus Aurelius', participantRole: 'architect', lastMessage: 'VR walkthrough scheduled for Friday.', unreadCount: 2, projectId: '1' },
];

export const HOMEOWNER_STATS: Stat[] = [
  { label: 'Active Projects', value: '2', trend: '+1 this month', trendUp: true },
  { label: 'Total Proposals', value: '16', trend: '4 new today', trendUp: true },
  { label: 'Budget Used', value: '15%', trend: 'On track', trendUp: true },
];

export const ARCHITECT_STATS: Stat[] = [
  { label: 'Active Bids', value: '5', trend: '2 ending soon', trendUp: true },
  { label: 'Projects Won', value: '12', trend: '+15% vs last year', trendUp: true },
  { label: 'Profile Views', value: '1.2k', trend: '+8% this week', trendUp: true },
  { label: 'Revenue', value: 'IDR 1.2B', trend: 'YTD', trendUp: true },
];

// --- CONSTANTS FOR WIZARD ---

export const PROJECT_MAIN_CATEGORIES = [
  'New Build',
  'Renovation, Extension, Interior'
];

export const PROJECT_CATEGORIES = [
  'Residential',
  'Commercial',
  'Industrial',
  'Institutional, Cultural, Religious',
  'Interior Only',
  'Landscape',
  'Infrastructure / Utilities',
  'Other / Mixed Use'
];

export const PROJECT_SUB_CATEGORIES: Record<string, string[]> = {
  'Residential': [
    'Private House', 'Villa', 'Apartment Unit', 'Boarding House (Kos)', 'Townhouse', 'Dormitory', 'Shophouse (Residential)'
  ],
  'Commercial': [
    'Retail Store / Boutique', 'Office / Coworking Space', 'Shophouse (Business)', 'Showroom', 'Salon / Barbershop',
    'Clinic / Healthcare', 'Commercial Warehouse', 'Minimarket', 'Mall Booth', 'Cafe', 'Restaurant',
    'Food Court / Kiosk', 'Bar / Lounge', 'Bakery', 'Hotel', 'Guesthouse / Homestay / Hostel',
    'Airbnb Unit', 'Resort / Rental Villa', 'Gym / Fitness Center', 'Spa', 'Pharmacy', 'Laboratory'
  ],
  'Industrial': [
    'Factory / Production Facility', 'Food Processing Plant', 'Workshop', 'Packaging Plant', 'Cold Storage',
    'Utility Building', 'Waste Management Facility', 'Industrial Warehouse', 'Logistics Center'
  ],
  'Institutional, Cultural, Religious': [
    'Kindergarten', 'School', 'Learning Center', 'Library', 'Worship Facility', 'Community Center', 'Gallery / Museum', 'Government Office'
  ],
  'Infrastructure / Utilities': [
    'Park', 'Drainage', 'Road', 'Other'
  ],
  'Interior Only': ['Home Interior', 'Commercial Interior', 'Office Interior'],
  'Landscape': ['Home Garden', 'Public Park', 'Pool Area'],
  'Other / Mixed Use': ['Mixed Use', 'Other']
};

export const DESIGN_STYLES = {
  'Classic / Historical': [
    'Victorian', 'Art Deco', 'Mid-Century Modern', 'Colonial', 'Neoclassical', 'Mediterranean',
    'Tropical Classic', 'Japanese Traditional (Washitsu)', 'Classic Scandinavian'
  ],
  'Modern / Contemporary': [
    'Modern Minimalist', 'Contemporary', 'Urban Modern', 'Industrial', 'Tropical Modern',
    'Futuristic', 'High-Tech Architecture'
  ],
  'Natural / Warm': [
    'Scandinavian', 'Japandi', 'Tropical Resort', 'Rustic', 'Bohemian (Boho)',
    'Organic', 'Eco-Friendly / Sustainable'
  ]
};

export const DELIVERABLES_GROUPS = {
  'Design Development': [
    'Site Visit', 'Site Analysis Report',
    'Concept Design Package (plans, elevations, sections)',
    '3D Visualization / Render', 'Concept Diagrams',
    'Initial MEP Integration Notes', 'Initial Cost Estimate'
  ],
  'Technical': [
    'Detailed Architectural Drawings',
    'Construction Details',
    'Specifications Book',
    'MEP Coordination Drawings', 'Bill of Quantities (BoQ)'
  ],
  'Construction': [
    'Shop Drawings', 'Detailed MEP Drawings'
  ]
};

export const LOT_TYPES = [
  { label: 'Middle Lot (Normal)', description: 'Standard lot between two neighbors. Only one side faces the street.' },
  { label: 'Corner Lot (Hook)', description: 'Located at the intersection of two streets. Two sides face the street, usually brighter with better air circulation.' },
  { label: 'Cul-de-sac Lot', description: 'Located at the end of a dead-end street. Often quieter and safer.' },
  { label: 'Flag Lot', description: 'House is located behind another house with a long, narrow access driveway. More private.' },
  { label: 'T-Intersection Lot (Tusuk Sate)', description: 'Lot facing directly into a T-junction. Some avoid due to feng shui or headlight glare.' },
  { label: 'Perimeter Lot', description: 'Located at the edge of a block or adjacent to open space. Usually fewer neighbors.' },
  { label: 'Not Applicable', description: 'For apartments or interior renovations only' }
];
