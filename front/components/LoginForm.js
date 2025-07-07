'use client';
import { useState } from 'react';

export default function LoginForm({ onLogin, isLoading }) {
  const [playerName, setPlayerName] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (playerName.trim()) {
      onLogin(playerName.trim());
    }
  };

  return (
    <div className="min-h-screen bg-green-800 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-lg max-w-md w-full">
        <h1 className="text-3xl font-bold text-center text-green-800 mb-6">
          ブラックジャック
        </h1>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="playerName" className="block text-sm font-medium text-gray-700 mb-2">
              プレイヤー名
            </label>
            <input
              type="text"
              id="playerName"
              value={playerName}
              onChange={(e) => setPlayerName(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
              placeholder="プレイヤー名を入力"
              disabled={isLoading}
              required
            />
          </div>
          <button
            type="submit"
            disabled={!playerName.trim() || isLoading}
            className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-400 text-white font-bold py-2 px-4 rounded transition-colors"
          >
            {isLoading ? '接続中...' : 'ゲームに参加'}
          </button>
        </form>
      </div>
    </div>
  );
} 