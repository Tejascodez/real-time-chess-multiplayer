import { useGameStore } from '../store/gameStore'

export function GameStatusBanner() {

  const { inCheck, gameStatus, currentTurn, assignedColor } = useGameStore()

  if (gameStatus !== 'ONGOING') return null
  if (!inCheck) return null

  const isMyKingInCheck = currentTurn === assignedColor

  return (
    <div className={`
      text-center py-2 px-4 rounded-lg font-semibold text-sm
      ${isMyKingInCheck
        ? 'bg-red-500/20 border border-red-500 text-red-400'
        : 'bg-yellow-500/20 border border-yellow-500 text-yellow-400'}
    `}>
      {isMyKingInCheck ? '⚠️ Your king is in check!' : '⚔️ Opponent is in check'}
    </div>
  )
}