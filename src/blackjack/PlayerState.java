package blackjack;

public enum PlayerState {
    WAITING, // 初期状態
    READY, // 「OK」を送信した状態
    BET, // ベットを完了した状態
    PLAYING, // ヒット/スタンドを選択できる状態
    STAND, // スタンドを選択した or バーストした状態
    LOGOUT, // 継続しないことを選択した状態
    SPECTATOR,// 観戦状態
    // ここに他の状態を追加していく
}