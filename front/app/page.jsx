'use client';
import { useState, useRef, useCallback } from 'react';
import LoginForm from '../components/LoginForm';
import GameBoard from '../components/GameBoard';
import WebSocketGame from '../components/WebSocketGame';

export default function Home() {
  const [gameState, setGameState] = useState({
    playerId: '',
    playerName: '',
    chips: 500,
    bet: 0,
    cards: [],
    dealerCards: [],
    gameStatus: 'waiting',
    message: ''
  });
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [playerList, setPlayerList] = useState([]);
  const wsGameRef = useRef(null);

  const handleGameStateChange = useCallback((state) => {
    console.log('Game state change:', state);
    
    switch (state.type) {
      case 'playerId':
        setGameState(prev => ({ ...prev, playerId: state.id }));
        break;
      case 'chips':
        setGameState(prev => ({ ...prev, chips: state.chips }));
        break;
      case 'error':
        setError(state.message);
        break;
      case 'gameStart':
        setGameState(prev => ({ ...prev, gameStatus: 'betting', message: 'ゲーム開始！ベットしてください' }));
        break;
      case 'betAccepted':
        setGameState(prev => ({ 
          ...prev, 
          bet: parseInt(state.amount), 
          message: `ベット完了: ${state.amount}チップ`
        }));
        break;
      case 'waiting':
        setGameState(prev => ({ ...prev, message: state.message }));
        break;
      default:
        setGameState(prev => ({ ...prev, message: state.content || state.message }));
    }
  }, []);

  const handlePlayerListUpdate = useCallback((players) => {
    setPlayerList(players);
  }, []);

  const handleLogin = async (playerName) => {
    setIsLoading(true);
    setError(null);
    
    try {
      setGameState(prev => ({ ...prev, playerName }));
      setIsConnected(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : '接続エラーが発生しました');
    } finally {
      setIsLoading(false);
    }
  };

  const handleDisconnect = () => {
    setIsConnected(false);
    setGameState({
      playerId: '',
      playerName: '',
      chips: 500,
      bet: 0,
      cards: [],
      dealerCards: [],
      gameStatus: 'waiting',
      message: ''
    });
    setError(null);
  };

  if (!isConnected) {
    return (
      <LoginForm 
        onLogin={handleLogin} 
        isLoading={isLoading} 
      />
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-green-800 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-lg max-w-md w-full text-center">
          <h2 className="text-2xl font-bold text-red-600 mb-4">エラー</h2>
          <p className="text-gray-700 mb-4">{error}</p>
          <button
            onClick={handleDisconnect}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
          >
            戻る
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-green-800">
      <WebSocketGame
        ref={wsGameRef}
        username={gameState.playerName}
        onGameStateChange={handleGameStateChange}
        onPlayerListUpdate={handlePlayerListUpdate}
      />
      <GameBoard
        gameState={gameState}
        playerList={playerList}
        onDisconnect={handleDisconnect}
        wsGameRef={wsGameRef}
      />
    </div>
  );
}
