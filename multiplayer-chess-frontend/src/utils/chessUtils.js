/**
 * Format milliseconds into mm:ss display string
 * e.g. 125000 → "2:05"
 */
export function formatTime(ms) {
  if (ms <= 0) return '0:00'
  const totalSeconds = Math.floor(ms / 1000)
  const minutes      = Math.floor(totalSeconds / 60)
  const seconds      = totalSeconds % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
}

/**
 * Convert UCI move string to algebraic display
 * e.g. "e2e4" → "e4",  "e7e8q" → "e8=Q"
 */
export function formatMove(uciMove) {
  if (!uciMove) return ''
  const to        = uciMove.slice(2, 4)
  const promotion = uciMove[4]
  return promotion
    ? `${to}=${promotion.toUpperCase()}`
    : to
}

/**
 * Get result message from GameStatus + winner info
 */
export function getResultMessage(gameOverInfo, playerId) {

  if (!gameOverInfo) return ''

  const { gameStatus, reason, winnerPlayerId } = gameOverInfo
  const youWon = winnerPlayerId === playerId

  switch (gameStatus) {
    case 'WHITE_WINS':
    case 'BLACK_WINS':
      return reason === 'RESIGN'
        ? youWon ? '🏆 Opponent resigned. You win!'
                 : '🏳️ You resigned.'
        : reason === 'TIMEOUT'
        ? youWon ? '🏆 Opponent ran out of time. You win!'
                 : '⏰ You ran out of time.'
        : youWon ? '🏆 Checkmate! You win!'
                 : '♟️ Checkmate. You lose.'
    case 'STALEMATE':
      return '🤝 Stalemate — Draw!'
    case 'DRAW_FIFTY_MOVE':
      return '🤝 50-move rule — Draw!'
    default:
      return 'Game over'
  }
}

/**
 * Generate a random guest player ID
 */
export function generatePlayerId() {
  return 'player_' + Math.random().toString(36).slice(2, 9)
}

/**
 * Read whose turn it is from a FEN's active-color field
 * e.g. "...  w KQkq - 0 1" → 'WHITE', "... b ..." → 'BLACK'
 */
export function getTurnFromFen(fen) {
  return fen?.split(' ')[1] === 'b' ? 'BLACK' : 'WHITE'
}

/**
 * Find the square a given color's king is on (for check highlighting)
 */
export function findKingSquare(chess, color) {
  for (const row of chess.board()) {
    for (const piece of row) {
      if (piece && piece.type === 'k' && piece.color === color) {
        return piece.square
      }
    }
  }
  return null
}