import React, { useState } from 'react';
import Button from './Button';
import { ArrowLeft, Star, MapPin, CheckCircle2, MessageSquare, ShieldCheck, Ruler, Briefcase } from 'lucide-react';

interface ExploreContractorsProps {
  onBack: () => void;
}

const CONTRACTORS = [
  {
    id: 1,
    name: 'Wijaya Construction',
    type: 'General Contractor',
    location: 'Denpasar, Bali',
    rating: 4.8,
    reviews: 124,
    projectsDone: 45,
    experience: '12 Years',
    priceRange: 'IDR 5,000,000 - 6,500,000',
    specialty: 'Residential & Renovation',
    verified: true,
    image: 'https://images.unsplash.com/photo-1541888946425-d81bb19240f5?q=80&w=1000&auto=format&fit=crop'
  },
  {
    id: 2,
    name: 'Bali Modern Builders',
    type: 'Villa Specialist',
    location: 'Canggu, Bali',
    rating: 4.9,
    reviews: 86,
    projectsDone: 32,
    experience: '8 Years',
    priceRange: 'IDR 7,000,000 - 8,500,000',
    specialty: 'Modern Villa & Commercial',
    verified: true,
    image: 'https://images.unsplash.com/photo-1621619856624-42fd193a0661?q=80&w=1000&auto=format&fit=crop'
  },
  {
    id: 3,
    name: 'Elite Structures ID',
    type: 'Luxury Contractor',
    location: 'Ubud, Bali',
    rating: 5.0,
    reviews: 42,
    projectsDone: 18,
    experience: '15 Years',
    priceRange: 'IDR 9,000,000 - 10,000,000',
    specialty: 'High-End Luxury Homes',
    verified: true,
    image: 'https://images.unsplash.com/photo-1503387762-592deb58ef4e?q=80&w=1000&auto=format&fit=crop'
  }
];

const ExploreContractors: React.FC<ExploreContractorsProps> = ({ onBack }) => {
  const [contacted, setContacted] = useState<number[]>([]);

  const handleChat = (id: number) => {
    setContacted([...contacted, id]);
    alert("Chat request sent! The contractor will reply shortly.");
  };

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 duration-500 pb-20 max-w-6xl mx-auto">
      
      {/* Header */}
      <div className="mb-8">
        <Button variant="ghost" onClick={onBack} className="mb-4 !p-2 rounded-full h-10 w-10 bg-white border border-gray-200 hover:bg-gray-100">
          <ArrowLeft size={20} />
        </Button>
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
          <div>
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Curated Contractors</h2>
            <p className="text-gray-500">
              Based on your design style and budget, here are the top vetted builders suitable for this project.
            </p>
          </div>
          <div className="flex items-center gap-2 bg-green-50 text-green-700 px-4 py-2 rounded-full text-sm font-medium border border-green-100">
             <ShieldCheck size={18} /> All contractors verified & insured
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {CONTRACTORS.map((contractor) => (
          <div key={contractor.id} className="bg-white rounded-3xl border border-gray-200 overflow-hidden hover:shadow-lg transition-shadow duration-300 flex flex-col group">
             {/* Image Header */}
             <div className="h-48 overflow-hidden relative">
                <img 
                  src={contractor.image} 
                  alt={contractor.name} 
                  className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                />
                <div className="absolute top-4 left-4 bg-white/90 backdrop-blur-md px-3 py-1 rounded-full text-xs font-bold text-gray-900">
                   {contractor.type}
                </div>
                {contractor.verified && (
                   <div className="absolute bottom-4 right-4 bg-blue-600 text-white px-2 py-1 rounded-lg text-[10px] font-bold flex items-center gap-1 shadow-sm">
                      <CheckCircle2 size={12} /> VERIFIED
                   </div>
                )}
             </div>

             {/* Content */}
             <div className="p-6 flex-1 flex flex-col">
                <div className="flex justify-between items-start mb-2">
                   <div>
                      <h3 className="text-lg font-bold text-gray-900">{contractor.name}</h3>
                      <div className="flex items-center text-gray-500 text-sm gap-1">
                         <MapPin size={14} /> {contractor.location}
                      </div>
                   </div>
                   <div className="flex flex-col items-end">
                      <div className="flex items-center gap-1 bg-yellow-50 px-2 py-1 rounded-lg">
                         <Star size={14} className="fill-yellow-400 text-yellow-400" />
                         <span className="text-sm font-bold text-gray-900">{contractor.rating}</span>
                      </div>
                      <span className="text-[10px] text-gray-400 mt-1">{contractor.reviews} reviews</span>
                   </div>
                </div>

                <div className="grid grid-cols-2 gap-3 py-4 border-b border-gray-100 mb-4">
                   <div className="bg-gray-50 p-2 rounded-xl">
                      <p className="text-[10px] text-gray-400 uppercase font-bold tracking-wider mb-1 flex items-center gap-1"><Briefcase size={10}/> Experience</p>
                      <p className="text-sm font-semibold text-gray-900">{contractor.experience}</p>
                   </div>
                   <div className="bg-gray-50 p-2 rounded-xl">
                      <p className="text-[10px] text-gray-400 uppercase font-bold tracking-wider mb-1 flex items-center gap-1"><CheckCircle2 size={10}/> Projects</p>
                      <p className="text-sm font-semibold text-gray-900">{contractor.projectsDone}+ Built</p>
                   </div>
                </div>
                
                <div className="mb-6">
                   <p className="text-[10px] text-gray-400 uppercase font-bold tracking-wider mb-2 flex items-center gap-1"><Ruler size={10}/> Est. Build Cost / m²</p>
                   <p className="text-lg font-bold text-gray-900">{contractor.priceRange}</p>
                   <p className="text-xs text-gray-500 mt-1">Includes material & labor</p>
                </div>

                <div className="mt-auto">
                   <Button 
                      onClick={() => handleChat(contractor.id)}
                      disabled={contacted.includes(contractor.id)}
                      className={`w-full justify-center ${contacted.includes(contractor.id) ? 'bg-green-600 border-green-600 hover:bg-green-700' : ''}`}
                      icon={contacted.includes(contractor.id) ? <CheckCircle2 size={16}/> : <MessageSquare size={16}/>}
                   >
                      {contacted.includes(contractor.id) ? 'Request Sent' : 'Chat & Request Quote'}
                   </Button>
                </div>
             </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ExploreContractors;