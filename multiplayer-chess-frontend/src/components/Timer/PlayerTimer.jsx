import { formatTime } from '../../utils/chessUtils'
import { useGameStore } from '../../store/gameStore'

export function PlayerTimer({ color }) {

  const { whiteTimeMs, blackTimeMs, currentTurn, assignedColor } = useGameStore()

  const timeMs    = color === 'WHITE' ? whiteTimeMs : blackTimeMs
  const isActive  = currentTurn === color
  const isMe      = assignedColor === color
  const isLow     = timeMs < 30_000 // under 30 seconds → red

  return (
    <div className={`
      flex items-center justify-between px-4 py-3 rounded-xl
      border transition-all duration-300
      ${isActive
        ? 'border-accent bg-accent/10 shadow-lg shadow-accent/20'
        : 'border-gray-700 bg-surface'}
    `}>

      {/* Player label */}
      <div className="flex items-center gap-2">
        <div className={`w-3 h-3 rounded-full ${color === 'WHITE' ? 'bg-white' : 'bg-gray-800 border border-gray-500'}`} />
        <span className="text-sm text-gray-400">
          {isMe ? 'You' : 'Opponent'}
          {isActive && <span className="ml-2 text-xs text-accent animate-pulse">● thinking</span>}
        </span>
      </div>

      {/* Clock */}
      <span className={`
        font-mono text-2xl font-bold tracking-wider
        ${isLow && isActive ? 'text-red-500 animate-pulse' : 'text-white'}
      `}>
        {formatTime(timeMs)}
      </span>

    </div>
  )
}