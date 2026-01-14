import React, { useState, useMemo } from 'react';
import { Proposal, Project, AnalysisResponse } from '../types';
import { analyzeProposals, chatWithData } from '../services/geminiService';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer, 
  Radar, 
  RadarChart, 
  PolarGrid, 
  PolarAngleAxis, 
  PolarRadiusAxis, 
  Cell
} from 'recharts';
import { Loader2, Send, BrainCircuit, CheckCircle, XCircle, TrendingDown, Clock, Banknote, Sparkles, FileText, ExternalLink, SlidersHorizontal, ArrowUpDown, Filter, Download, Eye, Building2, Image as ImageIcon, Home, Armchair, Grid, Trophy, Check, AlertCircle, X } from 'lucide-react';

interface Props {
  project: Project;
  proposals: Proposal[];
  onSelectWinner?: (projectId: string, proposalId: string) => void;
}

type SortKey = 'default' | 'cost' | 'duration' | 'designScore';
type SortDirection = 'asc' | 'desc';

const ProposalAnalysis: React.FC<Props> = ({ project, proposals, onSelectWinner }) => {
  const [analysis, setAnalysis] = useState<AnalysisResponse | null>(null);
  const [loading, setLoading] = useState(false);
  
  // Chat state
  const [chatHistory, setChatHistory] = useState<{ role: 'user' | 'model', text: string }[]>([]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);

  // Selection State
  const [selectedProposalId, setSelectedProposalId] = useState<string | null>(null); // For visual highlight (step 1)
  const [confirmWinnerId, setConfirmWinnerId] = useState<string | null>(null); // For modal (step 2)
  const [justAwarded, setJustAwarded] = useState(false); // For local success state

  // Sort and Filter State
  const [sortKey, setSortKey] = useState<SortKey>('default');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
  const [filters, setFilters] = useState({
    maxCost: '',
    maxDuration: '',
    minDesignScore: ''
  });
  const [showFilters, setShowFilters] = useState(false);

  // Comparison Metrics
  const metrics = useMemo(() => {
    const data = proposals.map(p => ({
      id: p.id,
      architectName: p.architectName,
      costPerM2: p.estimatedCost / project.lotSize,
      duration: p.durationMonths,
      totalCost: p.estimatedCost
    }));

    const avgCostPerM2 = data.reduce((sum, item) => sum + item.costPerM2, 0) / (data.length || 1);
    const avgDuration = data.reduce((sum, item) => sum + item.duration, 0) / (data.length || 1);

    return { data, avgCostPerM2, avgDuration };
  }, [proposals, project.lotSize]);

  // Filter & Sort Logic
  const processedItems = useMemo(() => {
    if (!analysis) return [];

    let items = analysis.comparison.map(item => {
      const metric = metrics.data.find(m => m.architectName === item.architectName);
      const originalProposal = proposals.find(p => p.architectName === item.architectName);
      return { ...item, metric, originalProposal };
    });

    // Apply Filters
    if (filters.maxCost) items = items.filter(i => i.metric && i.metric.totalCost <= Number(filters.maxCost));
    if (filters.maxDuration) items = items.filter(i => i.metric && i.metric.duration <= Number(filters.maxDuration));
    if (filters.minDesignScore) items = items.filter(i => i.designScore >= Number(filters.minDesignScore));

    // Apply Sorting
    if (sortKey !== 'default') {
      items.sort((a, b) => {
        let valA = 0;
        let valB = 0;
        switch (sortKey) {
          case 'cost': valA = a.metric?.totalCost || 0; valB = b.metric?.totalCost || 0; break;
          case 'duration': valA = a.metric?.duration || 0; valB = b.metric?.duration || 0; break;
          case 'designScore': valA = a.designScore; valB = b.designScore; break;
        }
        return sortDirection === 'asc' ? valA - valB : valB - valA;
      });
    }
    return items;
  }, [analysis, metrics, filters, sortKey, sortDirection, proposals]);

  const handleAnalyze = async () => {
    setLoading(true);
    try {
      const result = await analyzeProposals(project, proposals);
      setAnalysis(result);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async () => {
    if (!chatInput.trim()) return;
    const userMsg = chatInput;
    setChatInput('');
    setChatHistory(prev => [...prev, { role: 'user', text: userMsg }]);
    setChatLoading(true);

    try {
      const response = await chatWithData(chatHistory, project, proposals, userMsg);
      if (response) {
        setChatHistory(prev => [...prev, { role: 'model', text: response }]);
      }
    } catch (e) {
      setChatHistory(prev => [...prev, { role: 'model', text: "Sorry, I couldn't process that request." }]);
    } finally {
      setChatLoading(false);
    }
  };

  // Selection Handlers
  const handleSelectArchitect = (proposalId: string) => {
    setSelectedProposalId(proposalId);
  };

  const handleCancelSelection = () => {
    setSelectedProposalId(null);
  };

  const handleInitiateConfirmation = () => {
    if (selectedProposalId) {
      setConfirmWinnerId(selectedProposalId);
    }
  };

  const confirmSelection = () => {
    if (confirmWinnerId && onSelectWinner) {
      onSelectWinner(project.id, confirmWinnerId);
      setJustAwarded(true);
      setConfirmWinnerId(null);
      setSelectedProposalId(null); // Clear selection state as project is now awarded
    }
  };

  const formatMillions = (val: number) => `IDR ${(val / 1000000).toFixed(1)} M`;

  // Determine View State
  const isProjectAwarded = project.status === 'AWARDED' || justAwarded;
  const activeWinnerId = project.winnerProposalId || (justAwarded ? confirmWinnerId : null); // Use confirmWinnerId briefly during transition if needed, usually props update fast
  
  // If we just awarded locally but props haven't updated yet, use the ID we just confirmed
  const finalWinnerId = project.winnerProposalId || (justAwarded ? proposals.find(p => p.id === selectedProposalId)?.id : null) || (justAwarded ? proposals.find(p => p.id === confirmWinnerId)?.id : null);

  const isAnalyzed = !!analysis;
  const displayProposals = isAnalyzed ? processedItems.map(i => i.originalProposal!) : proposals;

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-24 animate-fade-in">
        <Loader2 className="w-12 h-12 text-black animate-spin mb-6" />
        <p className="text-black font-bold text-lg">Analyzing proposals...</p>
        <p className="text-neutral-400 text-sm mt-2">Comparing costs, timelines, and concepts</p>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fade-in">
      
      {/* Winner Banner */}
      {isProjectAwarded && (
        <div className="bg-black rounded-3xl p-8 text-white relative overflow-hidden shadow-2xl animate-fade-in">
           <div className="absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_top_right,_var(--tw-gradient-stops))] from-neutral-800 via-black to-black"></div>
           <div className="relative z-10 flex flex-col md:flex-row items-center gap-8">
              <div className="w-24 h-24 bg-white rounded-full flex items-center justify-center flex-shrink-0 shadow-lg">
                 <Trophy className="w-12 h-12 text-black" />
              </div>
              <div className="text-center md:text-left flex-1">
                 <h2 className="text-3xl font-bold mb-3 tracking-tight">Project Awarded</h2>
                 <p className="text-neutral-300 text-sm leading-relaxed max-w-2xl">
                   Congratulations! You have selected <strong>{proposals.find(p => p.id === finalWinnerId)?.architectName}</strong>. 
                   Notifications have been sent, and the next steps for contracting have been initiated.
                 </p>
                 <div className="flex flex-wrap gap-3 mt-6 justify-center md:justify-start">
                    <span className="bg-white/10 backdrop-blur px-4 py-2 rounded-full text-xs font-medium border border-white/20 flex items-center gap-2">
                      <Check className="w-3 h-3" /> Architect Notified
                    </span>
                    <span className="bg-white/10 backdrop-blur px-4 py-2 rounded-full text-xs font-medium border border-white/20">
                      Contract Drafting
                    </span>
                 </div>
              </div>
           </div>
        </div>
      )}

      {/* Analysis Summary & Controls (Only if NOT awarded or hidden if preferred, usually good to keep for reference) */}
      {!isProjectAwarded && analysis && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 gap-6">
            <div className="bg-neutral-50 border border-neutral-200 p-8 rounded-3xl">
              <h3 className="text-lg font-bold text-black mb-3 flex items-center gap-2">
                <BrainCircuit className="w-5 h-5" /> AI Recommendation
              </h3>
              <p className="text-neutral-700 leading-relaxed">{analysis.recommendation}</p>
            </div>
            <div className="bg-white border border-neutral-200 p-8 rounded-3xl shadow-sm">
              <h3 className="text-lg font-bold text-black mb-3 flex items-center gap-2">
                <Sparkles className="w-5 h-5" /> Top Contenders Summary
              </h3>
              <p className="text-neutral-600 leading-relaxed">{analysis.topOptionsSummary}</p>
            </div>
          </div>

          {/* Controls Bar */}
          <div className="bg-white p-4 rounded-2xl shadow-sm border border-neutral-200">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex items-center gap-2 text-sm font-medium text-neutral-600 px-2">
                <SlidersHorizontal className="w-4 h-4" /> <span>Sort & Filter</span>
              </div>
              <div className="flex flex-wrap gap-3 items-center">
                <div className="flex items-center gap-2 bg-neutral-50 rounded-full px-3 py-1 border border-neutral-200">
                  <select className="bg-transparent text-sm text-neutral-700 outline-none cursor-pointer font-medium" value={sortKey} onChange={(e) => setSortKey(e.target.value as SortKey)}>
                    <option value="default">Recommended</option>
                    <option value="cost">Cost</option>
                    <option value="duration">Duration</option>
                    <option value="designScore">Design Score</option>
                  </select>
                  {sortKey !== 'default' && (
                    <button onClick={() => setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc')} className="p-1 hover:bg-neutral-200 rounded-full transition-colors">
                      <ArrowUpDown className="w-4 h-4 text-neutral-500" />
                    </button>
                  )}
                </div>
                <button onClick={() => setShowFilters(!showFilters)} className={`px-4 py-1.5 rounded-full border text-sm font-medium flex items-center gap-2 transition-colors ${showFilters ? 'bg-black text-white border-black' : 'bg-white border-neutral-200 text-neutral-600 hover:bg-neutral-50'}`}>
                  <Filter className="w-4 h-4" /> Filters
                </button>
              </div>
            </div>
            {showFilters && (
              <div className="mt-4 pt-4 border-t border-neutral-100 grid grid-cols-1 md:grid-cols-3 gap-4 animate-fade-in px-2 pb-2">
                <div><label className="block text-xs font-bold text-neutral-400 uppercase mb-2">Max Cost</label><input type="number" className="w-full px-4 py-2 bg-neutral-50 border border-neutral-200 rounded-xl text-sm focus:ring-2 focus:ring-black outline-none" value={filters.maxCost} onChange={(e) => setFilters({...filters, maxCost: e.target.value})} /></div>
                <div><label className="block text-xs font-bold text-neutral-400 uppercase mb-2">Max Duration</label><input type="number" className="w-full px-4 py-2 bg-neutral-50 border border-neutral-200 rounded-xl text-sm focus:ring-2 focus:ring-black outline-none" value={filters.maxDuration} onChange={(e) => setFilters({...filters, maxDuration: e.target.value})} /></div>
                <div><label className="block text-xs font-bold text-neutral-400 uppercase mb-2">Min Score</label><input type="number" className="w-full px-4 py-2 bg-neutral-50 border border-neutral-200 rounded-xl text-sm focus:ring-2 focus:ring-black outline-none" value={filters.minDesignScore} onChange={(e) => setFilters({...filters, minDesignScore: e.target.value})} /></div>
              </div>
            )}
          </div>
        </div>
      )}

      {!analysis && !isProjectAwarded && (
        <div className="flex flex-col items-center justify-center py-12 bg-black rounded-3xl shadow-xl text-white relative overflow-hidden border border-neutral-800">
          <div className="absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_center,_var(--tw-gradient-stops))] from-neutral-700 via-black to-black"></div>
          <BrainCircuit className="w-12 h-12 text-white mb-4 relative z-10 opacity-80" />
          <h3 className="text-2xl font-bold mb-2 relative z-10">Analyze with Rumantra AI</h3>
          <p className="text-neutral-400 mb-8 text-center max-w-lg relative z-10 leading-relaxed text-sm">Comparing {proposals.length} proposals? Let AI help you identify the best value.</p>
          <button onClick={handleAnalyze} className="px-10 py-4 bg-white hover:bg-neutral-200 text-black font-bold rounded-full transition-all transform hover:scale-105 flex items-center gap-2 shadow-lg relative z-10">
            <Sparkles className="w-5 h-5" /> Start AI Analysis
          </button>
        </div>
      )}

      {/* Proposal Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {displayProposals.map((proposal, idx) => {
          const analysisItem = analysis?.comparison.find(c => c.architectName === proposal.architectName);
          const metric = isAnalyzed ? metrics.data.find(m => m.architectName === proposal.architectName) : null;
          
          const costChartData = metric ? [{ name: 'This', value: metric.costPerM2, fill: '#171717' }, { name: 'Avg', value: metrics.avgCostPerM2, fill: '#e5e5e5' }] : [];
          const timeChartData = metric ? [{ name: 'This', value: metric.duration, fill: '#171717' }, { name: 'Avg', value: metrics.avgDuration, fill: '#e5e5e5' }] : [];

          // Card Visual Logic
          const isSelected = selectedProposalId === proposal.id;
          const isWinner = finalWinnerId === proposal.id;
          
          // If project is awarded, dim non-winners. If selecting, dim non-selected.
          const isDimmed = (isProjectAwarded && !isWinner) || (selectedProposalId !== null && !isSelected);
          
          const cardClasses = isWinner || isSelected
            ? 'border-black ring-2 ring-black shadow-2xl transform scale-[1.02] z-10' 
            : isDimmed 
              ? 'border-neutral-200 opacity-40 grayscale-[0.5] pointer-events-none' 
              : 'border-neutral-200 hover:shadow-lg hover:border-neutral-300';

          return (
            <div key={proposal.id} className={`bg-white border rounded-3xl overflow-hidden transition-all duration-500 flex flex-col group ${cardClasses}`}>
              
              {/* Winner Badge */}
              {isWinner && (
                <div className="absolute top-0 right-0 z-20 bg-black text-white text-xs font-bold px-4 py-2 rounded-bl-2xl flex items-center gap-2 shadow-lg">
                  <Trophy className="w-3 h-3 text-yellow-400" /> WINNER
                </div>
              )}

              <div className="relative h-48 bg-neutral-100">
                {proposal.coverImage ? (
                  <img src={proposal.coverImage} alt={proposal.architectName} className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-neutral-200 text-neutral-400"><Building2 className="w-10 h-10" /></div>
                )}
                <div className={`absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent ${isDimmed ? 'opacity-100 bg-white/10' : ''}`}></div>
                <div className="absolute bottom-4 left-5 text-white">
                   <h4 className="font-bold truncate pr-2 text-lg leading-tight">{proposal.architectName}</h4>
                   <div className="text-xs opacity-80 font-light mt-0.5">{proposal.firmName || 'Independent Architect'}</div>
                </div>
              </div>

              <div className="p-5 border-b border-neutral-100 bg-white">
                 {analysisItem ? (
                   <>
                     <div className="flex flex-wrap gap-1.5 mb-4">
                        {analysisItem.designStyle && <span className="px-2.5 py-1 bg-neutral-100 text-neutral-800 text-[10px] font-bold rounded-md uppercase tracking-wider">{analysisItem.designStyle}</span>}
                     </div>
                     <div className="flex gap-2">
                        <span className="text-xs font-bold px-3 py-1.5 bg-black text-white rounded-full">Cost: {analysisItem.costScore}</span>
                        <span className="text-xs font-bold px-3 py-1.5 bg-neutral-200 text-neutral-800 rounded-full">Time: {analysisItem.timeScore}</span>
                     </div>
                   </>
                 ) : (
                   <div className="text-xs text-neutral-500">Run analysis for scores</div>
                 )}
              </div>

              {/* Charts */}
              {metric && !isDimmed && (
                <div className="px-5 pt-5 pb-2 bg-white animate-fade-in">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="bg-neutral-50 p-3 rounded-xl border border-neutral-100">
                      <div className="flex justify-between items-end mb-1"><span className="text-[10px] text-neutral-500 font-medium flex items-center gap-1"><Banknote className="w-3 h-3" /> /m²</span><span className="text-xs font-bold text-black">{formatMillions(metric.costPerM2)}</span></div>
                      <div className="h-16 w-full"><ResponsiveContainer width="100%" height="100%"><BarChart data={costChartData}><Bar dataKey="value" radius={[3, 3, 0, 0]} barSize={24} /></BarChart></ResponsiveContainer></div>
                    </div>
                    <div className="bg-neutral-50 p-3 rounded-xl border border-neutral-100">
                       <div className="flex justify-between items-end mb-1"><span className="text-[10px] text-neutral-500 font-medium flex items-center gap-1"><Clock className="w-3 h-3" /> Mos.</span><span className="text-xs font-bold text-black">{metric.duration} Mo</span></div>
                      <div className="h-16 w-full"><ResponsiveContainer width="100%" height="100%"><BarChart data={timeChartData}><Bar dataKey="value" radius={[3, 3, 0, 0]} barSize={24} /></BarChart></ResponsiveContainer></div>
                    </div>
                  </div>
                </div>
              )}

              <div className="p-5 space-y-5 flex-1 flex flex-col">
                {analysisItem && !isDimmed && (
                  <>
                    <p className="text-sm text-neutral-600 italic leading-relaxed bg-neutral-50 p-3 rounded-lg border border-neutral-100">"{analysisItem.summary}"</p>
                    <div>
                      <h5 className="text-xs font-bold text-neutral-900 mb-2 flex items-center gap-1 uppercase tracking-wide"><CheckCircle className="w-3 h-3" /> PROS</h5>
                      <ul className="text-xs text-neutral-600 list-disc list-inside space-y-1 pl-1">{analysisItem.pros.map((p, i) => <li key={i}>{p}</li>)}</ul>
                    </div>
                  </>
                )}

                {!analysisItem && (
                   <div className="space-y-2 text-sm text-neutral-600">
                      <div className="flex justify-between"><span>Cost:</span> <span className="font-bold text-black">{formatMillions(proposal.estimatedCost)}</span></div>
                      <div className="flex justify-between"><span>Duration:</span> <span className="font-bold text-black">{proposal.durationMonths} Months</span></div>
                      <p className="text-xs text-neutral-400 mt-2 line-clamp-3">{proposal.conceptDescription}</p>
                   </div>
                )}

                <div className="mt-auto pt-4 border-t border-neutral-100 space-y-3">
                   <div className="grid grid-cols-2 gap-3">
                      {proposal.pdfUrl && (
                        <button 
                          onClick={() => {
                             const win = window.open();
                             if(win) { win.document.write(`<html><head><title>${proposal.pdfFileName}</title></head><body style="margin:0"><iframe width="100%" height="100%" src="${proposal.pdfUrl}" style="border:none"></iframe></body></html>`); }
                          }}
                          className="flex items-center justify-center gap-2 py-2.5 bg-white border border-neutral-200 hover:bg-neutral-50 text-neutral-600 rounded-full text-xs font-bold transition-colors"
                        >
                          <Eye className="w-3 h-3" /> View PDF
                        </button>
                      )}
                      
                      {/* SELECTION BUTTONS */}
                      {onSelectWinner && !isProjectAwarded && (
                        isSelected ? (
                          <div className="col-span-2 flex gap-2 animate-fade-in">
                             <button 
                                onClick={handleCancelSelection}
                                className="flex-1 py-2.5 border border-neutral-300 text-neutral-700 rounded-full text-xs font-bold hover:bg-neutral-50 transition-colors"
                             >
                               Cancel
                             </button>
                             <button 
                                onClick={handleInitiateConfirmation}
                                className="flex-1 py-2.5 bg-green-600 text-white rounded-full text-xs font-bold hover:bg-green-700 transition-colors shadow-md flex items-center justify-center gap-1"
                             >
                               <Check className="w-3 h-3" /> Confirm & Hire
                             </button>
                          </div>
                        ) : (
                          <button 
                            onClick={() => handleSelectArchitect(proposal.id)}
                            disabled={selectedProposalId !== null}
                            className={`flex items-center justify-center gap-2 py-2.5 bg-black hover:bg-neutral-800 text-white rounded-full text-xs font-bold transition-colors ${!proposal.pdfUrl ? 'col-span-2' : ''} ${selectedProposalId !== null ? 'opacity-50 cursor-not-allowed' : ''}`}
                          >
                            Select This Architect
                          </button>
                        )
                      )}
                   </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Confirmation Modal */}
      {confirmWinnerId && (
        <div className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
           <div className="bg-white rounded-3xl shadow-2xl max-w-md w-full p-8 text-center">
              <div className="w-16 h-16 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-4">
                 <Trophy className="w-8 h-8 text-black" />
              </div>
              <h3 className="text-2xl font-bold text-black mb-2">Confirm Selection</h3>
              <p className="text-neutral-500 text-sm mb-8 leading-relaxed">
                Are you sure you want to hire <strong className="text-black">{proposals.find(p => p.id === confirmWinnerId)?.architectName}</strong>? 
                This will officially close the bidding process and notify the architect to prepare the contract.
              </p>
              
              <div className="flex gap-3">
                 <button 
                   onClick={() => setConfirmWinnerId(null)}
                   className="flex-1 py-3 border border-neutral-300 text-neutral-700 rounded-full font-bold hover:bg-neutral-50 transition-colors"
                 >
                   Cancel
                 </button>
                 <button 
                   onClick={confirmSelection}
                   className="flex-1 py-3 bg-black text-white rounded-full font-bold hover:bg-neutral-800 transition-colors shadow-lg flex items-center justify-center gap-2"
                 >
                   <Check className="w-4 h-4" /> Confirm
                 </button>
              </div>
           </div>
        </div>
      )}
    </div>
  );
};

export default ProposalAnalysis;