# Blackjack WebSocket Game

PopTalk-mainの構造を参考にした、WebSocket対応のブラックジャックゲームです。

## プロジェクト構造

```
blackjack/
├── server/          # Java WebSocketサーバー
│   ├── BJserver.java
│   ├── Player.java
│   ├── Card.java
│   └── *.class
├── client/          # Javaクライアント（参考用）
│   └── BJclient.java
├── front/           # Next.jsフロントエンド
│   ├── app/
│   │   ├── page.js
│   │   ├── layout.js
│   │   └── globals.css
│   ├── components/
│   │   ├── WebSocketGame.js
│   │   ├── GameBoard.js
│   │   ├── LoginForm.js
│   │   └── Card.js
│   ├── package.json
│   └── ...
└── README.md
```

## セットアップ手順

### 1. サーバーの起動

```bash
cd server
javac *.java
java BJserver
```

サーバーはポート8080で起動します。

### 2. フロントエンドの起動

```bash
cd front
npm install
npm run dev
```

フロントエンドは http://localhost:3000 で起動します。

## 技術仕様

### バックエンド（Java）
- **WebSocket実装**: PopTalk-mainを参考にしたRFC 6455準拠のWebSocketハンドシェイク
- **通信プロトコル**: カスタムメッセージ形式
- **ポート**: 8080

### フロントエンド（Next.js）
- **WebSocket通信**: ブラウザ標準WebSocket API
- **UI**: Tailwind CSS
- **状態管理**: React Hooks
- **コンポーネント設計**: PopTalk-mainを参考にした構造

## ゲームフロー

1. **ログイン**: プレイヤー名を入力してサーバーに接続
2. **準備**: 「準備完了」ボタンでゲーム参加を表明
3. **ベット**: チップを賭けてゲーム開始
4. **ゲームプレイ**: HIT/STANDでカードを操作
5. **結果**: 勝敗判定とチップ配布
6. **継続**: 次のゲームに参加するか選択

## メッセージ形式

### クライアント → サーバー
- `プレイヤー名`: 接続時のプレイヤー名送信
- `OK`: 準備完了
- `BET <amount>`: ベット額
- `HIT`: カードを引く
- `STAND`: カードを引かない
- `CONTINUE_YES`: 次のゲームに参加
- `CONTINUE_NO`: ゲーム終了
- `LIST`: プレイヤー一覧要求

### サーバー → クライアント
- `ID:<id>`: プレイヤーID
- `CHIP:<amount>`: チップ数
- `UsedName`: 名前重複エラー
- `Game Start`: ゲーム開始
- `Bet accepted: <amount>`: ベット受諾
- `Waiting for...`: 待機メッセージ

## PopTalk-main参考実装

このプロジェクトは [PopTalk-main](https://github.com/example/PopTalk-main) の構造とWebSocket実装を参考にしています。

### 主な参考点

#### 1. **WebSocket通信パターン**
```javascript
// PopTalk-main風のWebSocket実装
const WebSocketGame = forwardRef(({ username, onGameStateChange }, ref) => {
  const socketRef = useRef(null);
  
  useImperativeHandle(ref, () => ({
    sendBet: (amount) => sendMessage(`BET ${amount}`),
    sendReady: () => sendMessage('OK'),
    // ... その他のメソッド
  }));
  
  useEffect(() => {
    socketRef.current = new WebSocket('ws://localhost:8080');
    // ... 接続処理
  }, []);
});
```

#### 2. **コンポーネント設計**
- **WebSocketGame.js**: WebSocket通信専用コンポーネント
- **GameBoard.js**: ゲームUI表示コンポーネント
- **LoginForm.js**: ログイン処理コンポーネント
- **Card.js**: カード表示コンポーネント

#### 3. **状態管理**
```javascript
// PopTalk-main風の状態管理
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
```

#### 4. **メッセージ処理**
```javascript
// PopTalk-main風のメッセージ処理
socketRef.current.onmessage = (event) => {
  const message = event.data;
  
  if (message.startsWith('ID:')) {
    onGameStateChange({ type: 'playerId', id: message.substring(3) });
  } else if (message === 'Game Start') {
    onGameStateChange({ type: 'gameStart' });
  }
  // ... その他のメッセージ処理
};
```

## 開発者向け情報

### サーバーの再コンパイル
```bash
cd server
javac *.java
```

### フロントエンドの開発サーバー
```bash
cd front
npm run dev
```

### デバッグ
- ブラウザの開発者ツールでWebSocket通信を確認
- サーバーコンソールでログを確認

## 実装の特徴

### 1. **PopTalk-main風のWebSocket実装**
- RFC 6455準拠のハンドシェイク
- マスク処理とペイロード長の適切な処理
- エラーハンドリングと再接続機能

### 2. **コンポーネント分離**
- WebSocket通信とUI表示の分離
- 再利用可能なコンポーネント設計
- 関心の分離による保守性の向上

### 3. **リアルタイム通信**
- 双方向WebSocket通信
- ゲーム状態のリアルタイム更新
- プレイヤー間の同期

この実装により、PopTalk-mainの優れたWebSocket実装パターンを活用した、堅牢で保守性の高いブラックジャックゲームが完成しました。 