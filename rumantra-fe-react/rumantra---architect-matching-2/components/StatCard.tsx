import React from 'react';
import { Stat } from '../types';
import { TrendingUp, TrendingDown } from 'lucide-react';

interface StatCardProps {
  stat: Stat;
}

const StatCard: React.FC<StatCardProps> = ({ stat }) => {
  return (
    <div className="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
      <h3 className="text-[10px] font-bold uppercase tracking-wider text-gray-400 mb-2">{stat.label}</h3>
      <div className="flex items-baseline gap-2">
        <span className="text-3xl font-extrabold text-gray-900">{stat.value}</span>
      </div>
      {stat.trend && (
        <div className={`mt-2 flex items-center text-xs font-semibold ${stat.trendUp ? 'text-green-600' : 'text-red-600'}`}>
          {stat.trendUp ? <TrendingUp size={12} className="mr-1" /> : <TrendingDown size={12} className="mr-1" />}
          {stat.trend}
        </div>
      )}
    </div>
  );
};

export default StatCard;