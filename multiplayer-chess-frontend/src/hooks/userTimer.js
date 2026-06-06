import { useEffect, useRef } from 'react'
import { useGameStore } from '../store/gameStore'

export function useTimer() {

  const {
    whiteTimeMs,
    blackTimeMs,
    currentTurn,
    roomStatus,
    gameStatus,
    updateTimers,
  } = useGameStore()

  const intervalRef = useRef(null)

  useEffect(() => {

    // Only tick when the game is actively being played
    const shouldTick =
      roomStatus  === 'IN_PROGRESS' &&
      gameStatus  === 'ONGOING'

    if (!shouldTick) {
      clearInterval(intervalRef.current)
      return
    }

    // Tick every 100ms for smooth display
    // Server TIMER_UPDATE every 1s keeps us honest
    intervalRef.current = setInterval(() => {
      if (currentTurn === 'WHITE') {
        updateTimers(Math.max(0, whiteTimeMs - 100), blackTimeMs)
      } else {
        updateTimers(whiteTimeMs, Math.max(0, blackTimeMs - 100))
      }
    }, 100)

    return () => clearInterval(intervalRef.current)

  }, [roomStatus, gameStatus, currentTurn, whiteTimeMs, blackTimeMs])
}