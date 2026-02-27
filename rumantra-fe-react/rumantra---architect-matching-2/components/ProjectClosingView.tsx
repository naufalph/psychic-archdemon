import React, { useState, useEffect } from 'react';
import { Project, Proposal } from '../types';
import Button from './Button';
import { 
  ArrowLeft, CheckCircle2, Star, Download, ShieldCheck, 
  HardHat, ArrowRight, Calendar, DollarSign, FileCheck 
} from 'lucide-react';
import confetti from 'canvas-confetti';

interface ProjectClosingViewProps {
  project: Project;
  proposal: Proposal;
  onBack: () => void;
  onExploreContractors: () => void; // New prop
}

const ProjectClosingView: React.FC<ProjectClosingViewProps> = ({ project, proposal, onBack, onExploreContractors }) => {
  const [rating, setRating] = useState(0);
  const [review, setReview] = useState('');
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    // Celebration confetti on mount
    const duration = 3000;
    const end = Date.now() + duration;

    const frame = () => {
      confetti({ particleCount: 3, angle: 60, spread: 55, origin: { x: 0 }, colors: ['#000', '#333', '#666'] });
      confetti({ particleCount: 3, angle: 120, spread: 55, origin: { x: 1 }, colors: ['#000', '#333', '#666'] });

      if (Date.now() < end) {
        requestAnimationFrame(frame);
      }
    };
    frame();
  }, []);

  const handleSubmitReview = () => {
    setSubmitted(true);
    // Logic to save review would go here
  };

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 duration-500 pb-20 max-w-5xl mx-auto">
      
      {/* Header */}
      <div className="mb-8">
        <Button variant="ghost" onClick={onBack} className="mb-4 !p-2 rounded-full h-10 w-10 bg-white border border-gray-200 hover:bg-gray-100">
          <ArrowLeft size={20} />
        </Button>
        <div className="flex items-center gap-3 mb-2">
           <div className="bg-green-100 text-green-700 p-2 rounded-full">
              <CheckCircle2 size={24} />
           </div>
           <h2 className="text-3xl font-bold text-gray-900">Project Handover Complete</h2>
        </div>
        <p className="text-gray-500 text-lg">
          Congratulations! <span className="font-semibold text-black">{project.title}</span> has been successfully delivered by <span className="font-semibold text-black">{proposal.architectName}</span>.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* LEFT COLUMN: SUMMARY & REVIEW */}
        <div className="lg:col-span-2 space-y-8">
          
          {/* Performance Summary */}
          <section className="bg-white rounded-3xl border border-gray-200 p-8 shadow-sm">
             <h3 className="text-lg font-bold text-gray-900 mb-6 flex items-center gap-2">
                <FileCheck size={20} /> Performance Summary
             </h3>
             
             <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                   <p className="text-xs text-gray-500 uppercase font-bold tracking-wider mb-1">Final Cost</p>
                   <p className="text-xl font-bold text-gray-900">{proposal.bidAmount}</p>
                   <div className="flex items-center gap-1 text-xs text-green-600 font-medium mt-1">
                      <CheckCircle2 size={12} /> Paid in Full
                   </div>
                </div>
                <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                   <p className="text-xs text-gray-500 uppercase font-bold tracking-wider mb-1">Duration</p>
                   <p className="text-xl font-bold text-gray-900">{proposal.estimatedDuration}</p>
                   <div className="flex items-center gap-1 text-xs text-green-600 font-medium mt-1">
                      <CheckCircle2 size={12} /> On Time
                   </div>
                </div>
                <div className="bg-gray-50 p-4 rounded-2xl border border-gray-100">
                   <p className="text-xs text-gray-500 uppercase font-bold tracking-wider mb-1">Revisions Used</p>
                   <p className="text-xl font-bold text-gray-900">2 <span className="text-gray-400 text-sm font-normal">/ {proposal.revisions || '3'}</span></p>
                   <div className="flex items-center gap-1 text-xs text-blue-600 font-medium mt-1">
                      Within Limit
                   </div>
                </div>
             </div>

             <div className="border-t border-gray-100 pt-6">
                <h4 className="text-sm font-bold text-gray-900 mb-4">Final Deliverables</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                   {(proposal.features || []).map((item, i) => (
                      <div key={i} className="flex items-center gap-3 p-3 rounded-xl bg-gray-50 border border-gray-100 text-sm">
                         <div className="bg-green-500 text-white rounded-full p-0.5">
                            <CheckCircle2 size={14} />
                         </div>
                         <span className="text-gray-700">{item}</span>
                      </div>
                   ))}
                   <div className="flex items-center gap-3 p-3 rounded-xl bg-gray-50 border border-gray-100 text-sm">
                      <div className="bg-green-500 text-white rounded-full p-0.5">
                         <CheckCircle2 size={14} />
                      </div>
                      <span className="text-gray-700">Signed Handover Document</span>
                   </div>
                </div>
                <div className="mt-6 flex justify-end">
                   <Button variant="outline" size="sm" icon={<Download size={16}/>}>Download All Assets (ZIP)</Button>
                </div>
             </div>
          </section>

          {/* Review Form */}
          <section className="bg-white rounded-3xl border border-gray-200 p-8 shadow-sm">
             <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-bold text-gray-900">Rate your experience</h3>
                {submitted && <span className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-xs font-bold">Sent</span>}
             </div>

             {!submitted ? (
                <div className="space-y-6">
                   <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">How was working with {proposal.architectName}?</label>
                      <div className="flex gap-2">
                         {[1, 2, 3, 4, 5].map((star) => (
                            <button 
                              key={star}
                              onClick={() => setRating(star)}
                              className={`p-2 rounded-full transition-all ${rating >= star ? 'text-yellow-400 scale-110' : 'text-gray-300 hover:text-gray-400'}`}
                            >
                               <Star size={32} fill={rating >= star ? "currentColor" : "none"} />
                            </button>
                         ))}
                      </div>
                   </div>
                   
                   <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Write a review</label>
                      <textarea 
                         value={review}
                         onChange={(e) => setReview(e.target.value)}
                         placeholder="Share your feedback to help others..."
                         rows={4}
                         className="w-full p-4 rounded-xl border border-gray-200 focus:ring-1 focus:ring-black outline-none resize-none"
                      />
                   </div>

                   <div className="flex justify-end">
                      <Button onClick={handleSubmitReview} disabled={!rating}>Submit Review</Button>
                   </div>
                </div>
             ) : (
                <div className="bg-gray-50 rounded-xl p-6 text-center">
                   <div className="h-12 w-12 bg-white rounded-full shadow-sm flex items-center justify-center mx-auto mb-3 text-yellow-400">
                      <Star size={24} fill="currentColor" />
                   </div>
                   <h4 className="font-bold text-gray-900">Thank you for your feedback!</h4>
                   <p className="text-sm text-gray-500 mt-1">Your review helps maintain quality on Rumantra.</p>
                </div>
             )}
          </section>

        </div>

        {/* RIGHT COLUMN: CTA */}
        <div className="space-y-6">
           {/* Contractor CTA */}
           <div className="bg-black text-white rounded-3xl p-8 shadow-xl relative overflow-hidden">
               <div className="absolute top-0 right-0 w-32 h-32 bg-yellow-500 rounded-full blur-[60px] opacity-20"></div>
               <div className="relative z-10">
                  <div className="bg-white/10 w-12 h-12 rounded-2xl flex items-center justify-center mb-6 backdrop-blur-md">
                     <HardHat size={24} className="text-yellow-400" />
                  </div>
                  <h3 className="text-2xl font-bold mb-2">Ready to build?</h3>
                  <p className="text-gray-400 text-sm mb-8 leading-relaxed">
                     Now that your design is final, let's make it real. Connect with our vetted premium contractors.
                  </p>
                  <Button 
                    variant="primary" 
                    className="w-full bg-white text-black hover:bg-gray-100 border-none"
                    onClick={onExploreContractors}
                  >
                     Explore Contractors <ArrowRight size={16} />
                  </Button>
               </div>
           </div>

           {/* Legal & Warranty */}
           <div className="bg-white rounded-3xl border border-gray-200 p-6">
              <div className="flex items-center gap-3 mb-4">
                 <ShieldCheck size={24} className="text-blue-600" />
                 <h4 className="font-bold text-gray-900">Rumantra Warranty</h4>
              </div>
              <p className="text-sm text-gray-500 mb-4">
                 Your project designs are archived securely. You have 30 days to report any file issues to the architect.
              </p>
              <Button variant="outline" size="sm" className="w-full">View Warranty Terms</Button>
           </div>
        </div>

      </div>
    </div>
  );
};

export default ProjectClosingView;