
import React, { useState, useEffect } from 'react';
import { ProjectDraft } from '../types';
import Button from './Button';
import { 
  ArrowLeft, ArrowRight, CheckCircle, ChevronDown, Check, Sparkles, 
  MapPin, Calculator, Wand2, Info, LayoutGrid, Layers, Hammer, Calendar
} from 'lucide-react';
import { 
  PROJECT_MAIN_CATEGORIES, 
  PROJECT_CATEGORIES, 
  PROJECT_SUB_CATEGORIES, 
  DESIGN_STYLES, 
  DELIVERABLES_GROUPS,
  LOT_TYPES
} from '../constants';

// Mini-component for enhanced Lot Type Visuals
const LotTypeVisual: React.FC<{ type: string; active: boolean }> = ({ type, active }) => {
  const roadFill = active ? "#475569" : "#e2e8f0";
  const neighborFill = active ? "rgba(255,255,255,0.15)" : "#f8fafc";
  const neighborStroke = active ? "rgba(255,255,255,0.3)" : "#e2e8f0";
  const lotFill = "#fbbf24"; // Distinct yellow for the target lot
  const lotStroke = active ? "#fff" : "#000";
  const laneStroke = active ? "rgba(255,255,255,0.4)" : "rgba(0,0,0,0.1)";

  const renderVisual = () => {
    switch (type) {
      case 'Middle Lot (Normal)':
        return (
          <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-sm">
            <rect x="0" y="75" width="100" height="25" fill={roadFill} />
            <line x1="0" y1="87.5" x2="100" y2="87.5" stroke={laneStroke} strokeWidth="1" strokeDasharray="4 2" />
            <rect x="5" y="10" width="25" height="55" fill={neighborFill} stroke={neighborStroke} />
            <rect x="70" y="10" width="25" height="55" fill={neighborFill} stroke={neighborStroke} />
            <rect x="5" y="0" width="90" height="5" fill={neighborFill} stroke={neighborStroke} />
            <rect x="35" y="10" width="30" height="55" fill={lotFill} stroke={lotStroke} strokeWidth="1.5" />
          </svg>
        );
      case 'Corner Lot (Hook)':
        return (
          <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-sm">
            <rect x="0" y="70" width="100" height="30" fill={roadFill} />
            <rect x="70" y="0" width="30" height="100" fill={roadFill} />
            <line x1="0" y1="85" x2="70" y2="85" stroke={laneStroke} strokeWidth="1" strokeDasharray="4 2" />
            <line x1="85" y1="0" x2="85" y2="70" stroke={laneStroke} strokeWidth="1" strokeDasharray="4 2" />
            <rect x="5" y="10" width="55" height="5" fill={neighborFill} stroke={neighborStroke} />
            <rect x="0" y="10" width="5" height="50" fill={neighborFill} stroke={neighborStroke} />
            <rect x="10" y="20" width="50" height="40" fill={lotFill} stroke={lotStroke} strokeWidth="1.5" />
          </svg>
        );
      case 'Cul-de-sac Lot':
        return (
          <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-sm">
            <circle cx="50" cy="55" r="22" fill={roadFill} />
            <rect x="40" y="77" width="20" height="23" fill={roadFill} />
            <circle cx="50" cy="55" r="22" fill="none" stroke={laneStroke} strokeWidth="1" strokeDasharray="4 2" />
            <rect x="42" y="5" width="16" height="22" fill={lotFill} stroke={lotStroke} strokeWidth="1.5" />
            <rect x="15" y="15" width="20" height="20" fill={neighborFill} stroke={neighborStroke} transform="rotate(-30 25 25)" />
            <rect x="65" y="15" width="20" height="20" fill={neighborFill} stroke={neighborStroke} transform="rotate(30 75 25)" />
            <rect x="5" y="45" width="20" height="20" fill={neighborFill} stroke={neighborStroke} />
            <rect x="75" y="45" width="20" height="20" fill={neighborFill} stroke={neighborStroke} />
          </svg>
        );
      case 'Flag Lot':
        return (
          <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-sm">
            <rect x="0" y="85" width="100" height="15" fill={roadFill} />
            <rect x="5" y="40" width="35" height="40" fill={neighborFill} stroke={neighborStroke} />
            <rect x="45" y="40" width="35" height="40" fill={neighborFill} stroke={neighborStroke} />
            <rect x="85" y="40" width="10" height="45" fill={roadFill} />
            <rect x="55" y="5" width="40" height="30" fill={lotFill} stroke={lotStroke} strokeWidth="1.5" />
            <rect x="5" y="5" width="40" height="30" fill={neighborFill} stroke={neighborStroke} />
          </svg>
        );
      case 'T-Intersection Lot (Tusuk Sate)':
        return (
          <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-sm">
            <rect x="0" y="65" width="100" height="20" fill={roadFill} />
            <rect x="40" y="85" width="20" height="15" fill={roadFill} />
            <line x1="50" y1="85" x2="50" y2="100" stroke={laneStroke} strokeWidth="1" strokeDasharray="4 2" />
            <rect x="5" y="10" width="30" height="50" fill={neighborFill} stroke={neighborStroke} />
            <rect x="65" y="10" width="30" height="50" fill={neighborFill} stroke={neighborStroke} />
            <rect x="37.5" y="10" width="25" height="50" fill={lotFill} stroke={lotStroke} strokeWidth="1.5" />
          </svg>
        );
      case 'Perimeter Lot':
        return (
          <svg viewBox="0 0 100 100" className="w-full h-full drop-shadow-sm">
            <rect x="0" y="75" width="100" height="25" fill={roadFill} />
            <line x1="0" y1="87.5" x2="100" y2="87.5" stroke={laneStroke} strokeWidth="1" strokeDasharray="4 2" />
            <rect x="5" y="15" width="35" height="50" fill={neighborFill} stroke={neighborStroke} />
            <path d="M 95 0 L 95 75" stroke="#10b981" strokeWidth="2" strokeDasharray="4 4" />
            <rect x="92" y="0" width="8" height="75" fill="rgba(16,185,129,0.05)" />
            <rect x="45" y="15" width="45" height="50" fill={lotFill} stroke={lotStroke} strokeWidth="1.5" />
          </svg>
        );
      default:
        return (
          <div className={`w-full h-full flex items-center justify-center text-xs font-bold ${active ? 'text-white' : 'text-gray-400'}`}>
            N/A
          </div>
        );
    }
  };

  return (
    <div className={`w-20 h-20 shrink-0 rounded-xl p-2 transition-colors flex items-center justify-center ${active ? 'bg-slate-700 shadow-inner' : 'bg-slate-50'}`}>
      {renderVisual()}
    </div>
  );
};

interface CreateProjectWizardProps {
  onCancel: () => void;
  onComplete: (draft: ProjectDraft) => void;
}

const CreateProjectWizard: React.FC<CreateProjectWizardProps> = ({ onCancel, onComplete }) => {
  const [step, setStep] = useState(1);
  const [isReviewing, setIsReviewing] = useState(false);
  
  const [formData, setFormData] = useState<ProjectDraft>({
    title: '',
    description: '',
    location: '',
    landArea: '',
    landFrontage: '',
    landDepth: '',
    buildArea: '',
    floors: '1',
    mainCategory: '',
    category: '',
    subCategory: '',
    lotType: '',
    locationMapLink: '',
    designPreferences: [],
    contactName: '',
    contactPhone: '',
    startDate: '',
    deliverables: []
  });

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [step, isReviewing]);

  useEffect(() => {
    const w = parseFloat(formData.landFrontage);
    const d = parseFloat(formData.landDepth);
    if (!isNaN(w) && !isNaN(d)) {
      setFormData(prev => ({ ...prev, landArea: (w * d).toString() }));
    }
  }, [formData.landFrontage, formData.landDepth]);

  const updateField = (field: keyof ProjectDraft, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const applyRecommendedBuildArea = (bcrPercentage: number) => {
    const land = parseFloat(formData.landArea) || 0;
    const floors = parseInt(formData.floors) || 1;
    if (land > 0) {
      const calculated = Math.round(land * (bcrPercentage / 100) * floors);
      updateField('buildArea', calculated.toString());
    }
  };

  const toggleArrayItem = (field: 'designPreferences' | 'deliverables', item: string) => {
    setFormData(prev => {
      const current = prev[field];
      if (current.includes(item)) {
        return { ...prev, [field]: current.filter(i => i !== item) };
      } else {
        return { ...prev, [field]: [...current, item] };
      }
    });
  };

  const toggleAllDeliverables = () => {
    const all = Object.values(DELIVERABLES_GROUPS).flat();
    const current = formData.deliverables;
    if (current.length === all.length) {
      updateField('deliverables', []);
    } else {
      updateField('deliverables', all);
    }
  };

  const toggleCategoryDeliverables = (categoryItems: string[]) => {
    const current = formData.deliverables;
    const hasAllInCategory = categoryItems.every(item => current.includes(item));
    
    if (hasAllInCategory) {
      updateField('deliverables', current.filter(item => !categoryItems.includes(item)));
    } else {
      const otherItems = current.filter(item => !categoryItems.includes(item));
      updateField('deliverables', [...otherItems, ...categoryItems]);
    }
  };

  const handleNext = () => {
    if (isReviewing) {
      if (step < 3) {
        setStep(step + 1);
        setIsReviewing(false);
      } else {
        onComplete(formData);
      }
    } else {
      setIsReviewing(true);
    }
  };

  const handleBack = () => {
    if (isReviewing) {
      setIsReviewing(false);
    } else if (step > 1) {
      setStep(step - 1);
      setIsReviewing(false);
    } else {
      onCancel();
    }
  };

  const renderStepIndicator = () => (
    <div className="flex items-center justify-center space-x-4 mb-10">
      {[1, 2, 3].map(i => (
        <div key={i} className="flex items-center">
          <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-colors duration-300 ${
            step === i ? 'bg-black text-white' : 
            step > i ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-500'
          }`}>
            {step > i ? <Check size={14} /> : i}
          </div>
          {i < 3 && <div className={`w-12 h-1 mx-2 rounded-full transition-colors duration-300 ${step > i ? 'bg-green-500' : 'bg-gray-200'}`}></div>}
        </div>
      ))}
    </div>
  );

  const renderReviewSection = () => {
    let content;
    
    if (step === 1) {
      content = (
        <div className="space-y-4 text-sm">
           <div className="grid grid-cols-2 gap-4">
             <div className="col-span-2"><span className="text-gray-500 block">Project Title</span> <span className="font-medium">{formData.title}</span></div>
             <div className="col-span-2"><span className="text-gray-500 block">Description</span> <span className="font-medium text-gray-800">{formData.description}</span></div>
             <div className="col-span-2"><span className="text-gray-500 block">Location</span> <span className="font-medium">{formData.location}</span></div>
             <div><span className="text-gray-500 block">Project Type</span> <span className="font-medium">{formData.mainCategory}</span></div>
             <div><span className="text-gray-500 block">Category</span> <span className="font-medium">{formData.category}</span></div>
             <div className="col-span-2"><span className="text-gray-500 block">Building Function</span> <span className="font-medium">{formData.subCategory || 'N/A'}</span></div>
           </div>
        </div>
      );
    } else if (step === 2) {
      content = (
         <div className="space-y-4 text-sm">
           <div className="grid grid-cols-2 gap-4">
             <div>
               <span className="text-gray-500 block">Land Dimensions</span> 
               <span className="font-medium">{formData.landFrontage}m x {formData.landDepth}m</span>
             </div>
             <div>
               <span className="text-gray-500 block">Total Land Area</span> 
               <span className="font-medium">{formData.landArea} m²</span>
             </div>
             <div><span className="text-gray-500 block">Build Area</span> <span className="font-medium">{formData.buildArea} m²</span></div>
             <div><span className="text-gray-500 block">Floors</span> <span className="font-medium">{formData.floors}</span></div>
             <div className="col-span-2"><span className="text-gray-500 block">Lot Type</span> <span className="font-medium">{formData.lotType}</span></div>
           </div>
         </div>
      );
    } else {
      content = (
        <div className="space-y-6 text-sm">
           <div>
             <span className="text-gray-500 block mb-2">Design Preferences</span>
             <div className="flex flex-wrap gap-2">
                {formData.designPreferences.length > 0 ? formData.designPreferences.map(t => (
                  <span key={t} className="px-2 py-1 bg-gray-100 rounded text-xs">{t}</span>
                )) : <span className="italic text-gray-400">None selected</span>}
             </div>
           </div>
           <div>
             <span className="text-gray-500 block mb-2">Scope of Work (Deliverables)</span>
             <ul className="list-disc list-inside space-y-1 text-gray-700">
                {formData.deliverables.map(d => <li key={d}>{d}</li>)}
             </ul>
           </div>
           <div className="grid grid-cols-2 gap-4 border-t border-gray-100 pt-4">
              <div><span className="text-gray-500 block">Contact Name</span> <span className="font-medium">{formData.contactName}</span></div>
              <div><span className="text-gray-500 block">Phone</span> <span className="font-medium">{formData.contactPhone}</span></div>
           </div>
        </div>
      );
    }

    return (
      <div className="bg-gray-50 border border-gray-200 rounded-2xl p-8 animate-in fade-in zoom-in-95 duration-300">
        <div className="flex flex-col items-center text-center mb-6">
          <div className="h-12 w-12 bg-green-100 text-green-600 rounded-full flex items-center justify-center mb-3">
             <CheckCircle size={24} />
          </div>
          <h3 className="text-xl font-bold text-gray-900">Is the data in Step {step} correct?</h3>
          <p className="text-gray-500 text-sm mt-1">Please review your details before proceeding.</p>
        </div>
        <div className="bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
           {content}
        </div>
      </div>
    );
  };

  const renderStep1 = () => (
    <div className="space-y-6 animate-in slide-in-from-right fade-in duration-300">
      <h2 className="text-2xl font-bold text-gray-900">General Info & Category</h2>
      <p className="text-gray-500 text-sm">Tell us about your project and its type.</p>
      <div className="space-y-6">
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Project Title</label>
            <input 
              type="text" 
              value={formData.title}
              onChange={(e) => updateField('title', e.target.value)}
              className="w-full px-4 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all"
              placeholder="e.g. Modern Family Villa in Bali"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Project Description</label>
            <textarea 
              rows={3}
              value={formData.description}
              onChange={(e) => updateField('description', e.target.value)}
              className="w-full px-4 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all resize-none"
              placeholder="Describe your vision, requirements, and specific challenges..."
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Location / Address</label>
            <input 
              type="text" 
              value={formData.location}
              onChange={(e) => updateField('location', e.target.value)}
              className="w-full px-4 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all"
              placeholder="City, District, or full address"
            />
          </div>
        </div>
        <div className="border-t border-gray-100 pt-6 space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Main Category</label>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {PROJECT_MAIN_CATEGORIES.map(cat => (
                <button
                  key={cat}
                  onClick={() => updateField('mainCategory', cat)}
                  className={`px-4 py-3 rounded-2xl text-left border transition-all ${
                    formData.mainCategory === cat 
                    ? 'bg-black text-white border-black' 
                    : 'bg-white text-gray-700 border-gray-200 hover:border-gray-300'
                  }`}
                >
                  {cat}
                </button>
              ))}
            </div>
          </div>
          {formData.mainCategory && (
            <div className="animate-in fade-in slide-in-from-top-2 duration-300">
              <label className="block text-sm font-medium text-gray-700 mb-2">Category</label>
              <div className="relative">
                <select
                  value={formData.category}
                  onChange={(e) => {
                    updateField('category', e.target.value);
                    updateField('subCategory', '');
                  }}
                  className="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white appearance-none focus:ring-1 focus:ring-black focus:border-black outline-none cursor-pointer"
                >
                  <option value="" disabled>Select category</option>
                  {PROJECT_CATEGORIES.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
                <ChevronDown className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" size={16} />
              </div>
            </div>
          )}
          {formData.category && PROJECT_SUB_CATEGORIES[formData.category] && (
            <div className="animate-in fade-in slide-in-from-top-2 duration-300">
              <label className="block text-sm font-medium text-gray-700 mb-2">Building Function / Sub-Category</label>
              <div className="relative">
                <select
                  value={formData.subCategory}
                  onChange={(e) => updateField('subCategory', e.target.value)}
                  className="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white appearance-none focus:ring-1 focus:ring-black focus:border-black outline-none cursor-pointer"
                >
                  <option value="" disabled>Select specific function</option>
                  {PROJECT_SUB_CATEGORIES[formData.category].map(sub => (
                    <option key={sub} value={sub}>{sub}</option>
                  ))}
                </select>
                <ChevronDown className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" size={16} />
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );

  const renderStep2 = () => (
    <div className="space-y-6 animate-in slide-in-from-right fade-in duration-300">
      <h2 className="text-2xl font-bold text-gray-900">Land Details & Dimensions</h2>
      <p className="text-gray-500 text-sm">Provide specific measurements and type of your land.</p>
      
      <div className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Frontage Width (m)</label>
            <input 
              type="number" 
              value={formData.landFrontage}
              onChange={(e) => updateField('landFrontage', e.target.value)}
              className="w-full px-4 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all"
              placeholder="e.g. 15"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Depth (m)</label>
            <input 
              type="number" 
              value={formData.landDepth}
              onChange={(e) => updateField('landDepth', e.target.value)}
              className="w-full px-4 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all"
              placeholder="e.g. 30"
            />
          </div>
        </div>
        <div className="p-4 bg-gray-50 rounded-2xl border border-gray-100 flex justify-between items-center text-indigo-900">
          <span className="text-sm font-medium flex items-center gap-2">
            <Calculator size={16} /> Total Land Area
          </span>
          <span className="text-lg font-bold">{formData.landArea || '0'} m²</span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 border-t border-gray-100 pt-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Number of Floors</label>
            <input 
              type="number" 
              value={formData.floors}
              onChange={(e) => updateField('floors', e.target.value)}
              className="w-full px-4 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all"
              placeholder="e.g. 2"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Build Area (m²)</label>
            <div className="relative">
              <input 
                type="number" 
                value={formData.buildArea}
                onChange={(e) => updateField('buildArea', e.target.value)}
                className="w-full pl-4 pr-12 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all"
                placeholder="e.g. 250"
              />
              <button 
                type="button"
                onClick={() => applyRecommendedBuildArea(parseInt(formData.floors) === 1 ? 55 : 40)}
                title="Calculate Recommended Build Area"
                className="absolute right-3 top-1/2 -translate-y-1/2 text-indigo-500 hover:text-indigo-700 bg-indigo-50 p-1.5 rounded-lg transition-colors"
              >
                <Wand2 size={16} />
              </button>
            </div>
          </div>
      </div>

      <div className="bg-gradient-to-br from-indigo-50 via-blue-50 to-white p-6 rounded-3xl border border-indigo-100 space-y-4">
          <div className="flex items-start gap-3">
              <div className="mt-0.5 bg-indigo-100 p-2 rounded-full text-indigo-600">
                <Sparkles size={18} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-indigo-900 uppercase tracking-wide mb-1">Rumantra AI Insight</h4>
                <p className="text-sm text-indigo-800 leading-relaxed mb-4">
                  Estimate your build area based on typical Indonesian Building Coverage Ratios (BCR).
                </p>
              </div>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <button 
                  type="button"
                  onClick={() => applyRecommendedBuildArea(55)}
                  className="bg-white/80 backdrop-blur-sm hover:bg-white border border-indigo-200 p-4 rounded-2xl text-left transition-all group shadow-sm hover:shadow-md border-b-4 active:border-b-0 active:translate-y-1"
              >
                  <span className="block text-xs font-bold text-indigo-600 mb-1 uppercase tracking-tighter">1-Story Standard (55% BCR)</span>
                  <span className="text-sm text-indigo-900 font-bold group-hover:underline">Apply ~{Math.round(parseFloat(formData.landArea || '0') * 0.55)} m²</span>
              </button>
              <button 
                  type="button"
                  onClick={() => applyRecommendedBuildArea(40)}
                  className="bg-white/80 backdrop-blur-sm hover:bg-white border border-indigo-200 p-4 rounded-2xl text-left transition-all group shadow-sm hover:shadow-md border-b-4 active:border-b-0 active:translate-y-1"
              >
                  <span className="block text-xs font-bold text-indigo-600 mb-1 uppercase tracking-tighter">Multi-Story (40% / floor)</span>
                  <span className="text-sm text-indigo-900 font-bold group-hover:underline">Apply ~{Math.round(parseFloat(formData.landArea || '0') * 0.4 * (parseInt(formData.floors) || 1))} m²</span>
              </button>
          </div>
      </div>

      <div className="border-t border-gray-100 pt-6 space-y-4">
        <label className="block text-sm font-medium text-gray-700">Site Configuration (Lot Type)</label>
        <div className="grid grid-cols-1 gap-4">
          {LOT_TYPES.map(type => (
            <div 
              key={type.label}
              onClick={() => updateField('lotType', type.label)}
              className={`p-4 rounded-3xl border cursor-pointer transition-all flex items-center gap-6 ${
                formData.lotType === type.label
                ? 'bg-slate-900 border-slate-900 text-white shadow-xl scale-[1.01]'
                : 'bg-white border-gray-100 hover:border-gray-300 text-gray-900 shadow-sm'
              }`}
            >
              <LotTypeVisual type={type.label} active={formData.lotType === type.label} />
              <div className="flex-1">
                <div className="flex items-center justify-between mb-1">
                   <h4 className="font-bold text-sm md:text-base">{type.label}</h4>
                   {formData.lotType === type.label && <div className="bg-amber-400 text-slate-900 p-1 rounded-full"><Check size={14} strokeWidth={3}/></div>}
                </div>
                <p className={`text-xs md:text-sm leading-snug ${formData.lotType === type.label ? 'text-slate-300' : 'text-gray-500'}`}>
                  {type.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      </div>
      
      <div className="border-t border-gray-100 pt-6">
        <label className="block text-sm font-medium text-gray-700 mb-1">Google Maps Link <span className="text-gray-400 font-normal">(Optional)</span></label>
        <div className="relative">
          <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input 
            type="url" 
            value={formData.locationMapLink}
            onChange={(e) => updateField('locationMapLink', e.target.value)}
            className="w-full pl-12 pr-4 py-3 rounded-2xl border border-gray-200 focus:ring-1 focus:ring-black focus:border-black outline-none transition-all"
            placeholder="https://maps.google.com/..."
          />
        </div>
      </div>
    </div>
  );

  const renderStep3 = () => {
    const allDeliverables = Object.values(DELIVERABLES_GROUPS).flat();
    const isAllDeliverablesSelected = formData.deliverables.length === allDeliverables.length;

    return (
      <div className="space-y-10 animate-in slide-in-from-right fade-in duration-300">
        <div className="space-y-2">
          <h2 className="text-2xl font-bold text-gray-900">Scope & Design</h2>
          <p className="text-gray-500 text-sm">Define your style preferences and the exact items you need for your project.</p>
        </div>
        
        {/* Design Preferences */}
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <label className="block text-sm font-bold text-gray-900 uppercase tracking-wider">Architectural Direction</label>
            <Button variant="ghost" size="sm" className="text-xs text-indigo-600 hover:bg-indigo-50" icon={<Info size={14}/>}>Learn about styles</Button>
          </div>
          
          <div className="space-y-6">
            {Object.entries(DESIGN_STYLES).map(([group, styles]) => (
              <div key={group} className="bg-white p-6 rounded-[2rem] border border-gray-100 shadow-sm transition-all hover:shadow-md">
                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-xs font-bold text-gray-400 uppercase tracking-[0.15em]">{group}</h4>
                </div>
                <div className="flex flex-wrap gap-2.5">
                  {styles.map(style => (
                    <button
                      key={style}
                      onClick={() => toggleArrayItem('designPreferences', style)}
                      className={`px-4 py-2 rounded-full text-xs font-bold transition-all border-2 ${
                        formData.designPreferences.includes(style)
                        ? 'bg-black text-white border-black scale-[1.05] shadow-lg'
                        : 'bg-white text-gray-600 border-gray-100 hover:border-gray-300'
                      }`}
                    >
                      {style}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Deliverables */}
        <div className="space-y-6 border-t border-gray-100 pt-10">
          <div className="flex items-center justify-between">
            <div className="space-y-1">
              <label className="block text-sm font-bold text-gray-900 uppercase tracking-wider">Scope of Work</label>
              <p className="text-xs text-gray-500">Select what technical documents and services you require.</p>
            </div>
            <div className="flex gap-2">
              <Button variant="ghost" size="sm" className="text-xs text-indigo-600 hover:bg-indigo-50" icon={<Info size={14}/>}>Learn about scope</Button>
              <Button 
                variant={isAllDeliverablesSelected ? "primary" : "outline"} 
                size="sm" 
                onClick={toggleAllDeliverables}
                className="text-xs"
              >
                {isAllDeliverablesSelected ? 'Deselect All' : 'Select All Scope'}
              </Button>
            </div>
          </div>

          <div className="space-y-6">
            {Object.entries(DELIVERABLES_GROUPS).map(([phase, items]) => {
              const hasAllInCategory = items.every(item => formData.deliverables.includes(item));
              const categoryIcon = phase.includes('Development') ? <LayoutGrid size={16}/> : phase.includes('Technical') ? <Layers size={16}/> : <Hammer size={16}/>;
              
              return (
                <div key={phase} className="bg-white rounded-[2rem] border border-gray-100 overflow-hidden shadow-sm">
                  <div className="flex items-center justify-between px-6 py-4 bg-gray-50/50 border-b border-gray-100">
                    <div className="flex items-center gap-3">
                      <div className="p-2 bg-white rounded-xl text-gray-900 shadow-sm">{categoryIcon}</div>
                      <h4 className="text-sm font-bold text-gray-900">{phase}</h4>
                    </div>
                    <button 
                      onClick={() => toggleCategoryDeliverables(items)}
                      className={`text-xs font-bold transition-colors ${hasAllInCategory ? 'text-indigo-600' : 'text-gray-400 hover:text-indigo-500'}`}
                    >
                      {hasAllInCategory ? 'Unselect All' : 'Select Phase'}
                    </button>
                  </div>
                  <div className="p-4 grid grid-cols-1 md:grid-cols-2 gap-2">
                     {items.map(item => (
                       <label 
                         key={item} 
                         className={`flex items-start gap-3 p-3 rounded-2xl cursor-pointer transition-all border-2 ${
                           formData.deliverables.includes(item) 
                           ? 'bg-slate-900 border-slate-900 text-white shadow-sm' 
                           : 'bg-white border-transparent hover:bg-gray-50 text-gray-600'
                         }`}
                       >
                          <div className={`mt-0.5 w-5 h-5 rounded-lg border-2 flex items-center justify-center transition-colors ${
                            formData.deliverables.includes(item) ? 'bg-indigo-500 border-indigo-500' : 'bg-white border-gray-200'
                          }`}>
                            {formData.deliverables.includes(item) && <Check size={12} className="text-white font-bold" strokeWidth={4} />}
                          </div>
                          <input 
                            type="checkbox" 
                            className="hidden"
                            checked={formData.deliverables.includes(item)}
                            onChange={() => toggleArrayItem('deliverables', item)}
                          />
                          <span className="text-sm font-medium leading-tight">{item}</span>
                       </label>
                     ))}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Contact & Date */}
        <div className="border-t border-gray-100 pt-10 grid grid-cols-1 md:grid-cols-2 gap-6">
           <div className="space-y-2">
              <label className="block text-sm font-bold text-gray-700">Project Owner Name</label>
              <input 
                type="text" 
                value={formData.contactName}
                onChange={(e) => updateField('contactName', e.target.value)}
                className="w-full px-5 py-4 rounded-2xl border-2 border-gray-100 focus:border-black outline-none transition-all font-medium"
                placeholder="Full Name"
              />
           </div>
           <div className="space-y-2">
              <label className="block text-sm font-bold text-gray-700">WhatsApp Number</label>
              <input 
                type="tel" 
                value={formData.contactPhone}
                onChange={(e) => updateField('contactPhone', e.target.value)}
                className="w-full px-5 py-4 rounded-2xl border-2 border-gray-100 focus:border-black outline-none transition-all font-medium"
                placeholder="+62"
              />
           </div>
           <div className="md:col-span-2 space-y-2">
              <label className="block text-sm font-bold text-gray-700">Target Start Date</label>
              <div className="relative">
                <Calendar className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input 
                  type="date" 
                  value={formData.startDate}
                  onChange={(e) => updateField('startDate', e.target.value)}
                  className="w-full pl-12 pr-5 py-4 rounded-2xl border-2 border-gray-100 focus:border-black outline-none transition-all font-medium"
                />
              </div>
           </div>
        </div>
      </div>
    );
  };

  const isValidStep1 = formData.title && formData.location && formData.mainCategory && formData.category;
  const isValidStep2 = formData.lotType && formData.landFrontage && formData.landDepth && formData.buildArea && formData.floors;
  const isValidStep3 = formData.contactName && formData.contactPhone;

  const canProceed = () => {
    if (step === 1) return !!isValidStep1;
    if (step === 2) return !!isValidStep2;
    if (step === 3) return !!isValidStep3;
    return false;
  };

  return (
    <div className="max-w-3xl mx-auto py-8">
      {renderStepIndicator()}
      <div className="mb-10">
        {isReviewing ? renderReviewSection() : (
          step === 1 ? renderStep1() : 
          step === 2 ? renderStep2() : 
          renderStep3()
        )}
      </div>
      <div className="flex justify-between items-center pt-6 border-t border-gray-100">
        <Button variant="ghost" onClick={handleBack} icon={<ArrowLeft size={16} />}>
          {step === 1 && !isReviewing ? 'Cancel' : 'Back'}
        </Button>
        <Button 
          variant="primary" 
          onClick={handleNext} 
          disabled={!canProceed()}
          className="px-8"
        >
          {isReviewing 
            ? (step === 3 ? 'Post Project' : 'Yes, Correct') 
            : (step === 3 ? 'Review & Submit' : 'Next')
          }
          {!isReviewing && <ArrowRight size={16} className="ml-2" />}
        </Button>
      </div>
    </div>
  );
};

export default CreateProjectWizard;
