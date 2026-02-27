import React from 'react';
import { UserRole } from '../types';
import { Bell, Menu, User } from 'lucide-react';
import AiAssistant from './AiAssistant';

interface LayoutProps {
  children: React.ReactNode;
  role: UserRole;
  onRoleChange: (role: UserRole) => void;
}

const Layout: React.FC<LayoutProps> = ({ children, role, onRoleChange }) => {
  return (
    <div className="min-h-screen bg-[#FAFAFA] flex flex-col">
      {/* Navigation */}
      <nav className="sticky top-0 z-40 bg-white/80 backdrop-blur-md border-b border-gray-100">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
          
          {/* Logo */}
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 bg-black rounded-lg flex items-center justify-center text-white font-black text-lg shadow-md">R</div>
            <span className="text-xl font-extrabold tracking-tighter text-gray-900 uppercase">rumantra</span>
          </div>

          {/* Right Actions */}
          <div className="flex items-center gap-6">
            
            {/* Role Switcher */}
            <div className="hidden md:flex bg-gray-100 rounded-full p-1 items-center">
               <button 
                 onClick={() => onRoleChange('homeowner')}
                 className={`px-4 py-1.5 rounded-full text-[10px] font-bold uppercase tracking-widest transition-all ${role === 'homeowner' ? 'bg-white text-black shadow-sm' : 'text-gray-400'}`}
               >
                 Owner
               </button>
               <button 
                 onClick={() => onRoleChange('architect')}
                 className={`px-4 py-1.5 rounded-full text-[10px] font-bold uppercase tracking-widest transition-all ${role === 'architect' ? 'bg-white text-black shadow-sm' : 'text-gray-400'}`}
               >
                 Studio
               </button>
            </div>

            <div className="flex items-center gap-4">
              <button className="text-gray-400 hover:text-black transition-all relative">
                 <Bell size={20} />
                 <span className="absolute -top-1 -right-1 w-2 h-2 bg-indigo-600 rounded-full border-2 border-white"></span>
              </button>
              
              <div className="h-9 w-9 bg-gray-900 rounded-xl flex items-center justify-center text-white cursor-pointer hover:bg-black transition-all shadow-md">
                 <User size={16} />
              </div>

               <div className="md:hidden">
                 <Menu size={24} className="text-gray-900" />
               </div>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <main className="flex-1 max-w-7xl mx-auto w-full px-6 py-10 md:py-12">
        {children}
      </main>

      {/* Footer */}
      <footer className="border-t border-gray-100 bg-white py-12">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row justify-between items-center gap-8">
           <div className="flex items-center gap-3">
              <span className="text-lg font-black tracking-tighter uppercase text-gray-400">rumantra</span>
              <div className="h-3 w-px bg-gray-200"></div>
              <div className="text-[10px] font-semibold text-gray-400 tracking-wider">© 2025 PREMIUM MATCHMAKING</div>
           </div>
           <div className="flex gap-8 text-[10px] font-bold text-gray-400 uppercase tracking-widest">
             <a href="#" className="hover:text-black">Identity</a>
             <a href="#" className="hover:text-black">Protocol</a>
             <a href="#" className="hover:text-black">Assistance</a>
           </div>
        </div>
      </footer>

      <AiAssistant />
    </div>
  );
};

export default Layout;