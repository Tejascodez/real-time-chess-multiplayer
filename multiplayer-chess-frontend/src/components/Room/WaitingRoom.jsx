import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useGameStore } from '../../store/gameStore'

export function WaitingRoom() {

  const { roomId, assignedColor } = useGameStore()
  const navigate = useNavigate()
  const [copied, setCopied] = useState(false)

  const copyRoomId = async () => {

    if (!roomId) return

    try {
      await navigator.clipboard.writeText(roomId)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch (err) {
      console.error('Failed to copy room ID', err)
    }
  }

  // Prevent rendering before room exists
  if (!roomId) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-primary text-white">
        Loading room...
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-primary p-4 sm:p-6">

      <div className="w-full max-w-md rounded-[20px] border border-white/8 bg-surface p-8 sm:p-10 text-center shadow-2xl">

        {/* Spinner */}
        <div className="mx-auto mb-6 h-16 w-16 rounded-full border-4 border-accent border-t-transparent animate-spin" />

        <h2 className="text-2xl font-bold mb-2">
          Waiting for opponent
        </h2>

        <p className="text-gray-400 mb-6">
          You are playing as{' '}
          <span className="text-white font-semibold">
            {assignedColor === 'WHITE' ? '♔ White' : '♚ Black'}
          </span>
        </p>

        {/* Room ID */}
        <div className="rounded-xl bg-primary border border-white/6 p-4 mb-4">

          <p className="text-sm text-gray-400 mb-2">
            Share this Room ID
          </p>

          <div className="flex items-center gap-2">

            <code className="flex-1 text-accent-light font-mono text-sm break-all text-left">
              {roomId}
            </code>

            <button
              onClick={copyRoomId}
              className="text-gray-400 hover:text-white transition-colors text-sm px-2 shrink-0"
            >
              {copied ? 'Copied ✓' : 'Copy'}
            </button>

          </div>
        </div>

        <p className="text-gray-600 text-sm mb-6">
          Game starts automatically when opponent joins
        </p>

        <button
          onClick={() => navigate('/')}
          className="text-sm text-gray-500 hover:text-white transition-colors"
        >
          ← Leave room
        </button>

      </div>
    </div>
  )
}