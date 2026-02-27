
import React, { useState, useEffect } from 'react';
import { Project, Proposal } from '../types';
import Button from './Button';
import { 
  ArrowLeft, UploadCloud, FileIcon, X, DollarSign, Calendar, CheckSquare, 
  Square, Check, Image as ImageIcon, Briefcase, Award, Info, Sparkles, 
  MapPin, Clock, Eye, ChevronDown, ChevronUp, User, Target, Lightbulb
} from 'lucide-react';
import { DELIVERABLES_GROUPS } from '../constants';

interface SubmitProposalFormProps {
  project: Project;
  onCancel: () => void;
  onSubmit: (proposal: Proposal) => void;
}

const SubmitProposalForm: React.FC<SubmitProposalFormProps> = ({ project, onCancel, onSubmit }) => {
  // UI State
  const [showBrief, setShowBrief] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Section 1: General Info
  const [architectName, setArchitectName] = useState('Studio Architect'); 
  const [architectType, setArchitectType] = useState<'Freelancer' | 'Firm'>('Freelancer');
  const [iaiCertified, setIaiCertified] = useState(false);
  const [bidAmount, setBidAmount] = useState('');
  const [duration, setDuration] = useState('');
  const [revisions, setRevisions] = useState('3');
  const [startDate, setStartDate] = useState('');

  // Section 2: Deliverables
  const [selectedDeliverables, setSelectedDeliverables] = useState<string[]>([]);

  // Section 3: Attachments & Pitch
  const [whyFit, setWhyFit] = useState('');
  const [uploadedImages, setUploadedImages] = useState<Record<string, boolean>>({
    Facade: false,
    Interior: false,
    Massing: false,
    Zoning: false
  });
  const [pdfUploaded, setPdfUploaded] = useState(false);
  
  const requestedDeliverablesList = project.deliverables || [];
  const requestedDeliverablesSet = new Set(requestedDeliverablesList);

  // Initialize selected deliverables
  useEffect(() => {
    // Optionally auto-select requested ones on mount
    setSelectedDeliverables([...requestedDeliverablesList]);
  }, [project.deliverables]);

  const toggleDeliverable = (item: string) => {
    setSelectedDeliverables(prev => 
      prev.includes(item) ? prev.filter(i => i !== item) : [...prev, item]
    );
  };

  const selectAllRequested = () => {
    setSelectedDeliverables(prev => {
      const otherSelected = prev.filter(item => !requestedDeliverablesSet.has(item));
      return [...otherSelected, ...requestedDeliverablesList];
    });
  };

  const toggleImageUpload = (key: string) => {
    setUploadedImages(prev => ({ ...prev, [key]: !prev[key] }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    const MOCK_IMAGE_URL = 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?q=80&w=2000&auto=format&fit=crop';

    setTimeout(() => {
      const newProposal: Proposal = {
        id: Math.random().toString(36).substr(2, 9),
        architectName: architectName,
        projectId: project.id,
        projectTitle: project.title,
        bidAmount: `IDR ${bidAmount}M`, 
        estimatedDuration: duration,
        status: 'Pending',
        submittedDate: 'Just now',
        architectRating: 5.0,
        architectExperience: '10 years',
        coverLetter: whyFit,
        tags: [architectType, iaiCertified ? 'IAI Certified' : ''],
        features: selectedDeliverables,
        proposalImages: [MOCK_IMAGE_URL, MOCK_IMAGE_URL],
        proposalPdf: pdfUploaded ? 'proposal_final.pdf' : undefined,
        architectType,
        iaiCertified,
        revisions,
        availabilityStart: startDate
      };

      onSubmit(newProposal);
    }, 1500);
  };

  const MOCK_IMAGE_URL = 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?q=80&w=2000&auto=format&fit=crop';

  return (
    <div className="max-w-6xl mx-auto py-8 animate-in slide-in-from-bottom-4 fade-in duration-500">
       <div className="flex items-center justify-between mb-8">
         <div className="flex items-center gap-4">
           <Button variant="ghost" onClick={onCancel} icon={<ArrowLeft size={16} />}>
             Cancel
           </Button>
           <h1 className="text-3xl font-bold text-gray-900 tracking-tight">Submit Proposal</h1>
         </div>
         <div className="flex items-center gap-2 px-4 py-2 bg-indigo-50 text-indigo-700 rounded-full text-xs font-bold border border-indigo-100">
            <Sparkles size={14} /> Proposal Strength: High
         </div>
       </div>

       <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
         
         {/* LEFT: FORM (8 columns) */}
         <div className="lg:col-span-8 space-y-8">
           
           {/* PROJECT BRIEF ACCORDION (Mobile optimization & architect navigation) */}
           <div className="bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden lg:hidden">
              <button 
                onClick={() => setShowBrief(!showBrief)}
                className="w-full p-6 flex items-center justify-between bg-gray-50/50"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-white rounded-xl shadow-sm text-gray-900"><Info size={18}/></div>
                  <span className="font-bold text-gray-900">Project Quick Brief</span>
                </div>
                {showBrief ? <ChevronUp size={20}/> : <ChevronDown size={20}/>}
              </button>
              {showBrief && (
                <div className="p-6 border-t border-gray-100 space-y-4">
                  <h3 className="font-bold text-gray-900 text-lg">{project.title}</h3>
                  <p className="text-sm text-gray-600 line-clamp-3">{project.description}</p>
                  <div className="grid grid-cols-2 gap-3 text-xs">
                    <div className="p-2 bg-gray-50 rounded-lg">
                      <span className="text-gray-400 block mb-1">Budget</span>
                      <span className="font-bold text-gray-900">{project.budget}</span>
                    </div>
                    <div className="p-2 bg-gray-50 rounded-lg">
                      <span className="text-gray-400 block mb-1">Lot Type</span>
                      <span className="font-bold text-gray-900">{project.lotType || 'Middle Lot'}</span>
                    </div>
                  </div>
                </div>
              )}
           </div>

           <form onSubmit={handleSubmit} className="space-y-8">
             
             {/* SECTION 1: GENERAL INFORMATION */}
             <div className="bg-white rounded-[2.5rem] shadow-sm border border-gray-100 p-8 md:p-10">
                <div className="flex items-center gap-4 mb-8 border-b border-gray-50 pb-6">
                   <div className="bg-black text-white w-10 h-10 rounded-2xl flex items-center justify-center font-bold text-lg shadow-lg">1</div>
                   <div>
                     <h2 className="text-xl font-bold text-gray-900">Terms & Identity</h2>
                     <p className="text-sm text-gray-500">Define your professional standing and primary bid.</p>
                   </div>
                </div>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                   <div className="md:col-span-2">
                      <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Architect / Firm Name</label>
                      <input 
                        required
                        type="text" 
                        value={architectName}
                        onChange={(e) => setArchitectName(e.target.value)}
                        className="w-full px-5 py-4 rounded-2xl border-2 border-gray-100 focus:border-black outline-none transition-all font-medium bg-gray-50/30"
                      />
                   </div>
                   
                   <div>
                      <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Entity Type</label>
                      <div className="flex gap-3">
                        {(['Freelancer', 'Firm'] as const).map(type => (
                          <button
                            key={type}
                            type="button"
                            onClick={() => setArchitectType(type)}
                            className={`flex-1 py-4 px-4 rounded-2xl border-2 flex items-center justify-center gap-2 transition-all ${
                              architectType === type 
                              ? 'bg-black text-white border-black shadow-md' 
                              : 'bg-white text-gray-500 border-gray-100 hover:border-gray-200'
                            }`}
                          >
                            <Briefcase size={16} />
                            <span className="font-bold text-sm">{type}</span>
                          </button>
                        ))}
                      </div>
                   </div>

                   <div className="flex items-center">
                      <label className="flex items-center gap-4 cursor-pointer p-4 rounded-2xl hover:bg-indigo-50/50 transition-all w-full border-2 border-transparent hover:border-indigo-100 group">
                        <div className={`w-7 h-7 rounded-xl border-2 flex items-center justify-center transition-all ${
                           iaiCertified ? 'bg-indigo-600 border-indigo-600 text-white shadow-lg' : 'bg-white border-gray-200 group-hover:border-indigo-300'
                        }`}>
                           {iaiCertified && <Check size={16} strokeWidth={3}/>}
                        </div>
                        <input 
                          type="checkbox" 
                          className="hidden"
                          checked={iaiCertified}
                          onChange={(e) => setIaiCertified(e.target.checked)}
                        />
                        <div>
                          <span className="block text-sm font-bold text-gray-900">IAI Certified Member</span>
                          <span className="block text-[10px] text-indigo-500 uppercase font-bold tracking-tight">Verified Credential</span>
                        </div>
                      </label>
                   </div>

                   <div>
                      <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Bid Amount</label>
                      <div className="relative group">
                         <span className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400 font-bold text-sm">IDR</span>
                         <input 
                           required
                           type="number" 
                           value={bidAmount}
                           onChange={(e) => setBidAmount(e.target.value)}
                           placeholder="0"
                           className="w-full pl-14 pr-16 py-4 rounded-2xl border-2 border-gray-100 focus:border-black outline-none transition-all font-bold text-lg bg-gray-50/30"
                         />
                         <span className="absolute right-5 top-1/2 -translate-y-1/2 text-gray-400 font-bold text-xs uppercase">Million</span>
                      </div>
                   </div>

                   <div>
                      <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Timeline</label>
                      <div className="relative">
                        <Clock className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-300" size={18} />
                        <input 
                           required
                           type="text" 
                           value={duration}
                           onChange={(e) => setDuration(e.target.value)}
                           placeholder="e.g. 12 Weeks"
                           className="w-full pl-12 pr-5 py-4 rounded-2xl border-2 border-gray-100 focus:border-black outline-none transition-all font-medium bg-gray-50/30"
                        />
                      </div>
                   </div>
                </div>
             </div>

             {/* SECTION 2: DELIVERABLES */}
             <div className="bg-white rounded-[2.5rem] shadow-sm border border-gray-100 p-8 md:p-10">
                <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8 border-b border-gray-50 pb-6">
                   <div className="flex items-center gap-4">
                      <div className="bg-black text-white w-10 h-10 rounded-2xl flex items-center justify-center font-bold text-lg shadow-lg">2</div>
                      <div>
                        <h2 className="text-xl font-bold text-gray-900">Project Deliverables</h2>
                        <p className="text-sm text-gray-500">Items requested by the client are highlighted.</p>
                      </div>
                   </div>
                   <Button 
                      type="button"
                      variant="secondary" 
                      size="sm" 
                      onClick={selectAllRequested}
                      className="bg-indigo-50 text-indigo-700 hover:bg-indigo-100 border-none text-xs font-bold py-3"
                      icon={<CheckSquare size={14}/>}
                   >
                      Select All Requested
                   </Button>
                </div>

                <div className="space-y-8">
                  {Object.entries(DELIVERABLES_GROUPS).map(([group, items]) => (
                    <div key={group} className="space-y-4">
                      <h4 className="text-[10px] font-bold text-gray-400 uppercase tracking-[0.2em]">{group}</h4>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {items.map(item => {
                          const isRequested = requestedDeliverablesSet.has(item);
                          const isSelected = selectedDeliverables.includes(item);
                          return (
                            <label 
                              key={item} 
                              className={`flex items-start gap-4 p-4 rounded-2xl border-2 cursor-pointer transition-all ${
                                isSelected 
                                  ? 'bg-slate-900 border-slate-900 text-white shadow-md scale-[1.02]' 
                                  : isRequested 
                                    ? 'bg-indigo-50 border-indigo-100 text-slate-900 hover:border-indigo-300' 
                                    : 'bg-white border-gray-100 text-gray-400 hover:border-gray-200'
                              }`}
                            >
                              <div className={`mt-0.5 shrink-0 w-5 h-5 rounded-lg border-2 flex items-center justify-center transition-all ${
                                 isSelected ? 'bg-indigo-500 border-indigo-500 text-white' : 'bg-white border-gray-200'
                              }`}>
                                 {isSelected && <Check size={12} strokeWidth={4} />}
                              </div>
                              <input 
                                type="checkbox" 
                                className="hidden"
                                checked={isSelected}
                                onChange={() => toggleDeliverable(item)}
                              />
                              <div className="flex-1">
                                 <span className={`text-sm font-bold block leading-tight ${isSelected ? 'text-white' : 'text-slate-700'}`}>{item}</span>
                                 {isRequested && !isSelected && (
                                   <span className="text-[9px] uppercase font-black text-indigo-600 mt-1 inline-block">Client Requested</span>
                                 )}
                              </div>
                            </label>
                          );
                        })}
                      </div>
                    </div>
                  ))}
                </div>
             </div>

             {/* SECTION 3: ATTACHMENTS & PITCH */}
             <div className="bg-white rounded-[2.5rem] shadow-sm border border-gray-100 p-8 md:p-10">
                <div className="flex items-center gap-4 mb-8 border-b border-gray-50 pb-6">
                   <div className="bg-black text-white w-10 h-10 rounded-2xl flex items-center justify-center font-bold text-lg shadow-lg">3</div>
                   <div>
                     <h2 className="text-xl font-bold text-gray-900">Personal Pitch & Portfolio</h2>
                     <p className="text-sm text-gray-500">Convince the homeowner with your vision.</p>
                   </div>
                </div>

                <div className="space-y-8">
                  <div className="relative">
                     <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">Why you are the best fit</label>
                     
                     {/* AI Pitch Assistant UI */}
                     <div className="mb-6 bg-gradient-to-br from-indigo-600 to-violet-700 rounded-3xl p-6 text-white shadow-xl relative overflow-hidden group">
                        <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-3xl -mr-16 -mt-16 group-hover:scale-150 transition-transform duration-700"></div>
                        <div className="relative z-10">
                          <div className="flex items-center gap-3 mb-4">
                            <div className="bg-white/20 p-2 rounded-xl backdrop-blur-md">
                              <Sparkles size={18} className="text-yellow-300" />
                            </div>
                            <span className="font-bold text-sm tracking-tight">Rumantra AI Pitch Assistant</span>
                          </div>
                          
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                             <div className="space-y-3">
                                <div className="flex items-center gap-2 text-xs font-bold opacity-80 uppercase tracking-wider">
                                   <User size={12}/> Owner Characteristic
                                </div>
                                <p className="text-sm font-medium leading-relaxed">
                                   "Alex values <span className="text-yellow-300">speed</span> and <span className="text-yellow-300">minimalism</span>. Mention your fast permit turnaround."
                                </p>
                             </div>
                             <div className="space-y-3">
                                <div className="flex items-center gap-2 text-xs font-bold opacity-80 uppercase tracking-wider">
                                   <Target size={12}/> Winning Strategy
                                </div>
                                <p className="text-sm font-medium leading-relaxed">
                                   Highlight your <span className="text-yellow-300">Balinese Modern</span> portfolio. It perfectly aligns with their hidden preference.
                                </p>
                             </div>
                          </div>
                          
                          <div className="mt-6 pt-4 border-t border-white/10 flex items-center justify-between">
                             <div className="flex items-center gap-2 text-[10px] font-bold uppercase tracking-widest text-white/60">
                                <Lightbulb size={12} className="text-yellow-400"/> Context-Aware Insights
                             </div>
                             <button type="button" className="text-xs font-bold bg-white text-indigo-700 px-4 py-2 rounded-full shadow-sm hover:scale-105 transition-all">
                                Auto-Draft Pitch
                             </button>
                          </div>
                        </div>
                     </div>

                     <textarea 
                       required
                       rows={6}
                       value={whyFit}
                       onChange={(e) => setWhyFit(e.target.value)}
                       placeholder="Tailor your pitch based on AI insights above..."
                       className="w-full p-6 rounded-3xl border-2 border-gray-100 focus:border-black outline-none resize-none transition-all font-medium bg-gray-50/20 leading-relaxed"
                     />
                  </div>

                  <div>
                    <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-4">Key Proposal Visuals</label>
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                      {['Facade', 'Interior', 'Massing', 'Zoning'].map((label) => (
                        <div 
                          key={label}
                          onClick={() => toggleImageUpload(label)}
                          className={`aspect-square rounded-3xl border-2 border-dashed flex flex-col items-center justify-center cursor-pointer transition-all relative overflow-hidden ${
                            uploadedImages[label] 
                            ? 'border-indigo-500 bg-indigo-50' 
                            : 'border-gray-100 bg-gray-50/50 hover:border-gray-300 hover:bg-gray-100'
                          }`}
                        >
                          {uploadedImages[label] ? (
                            <>
                               <img 
                                 src={MOCK_IMAGE_URL} 
                                 alt={label} 
                                 className="absolute inset-0 w-full h-full object-cover opacity-80"
                               />
                               <div className="absolute inset-0 flex items-center justify-center bg-black/40 backdrop-blur-[2px]">
                                  <div className="bg-indigo-600 text-white px-3 py-1.5 rounded-xl text-[10px] font-bold flex items-center gap-1 shadow-lg">
                                    <Check size={12} strokeWidth={3}/> {label}
                                  </div>
                               </div>
                            </>
                          ) : (
                            <>
                              <div className="w-10 h-10 rounded-2xl bg-white flex items-center justify-center mb-2 shadow-sm">
                                 <ImageIcon size={18} className="text-gray-400" />
                              </div>
                              <span className="text-[10px] font-bold text-gray-500 uppercase tracking-tighter">{label}</span>
                              <span className="text-[9px] text-gray-400 mt-1">+ Upload</span>
                            </>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>

                  <div>
                     <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Final Proposal Document (PDF)</label>
                     <div 
                       onClick={() => setPdfUploaded(!pdfUploaded)}
                       className={`border-2 border-gray-100 rounded-3xl p-5 flex items-center justify-between cursor-pointer transition-all ${
                         pdfUploaded ? 'bg-indigo-50 border-indigo-200' : 'hover:border-gray-300 hover:bg-gray-50'
                       }`}
                     >
                        <div className="flex items-center gap-4">
                           <div className={`h-12 w-12 rounded-2xl flex items-center justify-center shadow-sm ${pdfUploaded ? 'bg-indigo-600 text-white' : 'bg-white text-gray-400'}`}>
                              <FileIcon size={24} />
                           </div>
                           <div>
                              <p className="text-sm font-bold text-gray-900">{pdfUploaded ? 'Detailed_Proposal_v2.pdf' : 'Attach PDF Document'}</p>
                              <p className="text-[10px] font-bold text-gray-400 uppercase tracking-wider">{pdfUploaded ? '2.4 MB • READY' : 'Professional Quotation'}</p>
                           </div>
                        </div>
                        {pdfUploaded ? (
                           <button className="bg-white text-gray-400 hover:text-red-500 p-2 rounded-full shadow-sm transition-colors"><X size={18} /></button>
                        ) : (
                           <Button variant="outline" size="sm" className="bg-white">Browse</Button>
                        )}
                     </div>
                  </div>
                </div>
             </div>

             {/* Submit Footer */}
             <div className="flex items-center justify-end gap-6 pt-4">
                <button 
                   type="button" 
                   onClick={onCancel}
                   className="text-sm font-bold text-gray-400 hover:text-gray-900 transition-colors"
                >
                  Discard Draft
                </button>
                <Button 
                  type="submit" 
                  size="lg"
                  disabled={isSubmitting}
                  className={`px-12 shadow-2xl ${isSubmitting ? 'opacity-70 cursor-wait' : ''}`}
                >
                  {isSubmitting ? 'Submitting...' : 'Send Final Proposal'}
                </Button>
             </div>
           </form>
         </div>

         {/* RIGHT: PROJECT SIDEBAR (4 columns) */}
         <div className="lg:col-span-4 hidden lg:block sticky top-28 space-y-6">
            <div className="bg-white rounded-[2.5rem] border border-gray-100 shadow-sm overflow-hidden p-8">
               <div className="flex items-center gap-3 mb-6">
                  <div className="p-2 bg-slate-900 rounded-xl text-white shadow-lg"><Eye size={18}/></div>
                  <h3 className="font-bold text-gray-900 text-lg">Project Reference</h3>
               </div>
               
               <div className="space-y-6">
                  <div>
                    <h4 className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-2">The Brief</h4>
                    <h3 className="font-bold text-gray-900 leading-tight mb-2">{project.title}</h3>
                    <p className="text-sm text-gray-500 leading-relaxed italic line-clamp-4">
                      "{project.description}"
                    </p>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1">
                      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block">Budget</span>
                      <span className="text-sm font-bold text-green-600 flex items-center gap-1"><DollarSign size={14}/> {project.budget}</span>
                    </div>
                    <div className="space-y-1">
                      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block">Start Date</span>
                      <span className="text-sm font-bold text-gray-900 flex items-center gap-1"><Calendar size={14}/> {project.expectedDuration || 'TBD'}</span>
                    </div>
                    <div className="space-y-1">
                      <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest block">Lot Context</span>
                      <span className="text-sm font-bold text-gray-900 flex items-center gap-1"><MapPin size={14}/> {project.lotType || 'Normal'}</span>
                    </div>
                  </div>

                  <div className="pt-6 border-t border-gray-50">
                    <h4 className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-3">Homeowner Request</h4>
                    <div className="flex flex-wrap gap-2">
                       {requestedDeliverablesList.map(req => (
                         <span key={req} className="px-3 py-1.5 bg-gray-100 text-gray-600 rounded-full text-[10px] font-bold">
                           {req}
                         </span>
                       ))}
                    </div>
                  </div>

                  <Button variant="outline" size="sm" className="w-full mt-4 text-[10px] font-black uppercase tracking-widest">
                     View Full Project Details
                  </Button>
               </div>
            </div>

            <div className="bg-amber-50 rounded-[2.5rem] p-8 border border-amber-100">
               <h4 className="font-bold text-amber-900 flex items-center gap-2 mb-3">
                  <Lightbulb size={18} className="text-amber-600"/> Pro Tip
               </h4>
               <p className="text-xs text-amber-800 leading-relaxed font-medium">
                  Homeowners are <b>40% more likely</b> to accept proposals that include "3D Visualization" and a detailed "Specifications Book". 
                  <br/><br/>
                  Make sure to attach a custom facade render to stand out!
               </p>
            </div>
         </div>

       </div>
    </div>
  );
};

export default SubmitProposalForm;
