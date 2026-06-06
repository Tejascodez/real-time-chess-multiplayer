import { useState, useCallback, useMemo } from 'react'
import { Chess } from 'chess.js'
import { useGameStore } from '../store/gameStore'

export function useChessGame() {

  const {
    fen,
    assignedColor,
    currentTurn,
    gameStatus,
  } = useGameStore()

  // chess.js instance for local validation only
  // Always in sync with the FEN from the store
  const chess = useMemo(() => new Chess(fen), [fen])

  // Promotion state — set when a pawn reaches the back rank
  const [pendingPromotion, setPendingPromotion] = useState(null)
  // { from, to } — held until player picks piece

  // ----------------------------------------------------------------
  //  Is it this player's turn?
  // ----------------------------------------------------------------

  const isMyTurn = useCallback(() => {
    if (gameStatus !== 'ONGOING') return false
    return currentTurn === assignedColor
  }, [currentTurn, assignedColor, gameStatus])

  // ----------------------------------------------------------------
  //  Legal move squares for a given piece (for highlighting)
  // ----------------------------------------------------------------

  const getLegalMovesFrom = useCallback((square) => {
    const moves = chess.moves({ square, verbose: true })
    return moves.map(m => m.to)
  }, [chess])

  // ----------------------------------------------------------------
  //  Validate a drag-and-drop move attempt
  //  Returns: 'promotion' | 'valid' | 'invalid'
  // ----------------------------------------------------------------

  const validateMove = useCallback((from, to) => {

    if (!isMyTurn()) return 'invalid'

    const moves = chess.moves({ square: from, verbose: true })
    const match  = moves.find(m => m.to === to)

    if (!match) return 'invalid'

    // Check if this move is a pawn promotion
    if (match.flags.includes('p')) {
      setPendingPromotion({ from, to })
      return 'promotion'
    }

    return 'valid'

  }, [chess, isMyTurn])

  // ----------------------------------------------------------------
  //  Cancel a pending promotion (player closed the modal)
  // ----------------------------------------------------------------

  const cancelPromotion = useCallback(() => {
    setPendingPromotion(null)
  }, [])

  // ----------------------------------------------------------------
  //  Confirm promotion piece chosen
  // ----------------------------------------------------------------

  const confirmPromotion = useCallback((piece) => {
    const move = pendingPromotion
    setPendingPromotion(null)
    return { ...move, promotion: piece } // caller sends to backend
  }, [pendingPromotion])

  // ----------------------------------------------------------------
  //  Board orientation — flip for Black player
  // ----------------------------------------------------------------

  const boardOrientation = assignedColor === 'BLACK' ? 'black' : 'white'

  return {
    chess,
    isMyTurn,
    getLegalMovesFrom,
    validateMove,
    pendingPromotion,
    cancelPromotion,
    confirmPromotion,
    boardOrientation,
  }
}