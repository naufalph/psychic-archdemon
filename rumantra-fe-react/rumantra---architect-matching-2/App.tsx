import React, { useState } from 'react';
import { UserRole, Project, Proposal, ProjectDraft } from './types';
import Layout from './components/Layout';
import HomeownerDashboard from './components/HomeownerDashboard';
import ArchitectDashboard from './components/ArchitectDashboard';
import { MOCK_PROJECTS, MOCK_PROPOSALS } from './constants';

const App: React.FC = () => {
  const [userRole, setUserRole] = useState<UserRole>('homeowner');
  const [projects, setProjects] = useState<Project[]>(MOCK_PROJECTS);
  const [proposals, setProposals] = useState<Proposal[]>(MOCK_PROPOSALS);

  const handleAddProject = (draft: ProjectDraft) => {
    // Consistent mock image for the prototype
    const MOCK_IMAGE_URL = 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?q=80&w=2000&auto=format&fit=crop';

    const newProject: Project = {
      id: Math.random().toString(36).substr(2, 9),
      title: draft.title,
      description: draft.description,
      location: draft.location,
      budget: 'Open Bid', // Default for new projects
      status: 'Open',
      proposalsCount: 0,
      postedDate: 'Just now',
      // Map categories to internal types if needed, or maintain consistency
      type: (draft.mainCategory === 'New Build' ? 'New Build' : 'Renovation') as any,
      imageUrl: MOCK_IMAGE_URL,
      expectedDuration: draft.startDate ? `Starts ${draft.startDate}` : 'Flexible',
      deliverables: draft.deliverables,
      landArea: draft.landArea,
      landFrontage: draft.landFrontage,
      landDepth: draft.landDepth,
      lotType: draft.lotType,
      locationMapLink: draft.locationMapLink
    };

    setProjects([newProject, ...projects]);
  };

  const handleAddProposal = (proposal: Proposal) => {
    setProposals([proposal, ...proposals]);
    
    // Update the project's proposal count
    setProjects(projects.map(p => 
      p.id === proposal.projectId 
        ? { ...p, proposalsCount: p.proposalsCount + 1 } 
        : p
    ));
  };

  const handleProposalAccepted = (proposal: Proposal) => {
    // 1. Update Project Status to 'Contracting' and set winningProposalId
    setProjects(projects.map(p => 
      p.id === proposal.projectId 
        ? { ...p, status: 'Contracting', winningProposalId: proposal.id } 
        : p
    ));

    // 2. Update Proposal Status to 'Accepted'
    setProposals(proposals.map(p => 
      p.id === proposal.id 
        ? { ...p, status: 'Accepted' } 
        : p
    ));
  };

  return (
    <Layout role={userRole} onRoleChange={setUserRole}>
      {userRole === 'homeowner' ? (
        <HomeownerDashboard 
          projects={projects} 
          proposals={proposals}
          onAddProject={handleAddProject} 
          onProposalAccepted={handleProposalAccepted}
        />
      ) : (
        <ArchitectDashboard 
          projects={projects} 
          onSubmitProposal={handleAddProposal} 
        />
      )}
    </Layout>
  );
};

export default App;