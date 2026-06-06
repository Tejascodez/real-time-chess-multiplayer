import { BrowserRouter, Routes, Route } from 'react-router-dom'

import { LandingPage } from './components/Room/LandingPage'
import { GamePage } from './Game/GamePage'

import { WebSocketProvider } from './context/WebSocketContext'

export default function App() {

  return (

    <BrowserRouter>

      <WebSocketProvider>

        <Routes>
          <Route path="/" element={<LandingPage />} />

          <Route
            path="/game/:roomId"
            element={<GamePage />}
          />
        </Routes>

      </WebSocketProvider>

    </BrowserRouter>
  )
}