import React, { useState } from 'react';
import { HOMEOWNER_STATS, MOCK_PROPOSALS } from '../constants';
import { Project, ProjectDraft, Proposal } from '../types';
import StatCard from './StatCard';
import ProjectCard from './ProjectCard';
import Button from './Button';
import ProposalComparison from './ProposalComparison';
import CreateProjectWizard from './CreateProjectWizard';
import ContractView from './ContractView';
import ProjectWorkspace from './ProjectWorkspace';
import ProjectClosingView from './ProjectClosingView';
import ExploreContractors from './ExploreContractors';
import { Plus, ArrowRight, Activity } from 'lucide-react';

interface HomeownerDashboardProps {
  projects: Project[];
  proposals: Proposal[]; 
  onAddProject: (draft: ProjectDraft) => void;
  onProposalAccepted: (proposal: Proposal) => void; 
}

const HomeownerDashboard: React.FC<HomeownerDashboardProps> = ({ projects, proposals, onAddProject, onProposalAccepted }) => {
  const [view, setView] = useState<'dashboard' | 'compare' | 'create' | 'contract' | 'workspace' | 'closing' | 'contractors'>('dashboard');
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);

  const handleProjectAction = (project: Project) => {
    setSelectedProject(project);
    if (project.status === 'Contracting') {
      setView('contract');
    } else if (project.status === 'In Progress' || project.status === 'Completed') {
      setView('workspace');
    } else {
      setView('compare');
    }
  };

  const handleCreateComplete = (draft: ProjectDraft) => {
    onAddProject(draft);
    setView('dashboard');
  };

  const handleAcceptProposal = (proposal: Proposal) => {
    onProposalAccepted(proposal);
    setView('contract');
  };
  
  const handleFinalizeContract = () => {
    setView('workspace');
  };

  const handleSkipToClosing = () => {
    setView('closing');
  };
  
  const handleExploreContractors = () => {
    setView('contractors');
  };

  if (view === 'create') {
    return <CreateProjectWizard onCancel={() => setView('dashboard')} onComplete={handleCreateComplete} />;
  }

  const winningProposal = selectedProject 
    ? proposals.find(p => p.id === selectedProject.winningProposalId) || MOCK_PROPOSALS[0]
    : MOCK_PROPOSALS[0];

  if (view === 'contract' && selectedProject) {
    return (
      <ContractView 
        project={selectedProject}
        proposal={winningProposal}
        onBack={() => {
          setSelectedProject(null);
          setView('dashboard');
        }}
        onFinalize={handleFinalizeContract}
      />
    );
  }

  if (view === 'workspace' && selectedProject) {
    return (
      <ProjectWorkspace
        project={selectedProject}
        proposal={winningProposal}
        onBack={() => {
          setSelectedProject(null);
          setView('dashboard');
        }}
        onSkipToClosing={handleSkipToClosing}
      />
    );
  }

  if (view === 'closing' && selectedProject) {
    return (
      <ProjectClosingView
        project={selectedProject}
        proposal={winningProposal}
        onBack={() => {
          setView('workspace');
        }}
        onExploreContractors={handleExploreContractors}
      />
    );
  }

  if (view === 'contractors') {
    return (
      <ExploreContractors 
        onBack={() => {
          setView('closing');
        }} 
      />
    );
  }

  if (view === 'compare' && selectedProject) {
    return (
      <ProposalComparison 
        proposals={MOCK_PROPOSALS} 
        onBack={() => {
          setSelectedProject(null);
          setView('dashboard');
        }}
        project={selectedProject}
        onProposalAccepted={handleAcceptProposal}
      />
    );
  }

  return (
    <div className="space-y-10 animate-in fade-in duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Welcome, Alex</h2>
          <p className="text-gray-500 font-medium">Curating your architectural vision.</p>
        </div>
        <Button 
          icon={<Plus size={18} />} 
          onClick={() => setView('create')}
          className="shadow-lg px-8 py-4 font-bold uppercase text-xs tracking-wider"
        >
          New Project
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard stat={{ ...HOMEOWNER_STATS[0], value: projects.length.toString() }} />
        <StatCard stat={HOMEOWNER_STATS[1]} />
        <StatCard stat={HOMEOWNER_STATS[2]} />
      </div>

      <div className="space-y-6">
        <div className="flex items-center justify-between border-b border-gray-100 pb-4">
          <div className="flex items-center gap-2">
             <Activity size={20} className="text-gray-900" />
             <h3 className="text-xl font-bold text-gray-900 tracking-tight">Active Ventures</h3>
          </div>
          <button className="text-[10px] font-bold text-gray-400 uppercase tracking-widest hover:text-black">View Portfolio</button>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {projects.map((project) => (
            <ProjectCard 
              key={project.id} 
              project={project} 
              role="homeowner" 
              onAction={() => handleProjectAction(project)}
            />
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 pt-4">
        <div className="lg:col-span-2 bg-white rounded-3xl border border-gray-100 p-8 shadow-sm">
           <div className="flex items-center justify-between mb-8">
              <h3 className="text-lg font-bold text-gray-900">Recent Activity</h3>
              {projects.length > 0 && projects[0].status === 'Open' && (
                <Button 
                  variant="ghost" 
                  size="sm" 
                  onClick={() => handleProjectAction(projects[0])}
                  className="font-bold uppercase text-[10px] tracking-wider text-indigo-600 hover:bg-indigo-50"
                >
                   Review Submissions <ArrowRight size={14} className="ml-1" />
                </Button>
              )}
           </div>
           <div className="space-y-4">
             {MOCK_PROPOSALS.slice(0, 3).map((proposal) => (
               <div key={proposal.id} className="flex items-center justify-between p-4 rounded-2xl hover:bg-gray-50 border border-gray-50 transition-all group">
                 <div className="flex items-center gap-4">
                   <div className="h-12 w-12 rounded-xl bg-gray-100 flex items-center justify-center text-gray-400 font-bold text-lg shadow-inner group-hover:bg-black group-hover:text-white transition-colors">
                      {proposal.architectName.charAt(0)}
                   </div>
                   <div>
                     <h4 className="text-sm font-bold text-gray-900">{proposal.architectName}</h4>
                     <p className="text-[10px] uppercase font-bold text-gray-400 tracking-widest mt-0.5">Bid for <span className="text-gray-600">{proposal.projectTitle}</span></p>
                   </div>
                 </div>
                 <div className="text-right">
                   <div className="text-sm font-bold text-gray-900">{proposal.bidAmount}</div>
                   <div className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mt-0.5">{proposal.estimatedDuration}</div>
                 </div>
               </div>
             ))}
           </div>
        </div>

        <div className="bg-black text-white rounded-3xl p-8 flex flex-col justify-between shadow-xl">
           <div>
             <h3 className="text-xl font-bold mb-3 tracking-tight">Curated Selection</h3>
             <p className="text-gray-400 text-sm leading-relaxed">Overwhelmed by high-quality bids? Engage our design advisors to perform a professional alignment check for your lifestyle needs.</p>
           </div>
           <Button variant="secondary" className="mt-8 self-start font-bold uppercase text-[10px] tracking-widest py-3 px-8">Connect Advisor</Button>
        </div>
      </div>
    </div>
  );
};

export default HomeownerDashboard;