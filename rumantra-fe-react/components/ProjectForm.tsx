import React, { useState } from 'react';
import { Project } from '../types';
import { MapPin, Home, DollarSign, FileText, Ruler, Briefcase, CheckSquare, Calculator, Layers, Info } from 'lucide-react';

interface Props {
  onSubmit: (project: Project) => void;
  onCancel: () => void;
}

const DELIVERABLES_GROUPS = [
  {
    title: "Schematic & Concept",
    items: ["Schematic Design", "3D Visualization"]
  },
  {
    title: "Preliminary Design (IMB/PBG Requirements)",
    items: [
      "Lembar Konsep & Tema",
      "Mood Board",
      "3D Rendering Visualisation Exterior",
      "3D Rendering Visualisation Interior",
      "Site Plan",
      "Denah Lantai (Floor Plan)",
      "Potongan (Sections)",
      "Tampak Depan (Front Elevation)",
      "Tampak Belakang (Rear Elevation)",
      "Tampak Kiri/Kanan (Side Elevations)",
      "Spesifikasi Material"
    ]
  },
  {
    title: "Detailed Engineering Design (DED)",
    items: [
      "Architectural Detail Drawings",
      "Structural Drawings",
      "MEP (Mechanical, Electrical, Plumbing)"
    ]
  },
  {
    title: "Cost Estimation",
    items: ["BoQ / RAB (Bill of Quantities)"]
  }
];

const ProjectForm: React.FC<Props> = ({ onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    title: '',
    location: '',
    lotSize: '',
    buildingType: 'Residential',
    description: '',
    totalBudget: '',
    designBudget: ''
  });

  const [selectedDeliverables, setSelectedDeliverables] = useState<string[]>([]);

  const toggleDeliverable = (item: string) => {
    setSelectedDeliverables(prev => 
      prev.includes(item) ? prev.filter(i => i !== item) : [...prev, item]
    );
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const newProject: Project = {
      id: crypto.randomUUID(),
      title: formData.title,
      location: formData.location,
      lotSize: Number(formData.lotSize),
      buildingType: formData.buildingType,
      budget: formData.designBudget, // Use Design Budget as main display
      totalBudget: formData.totalBudget,
      designBudget: formData.designBudget,
      description: formData.description,
      deliverables: selectedDeliverables,
      createdAt: new Date().toISOString(),
      status: 'OPEN'
    };
    onSubmit(newProject);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="max-w-4xl mx-auto bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden animate-fade-in">
      <div className="bg-[#1B4D89] p-6 text-white">
        <h2 className="text-2xl font-bold flex items-center gap-2 font-['Barlow']">
          <Home className="w-6 h-6 text-[#FD5E53]" />
          Post New Project
        </h2>
        <p className="text-blue-100 mt-1 text-sm">Follow these 3 steps to define your requirements and find the perfect architect.</p>
      </div>
      
      <form onSubmit={handleSubmit} className="p-8 space-y-10">
        
        {/* PART 1: GENERAL INFORMATION */}
        <section className="space-y-6">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
            <span className="bg-blue-100 text-[#1B4D89] font-bold px-2.5 py-0.5 rounded text-sm">Part 1</span>
            <h3 className="text-lg font-bold text-slate-800 font-['Barlow']">General Information</h3>
          </div>

          <div className="grid grid-cols-1 gap-6">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Project Title</label>
              <div className="relative">
                <input
                  required
                  type="text"
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  placeholder="e.g., Modern Student Housing in Depok"
                  className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-[#1B4D89] focus:border-[#1B4D89] outline-none transition-all"
                />
                <Briefcase className="w-5 h-5 text-slate-400 absolute left-3 top-2.5" />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Location</label>
                <div className="relative">
                  <input
                    required
                    type="text"
                    name="location"
                    value={formData.location}
                    onChange={handleChange}
                    placeholder="City, Area"
                    className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-[#1B4D89] focus:border-[#1B4D89] outline-none"
                  />
                  <MapPin className="w-5 h-5 text-slate-400 absolute left-3 top-2.5" />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Lot Size (m²)</label>
                <div className="relative">
                  <input
                    required
                    type="number"
                    name="lotSize"
                    value={formData.lotSize}
                    onChange={handleChange}
                    placeholder="e.g., 200"
                    className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-[#1B4D89] focus:border-[#1B4D89] outline-none"
                  />
                  <Ruler className="w-5 h-5 text-slate-400 absolute left-3 top-2.5" />
                </div>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Building Type</label>
                <div className="relative">
                  <select
                    name="buildingType"
                    value={formData.buildingType}
                    onChange={handleChange}
                    className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-[#1B4D89] focus:border-[#1B4D89] outline-none appearance-none"
                  >
                    <option value="Residential">Residential Home</option>
                    <option value="Student Housing">Student Housing (Kost)</option>
                    <option value="Villa">Villa / Resort</option>
                    <option value="Commercial">Commercial / Office</option>
                    <option value="Renovation">Renovation</option>
                  </select>
                  <Layers className="w-5 h-5 text-slate-400 absolute left-3 top-2.5" />
                </div>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Detailed Requirements</label>
              <div className="relative">
                <textarea
                  required
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  rows={4}
                  placeholder="Describe number of rooms, style preference (e.g., Industrial, Tropical), timeline constraints..."
                  className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-[#1B4D89] focus:border-[#1B4D89] outline-none"
                />
                <FileText className="w-5 h-5 text-slate-400 absolute left-3 top-3" />
              </div>
            </div>
          </div>
        </section>

        {/* PART 2: DELIVERABLES */}
        <section className="space-y-6">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
            <span className="bg-blue-100 text-[#1B4D89] font-bold px-2.5 py-0.5 rounded text-sm">Part 2</span>
            <h3 className="text-lg font-bold text-slate-800 font-['Barlow']">Required Deliverables</h3>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {DELIVERABLES_GROUPS.map((group, idx) => (
              <div key={idx} className="bg-slate-50 p-4 rounded-lg border border-slate-100">
                <h4 className="font-bold text-[#1B4D89] text-sm mb-3 font-['Barlow']">{group.title}</h4>
                <div className="space-y-2">
                  {group.items.map(item => (
                    <label key={item} className="flex items-start gap-2 cursor-pointer group">
                      <div className={`mt-0.5 w-4 h-4 rounded border flex items-center justify-center transition-colors ${selectedDeliverables.includes(item) ? 'bg-[#1B4D89] border-[#1B4D89]' : 'bg-white border-slate-300'}`}>
                        {selectedDeliverables.includes(item) && <CheckSquare className="w-3 h-3 text-white" />}
                      </div>
                      <input 
                        type="checkbox" 
                        className="hidden"
                        checked={selectedDeliverables.includes(item)}
                        onChange={() => toggleDeliverable(item)}
                      />
                      <span className="text-sm text-slate-600 group-hover:text-slate-900">{item}</span>
                    </label>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* PART 3: BUDGETING */}
        <section className="space-y-6">
          <div className="flex items-center gap-2 border-b border-slate-100 pb-2">
            <span className="bg-blue-100 text-[#1B4D89] font-bold px-2.5 py-0.5 rounded text-sm">Part 3</span>
            <h3 className="text-lg font-bold text-slate-800 font-['Barlow']">Budgeting</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Total Construction Budget</label>
                <p className="text-xs text-slate-500 mb-2">Estimate for construction, material, and labor (Fisik Bangunan).</p>
                <div className="relative">
                  <input
                    type="text"
                    name="totalBudget"
                    value={formData.totalBudget}
                    onChange={handleChange}
                    placeholder="e.g., IDR 2,000,000,000"
                    className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-[#1B4D89] focus:border-[#1B4D89] outline-none"
                  />
                  <Calculator className="w-5 h-5 text-slate-400 absolute left-3 top-2.5" />
                </div>
              </div>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Design Budget (Architect Fee)</label>
                <p className="text-xs text-slate-500 mb-2">This is the main reference for architects to bid.</p>
                <div className="relative">
                  <input
                    required
                    type="text"
                    name="designBudget"
                    value={formData.designBudget}
                    onChange={handleChange}
                    placeholder="e.g., IDR 100,000,000"
                    className="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-[#1B4D89] focus:border-[#1B4D89] outline-none"
                  />
                  <DollarSign className="w-5 h-5 text-slate-400 absolute left-3 top-2.5" />
                </div>
              </div>
              
              <div className="bg-blue-50 p-3 rounded-lg border border-blue-100 flex gap-3 items-start">
                <Info className="w-5 h-5 text-[#1B4D89] flex-shrink-0 mt-0.5" />
                <p className="text-xs text-blue-900 leading-relaxed">
                  <strong>IAI Guideline:</strong> According to the Indonesian Institute of Architects (IAI), the design fee is typically around <strong>5% - 7%</strong> of the total construction budget.
                </p>
              </div>
            </div>
          </div>
        </section>

        <div className="flex gap-4 pt-6 border-t border-slate-100">
          <button
            type="button"
            onClick={onCancel}
            className="px-6 py-3 text-slate-700 bg-white border border-slate-300 rounded-lg hover:bg-slate-50 transition-colors font-medium"
          >
            Cancel
          </button>
          <button
            type="submit"
            className="flex-1 px-6 py-3 text-white bg-[#1B4D89] rounded-lg hover:bg-[#163E75] shadow-md hover:shadow-lg transition-all font-bold font-['Barlow'] flex items-center justify-center gap-2"
          >
            <CheckSquare className="w-5 h-5" /> Post Project
          </button>
        </div>
      </form>
    </div>
  );
};

export default ProjectForm;