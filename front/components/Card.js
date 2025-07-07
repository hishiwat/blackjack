'use client';

export default function Card({ card, hidden = false }) {
  if (hidden) {
    return (
      <div className="w-16 h-24 bg-blue-800 border-2 border-white rounded-lg flex items-center justify-center">
        <div className="text-white text-xs">?</div>
      </div>
    );
  }

  if (!card) {
    return null;
  }

  const suit = card.charAt(0);
  const rank = card.substring(1);

  const getCardEmoji = (suit, rank) => {
    // トランプの絵文字を使用
    const cardEmojis = {
      'SA': '🂡', 'S2': '🂢', 'S3': '🂣', 'S4': '🂤', 'S5': '🂥', 'S6': '🂦', 'S7': '🂧', 'S8': '🂨', 'S9': '🂩', 'ST': '🂪', 'SJ': '🂫', 'SQ': '🂭', 'SK': '🂮',
      'HA': '🂱', 'H2': '🂲', 'H3': '🂳', 'H4': '🂴', 'H5': '🂵', 'H6': '🂶', 'H7': '🂷', 'H8': '🂸', 'H9': '🂹', 'HT': '🂺', 'HJ': '🂻', 'HQ': '🂽', 'HK': '🂾',
      'DA': '🃁', 'D2': '🃂', 'D3': '🃃', 'D4': '🃄', 'D5': '🃅', 'D6': '🃆', 'D7': '🃇', 'D8': '🃈', 'D9': '🃉', 'DT': '🃊', 'DJ': '🃋', 'DQ': '🃍', 'DK': '🃎',
      'CA': '🃑', 'C2': '🃒', 'C3': '🃓', 'C4': '🃔', 'C5': '🃕', 'C6': '🃖', 'C7': '🃗', 'C8': '🃘', 'C9': '🃙', 'CT': '🃚', 'CJ': '🃛', 'CQ': '🃝', 'CK': '🃞'
    };
    
    const cardKey = suit + rank;
    return cardEmojis[cardKey] || '🂠'; // デフォルトは白いカード
  };



  const cardEmoji = getCardEmoji(suit, rank);

  return (
    <div className="w-16 h-24 bg-white border-2 border-gray-300 rounded-lg flex items-center justify-center shadow-md">
      <div className="text-2xl">
        {cardEmoji}
      </div>
    </div>
  );
} 