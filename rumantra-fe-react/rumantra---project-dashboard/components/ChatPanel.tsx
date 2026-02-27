import React, { useState, useEffect, useRef } from 'react';
import { Send, MoreVertical, X } from 'lucide-react';
import { MOCK_MESSAGES, CURRENT_PROJECT } from '../constants';

interface ChatPanelProps {
    isOpen: boolean;
    onClose: () => void;
}

export const ChatPanel: React.FC<ChatPanelProps> = ({ isOpen, onClose }) => {
  const [messageText, setMessageText] = useState('');
  const [messages, setMessages] = useState(MOCK_MESSAGES);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isOpen && bottomRef.current) {
        bottomRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [isOpen, messages]);

  const handleSend = () => {
    if (!messageText.trim()) return;
    const newMsg = {
        id: Date.now().toString(),
        senderId: 'user_001',
        senderName: 'Me',
        text: messageText,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        isMe: true
    };
    setMessages([...messages, newMsg]);
    setMessageText('');
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-y-0 right-0 w-full md:w-96 bg-white shadow-2xl z-50 border-l border-gray-200 flex flex-col transform transition-transform duration-300 ease-in-out">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between bg-white z-10">
        <div className="flex items-center space-x-3">
            <div className="relative">
                <img 
                    src={CURRENT_PROJECT.architect.avatarUrl} 
                    alt={CURRENT_PROJECT.architect.name} 
                    className="w-10 h-10 rounded-full object-cover border border-gray-100"
                />
                <span className="absolute bottom-0 right-0 w-2.5 h-2.5 bg-green-500 border-2 border-white rounded-full"></span>
            </div>
            <div>
                <h3 className="font-semibold text-gray-900 text-sm">{CURRENT_PROJECT.architect.name}</h3>
                <p className="text-xs text-gray-500">{CURRENT_PROJECT.architect.firmName}</p>
            </div>
        </div>
        <div className="flex items-center space-x-1">
             <button className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition">
                <MoreVertical size={18} />
            </button>
            <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition">
                <X size={20} />
            </button>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-6 space-y-6 bg-gray-50">
        {messages.map((msg) => (
            <div key={msg.id} className={`flex ${msg.isMe ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[80%] ${msg.isMe ? 'order-1' : 'order-2'}`}>
                    <div className={`p-4 rounded-2xl text-sm leading-relaxed shadow-sm ${
                        msg.isMe 
                        ? 'bg-gray-900 text-white rounded-br-none' 
                        : 'bg-white text-gray-700 border border-gray-200 rounded-bl-none'
                    }`}>
                        {msg.text}
                    </div>
                    <span className={`text-[10px] text-gray-400 mt-1 block ${msg.isMe ? 'text-right' : 'text-left'}`}>
                        {msg.timestamp}
                    </span>
                </div>
            </div>
        ))}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div className="p-4 bg-white border-t border-gray-200">
        <form 
            onSubmit={(e) => { e.preventDefault(); handleSend(); }}
            className="flex items-center bg-gray-50 rounded-full border border-gray-200 px-2 py-2 focus-within:ring-2 focus-within:ring-gray-900 focus-within:bg-white transition-all"
        >
            <input 
                type="text" 
                value={messageText}
                onChange={(e) => setMessageText(e.target.value)}
                placeholder="Type a message..."
                className="flex-1 bg-transparent border-none focus:ring-0 text-sm px-4 text-gray-900 placeholder-gray-400 focus:outline-none"
            />
            <button 
                type="submit"
                disabled={!messageText.trim()} 
                className="p-2 bg-gray-900 text-white rounded-full hover:bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
                <Send size={16} />
            </button>
        </form>
      </div>
    </div>
  );
};