import React, { useState } from 'react';
import { ARCHITECT_STATS } from '../constants';
import { Project, Proposal } from '../types';
import StatCard from './StatCard';
import ProjectCard from './ProjectCard';
import Button from './Button';
import ProjectDetailView from './ProjectDetailView';
import SubmitProposalForm from './SubmitProposalForm';
import { Search, Filter } from 'lucide-react';

interface ArchitectDashboardProps {
  projects: Project[];
  onSubmitProposal: (proposal: Proposal) => void;
}

const ArchitectDashboard: React.FC<ArchitectDashboardProps> = ({ projects, onSubmitProposal }) => {
  const [view, setView] = useState<'list' | 'detail' | 'apply'>('list');
  const [selectedProject, setSelectedProject] = useState<Project | null>(null);

  // In the marketplace view, architects typically see the newest projects first
  const marketplaceProjects = [...projects].sort((a, b) => {
     // Mock sorting: if one is "Just now", it comes first
     if (a.postedDate === 'Just now') return -1;
     if (b.postedDate === 'Just now') return 1;
     return 0;
  });

  const handleViewProject = (project: Project) => {
    setSelectedProject(project);
    setView('detail');
  };

  const handleApplyClick = () => {
    setView('apply');
  };

  const handleProposalSubmit = (proposal: Proposal) => {
    onSubmitProposal(proposal);
    // Show success message or redirect logic here
    setView('list');
    setSelectedProject(null);
    alert('Proposal submitted successfully!');
  };

  if (view === 'detail' && selectedProject) {
    return (
      <ProjectDetailView 
        project={selectedProject} 
        onBack={() => setView('list')}
        onApply={handleApplyClick}
      />
    );
  }

  if (view === 'apply' && selectedProject) {
    return (
      <SubmitProposalForm 
        project={selectedProject}
        onCancel={() => setView('detail')} // Go back to details
        onSubmit={handleProposalSubmit}
      />
    );
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
       <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Studio Dashboard</h2>
          <p className="text-gray-500 mt-1">Manage your active bids and find your next masterpiece.</p>
        </div>
        <div className="flex items-center gap-2">
           <span className="text-sm text-gray-500 mr-2">Availability: <span className="text-green-600 font-bold">Open for Work</span></span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {ARCHITECT_STATS.map((stat, idx) => (
          <StatCard key={idx} stat={stat} />
        ))}
      </div>

      <div className="space-y-6">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <h3 className="text-lg font-bold text-gray-900">Project Marketplace</h3>
          <div className="flex gap-3">
             <div className="relative">
                <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
                <input 
                  type="text" 
                  placeholder="Search projects..." 
                  className="pl-10 pr-4 py-2 bg-white border border-gray-200 rounded-full text-sm focus:outline-none focus:border-black focus:ring-1 focus:ring-black w-64"
                />
             </div>
             <Button variant="outline" size="sm" icon={<Filter size={16} />}>Filters</Button>
          </div>
        </div>
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {marketplaceProjects.map((project) => (
            <ProjectCard 
              key={project.id} 
              project={project} 
              role="architect" 
              onAction={() => handleViewProject(project)}
            />
          ))}
        </div>
      </div>
      
      <div className="bg-gray-900 rounded-2xl p-8 text-center text-white relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-full opacity-10 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]"></div>
        <div className="relative z-10 max-w-2xl mx-auto">
           <h3 className="text-2xl font-bold mb-4">Upgrade to Pro Studio</h3>
           <p className="text-gray-400 mb-8">Get priority access to high-value projects, reduced platform fees, and advanced team management tools.</p>
           <Button variant="secondary" size="lg">View Plans</Button>
        </div>
      </div>
    </div>
  );
};

export default ArchitectDashboard;