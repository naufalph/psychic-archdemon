
import React, { useState, useRef, useEffect } from 'react';
import { ChatMessage, Conversation } from '../types';
import { Send, Paperclip, MoreVertical, Image as ImageIcon, FileText, Check, CheckCheck } from 'lucide-react';
import Button from './Button';

interface ChatInterfaceProps {
  initialMessages: ChatMessage[];
  recipientName: string;
  recipientRole: string;
}

const ChatInterface: React.FC<ChatInterfaceProps> = ({ initialMessages, recipientName, recipientRole }) => {
  const [messages, setMessages] = useState<ChatMessage[]>(initialMessages);
  const [inputValue, setInputValue] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = () => {
    if (!inputValue.trim()) return;
    
    const newMessage: ChatMessage = {
      id: Math.random().toString(36).substr(2, 9),
      senderId: 'me',
      senderName: 'Me',
      text: inputValue,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      isMe: true
    };
    
    setMessages([...messages, newMessage]);
    setInputValue('');
  };

  return (
    <div className="flex flex-col h-[600px] bg-white rounded-3xl border border-gray-100 shadow-sm overflow-hidden animate-in fade-in duration-500">
      {/* Chat Header */}
      <div className="p-6 border-b border-gray-100 flex items-center justify-between bg-white">
        <div className="flex items-center gap-4">
          <div className="w-10 h-10 rounded-xl bg-gray-900 text-white flex items-center justify-center font-bold shadow-md">
            {recipientName.charAt(0)}
          </div>
          <div>
            <h3 className="text-sm font-bold text-gray-900">{recipientName}</h3>
            <p className="text-[10px] text-green-500 font-bold uppercase tracking-widest flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-green-500 animate-pulse"></span>
              {recipientRole} • Online
            </p>
          </div>
        </div>
        <button className="text-gray-400 hover:text-black">
          <MoreVertical size={20} />
        </button>
      </div>

      {/* Message Area */}
      <div className="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar bg-gray-50/30">
        {messages.map((msg) => (
          <div key={msg.id} className={`flex ${msg.isMe ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-[80%] space-y-1`}>
              {!msg.isMe && <span className="text-[10px] font-bold text-gray-400 ml-1">{msg.senderName}</span>}
              <div className={`p-4 rounded-2xl text-sm ${
                msg.isMe 
                  ? 'bg-black text-white rounded-tr-none shadow-md' 
                  : 'bg-white border border-gray-100 text-gray-800 rounded-tl-none'
              }`}>
                {msg.text}
                
                {msg.attachments?.map((att, i) => (
                  <div key={i} className={`mt-3 p-3 rounded-xl flex items-center gap-3 border ${
                    msg.isMe ? 'bg-white/10 border-white/20' : 'bg-gray-50 border-gray-100'
                  }`}>
                    {att.type === 'image' ? <ImageIcon size={16} /> : <FileText size={16} />}
                    <span className="text-xs truncate font-medium">{att.name}</span>
                  </div>
                ))}
              </div>
              <div className={`flex items-center gap-1.5 px-1 ${msg.isMe ? 'justify-end' : 'justify-start'}`}>
                <span className="text-[9px] text-gray-400 font-bold">{msg.timestamp}</span>
                {msg.isMe && <CheckCheck size={12} className="text-indigo-500" />}
              </div>
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="p-4 bg-white border-t border-gray-100">
        <div className="flex items-center gap-3 bg-gray-50 rounded-2xl p-2 focus-within:bg-white focus-within:ring-2 focus-within:ring-gray-100 transition-all group">
          <button className="p-2.5 text-gray-400 hover:text-black hover:bg-white rounded-xl transition-all">
            <Paperclip size={20} />
          </button>
          <input 
            type="text" 
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSendMessage()}
            placeholder="Write a message..."
            className="flex-1 bg-transparent border-none outline-none text-sm font-medium placeholder:text-gray-400 px-2"
          />
          <button 
            onClick={handleSendMessage}
            disabled={!inputValue.trim()}
            className="p-3 bg-black text-white rounded-xl hover:bg-gray-800 disabled:opacity-30 disabled:cursor-not-allowed transition-all shadow-lg active:scale-95"
          >
            <Send size={18} />
          </button>
        </div>
        <p className="text-[10px] text-center text-gray-400 mt-3 font-bold uppercase tracking-widest">
          End-to-End Encrypted Secure Channel
        </p>
      </div>
    </div>
  );
};

export default ChatInterface;
