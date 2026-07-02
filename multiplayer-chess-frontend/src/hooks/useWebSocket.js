import { useRef, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

import { useGameStore } from '../store/gameStore'
import { useNavigate } from 'react-router-dom'

const WS_URL = 'http://localhost:8080/ws/chess'

export function useWebSocket() {

  const stompClient = useRef(null)

  const roomSubRef = useRef(null)

  const handleMsgRef = useRef(null)

  const navigate = useNavigate()

  const {
    setRoomJoined,
    setRoomStatus,
    applyMove,
    applyUndo,
    updateTimers,
    setGameOver,
    setOpponentDisconnected,
    setErrorMessage,
  } = useGameStore()

  // ------------------------------------------------------------
  // CONNECT
  // ------------------------------------------------------------

  const connect = useCallback((onConnected) => {

    // Prevent duplicate client creation
    if (stompClient.current?.connected) {
      console.log('Already connected')
      return
    }

    const client = new Client({

      webSocketFactory: () => new SockJS(WS_URL),

      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      reconnectDelay: 3000,

      onConnect: () => {

        console.log('✅ WebSocket connected')

        // ----------------------------------------------------
        // ROOM QUEUE
        // ----------------------------------------------------

        client.subscribe('/user/queue/room', (msg) => {

          console.log('📩 ROOM MESSAGE', msg.body)

          const wsMsg = JSON.parse(msg.body)

          handleMsgRef.current?.(wsMsg)
        })

        // ----------------------------------------------------
        // ERROR QUEUE
        // ----------------------------------------------------

        client.subscribe('/user/queue/errors', (msg) => {

          console.log('❌ ERROR MESSAGE', msg.body)

          const wsMsg = JSON.parse(msg.body)

          setErrorMessage(wsMsg.payload)

          setTimeout(() => {
            setErrorMessage(null)
          }, 3000)
        })

        // Ensure subscriptions ready
        setTimeout(() => {

          if (onConnected) {
            onConnected(client)
          }

        }, 100)
      },

      onDisconnect: () => {

        console.log('❌ WebSocket disconnected')

        roomSubRef.current = null
      },

      onStompError: (frame) => {

        console.error('STOMP ERROR', frame)

        setErrorMessage('Connection error')
      },
    })

    client.activate()

    stompClient.current = client

  }, [setErrorMessage])

  // ------------------------------------------------------------
  // ROOM SUBSCRIBE
  // ------------------------------------------------------------

  const subscribeToRoom = useCallback((client, roomId) => {

    if (!roomId) return

    // Prevent duplicate subscriptions
    if (roomSubRef.current) {
      console.log('Already subscribed to room')
      return
    }

    console.log('📡 Subscribing to room', roomId)

    roomSubRef.current = client.subscribe(

      `/topic/game/${roomId}`,

      (msg) => {

        console.log('📩 ROOM BROADCAST', msg.body)

        const wsMsg = JSON.parse(msg.body)

        handleMsgRef.current?.(wsMsg)
      }
    )

  }, [])

  // ------------------------------------------------------------
  // MESSAGE HANDLER
  // ------------------------------------------------------------

  const handleMessage = useCallback((wsMsg) => {

    console.log('📨 MESSAGE TYPE', wsMsg.type)

    const { type, payload } = wsMsg

    switch (type) {

      case 'ROOM_JOINED': {

        console.log('✅ ROOM_JOINED', payload)

        setRoomJoined(
          payload.roomId,
          payload.assignedColor,
          payload.roomStatus,
          payload.currentFen,
          payload.whiteUndoUsed,
          payload.blackUndoUsed
        )

        if (stompClient.current) {

          subscribeToRoom(
            stompClient.current,
            payload.roomId
          )
        }

        navigate(`/game/${payload.roomId}`)

        break
      }

      case 'GAME_STARTED': {

        console.log('🎮 GAME_STARTED')

        setRoomStatus('IN_PROGRESS')

        break
      }

      case 'MOVE_MADE': {

        console.log('♟️ MOVE_MADE RECEIVED', payload)

        applyMove(
          payload.from,
          payload.to,
          payload.promotion,
          payload.newFen,
          payload.nextTurn,
          payload.inCheck,
          payload.gameStatus
        )

        updateTimers(
          payload.whiteTimeRemainingMs,
          payload.blackTimeRemainingMs
        )

        break
      }

      case 'MOVE_UNDONE': {

        console.log('↩️ MOVE_UNDONE RECEIVED', payload)

        applyUndo(
          payload.newFen,
          payload.nextTurn,
          payload.inCheck,
          payload.gameStatus,
          payload.whiteUndoUsed,
          payload.blackUndoUsed
        )

        break
      }

      case 'TIMER_UPDATE': {

        updateTimers(
          payload.whiteTimeRemainingMs,
          payload.blackTimeRemainingMs
        )

        break
      }

      case 'INVALID_MOVE': {

        console.log('❌ INVALID MOVE', payload)

        setErrorMessage(payload)

        setTimeout(() => {
          setErrorMessage(null)
        }, 3000)

        break
      }

      case 'GAME_OVER': {

        console.log('🏁 GAME OVER', payload)

        setGameOver({
          gameStatus: payload.gameStatus,
          reason: payload.reason,
          winnerPlayerId: payload.winnerPlayerId,
        })

        break
      }

      case 'PLAYER_DISCONNECTED': {

        console.log('⚠️ PLAYER DISCONNECTED')

        setOpponentDisconnected(true)

        break
      }

      case 'PLAYER_RECONNECTED': {

        console.log('✅ PLAYER RECONNECTED')

        setOpponentDisconnected(false)

        break
      }

      case 'ERROR': {

        console.log('❌ GENERAL ERROR', payload)

        setErrorMessage(payload)

        setTimeout(() => {
          setErrorMessage(null)
        }, 3000)

        break
      }

      default:

        console.warn('Unknown message type', type)
    }

  }, [
    navigate,
    setRoomJoined,
    setRoomStatus,
    applyMove,
    applyUndo,
    updateTimers,
    setGameOver,
    setOpponentDisconnected,
    setErrorMessage,
    subscribeToRoom,
  ])

  handleMsgRef.current = handleMessage

  // ------------------------------------------------------------
  // SEND JOIN
  // ------------------------------------------------------------

  const sendJoin = useCallback((roomId, playerId, username) => {

    console.log('🚪 SENDING JOIN', {
      roomId,
      playerId,
      username,
    })

    stompClient.current?.publish({

      destination: '/app/join',

      body: JSON.stringify({
        roomId,
        playerId,
        username,
      }),
    })

  }, [])

  // ------------------------------------------------------------
  // SEND MOVE
  // ------------------------------------------------------------

  const sendMove = useCallback((roomId, playerId, from, to, promotion = null) => {

    console.log('♟️ SENDING MOVE', {
      roomId,
      playerId,
      from,
      to,
      promotion,
    })

    stompClient.current?.publish({

      destination: '/app/move',

      body: JSON.stringify({
        roomId,
        playerId,
        from,
        to,
        promotion,
      }),
    })

  }, [])

  // ------------------------------------------------------------
  // SEND RESIGN
  // ------------------------------------------------------------

  const sendResign = useCallback((roomId, playerId) => {

    stompClient.current?.publish({

      destination: '/app/resign',

      body: JSON.stringify({
        roomId,
        playerId,
      }),
    })

  }, [])

  // ------------------------------------------------------------
  // SEND UNDO
  // ------------------------------------------------------------

  const sendUndo = useCallback((roomId, playerId) => {

    stompClient.current?.publish({

      destination: '/app/undo',

      body: JSON.stringify({
        roomId,
        playerId,
      }),
    })

  }, [])

  // ------------------------------------------------------------
  // DISCONNECT
  // ------------------------------------------------------------

  const disconnect = useCallback(() => {

    if (roomSubRef.current) {

      roomSubRef.current.unsubscribe()

      roomSubRef.current = null
    }

    stompClient.current?.deactivate()

  }, [])

  return {
    connect,
    subscribeToRoom,
    sendJoin,
    sendMove,
    sendResign,
    sendUndo,
    disconnect,
    stompClient,
  }
}