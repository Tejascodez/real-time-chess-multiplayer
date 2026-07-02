import { useCallback, useEffect, useMemo, useState } from 'react'
import { Chessboard } from 'react-chessboard'
import { useGameStore } from '../../store/gameStore'
import { useChessGame } from '../../hooks/useChessGame'
import { findKingSquare } from '../../utils/chessUtils'
import { PromotionModal } from './PromotionModal'

const LAST_MOVE_COLOR    = 'rgba(124, 58, 237, 0.28)'
const SELECTED_COLOR     = 'rgba(124, 58, 237, 0.45)'
const LEGAL_DOT_COLOR    = 'rgba(34, 197, 94, 0.7)'
const LEGAL_CAPTURE_RING = 'rgba(34, 197, 94, 0.8)'

export function ChessBoard({ onMove }) {
  const { fen, errorMessage, moveHistory, inCheck, currentTurn, assignedColor } = useGameStore()
  const {
    chess,
    isMyTurn,
    boardOrientation,
    validateMove,
    getLegalMovesFrom,
    pendingPromotion,
    cancelPromotion,
    confirmPromotion,
  } = useChessGame()

  const [selectedSquare, setSelectedSquare] = useState(null)

  // Clear any in-progress selection whenever the position changes
  // (our move landed, opponent moved, or the game reset)
  useEffect(() => setSelectedSquare(null), [fen])

  const legalTargets = useMemo(
    () => (selectedSquare ? getLegalMovesFrom(selectedSquare) : []),
    [selectedSquare, getLegalMovesFrom]
  )

  const lastMove = useMemo(() => {
    const last = moveHistory[moveHistory.length - 1]
    return last ? { from: last.slice(0, 2), to: last.slice(2, 4) } : null
  }, [moveHistory])

  const checkedKingSquare = useMemo(() => {
    if (!inCheck) return null
    return findKingSquare(chess, currentTurn === 'WHITE' ? 'w' : 'b')
  }, [inCheck, currentTurn, chess])

  const squareStyles = useMemo(() => {
    const styles = {}

    if (lastMove) {
      styles[lastMove.from] = { backgroundColor: LAST_MOVE_COLOR }
      styles[lastMove.to]   = { backgroundColor: LAST_MOVE_COLOR }
    }

    if (checkedKingSquare) {
      styles[checkedKingSquare] = {
        ...styles[checkedKingSquare],
        background: 'radial-gradient(circle, rgba(239,68,68,0.85) 0%, rgba(239,68,68,0.35) 55%, transparent 80%)',
      }
    }

    if (selectedSquare) {
      styles[selectedSquare] = { ...styles[selectedSquare], backgroundColor: SELECTED_COLOR }
    }

    legalTargets.forEach((square) => {
      const isCapture = !!chess.get(square)
      styles[square] = {
        ...styles[square],
        backgroundImage: isCapture
          ? `radial-gradient(circle, transparent 52%, ${LEGAL_CAPTURE_RING} 54%, ${LEGAL_CAPTURE_RING} 72%, transparent 74%)`
          : `radial-gradient(circle, ${LEGAL_DOT_COLOR} 30%, transparent 32%)`,
        backgroundRepeat: 'no-repeat',
        backgroundPosition: 'center',
      }
    })

    return styles
  }, [lastMove, checkedKingSquare, selectedSquare, legalTargets, chess])

  // ------------------------------------------------------------
  // Drag & drop
  // ------------------------------------------------------------
  const onDrop = useCallback(
    ({ sourceSquare, targetSquare }) => {
      if (!targetSquare) return false

      const result = validateMove(sourceSquare, targetSquare)

      if (result === 'invalid') return false

      // Promotion: hold the move until the player picks a piece
      if (result === 'promotion') return false

      onMove(sourceSquare, targetSquare, null)
      return true
    },
    [validateMove, onMove]
  )

  const canDrag = useCallback(() => isMyTurn(), [isMyTurn])

  // ------------------------------------------------------------
  // Click to select + click to move
  // ------------------------------------------------------------
  const onSquareClick = useCallback(
    ({ square, piece }) => {
      if (!isMyTurn()) return

      if (selectedSquare && legalTargets.includes(square)) {
        const result = validateMove(selectedSquare, square)
        setSelectedSquare(null)

        if (result === 'invalid') return
        if (result === 'promotion') return // PromotionModal takes over

        onMove(selectedSquare, square, null)
        return
      }

      const pieceColor = piece?.pieceType?.[0] === 'w' ? 'WHITE'
                        : piece?.pieceType?.[0] === 'b' ? 'BLACK'
                        : null

      setSelectedSquare(pieceColor === assignedColor ? square : null)
    },
    [selectedSquare, legalTargets, isMyTurn, validateMove, onMove, assignedColor]
  )

  const handlePromotionSelect = useCallback(
    (piece) => {
      const move = confirmPromotion(piece)
      if (move) onMove(move.from, move.to, move.promotion)
    },
    [confirmPromotion, onMove]
  )

  const chessboardOptions = {
    position: fen,
    boardOrientation,
    onPieceDrop: onDrop,
    onSquareClick,
    canDragPiece: canDrag,
    squareStyles,
    darkSquareStyle: { backgroundColor: '#9c7a54' },
    lightSquareStyle: { backgroundColor: '#f0e2c6' },
    dropSquareStyle: { boxShadow: 'inset 0 0 0 3px rgba(124, 58, 237, 0.65)' },
    boardStyle: { borderRadius: 10, overflow: 'hidden' },
    animationDurationInMs: 200,
  }

  return (
    <div className="relative">
      {/* Error Message */}
      {errorMessage && (
        <div
          className="
            absolute
            -top-10
            left-0
            right-0
            text-center
            text-red-400
            text-sm
            font-medium
            animate-pulse
            z-10
          "
        >
          {errorMessage}
        </div>
      )}

      <Chessboard options={chessboardOptions} />

      <PromotionModal
        isOpen={!!pendingPromotion}
        onSelect={handlePromotionSelect}
        onClose={cancelPromotion}
      />
    </div>
  )
}
