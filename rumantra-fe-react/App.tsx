import React, { useState } from 'react';
import { UserRole, Project, Proposal } from './types';
import ProjectForm from './components/ProjectForm';
import ProposalForm from './components/ProposalForm';
import ProposalAnalysis from './components/ProposalAnalysis';
import { 
  Building2, 
  Users, 
  Briefcase, 
  MapPin, 
  Plus, 
  LayoutDashboard, 
  ChevronRight,
  Clock,
  CheckSquare,
  Timer,
  Trophy,
  CheckCircle
} from 'lucide-react';

// --- MOCK DATA FOR DEMO ---
const MOCK_PROJECTS: Project[] = [
  {
    id: 'p1',
    title: 'Eco-Friendly Student Housing',
    location: 'Yogyakarta, Sleman',
    lotSize: 500,
    buildingType: 'Student Housing',
    budget: 'IDR 150 Million', // Design Budget
    totalBudget: 'IDR 2.5 Billion',
    designBudget: 'IDR 150 Million',
    description: 'Looking for a sustainable design for a 20-room student housing complex. Must use natural ventilation and maximize communal spaces. Target audience is university students.',
    deliverables: ['Schematic Design', '3D Rendering Visualisation Exterior', 'Site Plan', 'BoQ / RAB (Bill of Quantities)'],
    biddingDuration: 14,
    createdAt: new Date().toISOString(),
    status: 'OPEN'
  },
  {
    id: 'p2',
    title: 'Modern Tropical Villa',
    location: 'Canggu, Bali',
    lotSize: 300,
    buildingType: 'Villa',
    budget: 'IDR 200 Million', // Design Budget
    totalBudget: 'IDR 4 Billion',
    designBudget: 'IDR 200 Million',
    description: 'A 3-bedroom private villa with a pool. Style should be brutalist tropical. Need high privacy but open feeling.',
    deliverables: ['Schematic Design', 'Mood Board', '3D Rendering Visualisation Exterior', '3D Rendering Visualisation Interior', 'Architectural Detail Drawings', 'MEP (Mechanical, Electrical, Plumbing)'],
    biddingDuration: 30,
    createdAt: new Date(Date.now() - 86400000).toISOString(),
    status: 'OPEN'
  }
];

// Simple dummy PDF base64 for testing view/download
const DUMMY_PDF = "data:application/pdf;base64,JVBERi0xLjcKCjEgMCBvYmogICUgZW50cnkgcG9pbnQKPDwKICAvVHlwZSAvQ2F0YWxvZwogIC9QYWdlcyAyIDAgUgo+PgRlbmRvYmoKCjIgMCBvYmoKPDwKICAvVHlwZSAvUGFnZXMKICAvTWVkaWFCb3ggWyAwIDAgMjAwIDIwMCBdCiAgL0NvdW50IDEKICAvS2lkcyBbIDMgMCBSIF0KPj4KZW5kb2JqCgozIDAgb2JqCjw8CiAgL1R5cGUgL1BhZ2UKICAvUGFyZW50IDIgMCBSCiAgL1Jlc291cmNlcyA8PAogICAgL0ZvbnQgPDwKICAgICAgL0YxIDQgMCBSCisgICAgPj4KICA+PgogIC9Db250ZW50cyA1IDAgUgo+PgRlbmRvYmoKCjQgMCBvYmoKPDwKICAvVHlwZSAvRm9udAogIC9TdWJ0eXBlIC9UeXBlMQogIC9CYXNlRm9udCAvVGltZXMtUm9tYW4KPj4KZW5kb2JqCgo1IDAgb2JqCiAgPDwgL0xlbmd0aCA0NCA+PgpzdHJlYW0KQlQKNzAgNTAgVGQKL0YxIDEyIFRmCihIZWxsbywgV2VsY29tZSB0byBSdW1hbnRyYSEpIFRqCkVUCmVuZHN0cmVhbQRlbmRvYmoKCnhyZWYKMCA2CjAwMDAwMDAwMDAgNjU1MzUgZiAKMDAwMDAwMDAxMCAwMDAwMCBuIAowMDAwMDAwMDYwIDAwMDAwIG4gCjAwMDAwMDAxNTcgMDAwMDAgbiAKMDAwMDAwMDI1NSAwMDAwMCBuIAowMDAwMDAwMzQ0IDAwMDAwIG4gCgp0cmFpbGVyCjw8CiAgL1NpemUgNgogIC9Sb290IDEgMCBSCj4+CnN0YXJ0eHJlZgo0MTMKJSVFT0YK";

// Start with 3 demo proposals for testing
const MOCK_PROPOSALS: Proposal[] = [
  {
    id: 'prop1',
    projectId: 'p1',
    architectName: 'Eka Nugraha',
    firmName: 'Nugraha Studio',
    estimatedCost: 2400000000, // 2.4B
    durationMonths: 8,
    conceptDescription: 'A modern interpretation of the "Kost" concept using locally sourced bricks and bamboo screens for natural airflow. Focus on durability and low maintenance.',
    materialsStrategy: 'Exposed brick, bamboo facade, polished concrete floors.',
    submittedAt: new Date(Date.now() - 100000000).toISOString(),
    coverImage: 'https://images.unsplash.com/photo-1518780664697-55e3ad937233?q=80&w=1000&auto=format&fit=crop',
    facadeImage: 'https://images.unsplash.com/photo-1518780664697-55e3ad937233?q=80&w=1000&auto=format&fit=crop',
    pdfUrl: DUMMY_PDF,
    pdfFileName: "Nugraha_Proposal.pdf"
  },
  {
    id: 'prop2',
    projectId: 'p1',
    architectName: 'Sarah Wijaya',
    firmName: 'Green Space Arch',
    estimatedCost: 2650000000, // 2.65B
    durationMonths: 6,
    conceptDescription: 'Eco-tech approach featuring solar panel integration on roof and rainwater harvesting system. Modular construction for faster delivery.',
    materialsStrategy: 'Prefabricated concrete panels, steel structure, solar glass.',
    submittedAt: new Date(Date.now() - 80000000).toISOString(),
    coverImage: 'https://images.unsplash.com/photo-1580587771525-78b9dba3b91d?q=80&w=1000&auto=format&fit=crop',
    facadeImage: 'https://images.unsplash.com/photo-1580587771525-78b9dba3b91d?q=80&w=1000&auto=format&fit=crop',
    pdfUrl: DUMMY_PDF,
    pdfFileName: "GreenSpace_Proposal.pdf"
  },
  {
    id: 'prop3',
    projectId: 'p1',
    architectName: 'Budi Santoso',
    firmName: 'Urban Design Co.',
    estimatedCost: 2200000000, // 2.2B
    durationMonths: 10,
    conceptDescription: 'Community-centric layout with a central courtyard. Simple, minimalist aesthetic to keep costs low while maximizing light and air.',
    materialsStrategy: 'Conventional reinforced concrete, aluminum window frames, ceramic tiles.',
    submittedAt: new Date(Date.now() - 50000000).toISOString(),
    coverImage: 'https://images.unsplash.com/photo-1448630360428-65456885c650?q=80&w=1000&auto=format&fit=crop',
    facadeImage: 'https://images.unsplash.com/photo-1448630360428-65456885c650?q=80&w=1000&auto=format&fit=crop',
    pdfUrl: DUMMY_PDF,
    pdfFileName: "UrbanDesign_Proposal.pdf"
  }
];

function App() {
  const [role, setRole] = useState<UserRole>(UserRole.CLIENT);
  const [projects, setProjects] = useState<Project[]>(MOCK_PROJECTS);
  const [proposals, setProposals] = useState<Proposal[]>(MOCK_PROPOSALS);
  
  // Navigation State
  const [view, setView] = useState<'LIST' | 'CREATE_PROJECT' | 'PROJECT_DETAIL' | 'SUBMIT_PROPOSAL'>('LIST');
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);

  const handleCreateProject = (project: Project) => {
    setProjects([project, ...projects]);
    setView('LIST');
  };

  const handleSubmitProposal = (proposal: Proposal) => {
    setProposals([...proposals, proposal]);
    setView('LIST'); // Or back to detail
  };

  const handleSelectWinner = (projectId: string, proposalId: string) => {
    const updatedProjects = projects.map(p => 
      p.id === projectId 
        ? { ...p, status: 'AWARDED' as const, winnerProposalId: proposalId } 
        : p
    );
    setProjects(updatedProjects);
  };

  const getProposalsForProject = (pid: string) => proposals.filter(p => p.projectId === pid);

  const renderClientView = () => {
    if (view === 'CREATE_PROJECT') {
      return <ProjectForm onSubmit={handleCreateProject} onCancel={() => setView('LIST')} />;
    }

    if (view === 'PROJECT_DETAIL' && selectedProjectId) {
      const project = projects.find(p => p.id === selectedProjectId)!;
      const projectProposals = getProposalsForProject(selectedProjectId);

      return (
        <div className="space-y-8">
          <button onClick={() => setView('LIST')} className="text-sm text-neutral-500 font-medium hover:text-black transition-colors mb-2">
            &larr; Back to Dashboard
          </button>
          
          <div className="bg-white rounded-3xl p-8 shadow-sm border border-neutral-200">
            <div className="flex justify-between items-start">
              <div>
                <h2 className="text-3xl font-bold text-black tracking-tight">{project.title}</h2>
                <div className="flex gap-4 mt-3 text-neutral-500 text-sm">
                  <span className="flex items-center gap-1"><MapPin className="w-4 h-4" /> {project.location}</span>
                  <span className="flex items-center gap-1"><Briefcase className="w-4 h-4" /> {project.buildingType}</span>
                  <span className="flex items-center gap-1"><Clock className="w-4 h-4" /> Design Fee: {project.designBudget || project.budget}</span>
                </div>
              </div>
              <span className={`px-4 py-1.5 rounded-full text-xs font-medium border ${
                project.status === 'AWARDED' 
                  ? 'bg-black text-white border-black flex items-center gap-2' 
                  : 'bg-neutral-100 text-neutral-500 border-neutral-200'
              }`}>
                {project.status === 'AWARDED' && <Trophy className="w-3 h-3" />}
                {project.status}
              </span>
            </div>
            
            <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-10 border-t border-neutral-100 pt-8">
               <div>
                  <h4 className="font-semibold text-black text-sm mb-3">Description</h4>
                  <p className="text-neutral-600 text-sm leading-relaxed">{project.description}</p>
                  
                  {project.totalBudget && (
                    <div className="mt-6 pt-6 border-t border-neutral-100">
                       <h4 className="font-semibold text-neutral-400 text-xs uppercase tracking-wider mb-1">Total Construction Budget</h4>
                       <p className="text-black text-base font-medium">{project.totalBudget}</p>
                    </div>
                  )}

                  {project.biddingDuration && project.status === 'OPEN' && (
                     <div className="mt-4 pt-4 border-t border-neutral-100 flex items-center gap-2 text-neutral-600 text-sm">
                       <Timer className="w-4 h-4 text-black" />
                       <span>Bidding Window Open For: <strong className="text-black">{project.biddingDuration} Days</strong></span>
                     </div>
                  )}
               </div>

               {project.deliverables && project.deliverables.length > 0 && (
                 <div className="bg-neutral-50 p-6 rounded-2xl border border-neutral-100">
                    <h4 className="font-semibold text-black text-sm mb-4">Requested Deliverables</h4>
                    <div className="grid grid-cols-1 gap-3">
                       {project.deliverables.map((item, i) => (
                         <div key={i} className="flex items-center gap-3 text-sm text-neutral-600">
                           <div className="w-1.5 h-1.5 rounded-full bg-black flex-shrink-0"></div>
                           {item}
                         </div>
                       ))}
                    </div>
                 </div>
               )}
            </div>
          </div>

          <div>
            <h3 className="text-xl font-bold text-black mb-6 flex items-center gap-2">
              <Users className="w-5 h-5" />
              Received Proposals ({projectProposals.length})
            </h3>
            
            {projectProposals.length > 0 ? (
              <ProposalAnalysis 
                project={project} 
                proposals={projectProposals}
                onSelectWinner={project.status === 'OPEN' ? handleSelectWinner : undefined} 
              />
            ) : (
              <div className="text-center py-16 bg-white rounded-3xl border border-dashed border-neutral-300">
                <p className="text-neutral-400">No proposals received yet.</p>
              </div>
            )}
          </div>
        </div>
      );
    }

    return (
      <div className="space-y-8">
        <div className="flex justify-between items-center">
          <h2 className="text-3xl font-bold text-black tracking-tight">My Projects</h2>
          <button 
            onClick={() => setView('CREATE_PROJECT')}
            className="bg-black hover:bg-neutral-800 text-white px-6 py-3 rounded-full flex items-center gap-2 transition-all shadow-lg hover:shadow-xl"
          >
            <Plus className="w-5 h-5" /> New Project
          </button>
        </div>

        <div className="grid grid-cols-1 gap-4">
          {projects.map(project => (
            <div 
              key={project.id} 
              onClick={() => { setSelectedProjectId(project.id); setView('PROJECT_DETAIL'); }}
              className={`bg-white p-8 rounded-3xl border shadow-sm hover:shadow-md transition-all cursor-pointer group relative overflow-hidden ${
                project.status === 'AWARDED' ? 'border-black' : 'border-neutral-200'
              }`}
            >
              {project.status === 'AWARDED' && (
                <div className="absolute top-0 right-0 bg-black text-white text-[10px] font-bold px-3 py-1 rounded-bl-xl">
                  AWARDED
                </div>
              )}
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="text-xl font-bold text-black group-hover:underline decoration-2 underline-offset-4 transition-all">
                    {project.title}
                  </h3>
                  <p className="text-neutral-500 text-sm mt-1">{project.location} • {project.buildingType}</p>
                </div>
                <div className={`w-10 h-10 rounded-full flex items-center justify-center transition-colors ${
                  project.status === 'AWARDED' ? 'bg-black text-white' : 'bg-neutral-50 text-neutral-400 group-hover:bg-black group-hover:text-white'
                }`}>
                   {project.status === 'AWARDED' ? <CheckCircle className="w-5 h-5" /> : <ChevronRight className="w-5 h-5" />}
                </div>
              </div>
              <div className="mt-6 flex flex-wrap gap-3 text-xs text-neutral-600 font-medium">
                <span className="bg-neutral-100 px-3 py-1.5 rounded-full border border-neutral-200">{project.lotSize} m²</span>
                <span className="bg-neutral-100 px-3 py-1.5 rounded-full border border-neutral-200">Fee: {project.designBudget || project.budget}</span>
                <span className="bg-black text-white px-3 py-1.5 rounded-full">{getProposalsForProject(project.id).length} Proposals</span>
                {project.biddingDuration && project.status === 'OPEN' && (
                   <span className="flex items-center gap-1 text-black bg-neutral-100 px-3 py-1.5 rounded-full border border-neutral-200">
                     <Timer className="w-3 h-3" /> {project.biddingDuration} Days Left
                   </span>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderArchitectView = () => {
    if (view === 'SUBMIT_PROPOSAL' && selectedProjectId) {
      return (
        <ProposalForm 
          projectId={selectedProjectId} 
          onSubmit={handleSubmitProposal} 
          onCancel={() => setView('LIST')} 
        />
      );
    }

    return (
      <div className="space-y-8">
        <h2 className="text-3xl font-bold text-black tracking-tight">Available Opportunities</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {projects.filter(p => p.status === 'OPEN').map(project => (
             <div key={project.id} className="bg-white rounded-3xl border border-neutral-200 overflow-hidden flex flex-col shadow-sm hover:shadow-lg transition-all duration-300 group">
               <div className="h-48 bg-neutral-900 relative overflow-hidden">
                  <img 
                    src={`https://picsum.photos/seed/${project.id}/600/300`} 
                    alt="Placeholder" 
                    className="w-full h-full object-cover opacity-40 group-hover:scale-105 transition-transform duration-500"
                  />
                  <div className="absolute top-4 right-4 bg-white/90 backdrop-blur px-4 py-1.5 rounded-full text-xs font-bold text-black">
                    {project.buildingType}
                  </div>
               </div>
               <div className="p-8 flex-1 flex flex-col">
                 <h3 className="text-xl font-bold text-black mb-2">{project.title}</h3>
                 <p className="text-neutral-600 text-sm mb-6 line-clamp-2 leading-relaxed">{project.description}</p>
                 
                 {project.deliverables && project.deliverables.length > 0 && (
                    <div className="mb-6">
                       <p className="text-xs font-bold text-neutral-400 mb-2 uppercase tracking-wider">Req. Deliverables</p>
                       <div className="flex flex-wrap gap-1.5">
                          {project.deliverables.slice(0, 3).map((d, i) => (
                             <span key={i} className="text-[10px] bg-neutral-100 px-2 py-1 rounded-md text-neutral-700 border border-neutral-200 font-medium">{d}</span>
                          ))}
                          {project.deliverables.length > 3 && (
                             <span className="text-[10px] bg-neutral-100 px-2 py-1 rounded-md text-neutral-500 border border-neutral-200">+{project.deliverables.length - 3} more</span>
                          )}
                       </div>
                    </div>
                 )}
                 
                 <div className="mt-auto pt-6 border-t border-neutral-100 grid grid-cols-2 gap-3 text-xs text-neutral-500 font-medium">
                    <div className="flex items-center gap-1"><MapPin className="w-3 h-3" /> {project.location}</div>
                    <div className="flex items-center gap-1"><Users className="w-3 h-3" /> {getProposalsForProject(project.id).length} Bids</div>
                    <div className="col-span-2 font-bold text-black text-base mt-1">{project.designBudget || project.budget}</div>
                    {project.biddingDuration && (
                       <div className="col-span-2 flex items-center gap-1 text-neutral-900 text-[10px] font-bold uppercase tracking-wide">
                         <Timer className="w-3 h-3" /> Ends in {project.biddingDuration} days
                       </div>
                    )}
                 </div>

                 <button 
                  onClick={() => { setSelectedProjectId(project.id); setView('SUBMIT_PROPOSAL'); }}
                  className="w-full mt-6 py-3 bg-black hover:bg-neutral-800 text-white rounded-full text-sm font-bold transition-all transform active:scale-95"
                 >
                   Submit Proposal
                 </button>
               </div>
             </div>
          ))}
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-neutral-50 flex flex-col font-sans text-neutral-900">
      {/* Header */}
      <header className="bg-white border-b border-neutral-200 sticky top-0 z-50">
        <div className="max-w-6xl mx-auto px-6 h-20 flex items-center justify-between">
          <div className="flex items-center cursor-pointer group" onClick={() => setView('LIST')}>
            <span className="font-['Barlow'] font-black text-3xl tracking-tighter text-black group-hover:opacity-80 transition-opacity">
              rumantra.
            </span>
          </div>

          <div className="flex items-center gap-4">
            <div className="bg-neutral-100 p-1 rounded-full flex text-sm font-medium">
              <button 
                onClick={() => { setRole(UserRole.CLIENT); setView('LIST'); }}
                className={`px-5 py-2 rounded-full transition-all ${role === UserRole.CLIENT ? 'bg-white text-black shadow-sm' : 'text-neutral-500 hover:text-neutral-900'}`}
              >
                Homeowner
              </button>
              <button 
                onClick={() => { setRole(UserRole.ARCHITECT); setView('LIST'); }}
                className={`px-5 py-2 rounded-full transition-all ${role === UserRole.ARCHITECT ? 'bg-white text-black shadow-sm' : 'text-neutral-500 hover:text-neutral-900'}`}
              >
                Architect
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 max-w-6xl w-full mx-auto px-6 py-12">
        {role === UserRole.CLIENT ? renderClientView() : renderArchitectView()}
      </main>

      {/* Simple Footer */}
      <footer className="bg-white border-t border-neutral-200 mt-auto">
        <div className="max-w-6xl mx-auto px-6 py-8 text-center text-sm text-neutral-400">
          &copy; 2024 rumantra. Powered by Google Gemini AI.
        </div>
      </footer>
    </div>
  );
}

export default App;