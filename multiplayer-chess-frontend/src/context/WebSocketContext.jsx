import { createContext, useContext } from 'react'
import { useWebSocket } from '../hooks/useWebSocket'

const WebSocketContext = createContext(null)

export function WebSocketProvider({ children }) {

  const ws = useWebSocket()

  return (
    <WebSocketContext.Provider value={ws}>
      {children}
    </WebSocketContext.Provider>
  )
}

export function useWS() {
  return useContext(WebSocketContext)
}