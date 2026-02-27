import React, { useState, useEffect, useRef } from 'react';
import { Proposal, Project } from '../types';
import Button from './Button';
import { 
  ArrowLeft, Check, Star, Clock, FileText, Award, ShieldCheck, 
  MapPin, Calendar, FileIcon, Eye, ChevronLeft, ChevronRight,
  Sparkles, X, Send, Bot, MessageSquare, TrendingDown, BarChart3, AlertCircle, ArrowRight, Plus, Minus, CheckCircle2, Info, Trophy, Maximize2, Split,
  Layout, Home, Layers, Map, ListChecks, Briefcase, Zap, DollarSign as DollarIcon, FileDown
} from 'lucide-react';
import { createComparisonChat, sendChatMessage } from '../services/geminiService';
// Standardizing Chat import from @google/genai
import { Chat } from '@google/genai';
import confetti from 'canvas-confetti';

interface ProposalComparisonProps {
  proposals: Proposal[];
  onBack: () => void;
  project: Project;
  onProposalAccepted?: (proposal: Proposal) => void; 
}

interface ChatMessage {
  role: 'user' | 'ai';
  text: string;
}

// Helper to parse IDR strings
const parseBidValue = (bidStr: string): number => {
  const clean = bidStr.replace(/[^0-9.]/g, '');
  const val = parseFloat(clean);
  if (bidStr.toUpperCase().includes('B')) return val * 1000; 
  return val;
};

// Helper to parse duration to weeks (approx)
const parseDurationWeeks = (durStr: string): number => {
  const clean = parseFloat(durStr.replace(/[^0-9.]/g, '') || '0');
  const lower = durStr.toLowerCase();
  if (lower.includes('month')) return clean * 4;
  if (lower.includes('year')) return clean * 52;
  return clean; 
};

// Helper to get budget comparison text/node
const getBudgetComparison = (bidStr: string, budgetStr: string) => {
  const bidVal = parseBidValue(bidStr);
  const budgetVal = parseBidValue(budgetStr);
  
  if (!budgetVal || isNaN(bidVal)) return null;

  const diff = bidVal - budgetVal;
  
  if (diff === 0) {
    return <span className="text-[10px] font-bold text-green-600 bg-green-50 px-2 py-0.5 rounded ml-1 uppercase">On Budget</span>;
  } else if (diff > 0) {
    return <span className="text-[10px] font-bold text-red-600 bg-red-50 px-2 py-0.5 rounded ml-1 uppercase">{diff}M Over</span>;
  } else {
    return <span className="text-[10px] font-bold text-green-600 bg-green-50 px-2 py-0.5 rounded ml-1 uppercase">{Math.abs(diff)}M Under</span>;
  }
};

// --- Sub-components ---

const ImageCarousel: React.FC<{ images?: string[], height?: string }> = ({ images, height = "h-48" }) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  if (!images || images.length === 0) return <div className={`w-full ${height} bg-gray-100 rounded-xl flex items-center justify-center text-gray-400 text-xs`}>No images</div>;

  const next = (e: React.MouseEvent) => { e.stopPropagation(); setCurrentIndex((prev) => (prev + 1) % images.length); };
  const prev = (e: React.MouseEvent) => { e.stopPropagation(); setCurrentIndex((prev) => (prev - 1 + images.length) % images.length); };

  return (
    <div className={`relative w-full ${height} rounded-xl overflow-hidden group bg-gray-100`}>
      <img src={images[currentIndex]} alt="Visual" className="w-full h-full object-cover" />
      {images.length > 1 && (
        <>
          <button onClick={prev} className="absolute left-2 top-1/2 -translate-y-1/2 bg-white/80 p-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"><ChevronLeft size={16}/></button>
          <button onClick={next} className="absolute right-2 top-1/2 -translate-y-1/2 bg-white/80 p-1 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"><ChevronRight size={16}/></button>
          <div className="absolute bottom-2 left-1/2 -translate-x-1/2 flex gap-1">
             {images.map((_, i) => (
               <div key={i} className={`w-1.5 h-1.5 rounded-full ${i === currentIndex ? 'bg-white' : 'bg-white/50'}`} />
             ))}
          </div>
        </>
      )}
    </div>
  );
};

const ComparisonPlaceholder: React.FC<{ label: string }> = ({ label }) => (
  <div className="h-full min-h-[400px] border-2 border-dashed border-gray-200 rounded-3xl flex flex-col items-center justify-center p-8 text-center bg-gray-50/50">
    <div className="h-12 w-12 bg-gray-100 rounded-full flex items-center justify-center text-gray-400 mb-4">
      <Plus size={24} />
    </div>
    <h3 className="text-base font-bold text-gray-900 mb-1">{label}</h3>
    <p className="text-gray-500 text-xs max-w-[200px]">Select a proposal from the registry to begin a comparative analysis.</p>
  </div>
);

const ComparisonDetailCard: React.FC<{ proposal: Proposal, budget: string, onSelectWinner: (p: Proposal) => void }> = ({ proposal, budget, onSelectWinner }) => {
  return (
    <div className="bg-white rounded-3xl border border-gray-200 overflow-hidden shadow-sm flex flex-col h-full hover:shadow-md transition-shadow">
      <div className="p-6 pb-0">
         <div className="flex items-center gap-3 mb-4">
            <div className="h-10 w-10 rounded-xl bg-black text-white flex items-center justify-center font-bold text-base shrink-0">
               {proposal.architectName.charAt(0)}
            </div>
            <div className="flex-1 min-w-0">
               <h3 className="font-bold text-gray-900 truncate text-sm">{proposal.architectName}</h3>
               <div className="flex items-center text-[10px] text-gray-500 gap-2">
                  <span className="flex items-center text-yellow-600 font-bold"><Star size={10} className="mr-0.5 fill-yellow-600"/> {proposal.architectRating}</span>
                  <span>•</span>
                  <span>{proposal.architectType || 'Consultant'}</span>
               </div>
            </div>
         </div>
         <ImageCarousel images={proposal.proposalImages} height="h-56" />
      </div>
      
      <div className="p-6 space-y-6 flex-1 flex flex-col">
         <div className="grid grid-cols-2 gap-3">
            <div className="bg-gray-50 p-3 rounded-2xl">
               <p className="text-[9px] text-gray-400 uppercase tracking-wider font-bold mb-1">Proposed Fee</p>
               <div className="flex flex-col items-start">
                  <p className="text-base font-bold text-gray-900 leading-none">{proposal.bidAmount}</p>
                  <div className="mt-1">{getBudgetComparison(proposal.bidAmount, budget)}</div>
               </div>
            </div>
            <div className="bg-gray-50 p-3 rounded-2xl">
               <p className="text-[9px] text-gray-400 uppercase tracking-wider font-bold mb-1">Timeframe</p>
               <p className="text-base font-bold text-gray-900">{proposal.estimatedDuration}</p>
            </div>
         </div>
         
         <div className="flex-1">
            <p className="text-[9px] text-gray-400 uppercase tracking-wider font-bold mb-2">Architectural Pitch</p>
            <p className="text-xs text-gray-600 italic line-clamp-3 leading-relaxed">"{proposal.coverLetter}"</p>
         </div>

         <div className="pt-4 border-t border-gray-50">
            <Button 
               variant="outline"
               className="w-full hover:bg-black hover:text-white transition-all shadow-sm border-gray-200 font-bold uppercase text-[10px] tracking-widest py-4" 
               onClick={() => onSelectWinner(proposal)}
            >
               Appoint Lead Architect <Trophy size={14} className="ml-2" />
            </Button>
         </div>
      </div>
    </div>
  );
};

// --- Suitability Metrics Chart (Transformed to Bar Graphs) ---

const ComparisonBarCharts: React.FC<{ 
  p1: Proposal, 
  p2: Proposal, 
  project: Project
}> = ({ p1, p2, project }) => {
  // Logic to calculate normalized percentage values for comparison
  const price1 = parseBidValue(p1.bidAmount);
  const price2 = parseBidValue(p2.bidAmount);
  const maxPrice = Math.max(price1, price2) * 1.1; 
  const s1_price = Math.min(100, Math.max(20, ((maxPrice - price1) / maxPrice) * 100));
  const s2_price = Math.min(100, Math.max(20, ((maxPrice - price2) / maxPrice) * 100));

  const dur1 = parseDurationWeeks(p1.estimatedDuration);
  const dur2 = parseDurationWeeks(p2.estimatedDuration);
  const maxDur = Math.max(dur1, dur2) * 1.1;
  const s1_speed = Math.min(100, Math.max(20, ((maxDur - dur1) / maxDur) * 100));
  const s2_speed = Math.min(100, Math.max(20, ((maxDur - dur2) / maxDur) * 100));

  const exp1 = parseFloat(p1.architectExperience?.replace(/[^0-9.]/g, '') || '0');
  const exp2 = parseFloat(p2.architectExperience?.replace(/[^0-9.]/g, '') || '0');
  const s1_exp = Math.min(100, (exp1 / 20) * 100);
  const s2_exp = Math.min(100, (exp2 / 20) * 100);

  const s1_rating = ((p1.architectRating || 0) / 5) * 100;
  const s2_rating = ((p2.architectRating || 0) / 5) * 100;

  const reqDel = project.deliverables || [];
  const getMatch = (p: Proposal) => {
    if (reqDel.length === 0) return 100;
    const provided = p.features || [];
    return Math.round((reqDel.filter(r => provided.includes(r)).length / reqDel.length) * 100);
  };
  const s1_scope = getMatch(p1);
  const s2_scope = getMatch(p2);

  const metrics = [
    { label: 'Cost Efficiency', v1: s1_price, v2: s2_price },
    { label: 'Timeline Speed', v1: s1_speed, v2: s2_speed },
    { label: 'Practice Depth', v1: s1_exp, v2: s2_exp },
    { label: 'Client Feedback', v1: s1_rating, v2: s2_rating },
    { label: 'Scope Alignment', v1: s1_scope, v2: s2_scope },
  ];

  return (
    <div className="w-full max-w-2xl mx-auto px-6 py-4 space-y-8">
      {metrics.map((m, idx) => (
        <div key={idx} className="space-y-3">
          <div className="flex justify-between items-end">
            <span className="text-[10px] font-black text-gray-400 uppercase tracking-widest">{m.label}</span>
            <div className="flex gap-4 text-[9px] font-bold">
               <span className="text-black">{Math.round(m.v1)}%</span>
               <span className="text-slate-400">{Math.round(m.v2)}%</span>
            </div>
          </div>
          
          <div className="space-y-1.5">
             {/* Architect 1 Bar */}
             <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                <div 
                   className="h-full bg-black rounded-full transition-all duration-1000" 
                   style={{ width: `${m.v1}%` }}
                ></div>
             </div>
             {/* Architect 2 Bar */}
             <div className="h-2 w-full bg-gray-100 rounded-full overflow-hidden">
                <div 
                   className="h-full bg-slate-300 rounded-full transition-all duration-1000" 
                   style={{ width: `${m.v2}%` }}
                ></div>
             </div>
          </div>
        </div>
      ))}

      <div className="flex items-center justify-center gap-8 pt-6 text-[9px] border-t border-gray-50">
         <div className="flex items-center gap-2">
            <div className="w-2.5 h-2.5 bg-black rounded-sm"></div>
            <span className="font-bold text-gray-900 uppercase tracking-widest">{p1.architectName}</span>
         </div>
         <div className="flex items-center gap-2">
            <div className="w-2.5 h-2.5 bg-slate-300 rounded-sm"></div>
            <span className="font-bold text-slate-400 uppercase tracking-widest">{p2.architectName}</span>
         </div>
      </div>
    </div>
  );
};


// --- Visual Overlay Components ---

const VisualOverlay: React.FC<{ p1: Proposal; p2: Proposal }> = ({ p1, p2 }) => {
   const [sliderPos, setSliderPos] = useState(50);
   const [activeCategory, setActiveCategory] = useState(0);
   const [isResizing, setIsResizing] = useState(false);
   const containerRef = useRef<HTMLDivElement>(null);

   const categories = [
     { name: 'Facade', icon: Home, label: 'Front Exterior' },
     { name: 'Interior', icon: Layout, label: 'Living Areas' },
     { name: 'Massing', icon: Layers, label: 'Building Form' },
     { name: 'Zoning', icon: Map, label: 'Site Plan' }
   ];

   const handleMouseMove = (e: React.MouseEvent | React.TouchEvent) => {
     if (!isResizing || !containerRef.current) return;
     const rect = containerRef.current.getBoundingClientRect();
     const x = ('touches' in e) ? e.touches[0].clientX : (e as React.MouseEvent).clientX;
     const pos = ((x - rect.left) / rect.width) * 100;
     setSliderPos(Math.max(0, Math.min(100, pos)));
   };

   const getImg = (p: Proposal, idx: number) => {
     if (p.proposalImages && p.proposalImages[idx]) return p.proposalImages[idx];
     return p.proposalImages?.[0] || 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?q=80&w=2000&auto=format&fit=crop';
   };

   return (
     <div className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
           <div className="flex items-center gap-2">
              <div className="p-1.5 bg-black text-white rounded-lg shadow-sm"><Split size={14}/></div>
              <div>
                 <h4 className="text-xs font-bold text-gray-900 uppercase tracking-wider">Visual Overlay</h4>
                 <p className="text-[9px] text-gray-400 font-bold uppercase tracking-widest">Design Review Side-by-Side</p>
              </div>
           </div>
           
           <div className="flex bg-gray-100 p-1 rounded-xl overflow-x-auto no-scrollbar">
              {categories.map((cat, idx) => (
                 <button
                   key={cat.name}
                   onClick={() => setActiveCategory(idx)}
                   className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-[9px] font-bold uppercase tracking-wider transition-all whitespace-nowrap ${
                     activeCategory === idx 
                     ? 'bg-white text-black shadow-sm' 
                     : 'text-gray-400 hover:text-gray-600'
                   }`}
                 >
                   <cat.icon size={12} />
                   {cat.name}
                 </button>
              ))}
           </div>
        </div>

        <div 
          ref={containerRef}
          className="relative h-[450px] w-full rounded-2xl overflow-hidden shadow-sm cursor-col-resize select-none bg-gray-50 border border-gray-100"
          onMouseMove={handleMouseMove}
          onMouseDown={() => setIsResizing(true)}
          onMouseUp={() => setIsResizing(false)}
          onMouseLeave={() => setIsResizing(false)}
          onTouchMove={handleMouseMove}
          onTouchStart={() => setIsResizing(true)}
          onTouchEnd={() => setIsResizing(false)}
        >
           <div className="absolute top-4 left-1/2 -translate-x-1/2 z-30 pointer-events-none">
              <div className="bg-white/90 backdrop-blur-sm px-4 py-1.5 rounded-full shadow-sm border border-gray-100 flex items-center gap-2">
                 <span className="text-[8px] font-bold text-gray-400 uppercase tracking-widest">Reviewing:</span>
                 <span className="text-[10px] font-bold text-black uppercase tracking-tight">{categories[activeCategory].label}</span>
              </div>
           </div>

           <div className="absolute inset-0">
              <img src={getImg(p2, activeCategory)} className="w-full h-full object-cover" alt="p2 design" />
              <div className="absolute bottom-6 right-6 bg-black/40 backdrop-blur-md px-4 py-2 rounded-xl text-white text-[9px] font-bold uppercase tracking-wider border border-white/20">
                 {p2.architectName}
              </div>
           </div>

           <div 
             className="absolute inset-0 overflow-hidden border-r border-white/40" 
             style={{ width: `${sliderPos}%` }}
           >
              <img 
                src={getImg(p1, activeCategory)} 
                className="w-screen h-full object-cover max-w-none" 
                style={{ width: containerRef.current?.offsetWidth }} 
                alt="p1 design" 
              />
              <div className="absolute bottom-6 left-6 bg-black/40 backdrop-blur-md px-4 py-2 rounded-xl text-white text-[9px] font-bold uppercase tracking-wider border border-white/20 whitespace-nowrap">
                 {p1.architectName}
              </div>
           </div>

           <div 
             className="absolute top-0 bottom-0 w-0.5 bg-white shadow-md flex items-center justify-center z-20"
             style={{ left: `${sliderPos}%` }}
           >
              <div className="h-10 w-10 bg-white rounded-full flex items-center justify-center shadow-lg border-2 border-gray-100 cursor-grab active:cursor-grabbing">
                 <div className="flex gap-0.5 text-gray-900">
                    <ChevronLeft size={14} />
                    <ChevronRight size={14} />
                 </div>
              </div>
           </div>
        </div>
        
        <div className="flex justify-center">
           <div className="flex items-center gap-2 text-[8px] font-bold text-gray-400 uppercase tracking-widest">
              <div className="h-1.5 w-1.5 rounded-full bg-black"></div> Adjust slider for relative review
           </div>
        </div>
     </div>
   );
};

// --- Core Comparison Layouts ---

const ComparisonRow: React.FC<{ label: string; val1: string | React.ReactNode; val2: string | React.ReactNode; highlight?: boolean }> = ({ label, val1, val2, highlight }) => (
  <div className={`grid grid-cols-[1fr_120px_1fr] items-center py-4 border-b border-gray-50 last:border-0 ${highlight ? 'bg-gray-50/30' : ''}`}>
    <div className="text-right px-6 font-bold text-gray-900 text-sm">{val1}</div>
    <div className="text-center text-[8px] uppercase tracking-widest font-bold text-gray-400">{label}</div>
    <div className="text-left px-6 font-bold text-gray-900 text-sm">{val2}</div>
  </div>
);

const DeliverablesList: React.FC<{ 
  proposal: Proposal; 
  requested: string[]; 
  alignment: 'left' | 'right' 
}> = ({ proposal, requested, alignment }) => {
  const features = proposal.features || [];
  const requestedSet = new Set(requested);

  return (
    <div className={`space-y-3 ${alignment === 'right' ? 'text-left' : 'text-right'}`}>
      <div className={`flex flex-col ${alignment === 'right' ? 'items-start' : 'items-end'}`}>
         {features.map((feature, idx) => {
            const isRequested = requestedSet.has(feature);
            return (
              <div key={idx} className={`flex items-center gap-3 py-1.5 group`}>
                 {alignment === 'right' && (
                   <div className={`w-5 h-5 rounded-full flex items-center justify-center shrink-0 transition-colors ${isRequested ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'}`}>
                      <Check size={12} strokeWidth={3} />
                   </div>
                 )}
                 <div className="flex flex-col">
                    <span className={`text-xs font-bold ${isRequested ? 'text-gray-900' : 'text-gray-500'}`}>{feature}</span>
                    {isRequested && <span className="text-[8px] uppercase font-bold text-green-600 tracking-tighter">Matches Request</span>}
                 </div>
                 {alignment === 'left' && (
                   <div className={`w-5 h-5 rounded-full flex items-center justify-center shrink-0 transition-colors ${isRequested ? 'bg-green-100 text-green-600' : 'bg-gray-100 text-gray-400'}`}>
                      <Check size={12} strokeWidth={3} />
                   </div>
                 )}
              </div>
            );
         })}
      </div>
    </div>
  );
};

const PastProjectCard: React.FC<{ title: string; location: string; img: string }> = ({ title, location, img }) => (
  <div className="min-w-[200px] max-w-[200px] flex-shrink-0 group cursor-pointer bg-white rounded-2xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-md transition-all">
    <div className="h-28 overflow-hidden relative">
      <img src={img} alt={title} className="w-full h-full object-cover transition-transform group-hover:scale-105" />
      <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
    </div>
    <div className="p-3">
       <h5 className="text-[10px] font-bold text-gray-900 line-clamp-1 leading-tight">{title}</h5>
       <p className="text-[8px] font-bold text-gray-400 uppercase tracking-widest mt-1 flex items-center gap-1">
          <MapPin size={8} /> {location}
       </p>
    </div>
  </div>
);

const PastProjectsCarousel: React.FC<{ architectName: string; alignment: 'left' | 'right' }> = ({ architectName, alignment }) => {
  // Mock projects for the demo
  const projects = [
    { title: 'The Zen Retreat', location: 'Ubud, Bali', img: 'https://images.unsplash.com/photo-1512917774080-9991f1c4c750?q=80&w=600&auto=format&fit=crop' },
    { title: 'Pererenan Lofts', location: 'Canggu, Bali', img: 'https://images.unsplash.com/photo-1480074568708-e7b720bb3f09?q=80&w=600&auto=format&fit=crop' },
    { title: 'Nordic Residence', location: 'Bandung, Java', img: 'https://images.unsplash.com/photo-1613490493576-7fde63acd811?q=80&w=600&auto=format&fit=crop' }
  ];

  return (
    <div className={`flex flex-col ${alignment === 'right' ? 'items-start' : 'items-end'}`}>
       <div className={`flex items-center gap-2 mb-4 ${alignment === 'right' ? 'flex-row' : 'flex-row-reverse'}`}>
          <div className="h-6 w-6 bg-gray-100 rounded-lg flex items-center justify-center text-gray-500">
             <Eye size={12} />
          </div>
          <span className="text-[9px] font-black text-gray-400 uppercase tracking-widest">{architectName} Portfolio</span>
       </div>
       
       <div className={`flex gap-3 overflow-x-auto no-scrollbar pb-4 max-w-full ${alignment === 'right' ? 'justify-start' : 'justify-end'}`}>
          {projects.map((p, idx) => (
            <PastProjectCard key={idx} {...p} />
          ))}
       </div>
    </div>
  );
};

const UnifiedComparisonCard: React.FC<{ p1: Proposal; p2: Proposal; project: Project; onSelectWinner: (p: Proposal) => void }> = ({ p1, p2, project, onSelectWinner }) => {
   return (
      <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden animate-in fade-in zoom-in-95 duration-500">
         <div className="grid grid-cols-2 border-b border-gray-100 bg-gray-50/30">
            <div className="p-8 text-center border-r border-gray-100">
               <div className="h-16 w-16 mx-auto bg-black text-white rounded-2xl flex items-center justify-center text-2xl font-black mb-4 shadow-md">{p1.architectName.charAt(0)}</div>
               <h3 className="font-extrabold text-gray-900 text-lg leading-tight tracking-tight">{p1.architectName}</h3>
               <div className="text-[10px] font-bold text-gray-400 mt-1 uppercase tracking-wider">{p1.architectType || 'Consultant'}</div>
               <Button 
                variant="outline" 
                className="mt-6 w-full hover:bg-black hover:text-white transition-all border-gray-200 font-bold uppercase text-[10px] tracking-widest py-4" 
                onClick={() => onSelectWinner(p1)}
               >
                 Appoint Lead Architect <Trophy size={14} className="ml-2"/>
               </Button>
            </div>
            <div className="p-8 text-center">
               <div className="h-16 w-16 mx-auto bg-gray-900 text-white rounded-2xl flex items-center justify-center text-2xl font-black mb-4 shadow-md">{p2.architectName.charAt(0)}</div>
               <h3 className="font-extrabold text-gray-900 text-lg leading-tight tracking-tight">{p2.architectName}</h3>
               <div className="text-[10px] font-bold text-gray-400 mt-1 uppercase tracking-wider">{p2.architectType || 'Consultant'}</div>
               <Button 
                variant="outline" 
                className="mt-6 w-full hover:bg-black hover:text-white transition-all border-gray-200 font-bold uppercase text-[10px] tracking-widest py-4" 
                onClick={() => onSelectWinner(p2)}
               >
                 Appoint Lead Architect <Trophy size={14} className="ml-2"/>
               </Button>
            </div>
         </div>
         
         <div className="p-8 border-b border-gray-100">
            <VisualOverlay p1={p1} p2={p2} />
         </div>

         <div className="border-b border-gray-100 py-10 bg-white">
            <div className="flex flex-col items-center gap-2 mb-8">
              <BarChart3 size={18} className="text-gray-900"/>
              <p className="text-[10px] uppercase tracking-widest font-bold text-gray-400 text-center">Competitive Performance Audit</p>
            </div>
            <ComparisonBarCharts p1={p1} p2={p2} project={project} />
         </div>

         <div className="py-2 border-b border-gray-100">
            <ComparisonRow 
               label="Fee Estimate" 
               val1={<div className="flex flex-col items-end"><span>{p1.bidAmount}</span>{getBudgetComparison(p1.bidAmount, project.budget)}</div>} 
               val2={<div className="flex flex-col items-start"><span>{p2.bidAmount}</span>{getBudgetComparison(p2.bidAmount, project.budget)}</div>} 
               highlight 
            />
            <ComparisonRow label="Timeframe" val1={p1.estimatedDuration} val2={p2.estimatedDuration} />
            <ComparisonRow label="Exp. Practice" val1={p1.architectExperience} val2={p2.architectExperience} highlight />
            <ComparisonRow 
               label="Client Score" 
               val1={<span className="flex items-center justify-end gap-1"><Star size={14} className="fill-yellow-500 text-yellow-500"/>{p1.architectRating}</span>} 
               val2={<span className="flex items-center justify-start gap-1"><Star size={14} className="fill-yellow-500 text-yellow-500"/>{p2.architectRating}</span>} 
            />
         </div>

         {/* Deliverables Offering Side-by-Side */}
         <div className="p-10 border-b border-gray-100">
            <div className="flex flex-col items-center gap-2 mb-10">
              <ListChecks size={18} className="text-gray-900"/>
              <p className="text-[10px] uppercase tracking-widest font-bold text-gray-400 text-center">Service Scope Comparison</p>
            </div>
            
            <div className="grid grid-cols-[1fr_120px_1fr] items-start gap-4">
               <div className="px-6">
                  <div className="text-right mb-6">
                     <h4 className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">P1 Offering</h4>
                     <p className="text-xs font-bold text-gray-900">{p1.architectName}</p>
                  </div>
                  <DeliverablesList proposal={p1} requested={project.deliverables || []} alignment="left" />
               </div>
               
               <div className="flex flex-col items-center gap-4 py-12">
                  <div className="h-full w-px bg-gray-200"></div>
                  <div className="text-[8px] font-black text-gray-300 uppercase rotate-90 tracking-[0.4em] whitespace-nowrap">SCOPE-MATRIX</div>
                  <div className="h-full w-px bg-gray-200"></div>
               </div>

               <div className="px-6">
                  <div className="text-left mb-6">
                     <h4 className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">P2 Offering</h4>
                     <p className="text-xs font-bold text-gray-900">{p2.architectName}</p>
                  </div>
                  <DeliverablesList proposal={p2} requested={project.deliverables || []} alignment="right" />
               </div>
            </div>
         </div>

         {/* Proven Track Record / Past Projects Side-by-Side */}
         <div className="p-10 border-b border-gray-100">
            <div className="flex flex-col items-center gap-2 mb-10">
              <Briefcase size={18} className="text-gray-900"/>
              <p className="text-[10px] uppercase tracking-widest font-bold text-gray-400 text-center">Proven Track Record</p>
            </div>
            
            <div className="grid grid-cols-[1fr_100px_1fr] items-start gap-6">
               <div className="px-2 overflow-hidden">
                  <PastProjectsCarousel architectName={p1.architectName} alignment="left" />
               </div>
               
               <div className="flex flex-col items-center gap-4 py-12">
                  <div className="h-full w-px bg-gray-100"></div>
                  <div className="h-8 w-8 rounded-full bg-white border border-gray-100 flex items-center justify-center shadow-sm">
                     <Award size={14} className="text-indigo-600" />
                  </div>
                  <div className="h-full w-px bg-gray-100"></div>
               </div>

               <div className="px-2 overflow-hidden">
                  <PastProjectsCarousel architectName={p2.architectName} alignment="right" />
               </div>
            </div>
         </div>

         {/* Technical Dossier Access Side-by-Side */}
         <div className="p-10 bg-gray-50/20">
            <div className="flex flex-col items-center gap-2 mb-10">
              <FileText size={18} className="text-gray-900"/>
              <p className="text-[10px] uppercase tracking-widest font-bold text-gray-400 text-center">Technical Documentation</p>
            </div>
            
            <div className="grid grid-cols-[1fr_100px_1fr] items-center gap-6">
               <div className="px-2 flex flex-col items-end">
                  {p1.proposalPdf ? (
                    <button 
                      onClick={() => alert(`Opening technical dossier: ${p1.proposalPdf}`)}
                      className="group flex items-center gap-3 bg-white border border-gray-200 px-5 py-3 rounded-2xl shadow-sm hover:border-black transition-all hover:shadow-md"
                    >
                      <div className="text-right">
                         <p className="text-[8px] font-black text-gray-400 uppercase tracking-widest">Full Proposal</p>
                         <p className="text-[10px] font-bold text-gray-900">Review technical data</p>
                      </div>
                      <div className="p-2 bg-gray-900 text-white rounded-xl group-hover:scale-110 transition-transform">
                        <FileDown size={16} />
                      </div>
                    </button>
                  ) : (
                    <span className="text-[10px] text-gray-400 italic">No document attached</span>
                  )}
               </div>
               
               <div className="flex flex-col items-center gap-4 py-4">
                  <div className="h-full w-px bg-gray-100"></div>
                  <div className="text-[7px] font-black text-gray-300 uppercase tracking-[0.4em] rotate-90">DOSSIER</div>
                  <div className="h-full w-px bg-gray-100"></div>
               </div>

               <div className="px-2 flex flex-col items-start">
                  {p2.proposalPdf ? (
                    <button 
                      onClick={() => alert(`Opening technical dossier: ${p2.proposalPdf}`)}
                      className="group flex items-center gap-3 bg-white border border-gray-200 px-5 py-3 rounded-2xl shadow-sm hover:border-black transition-all hover:shadow-md"
                    >
                      <div className="p-2 bg-gray-900 text-white rounded-xl group-hover:scale-110 transition-transform">
                        <FileDown size={16} />
                      </div>
                      <div className="text-left">
                         <p className="text-[8px] font-black text-gray-400 uppercase tracking-widest">Full Proposal</p>
                         <p className="text-[10px] font-bold text-gray-900">Review technical data</p>
                      </div>
                    </button>
                  ) : (
                    <span className="text-[10px] text-gray-400 italic">No document attached</span>
                  )}
               </div>
            </div>
         </div>
      </div>
   )
}


const StatisticalHighlights: React.FC<{ proposals: Proposal[], project: Project }> = ({ proposals, project }) => {
  if (proposals.length < 2) return null;
  const [p1, p2] = proposals;
  const price1 = parseBidValue(p1.bidAmount);
  const price2 = parseBidValue(p2.bidAmount);
  const priceDiff = Math.abs(price1 - price2);
  const cheaper = price1 < price2 ? p1 : p2;
  const exp1 = parseInt(p1.architectExperience || '0');
  const exp2 = parseInt(p2.architectExperience || '0');
  const moreExp = exp1 > exp2 ? p1 : exp2;
  const reqDel = project.deliverables || [];
  const getMatchScore = (p: Proposal) => {
     if (reqDel.length === 0) return 100;
     const matches = reqDel.filter(req => (p.features || []).includes(req)).length;
     return Math.round((matches / reqDel.length) * 100);
  };
  const score1 = getMatchScore(p1);
  const score2 = getMatchScore(p2);

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 animate-in fade-in duration-500">
       <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
          <div className="flex items-center gap-1.5 mb-1.5 text-green-600 font-bold text-[9px] uppercase tracking-wider">
             <TrendingDown size={12} /> Fee Efficiency
          </div>
          <p className="font-bold text-gray-900 text-sm">{cheaper.architectName}</p>
          <p className="text-[10px] text-gray-500 mt-0.5">Budget Optimized (IDR {priceDiff}M lower)</p>
       </div>
       <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
          <div className="flex items-center gap-1.5 mb-1.5 text-blue-600 font-bold text-[9px] uppercase tracking-wider">
             <Award size={12} /> Practice Depth
          </div>
          <p className="font-bold text-gray-900 text-sm">{moreExp.architectName}</p>
          <p className="text-[10px] text-gray-500 mt-0.5">{moreExp.architectExperience} established record</p>
       </div>
       <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm">
          <div className="flex items-center gap-1.5 mb-2 text-purple-600 font-bold text-[9px] uppercase tracking-wider">
             <CheckCircle2 size={12} /> Deliverables Match
          </div>
          <div className="space-y-2">
             <div className="flex justify-between text-[9px] font-bold text-gray-900">
                <span className="truncate max-w-[80px]">{p1.architectName}</span>
                <span>{score1}%</span>
             </div>
             <div className="h-1 w-full bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-purple-500 rounded-full" style={{ width: `${score1}%` }}></div>
             </div>
             <div className="flex justify-between text-[9px] font-bold text-gray-900">
                <span className="truncate max-w-[80px]">{p2.architectName}</span>
                <span>{score2}%</span>
             </div>
             <div className="h-1 w-full bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-purple-500 rounded-full" style={{ width: `${score2}%` }}></div>
             </div>
          </div>
       </div>
    </div>
  );
};

// --- Main Platform Interface ---

const ProposalComparison: React.FC<ProposalComparisonProps> = ({ proposals, onBack, project, onProposalAccepted }) => {
  const [compareSlots, setCompareSlots] = useState<(Proposal | null)[]>([null, null]);
  const [isChatOpen, setIsChatOpen] = useState(false);
  const [chatSession, setChatSession] = useState<Chat | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [confirmingProposal, setConfirmingProposal] = useState<Proposal | null>(null);
  const [successView, setSuccessView] = useState(false);
  const [acceptedProposal, setAcceptedProposal] = useState<Proposal | null>(null);

  const activeProposals = compareSlots.filter(p => p !== null) as Proposal[];
  const isComparisonFull = activeProposals.length === 2;

  const scrollToBottom = () => { messagesEndRef.current?.scrollIntoView({ behavior: "smooth" }); };
  useEffect(() => { scrollToBottom(); }, [messages]);

  const toggleComparison = (proposal: Proposal) => {
    const currentIndex = compareSlots.findIndex(p => p?.id === proposal.id);
    if (currentIndex >= 0) {
      const newSlots = [...compareSlots];
      newSlots[currentIndex] = null;
      setCompareSlots(newSlots);
      setIsChatOpen(false); 
    } else {
      const emptyIndex = compareSlots.findIndex(p => p === null);
      if (emptyIndex >= 0) {
        const newSlots = [...compareSlots];
        newSlots[emptyIndex] = proposal;
        setCompareSlots(newSlots);
      } else {
        const newSlots = [...compareSlots];
        newSlots[1] = proposal;
        setCompareSlots(newSlots);
        setIsChatOpen(false); 
      }
    }
  };

  const startAiAnalysis = async () => {
    setIsChatOpen(true);
    setLoading(true);
    const chat = createComparisonChat(project, activeProposals);
    if (chat) {
      setChatSession(chat);
      const initialPrompt = "Perform a professional deliverables gap analysis between these two proposals relative to the project requirements.";
      const response = await sendChatMessage(chat, initialPrompt);
      setMessages([{ role: 'ai', text: response }]);
    } else {
      setMessages([{ role: 'ai', text: "Service unavailable." }]);
    }
    setLoading(false);
  };

  const handleSendMessage = async () => {
    if (!input.trim() || !chatSession) return;
    const userMsg = input.trim();
    setInput('');
    setMessages(prev => [...prev, { role: 'user', text: userMsg }]);
    setLoading(true);
    const response = await sendChatMessage(chatSession, userMsg);
    setMessages(prev => [...prev, { role: 'ai', text: response }]);
    setLoading(false);
  };

  const handleSelectWinner = (proposal: Proposal) => setConfirmingProposal(proposal);

  const confirmAcceptance = () => {
    if (!confirmingProposal) return;
    setAcceptedProposal(confirmingProposal);
    setConfirmingProposal(null);
    const duration = 2500;
    const end = Date.now() + duration;
    const frame = () => {
      confetti({ particleCount: 3, angle: 60, spread: 55, origin: { x: 0 }, colors: ['#000', '#333'] });
      confetti({ particleCount: 3, angle: 120, spread: 55, origin: { x: 1 }, colors: ['#000', '#333'] });
      if (Date.now() < end) requestAnimationFrame(frame);
    };
    frame();
    setSuccessView(true);
  };
  
  const handleGoToWorkspace = () => {
     if (acceptedProposal && onProposalAccepted) onProposalAccepted(acceptedProposal);
  };

  const getQuickSynthesisData = () => {
    if (activeProposals.length < 2) return null;
    const [p1, p2] = activeProposals;
    
    const price1 = parseBidValue(p1.bidAmount);
    const price2 = parseBidValue(p2.bidAmount);
    const cheaper = price1 <= price2 ? p1 : p2;
    const moreExpensive = price1 > price2 ? p1 : p2;

    const dur1 = parseDurationWeeks(p1.estimatedDuration);
    const dur2 = parseDurationWeeks(p2.estimatedDuration);
    const faster = dur1 <= dur2 ? p1 : p2;
    const slower = dur1 > dur2 ? p1 : p2;

    const exp1 = parseInt(p1.architectExperience || '0');
    const exp2 = parseInt(p2.architectExperience || '0');
    const mostExp = exp1 >= exp2 ? p1 : p2;
    const lessExp = exp1 < exp2 ? p1 : p2;

    // Generate descriptive summary sentence
    let summaryText = "";
    if (cheaper.id === faster.id && cheaper.id === mostExp.id) {
      summaryText = `${cheaper.architectName} presents an exceptional value proposition, leading across cost efficiency, project velocity, and professional practice depth.`;
    } else if (cheaper.id === faster.id) {
      summaryText = `${cheaper.architectName} leads with a highly efficient proposal in terms of both fee and timeframe, while ${mostExp.architectName} provides superior practice authority and historical context.`;
    } else if (cheaper.id === mostExp.id) {
      summaryText = `${cheaper.architectName} offers the most established practice at a budget-optimized price point, whereas ${faster.architectName} prioritizes a more aggressive delivery schedule.`;
    } else {
      summaryText = `${cheaper.architectName} provides the most economical entry point, while ${faster.architectName} offers the shortest path to delivery, and ${mostExp.architectName} brings the deepest industry experience.`;
    }

    return { cheaper, faster, mostExp, summaryText };
  };

  const synthesis = getQuickSynthesisData();

  if (successView) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] text-center animate-in zoom-in-95 duration-700">
        <div className="h-24 w-24 bg-green-100 text-green-600 rounded-3xl flex items-center justify-center mb-8 shadow-md">
          <Check size={48} strokeWidth={3} />
        </div>
        <h2 className="text-3xl font-extrabold text-gray-900 mb-2 tracking-tight">Engagement Confirmed</h2>
        <p className="text-gray-500 text-lg max-w-lg mb-10">
          You have appointed <span className="font-bold text-black">{acceptedProposal?.architectName}</span> for this project.
        </p>
        <Button size="lg" onClick={handleGoToWorkspace} className="px-10 py-5 text-sm font-bold uppercase tracking-widest shadow-xl">
          Enter Workspace <ArrowRight size={18} className="ml-2" />
        </Button>
      </div>
    );
  }

  return (
    <div className="animate-in slide-in-from-bottom-5 fade-in duration-500 pb-20 relative px-4 md:px-0">
      
      {/* Appointment Confirmation Overlay */}
      {confirmingProposal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-md animate-in fade-in duration-300">
          <div className="bg-white rounded-[2.5rem] shadow-2xl p-10 max-w-xl w-full m-4">
             <div className="flex flex-col items-center text-center">
                <div className="h-20 w-20 bg-gray-50 rounded-2xl flex items-center justify-center mb-6 text-gray-900 shadow-inner">
                   <ShieldCheck size={40} />
                </div>
                <h3 className="text-2xl font-extrabold text-gray-900 mb-2 tracking-tight">Confirm Selection</h3>
                <p className="text-gray-600 mb-10 text-sm">
                  Confirm appointment of <span className="font-bold text-black">{confirmingProposal.architectName}</span> as lead architect.
                </p>
                <div className="flex gap-4 w-full">
                  <Button variant="ghost" className="flex-1 justify-center py-5 font-bold uppercase text-xs tracking-widest" onClick={() => setConfirmingProposal(null)}>Review Further</Button>
                  <Button variant="primary" className="flex-1 justify-center py-5 shadow-lg font-bold uppercase text-xs tracking-widest" onClick={confirmAcceptance}>Confirm</Button>
                </div>
             </div>
          </div>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-10">
        <div className="flex items-center gap-6">
          <Button variant="ghost" onClick={onBack} className="!p-0 rounded-full h-12 w-12 bg-white border border-gray-200 hover:bg-black hover:text-white transition-all shadow-sm">
            <ArrowLeft size={24} />
          </Button>
          <div>
             <h2 className="text-3xl font-extrabold text-gray-900 tracking-tight">Comparative Analysis</h2>
             <p className="text-gray-500 font-bold text-sm mt-1">{project.title} • Reviewing {proposals.length} Formal Bids</p>
          </div>
        </div>
      </div>

      {/* Analysis Workspace */}
      <div className="mb-20">
        <div className="flex items-center justify-between mb-8">
          <h3 className="text-xl font-bold text-gray-900 tracking-tight">Side-by-Side Review</h3>
          {isComparisonFull && (
            <div className="text-[9px] font-bold text-indigo-600 uppercase tracking-widest flex items-center gap-2 bg-indigo-50 px-5 py-2.5 rounded-full border border-indigo-100">
               <Sparkles size={14} className="text-yellow-500"/> Analytical Support enabled
            </div>
          )}
        </div>
        
        {isComparisonFull ? (
           <UnifiedComparisonCard p1={activeProposals[0]} p2={activeProposals[1]} project={project} onSelectWinner={handleSelectWinner} />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 relative">
            {compareSlots[0] ? (
               <ComparisonDetailCard proposal={compareSlots[0]} budget={project.budget} onSelectWinner={handleSelectWinner} />
            ) : (
               <ComparisonPlaceholder label="Subject A" />
            )}
            {compareSlots[1] ? (
               <ComparisonDetailCard proposal={compareSlots[1]} budget={project.budget} onSelectWinner={handleSelectWinner} />
            ) : (
               <ComparisonPlaceholder label="Subject B" />
            )}
          </div>
        )}
        
        {/* AI Synthesis Section */}
        {isComparisonFull && (
           <div className="mt-16 bg-black rounded-3xl p-1 shadow-2xl overflow-hidden animate-in fade-in slide-in-from-top-4 duration-500">
              <div className="bg-white/5 backdrop-blur-xl p-8 md:p-10 flex flex-col items-center text-white rounded-t-[1.75rem] gap-10">
                 
                 {/* Top Label & Assistant Header */}
                 <div className="w-full flex flex-col md:flex-row items-center justify-between gap-6">
                    <div className="flex items-center gap-4">
                       <div className="bg-yellow-400 p-2.5 rounded-xl text-black shadow-lg">
                         <Bot size={24} />
                       </div>
                       <div>
                         <span className="font-bold text-[10px] uppercase tracking-widest block opacity-60">Synthesis Engine</span>
                         <span className="font-bold text-lg tracking-tight">AI Comparative Assistant</span>
                       </div>
                    </div>
                    
                    {/* The Detail Button is still here */}
                    {!isChatOpen && (
                       <Button onClick={startAiAnalysis} size="lg" variant="secondary" className="!bg-white !text-black hover:bg-gray-100 border-none px-10 font-bold uppercase text-[9px] tracking-widest py-4 shrink-0 shadow-lg">
                          Initiate Detailed Gap Audit
                       </Button>
                    )}
                 </div>

                 {/* The "Quick Synthesis" Summary Section */}
                 {!isChatOpen && synthesis && (
                    <div className="w-full space-y-10 animate-in fade-in zoom-in-95 duration-700 delay-200">
                       
                       {/* Qualitative Insight Sentence */}
                       <div className="max-w-3xl mx-auto text-center">
                          <p className="text-lg md:text-xl font-medium text-white/90 leading-relaxed italic border-l-2 md:border-l-0 md:border-b-2 border-white/10 pb-6">
                             "{synthesis.summaryText}"
                          </p>
                          <div className="mt-4 flex items-center justify-center gap-2 text-[9px] font-black text-white/30 uppercase tracking-[0.3em]">
                             <Zap size={10}/> Relative Competitive Advantage Synthesis
                          </div>
                       </div>

                       {/* Data-Driven Badges */}
                       <div className="flex flex-wrap items-center justify-center gap-6 md:gap-12">
                          <div className="flex items-center gap-3 group">
                             <div className="p-2.5 bg-green-500/20 rounded-xl group-hover:bg-green-500/30 transition-colors"><DollarIcon size={18} className="text-green-400" /></div>
                             <div>
                                <p className="text-[9px] font-black text-white/40 uppercase tracking-widest mb-0.5">Price Leader</p>
                                <p className="text-xs font-bold text-white group-hover:text-green-300 transition-colors">{synthesis.cheaper.architectName}</p>
                             </div>
                          </div>
                          <div className="flex items-center gap-3 group">
                             <div className="p-2.5 bg-blue-500/20 rounded-xl group-hover:bg-blue-500/30 transition-colors"><Zap size={18} className="text-blue-400" /></div>
                             <div>
                                <p className="text-[9px] font-black text-white/40 uppercase tracking-widest mb-0.5">Velocity Peak</p>
                                <p className="text-xs font-bold text-white group-hover:text-blue-300 transition-colors">{synthesis.faster.architectName}</p>
                             </div>
                          </div>
                          <div className="flex items-center gap-3 group">
                             <div className="p-2.5 bg-purple-500/20 rounded-xl group-hover:bg-purple-500/30 transition-colors"><Award size={18} className="text-purple-400" /></div>
                             <div>
                                <p className="text-[9px] font-black text-white/40 uppercase tracking-widest mb-0.5">Practice Authority</p>
                                <p className="text-xs font-bold text-white group-hover:text-purple-300 transition-colors">{synthesis.mostExp.architectName}</p>
                             </div>
                          </div>
                       </div>
                    </div>
                 )}
              </div>
              
              {isChatOpen && (
                 <div className="bg-white rounded-[1.75rem] m-[1px] overflow-hidden">
                    <div className="p-10">
                       <StatisticalHighlights proposals={activeProposals} project={project} />
                       <div className="h-[350px] overflow-y-auto mb-8 space-y-8 pr-4 custom-scrollbar">
                          {messages.map((msg, idx) => (
                             <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                                <div className={`max-w-[85%] p-6 rounded-2xl text-xs leading-relaxed shadow-sm ${
                                   msg.role === 'user' 
                                   ? 'bg-black text-white shadow-md' 
                                   : 'bg-indigo-50/40 text-gray-800 border border-indigo-100'
                                }`}>
                                   {msg.role === 'ai' && <div className="text-[9px] font-bold text-indigo-600 mb-3 uppercase tracking-widest flex items-center gap-2"><Bot size={14}/> Analytical synthesis</div>}
                                   {msg.text.split('\n').map((line, i) => <p key={i} className="mb-2 last:mb-0">{line}</p>)}
                                </div>
                             </div>
                          ))}
                          {loading && (
                            <div className="flex items-center gap-3 ml-2">
                               <div className="flex gap-1">
                                  <div className="w-2 h-2 bg-indigo-600 rounded-full animate-bounce"></div>
                                  <div className="w-2 h-2 bg-indigo-600 rounded-full animate-bounce delay-100"></div>
                                  <div className="w-2 h-2 bg-indigo-600 rounded-full animate-bounce delay-200"></div>
                               </div>
                               <span className="text-[9px] text-gray-400 font-bold uppercase tracking-widest">Auditing frameworks...</span>
                            </div>
                          )}
                          <div ref={messagesEndRef} />
                       </div>
                       <div className="flex gap-4 bg-gray-50/80 p-3 rounded-2xl border border-gray-100 focus-within:border-black focus-within:bg-white transition-all shadow-inner group">
                          <input 
                             value={input}
                             onChange={e => setInput(e.target.value)}
                             onKeyDown={e => e.key === 'Enter' && handleSendMessage()}
                             placeholder="Query analysis (e.g., 'Compare permit handling')"
                             className="flex-1 bg-transparent px-6 py-3 text-xs outline-none font-bold placeholder:text-gray-400"
                          />
                          <button onClick={handleSendMessage} disabled={loading} className="bg-black text-white p-3.5 rounded-full hover:bg-gray-800 transition-all shadow-md">
                             <Send size={18} />
                          </button>
                       </div>
                    </div>
                 </div>
              )}
           </div>
        )}
      </div>

      {/* Formal Bid Registry Table */}
      <div className="bg-white rounded-3xl border border-gray-100 overflow-hidden shadow-sm">
        <div className="p-8 border-b border-gray-100 bg-gray-50/20 flex items-center justify-between">
           <h3 className="text-xl font-bold text-gray-900 tracking-tight">Bid Registry</h3>
           <span className="text-[9px] font-bold text-gray-400 uppercase tracking-widest bg-gray-100 px-4 py-1.5 rounded-full">{proposals.length} Formal Submissions</span>
        </div>
        <div className="overflow-x-auto">
           <table className="w-full text-left border-collapse">
              <thead>
                 <tr className="border-b border-gray-100 bg-gray-50/10">
                    <th className="p-8 py-5 text-[9px] font-bold text-gray-400 uppercase tracking-widest">Architect Entity</th>
                    <th className="p-8 py-5 text-[9px] font-bold text-gray-400 uppercase tracking-widest">Fee Estimate</th>
                    <th className="p-8 py-5 text-[9px] font-bold text-gray-400 uppercase tracking-widest">Timeframe</th>
                    <th className="p-8 py-5 text-[9px] font-bold text-gray-400 uppercase tracking-widest">Merit Score</th>
                    <th className="p-8 py-5 text-[9px] font-bold text-gray-400 uppercase tracking-widest text-right">Action</th>
                 </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                 {proposals.map((proposal) => {
                    const isSelected = compareSlots.some(s => s?.id === proposal.id);
                    return (
                       <tr key={proposal.id} className="hover:bg-gray-50/80 transition-all group">
                          <td className="p-8 py-8">
                             <div className="flex items-center gap-6">
                                <div className="h-12 w-12 rounded-xl bg-gray-900 text-white flex items-center justify-center text-xl font-black shadow-md">
                                   {proposal.architectName.charAt(0)}
                                </div>
                                <div>
                                   <div className="font-bold text-gray-900 text-base">{proposal.architectName}</div>
                                   <div className="text-[9px] uppercase font-bold text-gray-400 tracking-widest mt-1">{proposal.architectType || 'Consultant'}</div>
                                </div>
                             </div>
                          </td>
                          <td className="p-8 py-8 font-bold text-gray-900">{proposal.bidAmount}</td>
                          <td className="p-8 py-8 text-gray-600 font-medium">{proposal.estimatedDuration}</td>
                          <td className="p-8 py-8">
                             <div className="flex items-center gap-1.5 text-gray-900 font-bold bg-gray-50 self-start px-3 py-1.5 rounded-xl border border-gray-100">
                                <Star size={16} className="fill-yellow-400 text-yellow-400" />
                                {proposal.architectRating}
                             </div>
                          </td>
                          <td className="p-8 py-8 text-right flex items-center justify-end gap-6">
                             {proposal.proposalPdf && (
                               <button 
                                 title="Download Proposal PDF"
                                 onClick={(e) => { e.stopPropagation(); alert(`Downloading: ${proposal.proposalPdf}`); }}
                                 className="p-3 text-gray-400 hover:text-black hover:bg-gray-100 rounded-xl transition-all"
                               >
                                 <FileDown size={20} />
                               </button>
                             )}
                             <Button 
                                size="sm" 
                                variant={isSelected ? "primary" : "outline"}
                                className={`text-[9px] font-bold uppercase tracking-widest px-8 py-3.5 ${isSelected ? "bg-black border-black" : "hover:border-black border-gray-200"}`}
                                onClick={() => toggleComparison(proposal)}
                             >
                                {isSelected ? 'In Review' : 'Compare'}
                             </Button>
                             <Button 
                                variant="outline"
                                size="sm" 
                                className="shadow-sm text-[9px] font-bold uppercase tracking-widest px-10 py-3.5 border-gray-200 hover:bg-black hover:text-white transition-all" 
                                onClick={() => handleSelectWinner(proposal)}
                             >
                               Award
                             </Button>
                          </td>
                       </tr>
                    );
                 })}
              </tbody>
           </table>
        </div>
      </div>
    </div>
  );
};

export default ProposalComparison;