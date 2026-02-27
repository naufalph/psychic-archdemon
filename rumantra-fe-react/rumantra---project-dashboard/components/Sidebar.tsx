import React from 'react';
import { LayoutDashboard, FolderOpen, MessageSquare, CreditCard, Settings, LogOut } from 'lucide-react';

export const Sidebar: React.FC = () => {
  const navItems = [
    { icon: <LayoutDashboard size={20} />, label: 'Dashboard', active: true },
    { icon: <FolderOpen size={20} />, label: 'Projects', active: false },
    { icon: <MessageSquare size={20} />, label: 'Messages', active: false },
    { icon: <CreditCard size={20} />, label: 'Payments', active: false },
    { icon: <Settings size={20} />, label: 'Settings', active: false },
  ];

  return (
    <div className="w-64 bg-white border-r border-gray-200 h-screen fixed left-0 top-0 flex flex-col z-20 hidden md:flex">
      <div className="p-8">
        <h1 className="text-2xl font-bold tracking-tight text-gray-900">rumantra.</h1>
      </div>
      
      <nav className="flex-1 px-4 space-y-1">
        {navItems.map((item) => (
          <a
            key={item.label}
            href="#"
            className={`flex items-center px-4 py-3 rounded-full text-sm font-medium transition-colors ${
              item.active 
                ? 'bg-gray-900 text-white' 
                : 'text-gray-500 hover:bg-gray-100 hover:text-gray-900'
            }`}
          >
            <span className="mr-3">{item.icon}</span>
            {item.label}
          </a>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-200">
        <a href="#" className="flex items-center px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors">
          <LogOut size={20} className="mr-3" />
          Sign Out
        </a>
      </div>
    </div>
  );
};