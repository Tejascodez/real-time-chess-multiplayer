import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { useGameStore } from '../store/gameStore'
import { useWS } from '../context/WebSocketContext'

import { WaitingRoom } from '../components/Room/WaitingRoom'
import { ChessBoard } from '../components/Board/ChessBoard'
import { PlayerTimer } from '../components/Timer/PlayerTimer'
import { MoveHistory } from './MoveHistory'
import { GameStatusBanner } from './GameStatus'
import { Modal } from '../components/UI/Modal'

export function GamePage() {

  const { roomId: routeRoomId } = useParams()
  const navigate = useNavigate()
  const [showResignConfirm, setShowResignConfirm] = useState(false)
  const [showUndoConfirm, setShowUndoConfirm] = useState(false)

  const {
    playerId,
    username,
    roomId,
    roomStatus,
    assignedColor,
    gameOverInfo,
    opponentDisconnected,
    moveHistory,
    whiteUndoUsed,
    blackUndoUsed,
  } = useGameStore()

  const {
    connect,
    subscribeToRoom,
    sendJoin,
    sendMove,
    sendResign,
    sendUndo,
    stompClient,
  } = useWS()

  const myUndoUsed = assignedColor === 'WHITE' ? whiteUndoUsed : blackUndoUsed
  const lastMoverColor = moveHistory.length % 2 === 1 ? 'WHITE' : 'BLACK'
  const canUndo =
    roomStatus === 'IN_PROGRESS' &&
    !myUndoUsed &&
    moveHistory.length > 0 &&
    lastMoverColor === assignedColor

  useEffect(() => {
    if (!routeRoomId || !playerId || !username) return

    if (stompClient.current?.connected) {
      subscribeToRoom(stompClient.current, routeRoomId)
      return
    }

    connect((client) => {
      subscribeToRoom(client, routeRoomId)
      setTimeout(() => {
        sendJoin(routeRoomId, playerId, username)
      }, 100)
    })
  }, [routeRoomId, playerId, username, connect, subscribeToRoom, sendJoin, stompClient])

  const handleMove = (from, to, promotion) => {
    if (!roomId || !playerId) return
    sendMove(roomId, playerId, from, to, promotion)
  }

  const handleResign = () => {
    if (!roomId || !playerId) return
    setShowResignConfirm(false)
    sendResign(roomId, playerId)
  }

  const handleUndo = () => {
    if (!roomId || !playerId) return
    setShowUndoConfirm(false)
    sendUndo(roomId, playerId)
  }

  const getGameOverMessage = () => {
    if (!gameOverInfo) return null

    const { gameStatus, reason, winnerPlayerId } = gameOverInfo

    if (gameStatus === 'STALEMATE' || gameStatus === 'DRAW_FIFTY_MOVE') {
      return { title: "It's a Draw!", subtitle: reason === 'FIFTY_MOVE_RULE' ? 'Fifty-move rule' : 'Stalemate', isDraw: true }
    }

    const youWon = winnerPlayerId === playerId
    return {
      title: youWon ? '🏆 You Win!' : '😢 You Lose',
      subtitle: reason === 'CHECKMATE' ? 'Checkmate'
              : reason === 'RESIGN'    ? 'Opponent resigned'
              : reason === 'TIMEOUT'   ? 'Time out'
              : reason,
      isDraw: false,
      youWon,
    }
  }

  if (roomStatus === 'WAITING_FOR_PLAYER') {
    return <WaitingRoom />
  }

  const gameOver = getGameOverMessage()
  const opponentColor = assignedColor === 'WHITE' ? 'BLACK' : 'WHITE'

  return (
    <div className="min-h-screen bg-primary text-white">
      <div className="mx-auto w-full max-w-[1600px] p-4 sm:p-6 flex flex-col gap-4">

        {/* Top bar */}
        <div className="flex items-center justify-between gap-4">
          <div className="flex items-center gap-2 min-w-0">
            <span className="text-xl shrink-0">♟</span>
            <span className="text-sm text-gray-500 hidden sm:inline">Room</span>
            <code className="text-xs sm:text-sm font-mono text-accent-light truncate">{roomId}</code>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button
              onClick={() => setShowUndoConfirm(true)}
              disabled={!canUndo}
              title={myUndoUsed ? "You've already used your undo" : 'Take back your last move (one-time use)'}
              className="text-sm px-4 py-2 rounded-lg border border-accent/30 text-accent-light hover:bg-accent/10 hover:border-accent/60 transition-colors disabled:opacity-30 disabled:cursor-not-allowed disabled:hover:bg-transparent"
            >
              Undo
            </button>

            <button
              onClick={() => setShowResignConfirm(true)}
              className="text-sm px-4 py-2 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10 hover:border-red-500/60 transition-colors"
            >
              Resign
            </button>
          </div>
        </div>

        {/* Status banners */}
        <GameStatusBanner />

        {opponentDisconnected && (
          <div className="text-center py-2 px-4 rounded-lg font-semibold text-sm bg-yellow-500/10 border border-yellow-500/40 text-yellow-400">
            ⚠️ Opponent disconnected — waiting for them to reconnect…
          </div>
        )}

        {/* Board + sidebar */}
        <div className="grid grid-cols-1 lg:grid-cols-[minmax(0,1fr)_320px] gap-6 items-start">

          <div
            className="flex flex-col gap-3 mx-auto w-full"
            style={{ maxWidth: 'min(100%, calc(100vh - 17rem))' }}
          >
            <PlayerTimer color={opponentColor} />

            <div className="rounded-2xl border border-white/8 bg-surface p-2 sm:p-3 shadow-2xl">
              <ChessBoard onMove={handleMove} />
            </div>

            <PlayerTimer color={assignedColor} />
          </div>

          <div className="lg:h-[calc(100vh-14rem)] lg:min-h-80">
            <MoveHistory />
          </div>

        </div>

      </div>

      {/* Resign confirmation */}
      <Modal isOpen={showResignConfirm} onClose={() => setShowResignConfirm(false)}>
        <h2 className="text-xl font-bold text-center mb-2">Resign this game?</h2>
        <p className="text-gray-400 text-center text-sm mb-6">
          This counts as a loss and can't be undone.
        </p>
        <div className="flex gap-3">
          <button
            onClick={() => setShowResignConfirm(false)}
            className="flex-1 py-2.5 rounded-lg border border-gray-700 text-gray-300 hover:bg-white/5 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleResign}
            className="flex-1 py-2.5 rounded-lg bg-red-500/15 border border-red-500/40 text-red-400 hover:bg-red-500/25 transition-colors font-semibold"
          >
            Resign
          </button>
        </div>
      </Modal>

      {/* Undo confirmation */}
      <Modal isOpen={showUndoConfirm} onClose={() => setShowUndoConfirm(false)}>
        <h2 className="text-xl font-bold text-center mb-2">Undo your last move?</h2>
        <p className="text-gray-400 text-center text-sm mb-6">
          Each player only gets one undo per game. This can't be undone.
        </p>
        <div className="flex gap-3">
          <button
            onClick={() => setShowUndoConfirm(false)}
            className="flex-1 py-2.5 rounded-lg border border-gray-700 text-gray-300 hover:bg-white/5 transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={handleUndo}
            className="flex-1 py-2.5 rounded-lg bg-accent/15 border border-accent/40 text-accent-light hover:bg-accent/25 transition-colors font-semibold"
          >
            Undo
          </button>
        </div>
      </Modal>

      {/* Winner Alert Overlay */}
      {gameOver && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm flex items-center justify-center z-50 p-4">
          <div className="bg-surface border border-gray-700 rounded-2xl p-10 text-white text-center shadow-2xl max-w-sm w-full">

            <div className="text-6xl mb-4">
              {gameOver.isDraw ? '🤝' : gameOver.youWon ? '🏆' : '😢'}
            </div>

            <h2 className="text-3xl font-bold mb-2">{gameOver.title}</h2>
            <p className="text-gray-400 mb-8">{gameOver.subtitle}</p>

            <button
              onClick={() => navigate('/')}
              className="w-full py-3 rounded-xl bg-accent hover:bg-accent-light font-semibold transition-colors"
            >
              Back to Lobby
            </button>

          </div>
        </div>
      )}
    </div>
  )
}