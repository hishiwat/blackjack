'use client';
import { useState } from 'react';
import Card from './Card';

export default function GameBoard({ gameState, playerList, onDisconnect, wsGameRef }) {
  const [betAmount, setBetAmount] = useState('');

  const calculateScore = (cards) => {
    let score = 0;
    let aceCount = 0;
    
    for (const card of cards) {
      const rank = card.substring(1);
      if (rank === 'A') {
        aceCount++;
        score += 11;
      } else if (['T', 'J', 'Q', 'K'].includes(rank)) {
        score += 10;
      } else {
        score += parseInt(rank);
      }
    }
    
    while (score > 21 && aceCount > 0) {
      score -= 10;
      aceCount--;
    }
    
    return score;
  };

  const handleBet = () => {
    const amount = parseInt(betAmount);
    if (amount > 0 && amount <= gameState.chips && wsGameRef.current) {
      wsGameRef.current.sendBet(amount);
      setBetAmount('');
    }
  };

  const handleReady = () => {
    wsGameRef.current?.sendReady();
  };

  const handleHit = () => {
    wsGameRef.current?.sendHit();
  };

  const handleStand = () => {
    wsGameRef.current?.sendStand();
  };

  const handleContinueYes = () => {
    wsGameRef.current?.sendContinue(true);
  };

  const handleContinueNo = () => {
    wsGameRef.current?.sendContinue(false);
    onDisconnect();
  };

  const handleRequestPlayerList = () => {
    wsGameRef.current?.requestPlayerList();
  };
  console.log(gameState);

  return (
    <div className="max-w-4xl mx-auto p-6 bg-green-800 min-h-screen">
      {/* ヘッダー情報 */}
      <div className="bg-green-700 p-4 rounded-lg mb-6 text-white">
        <div className="flex justify-between items-center">
          <div className="flex">
            <h2 className="text-xl font-bold">プレイヤー: {gameState.playerName}</h2>
            <p>ID: {gameState.playerId}</p>
          </div>
          <div className="text-right">
            <p className="text-lg font-bold">チップ: {gameState.chips}</p>
            {gameState.bet > 0 && <p>ベット: {gameState.bet}</p>}
          </div>
        </div>
        <div className="mt-2">
          <button
            onClick={handleRequestPlayerList}
            className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-sm"
          >
            プレイヤー一覧
          </button>
          <button
            onClick={onDisconnect}
            className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-sm ml-2"
          >
            切断
          </button>
        </div>
      </div>

      {/* プレイヤー一覧 */}
      {playerList.length > 0 && (
        <div className="bg-green-700 p-4 rounded-lg mb-6">
          <h3 className="text-white text-lg font-bold mb-2">プレイヤー一覧</h3>
          <div className="text-white">
            {playerList.map((player, index) => (
              <div key={index} className="mb-1">
                {player}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ディーラーのカード */}
      <div className="bg-green-700 p-4 rounded-lg mb-6">
        <h3 className="text-white text-lg font-bold mb-2">ディーラー</h3>
        <div className="flex gap-2">
          {gameState.dealerCards.map((card, index) => (
            <Card key={index} card={card} />
          ))}
          {gameState.gameStatus === 'playing' && (
            <Card card="" hidden={true} />
          )}
        </div>
      </div>

      {/* プレイヤーのカード */}
      <div className="bg-green-700 p-4 rounded-lg mb-6">
        <h3 className="text-white text-lg font-bold mb-2">あなたのカード</h3>
        <div className="flex gap-2 mb-2">
          {gameState.cards.map((card, index) => (
            <Card key={index} card={card} />
          ))}
        </div>
        {gameState.cards.length > 0 && (
          <p className="text-white font-bold">
            スコア: {calculateScore(gameState.cards)}
          </p>
        )}
      </div>

      {/* メッセージ */}
      {gameState.message && (
        <div className="bg-blue-600 p-4 rounded-lg mb-6 text-white text-center">
          <p className="text-lg">{gameState.message}</p>
        </div>
      )}

      {/* ゲームコントロール */}
      <div className="bg-green-700 p-4 rounded-lg">
        {gameState.gameStatus === 'waiting' && (
          <button
            onClick={handleReady}
            className="bg-yellow-500 hover:bg-yellow-600 text-black font-bold py-2 px-4 rounded"
          >
            準備完了
          </button>
        )}

        {gameState.gameStatus === 'betting' && (
          <div className="flex gap-2 items-center">
            <input
              type="number"
              value={betAmount}
              onChange={(e) => setBetAmount(e.target.value)}
              placeholder="ベット額を入力"
              className="px-3 py-2 border rounded text-black"
              min="1"
              max={gameState.chips}
            />
            <button
              onClick={handleBet}
              disabled={!betAmount || parseInt(betAmount) <= 0 || parseInt(betAmount) > gameState.chips}
              className="bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white font-bold py-2 px-4 rounded"
            >
              ベット
            </button>
          </div>
        )}

        {gameState.gameStatus === 'playing' && (
          <div className="flex gap-4">
            <button
              onClick={handleHit}
              className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded"
            >
              HIT
            </button>
            <button
              onClick={handleStand}
              className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded"
            >
              STAND
            </button>
          </div>
        )}

        {gameState.gameStatus === 'finished' && (
          <div className="flex gap-4">
            <button
              onClick={handleContinueYes}
              className="bg-green-500 hover:bg-green-600 text-white font-bold py-2 px-4 rounded"
            >
              続ける
            </button>
            <button
              onClick={handleContinueNo}
              className="bg-red-500 hover:bg-red-600 text-white font-bold py-2 px-4 rounded"
            >
              終了
            </button>
          </div>
        )}
      </div>
    </div>
  );
} 