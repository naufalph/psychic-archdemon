
import React from 'react';
import { Project } from '../types';
import Button from './Button';
import { ArrowLeft, MapPin, Calendar, Clock, DollarSign, Share2, Flag, FileText, CheckCircle, ExternalLink } from 'lucide-react';

interface ProjectDetailViewProps {
  project: Project;
  onBack: () => void;
  onApply: () => void;
}

const ProjectDetailView: React.FC<ProjectDetailViewProps> = ({ project, onBack, onApply }) => {
  return (
    <div className="max-w-4xl mx-auto py-4 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="mb-6">
        <Button variant="ghost" onClick={onBack} icon={<ArrowLeft size={16} />}>Back to Marketplace</Button>
      </div>

      {/* Hero Header */}
      <div className="relative rounded-3xl overflow-hidden bg-gray-900 text-white mb-8 shadow-xl">
        <img 
          src={project.imageUrl} 
          alt={project.title} 
          className="w-full h-64 object-cover opacity-60"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent"></div>
        <div className="absolute bottom-0 left-0 p-8 w-full">
          <div className="flex flex-wrap items-center gap-3 mb-3">
             <span className="bg-white/20 backdrop-blur-md px-3 py-1 rounded-full text-xs font-semibold">{project.type}</span>
             <span className={`px-3 py-1 rounded-full text-xs font-semibold ${
               project.status === 'Open' ? 'bg-green-500/20 text-green-100 backdrop-blur-md' : 'bg-gray-500/20 text-gray-200'
             }`}>
               {project.status}
             </span>
          </div>
          <h1 className="text-3xl md:text-4xl font-bold mb-2">{project.title}</h1>
          <div className="flex flex-col md:flex-row md:items-center gap-4 text-gray-300 text-sm">
            <span className="flex items-center gap-1"><MapPin size={16} /> {project.location}</span>
            {project.locationMapLink && (
              <a href={project.locationMapLink} target="_blank" rel="noreferrer" className="flex items-center gap-1 text-blue-300 hover:text-white transition-colors underline">
                <ExternalLink size={14} /> View Map
              </a>
            )}
            <span className="flex items-center gap-1 md:ml-4"><Clock size={16} /> Posted {project.postedDate}</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-8">
          <section>
            <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <FileText size={20} /> Project Description
            </h3>
            <p className="text-gray-600 leading-relaxed whitespace-pre-line">
              {project.description}
            </p>
          </section>

          {(project.landArea || project.buildArea) && (
             <section>
                <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
                  <MapPin size={20} /> Site Details
                </h3>
                <div className="grid grid-cols-2 gap-4 bg-gray-50 p-6 rounded-2xl border border-gray-100">
                   {project.landArea && (
                      <div>
                         <p className="text-xs text-gray-500 uppercase font-bold tracking-wider mb-1">Land Area</p>
                         <p className="font-semibold text-gray-900">{project.landArea} m²</p>
                         {project.landFrontage && project.landDepth && (
                            <p className="text-xs text-gray-500 mt-1">{project.landFrontage}m x {project.landDepth}m</p>
                         )}
                      </div>
                   )}
                   {project.buildArea && (
                      <div>
                         <p className="text-xs text-gray-500 uppercase font-bold tracking-wider mb-1">Build Area</p>
                         <p className="font-semibold text-gray-900">{project.buildArea} m²</p>
                         <p className="text-xs text-gray-500 mt-1">{project.lotType}</p>
                      </div>
                   )}
                </div>
             </section>
          )}

          <section>
            <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
              <CheckCircle size={20} /> Deliverables Requested
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {project.deliverables?.map((item, idx) => (
                <div key={idx} className="flex items-start gap-2 p-3 rounded-lg bg-gray-50 border border-gray-100 text-sm text-gray-700">
                  <div className="mt-0.5 w-1.5 h-1.5 rounded-full bg-black shrink-0" />
                  {item}
                </div>
              ))}
              {(!project.deliverables || project.deliverables.length === 0) && (
                 <p className="text-gray-500 italic">No specific deliverables listed.</p>
              )}
            </div>
          </section>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm sticky top-24">
             <div className="mb-6">
                <p className="text-sm text-gray-500 font-medium mb-1">Estimated Budget</p>
                <div className="flex items-center gap-2 text-2xl font-bold text-gray-900">
                   <DollarSign className="text-green-600" size={24} />
                   {project.budget}
                </div>
             </div>

             <div className="mb-8">
                <p className="text-sm text-gray-500 font-medium mb-1">Timeline</p>
                <div className="flex items-center gap-2 text-lg font-semibold text-gray-900">
                   <Calendar className="text-gray-400" size={20} />
                   {project.expectedDuration || 'Flexible'}
                </div>
             </div>

             <Button className="w-full mb-3 shadow-lg" size="lg" onClick={onApply}>
               Submit Proposal
             </Button>
             
             <div className="flex items-center justify-between text-sm text-gray-500">
               <button className="flex items-center gap-1 hover:text-black transition-colors"><Share2 size={14} /> Share</button>
               <button className="flex items-center gap-1 hover:text-red-600 transition-colors"><Flag size={14} /> Report</button>
             </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProjectDetailView;