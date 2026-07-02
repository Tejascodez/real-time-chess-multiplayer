import { useRef, useEffect } from 'react'
import { useGameStore } from '../store/gameStore'
import { formatMove } from '../utils/chessUtils'

export function MoveHistory() {

  const { moveHistory } = useGameStore()
  const bottomRef = useRef(null)

  // Auto-scroll to latest move
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [moveHistory])

  // Group into pairs: [['e2e4','e7e5'], ['d2d4','d7d5'], ...]
  const pairs = []
  for (let i = 0; i < moveHistory.length; i += 2) {
    pairs.push([moveHistory[i], moveHistory[i + 1]])
  }

  return (
    <div className="bg-surface border border-gray-700 rounded-xl p-4 flex flex-col min-h-0 h-full">

      <h3 className="text-sm font-semibold text-gray-400 mb-3 uppercase tracking-wider shrink-0">
        Moves
      </h3>

      {pairs.length === 0 && (
        <p className="text-gray-600 text-sm">No moves yet</p>
      )}

      <div className="space-y-1 overflow-y-auto scrollbar-thin pr-1">
        {pairs.map(([white, black], idx) => (
          <div
            key={idx}
            className="flex gap-2 text-sm rounded-md px-2 py-1 odd:bg-white/2"
          >
            <span className="text-gray-600 w-6">{idx + 1}.</span>
            <span className="text-white w-16 font-mono">{formatMove(white)}</span>
            <span className="text-gray-300 w-16 font-mono">{formatMove(black)}</span>
          </div>
        ))}
        <div ref={bottomRef} />
      </div>
    </div>
  )
}