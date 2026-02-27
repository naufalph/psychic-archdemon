import React, { useState } from 'react';
import { Sparkles, X, Send } from 'lucide-react';
import Button from './Button';
import { generateProjectDescription } from '../services/geminiService';

const AiAssistant: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [response, setResponse] = useState('');
  const [loading, setLoading] = useState(false);

  const handleGenerate = async () => {
    if (!input.trim()) return;
    setLoading(true);
    setResponse('');
    
    // Simulate thinking if no API key or very fast response
    const result = await generateProjectDescription(input);
    setResponse(result);
    setLoading(false);
  };

  return (
    <div className="fixed bottom-8 right-8 z-50 flex flex-col items-end">
      {isOpen && (
        <div className="mb-4 w-80 md:w-96 bg-white rounded-2xl shadow-2xl border border-gray-100 overflow-hidden animate-in slide-in-from-bottom-5 fade-in duration-300">
          <div className="bg-black text-white p-4 flex justify-between items-center">
            <div className="flex items-center gap-2">
              <Sparkles size={18} className="text-yellow-300" />
              <h3 className="font-semibold text-sm">Rumantra AI</h3>
            </div>
            <button onClick={() => setIsOpen(false)} className="text-gray-400 hover:text-white">
              <X size={18} />
            </button>
          </div>
          <div className="p-4 bg-gray-50 min-h-[150px] max-h-[300px] overflow-y-auto text-sm text-gray-700">
             {response ? (
               <div className="bg-white p-3 rounded-lg border border-gray-200 shadow-sm">
                 {response}
               </div>
             ) : (
               <p className="text-gray-400 italic">Describe your project idea, and I'll draft a professional brief for you...</p>
             )}
             {loading && <div className="mt-2 text-xs text-gray-500 animate-pulse">Thinking...</div>}
          </div>
          <div className="p-3 bg-white border-t border-gray-100 flex gap-2">
            <input 
              type="text" 
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="e.g., A modern kitchen with an island..."
              className="flex-1 text-sm bg-gray-50 border-transparent rounded-full px-4 focus:ring-1 focus:ring-black focus:border-black outline-none"
              onKeyDown={(e) => e.key === 'Enter' && handleGenerate()}
            />
            <button 
              onClick={handleGenerate}
              disabled={loading || !input}
              className="bg-black text-white p-2 rounded-full hover:bg-gray-800 disabled:opacity-50"
            >
              <Send size={16} />
            </button>
          </div>
        </div>
      )}
      
      <Button 
        onClick={() => setIsOpen(!isOpen)} 
        variant="primary" 
        className="shadow-2xl h-14 w-14 !p-0 rounded-full flex items-center justify-center"
      >
        {isOpen ? <X size={24} /> : <Sparkles size={24} />}
      </Button>
    </div>
  );
};

export default AiAssistant;