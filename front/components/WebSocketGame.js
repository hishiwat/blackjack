'use client';
import React, { useEffect, useRef, useState, forwardRef, useImperativeHandle } from 'react';

const WebSocketGame = forwardRef(({ username, onGameStateChange, onPlayerListUpdate }, ref) => {
  const socketRef = useRef(null);
  const [isConnected, setIsConnected] = useState(false);
  const [messages, setMessages] = useState([]);

  useImperativeHandle(ref, () => ({
    sendBet: (amount) => {
      sendMessage(`BET ${amount}`);
    },
    sendReady: () => {
      sendMessage('OK');
    },
    sendHit: () => {
      sendMessage('HIT');
    },
    sendStand: () => {
      sendMessage('STAND');
    },
    sendContinue: (continueGame) => {
      sendMessage(continueGame ? 'CONTINUE_YES' : 'CONTINUE_NO');
    },
    requestPlayerList: () => {
      sendMessage('LIST');
    }
  }));

  useEffect(() => {
    socketRef.current = new WebSocket('ws://localhost:8080');

    socketRef.current.onopen = () => {
      console.log('Connected to Blackjack server');
      setIsConnected(true);
      // プレイヤー名を送信
      socketRef.current.send(username);
    };

    socketRef.current.onmessage = (event) => {
      const message = event.data;
      console.log('Received:', message);

      // メッセージの種類を判定
      if (message.startsWith('ID:')) {
        const playerId = message.substring(3);
        onGameStateChange({ type: 'playerId', id: playerId });
      } else if (message.startsWith('CHIP:')) {
        const chips = message.substring(5);
        onGameStateChange({ type: 'chips', chips: parseInt(chips) });
      } else if (message === 'UsedName') {
        onGameStateChange({ type: 'error', message: 'この名前は既に使用されています' });
      } else if (message === 'Game Start') {
        onGameStateChange({ type: 'gameStart' });
      } else if (message.startsWith('Bet accepted:')) {
        onGameStateChange({ type: 'betAccepted', amount: message.substring(13) });
      } else if (message.startsWith('Waiting for')) {
        onGameStateChange({ type: 'waiting', message });
      } else if (message.startsWith('LIST')) {
        // プレイヤーリストの処理
        const playerList = [];
        // 実際の実装では、プレイヤーリストの解析を行う
		console.log(message);
        onPlayerListUpdate(playerList);
      } else {
        // その他のメッセージ
        const newMessage = {
          type: 'info',
          content: message,
          timestamp: new Date().toLocaleTimeString()
        };
        setMessages(prev => [...prev, newMessage]);
      }
    };

    socketRef.current.onclose = () => {
      console.log('Disconnected from Blackjack server');
      setIsConnected(false);
    };

    // socketRef.current.onerror = (error) => {
    //   console.error('WebSocket error:', error);
    //   setIsConnected(false);
    // };

    return () => {
      socketRef.current?.close();
    };
  }, [username]); // 依存配列からonGameStateChangeとonPlayerListUpdateを削除

  const sendMessage = (message) => {
    if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
      socketRef.current.send(message);
    }
  };

  return null; // このコンポーネントはUIを表示せず、WebSocket通信のみを担当
});

WebSocketGame.displayName = 'WebSocketGame';

export default WebSocketGame; 