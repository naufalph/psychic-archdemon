import React, { useState } from 'react';
import { FileText, Download, CheckCircle, AlertCircle, Eye } from 'lucide-react';
import { Deliverable, PhaseStatus } from '../types';
import { Button } from './Button';

interface Props {
  deliverable: Deliverable;
  phaseStatus: PhaseStatus;
}

export const DeliverableReview: React.FC<Props> = ({ deliverable, phaseStatus }) => {
  const [feedbackOpen, setFeedbackOpen] = useState(false);

  return (
    <div className="bg-white rounded-2xl border border-gray-200 overflow-hidden flex flex-col lg:flex-row h-[500px]">
      {/* Visual Preview */}
      <div className="flex-1 bg-gray-50 relative flex items-center justify-center p-8 group overflow-hidden">
        {/* Mock Blueprint Background */}
        <div className="absolute inset-0 opacity-10" 
             style={{ 
               backgroundImage: 'radial-gradient(#000 1px, transparent 1px)', 
               backgroundSize: '20px 20px' 
             }}>
        </div>
        
        <div className="relative shadow-xl transition-transform transform group-hover:scale-105 duration-500">
             <img 
                src={deliverable.previewUrl || `https://picsum.photos/seed/${deliverable.id}/600/800`} 
                alt="Deliverable Preview" 
                className="max-h-[400px] w-auto object-cover border border-gray-200 rounded-sm bg-white"
             />
             <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-5 transition-all"></div>
        </div>

        <div className="absolute bottom-6 right-6 flex space-x-2">
            <button className="bg-white/90 backdrop-blur text-gray-700 p-2 rounded-full shadow hover:bg-white transition">
                <Eye size={20} />
            </button>
            <button className="bg-white/90 backdrop-blur text-gray-700 p-2 rounded-full shadow hover:bg-white transition">
                <Download size={20} />
            </button>
        </div>
      </div>

      {/* Review Actions */}
      <div className="w-full lg:w-80 border-t lg:border-t-0 lg:border-l border-gray-200 p-6 flex flex-col">
        <div className="mb-6">
            <div className="flex items-center space-x-2 mb-2">
                <FileText className="text-gray-400" size={20} />
                <h3 className="font-semibold text-gray-900 truncate" title={deliverable.title}>
                    {deliverable.title}
                </h3>
            </div>
            <p className="text-xs text-gray-500 uppercase tracking-wide font-medium">Version {deliverable.version} • {deliverable.dateUploaded}</p>
        </div>

        <div className="flex-1 overflow-y-auto">
            <div className="bg-gray-50 p-4 rounded-xl mb-4">
                <h4 className="text-xs font-semibold text-gray-500 uppercase mb-2">Architect Notes</h4>
                <p className="text-sm text-gray-700 leading-relaxed">
                    Adjusted the kitchen island orientation as requested. Please review the clearance between the island and the pantry.
                </p>
            </div>
            
            {feedbackOpen && (
                <div className="mb-4 animate-fade-in">
                    <label className="text-xs font-semibold text-gray-500 uppercase mb-2 block">Your Feedback</label>
                    <textarea 
                        className="w-full p-3 text-sm border border-gray-200 rounded-xl focus:ring-2 focus:ring-gray-900 focus:outline-none resize-none" 
                        rows={4} 
                        placeholder="Describe what needs to be changed..."
                    ></textarea>
                     <div className="flex justify-end mt-2">
                        <Button size="sm" onClick={() => setFeedbackOpen(false)}>Send Request</Button>
                    </div>
                </div>
            )}
        </div>

        {phaseStatus === PhaseStatus.IN_REVIEW && !feedbackOpen && (
            <div className="space-y-3 pt-6 border-t border-gray-100">
                <Button className="w-full justify-center" icon={<CheckCircle size={18} />}>
                    Approve Drawing
                </Button>
                <Button 
                    variant="outline" 
                    className="w-full justify-center" 
                    icon={<AlertCircle size={18} />}
                    onClick={() => setFeedbackOpen(true)}
                >
                    Request Revision
                </Button>
            </div>
        )}
        
        {phaseStatus === PhaseStatus.COMPLETED && (
             <div className="pt-6 border-t border-gray-100 text-center">
                 <span className="inline-flex items-center text-green-600 font-medium text-sm bg-green-50 px-3 py-1 rounded-full">
                    <CheckCircle size={16} className="mr-2" />
                    Approved
                 </span>
             </div>
        )}
      </div>
    </div>
  );
};