import React, { useState } from 'react';
import { 
  Bell, 
  ChevronRight, 
  Clock, 
  CheckCircle2, 
  Circle, 
  FileText, 
  MessageSquare,
  MoreHorizontal
} from 'lucide-react';
import { Sidebar } from './components/Sidebar';
import { Button } from './components/Button';
import { DeliverableReview } from './components/DeliverableReview';
import { ChatPanel } from './components/ChatPanel';
import { CURRENT_PROJECT } from './constants';
import { PhaseStatus } from './types';

function App() {
  const [activePhaseId, setActivePhaseId] = useState<string>('ph_003'); // Default to Phase 1 (Active)
  const [isChatOpen, setIsChatOpen] = useState(false);

  const activePhase = CURRENT_PROJECT.phases.find(p => p.id === activePhaseId) || CURRENT_PROJECT.phases[0];

  const getStatusColor = (status: PhaseStatus) => {
    switch (status) {
      case PhaseStatus.COMPLETED: return 'bg-green-100 text-green-700';
      case PhaseStatus.IN_REVIEW: return 'bg-amber-100 text-amber-700';
      case PhaseStatus.IN_PROGRESS: return 'bg-blue-100 text-blue-700';
      default: return 'bg-gray-100 text-gray-500';
    }
  };

  const getStatusIcon = (status: PhaseStatus) => {
    if (status === PhaseStatus.COMPLETED) return <CheckCircle2 size={16} />;
    if (status === PhaseStatus.LOCKED) return <Circle size={16} className="text-gray-300" />;
    return <Clock size={16} />;
  };

  // Helper to parse currency strings (e.g., "IDR 12,000,000" -> 12000000)
  const parseAmount = (amountStr: string) => {
    if (amountStr === 'Included') return 0;
    return parseInt(amountStr.replace(/[^0-9]/g, ''), 10);
  };

  const formatCurrencySimple = (amount: number) => {
    return "IDR " + (amount / 1000000).toFixed(1) + "M";
  };

  const paidAmount = CURRENT_PROJECT.phases
    .filter(p => p.paymentStatus === 'Paid')
    .reduce((acc, curr) => acc + parseAmount(curr.amount), 0);

  const totalAmount = parseAmount(CURRENT_PROJECT.totalCost);
  const pendingAmount = totalAmount - paidAmount;

  return (
    <div className="min-h-screen bg-gray-50 flex font-sans">
      <Sidebar />

      {/* Main Content */}
      <main className="flex-1 md:ml-64 p-4 lg:p-8 overflow-x-hidden">
        
        {/* Header */}
        <header className="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-4">
          <div>
            <div className="flex items-center text-sm text-gray-500 mb-1 space-x-2">
              <span>Projects</span>
              <ChevronRight size={14} />
              <span className="text-gray-900 font-medium">{CURRENT_PROJECT.name}</span>
            </div>
            <h2 className="text-3xl font-bold text-gray-900 tracking-tight">Project Dashboard</h2>
          </div>
          
          <div className="flex items-center space-x-4">
             <Button 
                variant="outline" 
                size="sm" 
                icon={<MessageSquare size={16} />}
                onClick={() => setIsChatOpen(true)}
             >
               Chat with Architect
             </Button>
            <button className="relative p-2 text-gray-500 hover:bg-white hover:text-gray-900 rounded-full transition-colors">
              <Bell size={20} />
              <span className="absolute top-2 right-2 w-2 h-2 bg-red-500 rounded-full border border-gray-50"></span>
            </button>
            <div className="h-8 w-8 rounded-full bg-gray-900 text-white flex items-center justify-center font-semibold text-sm">
                JD
            </div>
          </div>
        </header>

        {/* Project Overview Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
            <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col justify-between">
                <div>
                    <p className="text-sm font-medium text-gray-500 mb-1">Total Budget</p>
                    <p className="text-2xl font-bold text-gray-900">{CURRENT_PROJECT.totalCost}</p>
                </div>
                <div className="mt-4 pt-4 border-t border-gray-50 flex justify-between items-center text-xs text-gray-500">
                    <span>Paid: {formatCurrencySimple(paidAmount)}</span>
                    <span>Pending: {formatCurrencySimple(pendingAmount)}</span>
                </div>
            </div>

            <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col justify-between">
                <div>
                    <p className="text-sm font-medium text-gray-500 mb-1">Project Progress</p>
                    <div className="flex items-baseline space-x-2">
                        <p className="text-2xl font-bold text-gray-900">{CURRENT_PROJECT.progress}%</p>
                        <span className="text-sm text-green-600 font-medium">On Track</span>
                    </div>
                </div>
                <div className="mt-4 w-full bg-gray-100 rounded-full h-1.5 overflow-hidden">
                    <div className="bg-gray-900 h-1.5 rounded-full" style={{ width: `${CURRENT_PROJECT.progress}%` }}></div>
                </div>
            </div>

            <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm flex flex-col justify-between">
                <div>
                     <p className="text-sm font-medium text-gray-500 mb-1">Current Phase</p>
                     <p className="text-xl font-bold text-gray-900 truncate">{activePhase.title}</p>
                </div>
                 <div className="mt-4 pt-4 border-t border-gray-50 flex justify-between items-center">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(activePhase.status)}`}>
                        {activePhase.status}
                    </span>
                    <span className="text-xs text-gray-500">Due {activePhase.dueDate}</span>
                </div>
            </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
            {/* Left: Phase Timeline */}
            <div className="lg:col-span-4">
                <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
                    <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                        <h3 className="font-bold text-gray-900">Project Timeline</h3>
                        <button className="text-gray-400 hover:text-gray-600"><MoreHorizontal size={20}/></button>
                    </div>
                    <div className="p-2">
                        {CURRENT_PROJECT.phases.map((phase, index) => {
                             const isLast = index === CURRENT_PROJECT.phases.length - 1;
                             const isActive = phase.id === activePhaseId;
                             
                             return (
                                <div 
                                    key={phase.id} 
                                    onClick={() => setActivePhaseId(phase.id)}
                                    className={`relative flex items-start p-4 cursor-pointer transition-all rounded-xl hover:bg-gray-50 ${isActive ? 'bg-gray-50 ring-1 ring-gray-200' : ''}`}
                                >
                                    <div className="flex flex-col items-center mr-4">
                                        <div className={`
                                            w-8 h-8 rounded-full flex items-center justify-center border-2 z-10 bg-white
                                            ${phase.status === PhaseStatus.COMPLETED ? 'border-green-500 text-green-600' : 
                                              phase.status === PhaseStatus.IN_REVIEW ? 'border-amber-500 text-amber-600' : 
                                              phase.status === PhaseStatus.LOCKED ? 'border-gray-200 text-gray-300' : 'border-gray-900 text-gray-900'}
                                        `}>
                                            {getStatusIcon(phase.status)}
                                        </div>
                                        {!isLast && <div className="w-0.5 h-full bg-gray-200 absolute top-8 left-[27px] -z-0"></div>}
                                    </div>
                                    <div className="flex-1 pt-1">
                                        <div className="flex justify-between items-start mb-1">
                                            <h4 className={`text-sm font-semibold ${phase.status === PhaseStatus.LOCKED ? 'text-gray-400' : 'text-gray-900'}`}>{phase.title}</h4>
                                            {phase.status === PhaseStatus.COMPLETED && <span className="text-xs text-gray-400">Done</span>}
                                        </div>
                                        <p className="text-xs text-gray-500 line-clamp-2 mb-2">{phase.description}</p>
                                        {isActive && (
                                            <div className="mt-2 text-xs font-medium text-gray-900 flex items-center">
                                                <span className={`w-2 h-2 rounded-full mr-2 ${phase.paymentStatus === 'Paid' ? 'bg-green-500' : 'bg-gray-300'}`}></span>
                                                Payment: {phase.paymentStatus}
                                            </div>
                                        )}
                                    </div>
                                </div>
                             );
                        })}
                    </div>
                </div>
            </div>

            {/* Right: Deliverables & Action Area */}
            <div className="lg:col-span-8">
                <div className="mb-6 flex items-center justify-between">
                    <div>
                        <h3 className="text-xl font-bold text-gray-900">{activePhase.title}</h3>
                        <p className="text-gray-500 text-sm">Review deliverables and manage approval.</p>
                    </div>
                    {/* Status Badge Large */}
                    <div className={`px-4 py-2 rounded-full font-medium text-sm flex items-center shadow-sm ${getStatusColor(activePhase.status)}`}>
                         {activePhase.status === PhaseStatus.IN_REVIEW && <span className="w-2 h-2 bg-amber-500 rounded-full mr-2 animate-pulse"></span>}
                         {activePhase.status}
                    </div>
                </div>

                {/* Content Area */}
                {activePhase.deliverables.length > 0 ? (
                    <div className="space-y-6">
                        {/* Tab-like switcher for deliverables if multiple (Simplified for mockup to show first) */}
                        <div className="flex space-x-2 overflow-x-auto pb-2">
                             {activePhase.deliverables.map((d, i) => (
                                 <button key={d.id} className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${i === 0 ? 'bg-gray-900 text-white' : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'}`}>
                                     {d.title}
                                 </button>
                             ))}
                        </div>

                        {/* The Preview Component */}
                        <DeliverableReview 
                            deliverable={activePhase.deliverables[0]} 
                            phaseStatus={activePhase.status}
                        />

                    </div>
                ) : (
                    <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center h-[400px] flex flex-col items-center justify-center">
                        <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4 text-gray-400">
                             <FileText size={32} />
                        </div>
                        <h3 className="text-lg font-medium text-gray-900">No deliverables yet</h3>
                        <p className="text-gray-500 max-w-sm mt-2">The architect has not uploaded any documents for this phase yet. Please check back later or send a message.</p>
                        <Button variant="outline" className="mt-6" onClick={() => setIsChatOpen(true)}>
                            Message Architect
                        </Button>
                    </div>
                )}
            </div>
        </div>

      </main>

      {/* Floating Chat Panel */}
      <ChatPanel isOpen={isChatOpen} onClose={() => setIsChatOpen(false)} />
    </div>
  );
}

export default App;