import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'

import { useGameStore } from '../store/gameStore'
import { useWS } from '../context/WebSocketContext'

import { WaitingRoom } from '../components/Room/WaitingRoom'
import { ChessBoard } from '../components/Board/ChessBoard'

export function GamePage() {

  const { roomId: routeRoomId } = useParams()
  const navigate = useNavigate()

  const {
    playerId,
    username,
    roomId,
    roomStatus,
    assignedColor,
    currentTurn,
    gameOverInfo,
  } = useGameStore()

  const {
    connect,
    subscribeToRoom,
    sendJoin,
    sendMove,
    stompClient,
  } = useWS()

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

  return (
    <div className="min-h-screen bg-primary flex items-center justify-center p-4">
      <div className="w-full max-w-6xl grid grid-cols-1 lg:grid-cols-3 gap-6">

        <div className="lg:col-span-2">
          <ChessBoard onMove={handleMove} />
        </div>

        <div className="bg-surface border border-gray-700 rounded-2xl p-6 text-white">
          <h2 className="text-2xl font-bold mb-4">Game Info</h2>
          <div className="space-y-3">
            <div>
              <span className="text-gray-400">Room:</span>
              <p className="font-mono text-sm break-all">{roomId}</p>
            </div>
            <div>
              <span className="text-gray-400">You are:</span>
              <p>{assignedColor}</p>
            </div>
            <div>
              <span className="text-gray-400">Current Turn:</span>
              <p>{currentTurn}</p>
            </div>
          </div>
        </div>

      </div>

      {/* Winner Alert Overlay */}
      {gameOver && (
        <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
          <div className="bg-surface border border-gray-700 rounded-2xl p-10 text-white text-center shadow-2xl max-w-sm w-full mx-4">

            <div className="text-6xl mb-4">
              {gameOver.isDraw ? '🤝' : gameOver.youWon ? '🏆' : '😢'}
            </div>

            <h2 className="text-3xl font-bold mb-2">{gameOver.title}</h2>
            <p className="text-gray-400 mb-8">{gameOver.subtitle}</p>

            <button
              onClick={() => navigate('/')}
              className="w-full py-3 rounded-xl bg-blue-600 hover:bg-blue-500 font-semibold transition-colors"
            >
              Back to Lobby
            </button>

          </div>
        </div>
      )}
    </div>
  )
}