
import React, { useState } from 'react';
import { Project, Proposal } from '../types';
import Button from './Button';
import { 
  ArrowLeft, MessageSquare, FileText, CreditCard, LayoutDashboard, 
  CheckCircle2, Clock, Lock, AlertCircle, Calendar, FastForward
} from 'lucide-react';
import ChatInterface from './ChatInterface';
import { MOCK_CHATS } from '../constants';

interface ProjectWorkspaceProps {
  project: Project;
  proposal: Proposal;
  onBack: () => void;
  onSkipToClosing: () => void; 
}

const ProjectWorkspace: React.FC<ProjectWorkspaceProps> = ({ project, proposal, onBack, onSkipToClosing }) => {
  const [activeTab, setActiveTab] = useState('overview');

  const phases = [
    { id: 1, title: 'Contract Finalization', status: 'waiting', date: 'Due Today' },
    { id: 2, title: 'Project Kick-off', status: 'locked', date: 'TBD' },
    { id: 3, title: 'Phase 1: Concept', status: 'locked', date: '4 Weeks' },
    { id: 4, title: 'Phase 2: Development', status: 'locked', date: '6 Weeks' },
    { id: 5, title: 'Phase 3: Technical', status: 'locked', date: '4 Weeks' },
    { id: 6, title: 'Closing & Handover', status: 'locked', date: 'TBD' },
  ];

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 duration-500 pb-20">
      
      {/* Top Navigation */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-8 border-b border-gray-200 pb-6">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={onBack} className="!p-2 rounded-full h-10 w-10 bg-white border border-gray-200 hover:bg-gray-100">
            <ArrowLeft size={20} />
          </Button>
          <div>
            <div className="flex items-center gap-2 mb-1">
               <h2 className="text-2xl font-bold text-gray-900">{project.title}</h2>
               <span className="px-2 py-0.5 rounded text-xs font-bold bg-yellow-100 text-yellow-700">In Review</span>
            </div>
            <p className="text-gray-500 text-sm flex items-center gap-2 font-medium">
               Architect: <span className="font-bold text-black">{proposal.architectName}</span>
               <span className="w-1 h-1 rounded-full bg-gray-300"></span>
               <span>Shared Workspace</span>
            </p>
          </div>
        </div>
        
        <div className="flex bg-white rounded-xl p-1 border border-gray-200 shadow-sm overflow-x-auto">
          {[
            { id: 'overview', icon: LayoutDashboard, label: 'Timeline' },
            { id: 'chats', icon: MessageSquare, label: 'Messages' },
            { id: 'files', icon: FileText, label: 'Documents' },
            { id: 'payments', icon: CreditCard, label: 'Billing' },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-6 py-2.5 rounded-lg text-xs font-bold uppercase tracking-widest transition-all whitespace-nowrap ${
                activeTab === tab.id 
                ? 'bg-black text-white shadow-md scale-105' 
                : 'text-gray-400 hover:bg-gray-50 hover:text-gray-900'
              }`}
            >
              <tab.icon size={16} />
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Main Content (Left) */}
        <div className="lg:col-span-2 space-y-8">
           
           {activeTab === 'overview' && (
             <div className="space-y-8">
                {/* Alert Banner */}
                <div className="bg-yellow-50 border border-yellow-200 rounded-3xl p-6 flex items-start gap-4 shadow-sm animate-in fade-in duration-300">
                   <div className="bg-yellow-100 p-3 rounded-2xl text-yellow-600 mt-0.5">
                      <Clock size={20} />
                   </div>
                   <div>
                      <h3 className="font-bold text-yellow-900 text-base">Waiting for Architect Signature</h3>
                      <p className="text-yellow-700 text-sm mt-1 leading-relaxed font-medium">
                         The contract terms are finalized. We are waiting for <strong>{proposal.architectName}</strong> to review and sign. The project will officially launch once verified.
                      </p>
                   </div>
                </div>

                {/* Timeline */}
                <div className="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm">
                   <h3 className="text-lg font-bold text-gray-900 mb-8 tracking-tight">Project Execution Roadmap</h3>
                   <div className="relative space-y-0 mb-8">
                      {/* Vertical Line */}
                      <div className="absolute left-6 top-4 bottom-4 w-0.5 bg-gray-100"></div>

                      {phases.map((phase, idx) => {
                         const isWaiting = phase.status === 'waiting';
                         const isLocked = phase.status === 'locked';
                         
                         return (
                           <div key={idx} className={`relative pl-16 py-6 flex items-center justify-between group ${isLocked ? 'opacity-40' : ''}`}>
                              {/* Dot */}
                              <div className={`absolute left-0 top-1/2 -translate-y-1/2 w-12 h-12 rounded-2xl border-2 flex items-center justify-center bg-white z-10 transition-all ${
                                 isWaiting 
                                 ? 'border-yellow-400 text-yellow-600 shadow-lg shadow-yellow-100' 
                                 : 'border-gray-100 text-gray-300'
                              }`}>
                                 {isWaiting ? <Clock size={20} className="animate-pulse" /> : <Lock size={20} />}
                              </div>

                              <div>
                                 <h4 className={`text-base font-bold ${isWaiting ? 'text-gray-900' : 'text-gray-500'}`}>{phase.title}</h4>
                                 {isWaiting && <span className="text-[10px] text-yellow-600 font-bold uppercase tracking-widest mt-1 block">Pending Verification</span>}
                              </div>
                              
                              <div className="text-right">
                                 <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-1">Duration</span>
                                 <span className="text-sm font-bold text-gray-600">{phase.date}</span>
                              </div>
                           </div>
                         );
                      })}
                   </div>

                   <div className="border-t border-gray-50 pt-8">
                      <p className="text-[10px] text-gray-400 mb-4 uppercase tracking-widest font-black">Prototype Simulation</p>
                      <Button onClick={onSkipToClosing} variant="outline" size="sm" className="w-full border-gray-200 hover:border-black py-4 font-bold text-xs uppercase tracking-widest">
                         <FastForward size={16} /> Bypass to Handover View
                      </Button>
                   </div>
                </div>
             </div>
           )}

           {activeTab === 'chats' && (
             <ChatInterface 
               initialMessages={MOCK_CHATS[proposal.id] || []} 
               recipientName={proposal.architectName}
               recipientRole="Lead Architect"
             />
           )}

           {activeTab === 'files' && (
             <div className="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm flex flex-col items-center justify-center min-h-[400px] text-center">
                <div className="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-300 mb-4">
                  <FileText size={32} />
                </div>
                <h3 className="font-bold text-gray-900">Project Archive</h3>
                <p className="text-sm text-gray-500 max-w-xs mt-2">Design documents will appear here once the project phases commence.</p>
             </div>
           )}

           {activeTab === 'payments' && (
             <div className="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm flex flex-col items-center justify-center min-h-[400px] text-center">
                <div className="w-16 h-16 bg-gray-50 rounded-2xl flex items-center justify-center text-gray-300 mb-4">
                  <CreditCard size={32} />
                </div>
                <h3 className="font-bold text-gray-900">Financial Ledger</h3>
                <p className="text-sm text-gray-500 max-w-xs mt-2">Milestone payments and invoices will be managed in this secure area.</p>
             </div>
           )}
        </div>

        {/* Sidebar (Right) */}
        <div className="space-y-6">
           <div className="bg-white rounded-3xl border border-gray-100 p-8 shadow-sm">
              <h3 className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-6">Quick Direct Actions</h3>
              <div className="space-y-4">
                 <Button 
                   variant={activeTab === 'chats' ? 'primary' : 'outline'} 
                   className="w-full justify-start text-xs font-bold py-4 px-6" 
                   onClick={() => setActiveTab('chats')}
                   icon={<MessageSquare size={16}/>}
                 >
                   Open Conversation
                 </Button>
                 <Button variant="outline" className="w-full justify-start text-xs font-bold py-4 px-6" icon={<FileText size={16}/>}>Review Contract Draft</Button>
                 <Button variant="outline" className="w-full justify-start text-xs font-bold py-4 px-6" icon={<Calendar size={16}/>}>Request Video Call</Button>
              </div>
           </div>

           <div className="bg-gray-50 rounded-3xl border border-gray-100 p-8">
              <h3 className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-6">Financial Progress</h3>
              <div className="space-y-4 mb-8">
                 <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 font-medium">Contract Sum</span>
                    <span className="text-base font-bold text-gray-900">{proposal.bidAmount}</span>
                 </div>
                 <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 font-medium">Released Funds</span>
                    <span className="text-base font-bold text-gray-900">IDR 0</span>
                 </div>
                 <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500 font-medium">Next Tranche</span>
                    <span className="text-base font-bold text-indigo-600">IDR 15M</span>
                 </div>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                 <div className="bg-indigo-600 h-full w-0 transition-all duration-1000"></div>
              </div>
              <p className="text-[10px] font-bold text-center text-gray-400 mt-4 uppercase tracking-widest">Escrow Protection: Active</p>
           </div>
        </div>

      </div>
    </div>
  );
};

export default ProjectWorkspace;
