import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { getTurnFromFen } from '../utils/chessUtils'

const STARTING_FEN = 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1'

export const useGameStore = create(persist((set, get) => ({

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
  //  Undo — one-time use per player
  // ----------------------------------------------------------------
  whiteUndoUsed: false,
  blackUndoUsed: false,

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
  setRoomJoined: (roomId, assignedColor, roomStatus, fen, whiteUndoUsed = false, blackUndoUsed = false) =>
    set({
      roomId,
      assignedColor,
      roomStatus,
      fen,
      currentTurn:          getTurnFromFen(fen),
      moveHistory:          [],
      gameOverInfo:         null,
      inCheck:              false,
      opponentDisconnected: false,
      errorMessage:         null,
      whiteUndoUsed,
      blackUndoUsed,
    }),

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
  //  Actions — Move undone (from MOVE_UNDONE broadcast)
  // ----------------------------------------------------------------
  applyUndo: (newFen, nextTurn, inCheck, gameStatus, whiteUndoUsed, blackUndoUsed) =>
    set((state) => ({
      fen:         newFen,
      currentTurn: nextTurn,
      inCheck,
      gameStatus,
      moveHistory: state.moveHistory.slice(0, -1),
      whiteUndoUsed,
      blackUndoUsed,
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
      whiteUndoUsed:        false,
      blackUndoUsed:        false,
      whiteTimeMs:          600_000,
      blackTimeMs:          600_000,
      gameOverInfo:         null,
      opponentDisconnected: false,
      errorMessage:         null,
      roomStatus:           null,
      roomId:               null,
    }),
}), {
  name: 'chess-identity',
  // Only identity needs to survive a reload — board/room state is
  // re-fetched from the server on reconnect (see ROOM_JOINED handling).
  partialize: (state) => ({ playerId: state.playerId, username: state.username }),
}))