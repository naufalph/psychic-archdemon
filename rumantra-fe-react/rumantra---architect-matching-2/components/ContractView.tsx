import React, { useState } from 'react';
import { Project, Proposal } from '../types';
import Button from './Button';
import { 
  ArrowLeft, FileSignature, CheckCircle2, Plus, Trash2, 
  Sparkles, ShieldCheck, MessageSquare, Download, Lock, Check 
} from 'lucide-react';
import confetti from 'canvas-confetti';

interface ContractViewProps {
  project: Project;
  proposal: Proposal;
  onBack: () => void;
  onFinalize: () => void;
}

interface Milestone {
  id: string;
  description: string;
  percentage: number;
  amount: string;
  status: 'Pending' | 'Paid';
}

const ContractView: React.FC<ContractViewProps> = ({ project, proposal, onBack, onFinalize }) => {
  // Mock formatting helper
  const formatIDR = (val: number) => `IDR ${val} Million`;
  const bidValue = parseFloat(proposal.bidAmount.replace(/[^0-9.]/g, ''));

  // State: Milestones
  const [milestones, setMilestones] = useState<Milestone[]>([
    { id: '1', description: 'Down Payment / Mobilization', percentage: 30, amount: formatIDR(bidValue * 0.3), status: 'Pending' },
    { id: '2', description: 'Design Development Complete', percentage: 40, amount: formatIDR(bidValue * 0.4), status: 'Pending' },
    { id: '3', description: 'Final Construction Drawings', percentage: 30, amount: formatIDR(bidValue * 0.3), status: 'Pending' },
  ]);

  // State: Deliverables
  const [deliverables, setDeliverables] = useState<{ text: string; isOriginal: boolean }[]>(
    (proposal.features || []).map(f => ({ text: f, isOriginal: true }))
  );
  const [newDeliverable, setNewDeliverable] = useState('');

  // State: AI Contract Generation
  const [isGenerating, setIsGenerating] = useState(false);
  const [contractReady, setContractReady] = useState(false);
  const [isFinalizing, setIsFinalizing] = useState(false);

  // Handlers
  const handleAddDeliverable = () => {
    if (newDeliverable.trim()) {
      setDeliverables([...deliverables, { text: newDeliverable, isOriginal: false }]);
      setNewDeliverable('');
    }
  };

  const handleGenerateContract = () => {
    setIsGenerating(true);
    // Simulate AI delay
    setTimeout(() => {
      setIsGenerating(false);
      setContractReady(true);
    }, 2500);
  };

  const handleFinalize = () => {
    setIsFinalizing(true);
    setTimeout(() => {
       // Trigger confetti
       confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 } });
       alert(`Terms sent to ${proposal.architectName} for review!`);
       onFinalize();
    }, 1000);
  };

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 duration-500 pb-20">
      
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center gap-4">
          <Button variant="ghost" onClick={onBack} className="!p-2 rounded-full h-10 w-10 bg-white border border-gray-200 hover:bg-gray-100">
            <ArrowLeft size={20} />
          </Button>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Contract Workspace</h2>
            <p className="text-gray-500 text-sm">Drafting agreement for <span className="font-semibold text-black">{project.title}</span></p>
          </div>
        </div>
        <div className="text-right hidden md:block">
           <p className="text-xs text-gray-500 uppercase tracking-wider font-bold">Total Contract Value</p>
           <p className="text-xl font-bold text-gray-900">{proposal.bidAmount}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* LEFT COLUMN: EDITOR */}
        <div className="lg:col-span-2 space-y-8">
          
          {/* 1. Milestones */}
          <section className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <div className="flex items-center justify-between mb-6">
               <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                 <div className="w-6 h-6 rounded-full bg-black text-white flex items-center justify-center text-xs">1</div>
                 Payment Milestones
               </h3>
               <span className="text-xs font-medium text-gray-500 bg-gray-100 px-2 py-1 rounded">Total: 100%</span>
            </div>
            
            <div className="space-y-3">
              {milestones.map((m, idx) => (
                <div key={m.id} className="flex flex-col md:flex-row gap-4 p-4 rounded-xl border border-gray-100 bg-gray-50/50 hover:bg-white hover:border-gray-200 transition-colors">
                   <div className="flex items-center gap-3 flex-1">
                      <span className="text-xs font-bold text-gray-400">0{idx + 1}</span>
                      <input 
                        className="bg-transparent font-medium text-gray-900 w-full focus:outline-none focus:underline"
                        value={m.description}
                        onChange={(e) => {
                          const newM = [...milestones];
                          newM[idx].description = e.target.value;
                          setMilestones(newM);
                        }}
                      />
                   </div>
                   <div className="flex items-center gap-4 md:w-48">
                      <div className="relative w-20">
                        <input 
                          type="number"
                          className="w-full bg-white border border-gray-200 rounded px-2 py-1 text-sm text-right focus:ring-1 focus:ring-black outline-none"
                          value={m.percentage}
                          onChange={(e) => {
                            const newM = [...milestones];
                            newM[idx].percentage = parseInt(e.target.value) || 0;
                            // Recalculate amounts purely for display
                            setMilestones(newM);
                          }}
                        />
                        <span className="absolute right-6 top-1/2 -translate-y-1/2 text-xs text-gray-400 pointer-events-none">%</span>
                      </div>
                      <span className="text-sm font-bold text-gray-900 min-w-[80px] text-right">{m.amount}</span>
                   </div>
                </div>
              ))}
            </div>
          </section>

          {/* 2. Deliverables */}
          <section className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2 mb-6">
              <div className="w-6 h-6 rounded-full bg-black text-white flex items-center justify-center text-xs">2</div>
              Scope & Deliverables
            </h3>
            
            <div className="space-y-2 mb-4">
              {deliverables.map((item, idx) => (
                <div key={idx} className={`flex items-center justify-between p-3 rounded-xl border ${item.isOriginal ? 'bg-gray-50 border-gray-100' : 'bg-white border-blue-100 shadow-sm'}`}>
                   <div className="flex items-center gap-3">
                      {item.isOriginal ? <Lock size={14} className="text-gray-400"/> : <Plus size={14} className="text-blue-500"/>}
                      <span className={`text-sm ${item.isOriginal ? 'text-gray-600' : 'text-gray-900 font-medium'}`}>{item.text}</span>
                   </div>
                   {!item.isOriginal && (
                     <button onClick={() => setDeliverables(deliverables.filter((_, i) => i !== idx))} className="text-gray-400 hover:text-red-500">
                        <Trash2 size={16} />
                     </button>
                   )}
                </div>
              ))}
            </div>

            <div className="flex gap-2">
               <input 
                 value={newDeliverable}
                 onChange={(e) => setNewDeliverable(e.target.value)}
                 onKeyDown={(e) => e.key === 'Enter' && handleAddDeliverable()}
                 placeholder="Add extra item (e.g. 3D Animation Video)..."
                 className="flex-1 bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none"
               />
               <Button onClick={handleAddDeliverable} variant="secondary" size="sm">Add</Button>
            </div>
          </section>

        </div>

        {/* RIGHT COLUMN: ACTIONS */}
        <div className="space-y-6">
           
           {/* Contract Generation Card */}
           <div className="bg-gradient-to-br from-gray-900 to-black rounded-3xl p-8 text-white text-center shadow-xl relative overflow-hidden">
              <div className="relative z-10">
                 <div className="h-16 w-16 bg-white/10 backdrop-blur-md rounded-2xl flex items-center justify-center mx-auto mb-6 text-yellow-300">
                    <Sparkles size={32} />
                 </div>
                 <h3 className="text-xl font-bold mb-2">Generate Contract</h3>
                 <p className="text-gray-400 text-sm mb-8">AI will draft a professional IAI standard agreement based on the finalized terms above.</p>
                 
                 {!contractReady ? (
                   <Button 
                     onClick={handleGenerateContract} 
                     disabled={isGenerating}
                     className="w-full bg-white text-black hover:bg-gray-200 border-none"
                   >
                     {isGenerating ? 'Drafting...' : 'Generate with AI'}
                   </Button>
                 ) : (
                   <div className="space-y-3 animate-in fade-in slide-in-from-bottom-2">
                      <Button className="w-full bg-green-500 hover:bg-green-600 border-none text-white gap-2">
                         <Download size={18} /> Download Draft
                      </Button>
                      <p className="text-xs text-green-400 font-medium flex items-center justify-center gap-1">
                         <CheckCircle2 size={12} /> Ready for review
                      </p>
                   </div>
                 )}
              </div>
           </div>
           
           {/* Finalize CTA */}
           <div className="bg-green-50 rounded-3xl p-6 border border-green-100">
               <h4 className="font-bold text-green-900 mb-2">Ready to proceed?</h4>
               <p className="text-sm text-green-700 mb-4">Once finalized, we will send the milestones and scope to the architect for signature.</p>
               <Button 
                  onClick={handleFinalize} 
                  disabled={isFinalizing || !contractReady}
                  className={`w-full ${contractReady ? 'bg-green-600 hover:bg-green-700' : 'bg-gray-300 cursor-not-allowed'} border-none text-white`}
               >
                  {isFinalizing ? 'Sending...' : 'Finalize & Send Agreement'}
               </Button>
               {!contractReady && <p className="text-[10px] text-center text-gray-400 mt-2">Generate contract first to proceed</p>}
           </div>

           {/* Legal Partners */}
           <div className="bg-white rounded-3xl border border-gray-200 p-6">
              <div className="flex items-center gap-2 mb-4 text-indigo-600">
                 <ShieldCheck size={20} />
                 <h4 className="font-bold text-sm uppercase tracking-wide">Legal Protection</h4>
              </div>
              <p className="text-sm text-gray-500 mb-6">Need an expert review? Consult with our verified legal partners before signing.</p>
              
              <div className="space-y-4">
                 {[
                   { name: "Sarah H.", firm: "LegalShield ID", role: "Construction Law" },
                   { name: "Dimas P.", firm: "SafeBuild Legal", role: "Contract Specialist" }
                 ].map((partner, i) => (
                   <div key={i} className="flex items-center justify-between p-3 rounded-xl bg-gray-50 border border-gray-100 hover:border-indigo-100 transition-colors">
                      <div className="flex items-center gap-3">
                         <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-xs">
                            {partner.name.charAt(0)}
                         </div>
                         <div>
                            <p className="text-sm font-bold text-gray-900">{partner.name}</p>
                            <p className="text-[10px] text-gray-500">{partner.firm}</p>
                         </div>
                      </div>
                      <button className="text-gray-400 hover:text-indigo-600 p-2">
                         <MessageSquare size={16} />
                      </button>
                   </div>
                 ))}
              </div>
              
              <Button variant="outline" size="sm" className="w-full mt-4 text-xs">View All Partners</Button>
           </div>

        </div>

      </div>
    </div>
  );
};

export default ContractView;