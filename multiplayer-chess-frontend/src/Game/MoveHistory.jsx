import { useRef, useEffect } from 'react'
import { useGameStore } from '../store/gameStore'

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
    <div className="bg-surface border border-gray-700 rounded-xl p-4 h-64 overflow-y-auto">

      <h3 className="text-sm font-semibold text-gray-400 mb-3 uppercase tracking-wider">
        Moves
      </h3>

      {pairs.length === 0 && (
        <p className="text-gray-600 text-sm">No moves yet</p>
      )}

      <div className="space-y-1">
        {pairs.map(([white, black], idx) => (
          <div key={idx} className="flex gap-2 text-sm">
            <span className="text-gray-600 w-6">{idx + 1}.</span>
            <span className="text-white w-16">{white?.slice(0,4)}</span>
            <span className="text-gray-300 w-16">{black?.slice(0,4) ?? ''}</span>
          </div>
        ))}
      </div>

      <div ref={bottomRef} />
    </div>
  )
}