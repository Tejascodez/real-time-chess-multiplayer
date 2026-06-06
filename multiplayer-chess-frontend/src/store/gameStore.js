import { create } from 'zustand'

const STARTING_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1'

export const useGameStore = create((set, get) => ({

  // ----------------------------------------------------------------
  //  Identity
  // ----------------------------------------------------------------
  playerId:      null,
  username:      null,
  assignedColor: null,   // 'WHITE' | 'BLACK'

  // ----------------------------------------------------------------
  //  Room
  // ----------------------------------------------------------------
  roomId:     null,
  roomStatus: null,      // 'WAITING_FOR_PLAYER' | 'IN_PROGRESS' | 'FINISHED'

  // ----------------------------------------------------------------
  //  Board
  // ----------------------------------------------------------------
  fen:         STARTING_FEN,
  currentTurn: 'WHITE',
  inCheck:     false,
  moveHistory: [],       // ['e2e4', 'e7e5', ...]
  gameStatus:  'ONGOING', // matches backend GameStatus enum

  // ----------------------------------------------------------------
  //  Timers  (milliseconds)
  // ----------------------------------------------------------------
  whiteTimeMs: 600_000,
  blackTimeMs: 600_000,

  // ----------------------------------------------------------------
  //  UI state
  // ----------------------------------------------------------------
  gameOverInfo:        null,   // { gameStatus, reason, winnerPlayerId }
  opponentDisconnected: false,
  errorMessage:        null,

  // ----------------------------------------------------------------
  //  Actions — Identity
  // ----------------------------------------------------------------
  setIdentity: (playerId, username) =>
    set({ playerId, username }),

  // ----------------------------------------------------------------
  //  Actions — Room
  // ----------------------------------------------------------------
  setRoomJoined: (roomId, assignedColor, roomStatus, fen) =>
    set({ roomId, assignedColor, roomStatus, fen }),

  setRoomStatus: (roomStatus) =>
    set({ roomStatus }),

  // ----------------------------------------------------------------
  //  Actions — Move applied (from MOVE_MADE broadcast)
  // ----------------------------------------------------------------
  applyMove: (from, to, promotion, newFen, nextTurn, inCheck, gameStatus) =>
    set((state) => ({
      fen:         newFen,
      currentTurn: nextTurn,
      inCheck,
      gameStatus,
      moveHistory: [
        ...state.moveHistory,
        from + to + (promotion ?? ''),
      ],
    })),

  // ----------------------------------------------------------------
  //  Actions — Timers (from TIMER_UPDATE broadcast)
  // ----------------------------------------------------------------
  updateTimers: (whiteTimeMs, blackTimeMs) =>
    set({ whiteTimeMs, blackTimeMs }),

  // ----------------------------------------------------------------
  //  Actions — Game over
  // ----------------------------------------------------------------
  setGameOver: (gameOverInfo) =>
    set({ gameOverInfo, roomStatus: 'FINISHED' }),

  // ----------------------------------------------------------------
  //  Actions — Connection events
  // ----------------------------------------------------------------
  setOpponentDisconnected: (val) =>
    set({ opponentDisconnected: val }),

  setErrorMessage: (msg) =>
    set({ errorMessage: msg }),

  // ----------------------------------------------------------------
  //  Actions — Reset for new game
  // ----------------------------------------------------------------
  resetGame: () =>
    set({
      fen:                  STARTING_FEN,
      currentTurn:          'WHITE',
      inCheck:              false,
      moveHistory:          [],
      gameStatus:           'ONGOING',
      whiteTimeMs:          600_000,
      blackTimeMs:          600_000,
      gameOverInfo:         null,
      opponentDisconnected: false,
      errorMessage:         null,
      roomStatus:           null,
      roomId:               null,
    }),
}))