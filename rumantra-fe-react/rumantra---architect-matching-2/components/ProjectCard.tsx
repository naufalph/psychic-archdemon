import React from 'react';
import { Project, UserRole } from '../types';
import Button from './Button';
import { MapPin, Clock, DollarSign, ArrowRight } from 'lucide-react';

interface ProjectCardProps {
  project: Project;
  role: UserRole;
  onAction?: () => void;
}

const ProjectCard: React.FC<ProjectCardProps> = ({ project, role, onAction }) => {
  const getStatusLabel = (status: string) => {
    switch(status) {
      case 'Open': return 'Open';
      case 'In Progress': return 'In Progress';
      case 'Completed': return 'Completed';
      case 'Contracting': return 'Contracting';
      case 'Draft': return 'Draft';
      default: return status;
    }
  };

  return (
    <div className="bg-white rounded-3xl border border-gray-100 overflow-hidden shadow-sm hover:shadow-lg transition-all flex flex-col h-full group">
      <div className="relative h-48 overflow-hidden">
        <img 
          src={project.imageUrl} 
          alt={project.title} 
          className="w-full h-full object-cover transition-transform group-hover:scale-105"
        />
        <div className="absolute top-4 right-4 bg-white/90 backdrop-blur-sm px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider text-gray-900 shadow-sm">
          {project.type}
        </div>
        <div className={`absolute bottom-4 left-4 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider shadow-sm ${
          project.status === 'Open' ? 'bg-green-100 text-green-800' : 
          project.status === 'In Progress' ? 'bg-blue-100 text-blue-800' : 
          'bg-gray-100 text-gray-800'
        }`}>
          {getStatusLabel(project.status)}
        </div>
      </div>
      
      <div className="p-6 flex-1 flex flex-col">
        <h3 className="text-lg font-bold text-gray-900 mb-2 leading-tight">{project.title}</h3>
        <p className="text-gray-500 text-sm mb-6 line-clamp-2 flex-1">{project.description}</p>
        
        <div className="space-y-3 mb-6">
          <div className="flex items-center text-xs text-gray-600">
            <MapPin size={14} className="mr-2 text-gray-400" />
            {project.location}
          </div>
          <div className="flex items-center text-xs font-semibold text-gray-900">
            <DollarSign size={14} className="mr-2 text-green-600" />
            {project.budget}
          </div>
          <div className="flex items-center text-xs text-gray-400">
            <Clock size={14} className="mr-2" />
            Posted {project.postedDate}
          </div>
        </div>

        <div className="mt-auto pt-4 border-t border-gray-50 flex items-center justify-between">
          <div className="text-xs font-bold text-gray-900">
            {project.proposalsCount} <span className="text-gray-400">Bids</span>
          </div>
          <Button 
            onClick={onAction}
            variant="ghost" 
            size="sm" 
            className="!px-0 hover:bg-transparent font-bold text-[11px] uppercase tracking-wider"
          >
            {role === 'architect' ? 'View Brief' : 'Manage'} <ArrowRight size={14} className="ml-1" />
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ProjectCard;