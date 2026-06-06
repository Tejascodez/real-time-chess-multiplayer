import { useCallback } from 'react'
import { Chessboard } from 'react-chessboard'
import { useGameStore } from '../../store/gameStore'
import { useChessGame } from '../../hooks/useChessGame'

export function ChessBoard({ onMove }) {
  const { fen, errorMessage } = useGameStore()
  const { isMyTurn, boardOrientation } = useChessGame()

  // ------------------------------------------------------------
  // v5 API: onPieceDrop receives a single object, not 3 args
  // ------------------------------------------------------------
  const onDrop = useCallback(
    ({ sourceSquare, targetSquare, piece }) => {
      console.log('♟️ PIECE DROP', { sourceSquare, targetSquare, piece })

      if (!isMyTurn()) {
        console.log('❌ Not your turn')
        return false
      }

      if (!targetSquare) {
        return false
      }

      // Detect pawn promotion: pawn reaches rank 8 (white) or rank 1 (black)
      const isPawn = piece?.pieceType?.[1] === 'P'
      const isPromotion =
        isPawn &&
        ((piece.pieceType[0] === 'w' && targetSquare[1] === '8') ||
         (piece.pieceType[0] === 'b' && targetSquare[1] === '1'))

      const promotion = isPromotion ? 'q' : null

      onMove(sourceSquare, targetSquare, promotion)
      return true
    },
    [isMyTurn, onMove]
  )

  // ------------------------------------------------------------
  // v5 API: canDragPiece replaces arePiecesDraggable
  // ------------------------------------------------------------
  const canDrag = useCallback(
    () => isMyTurn(),
    [isMyTurn]
  )

  // ------------------------------------------------------------
  // v5 API: all config goes inside the `options` prop
  // ------------------------------------------------------------
  const chessboardOptions = {
    position: fen,
    boardOrientation,
    onPieceDrop: onDrop,
    canDragPiece: canDrag,
    darkSquareStyle: { backgroundColor: '#b58863' },
    lightSquareStyle: { backgroundColor: '#f0d9b5' },
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
    </div>
  )
}