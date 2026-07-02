import { useState, useEffect, useRef } from 'react'
import { useGameStore } from '../../store/gameStore'
import { useWS } from '../../context/WebSocketContext'
import { generatePlayerId } from '../../utils/chessUtils'

// Floating background particle
function Particle({ style }) {
  return <div className="absolute rounded-full pointer-events-none" style={style} />
}

export function LandingPage() {
  const [username, setUsername]   = useState(() => useGameStore.getState().username || '')
  const [roomInput, setRoomInput] = useState('')
  const [loading, setLoading]     = useState(false)
  const [particles, setParticles] = useState([])

  const { setIdentity } = useGameStore()
  const { connect, subscribeToRoom, sendJoin, stompClient } = useWS()

  // Generate particles once on mount
  useEffect(() => {
    setParticles(
      Array.from({ length: 22 }, (_, i) => ({
        id: i,
        left:     `${Math.random() * 100}%`,
        size:     `${Math.random() * 3 + 1}px`,
        duration: `${Math.random() * 14 + 8}s`,
        delay:    `${Math.random() * 10}s`,
        opacity:  Math.random() * 0.15 + 0.05,
      }))
    )
  }, [])

  const handleStart = (joinRoomId = null) => {
    if (!username.trim() || loading) return
    setLoading(true)

    const playerId = generatePlayerId()
    setIdentity(playerId, username)

    if (stompClient.current?.connected) {
      if (joinRoomId) subscribeToRoom(stompClient.current, joinRoomId)
      setTimeout(() => sendJoin(joinRoomId ?? null, playerId, username), 100)
      return
    }

    connect((client) => {
      if (joinRoomId) subscribeToRoom(client, joinRoomId)
      setTimeout(() => sendJoin(joinRoomId ?? null, playerId, username), 100)
    })
  }

  const hasUser = username.trim().length > 0
  const hasRoom = roomInput.trim().length > 0

  return (
    <div
      className="min-h-screen flex items-center justify-center p-6 relative overflow-hidden"
      style={{ background: '#0f1117' }}
    >
      {/* Floating particles */}
      {particles.map((p) => (
        <Particle
          key={p.id}
          style={{
            left: p.left,
            width: p.size,
            height: p.size,
            opacity: p.opacity,
            background: 'rgba(255,255,255,0.15)',
            animation: `floatUp ${p.duration} ${p.delay} linear infinite`,
          }}
        />
      ))}

      {/* Particle keyframes injected once */}
      <style>{`
        @keyframes floatUp {
          0%   { transform: translateY(100vh) scale(0); opacity: 0; }
          10%  { opacity: 1; }
          90%  { opacity: 1; }
          100% { transform: translateY(-10vh) scale(1); opacity: 0; }
        }
        @keyframes chessPulse {
          0%, 100% { transform: scale(1) rotate(-2deg); }
          50%       { transform: scale(1.07) rotate(2deg); }
        }
        @keyframes blink {
          0%, 100% { opacity: 1; }
          50%       { opacity: 0.3; }
        }
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
      `}</style>

      {/* Card */}
      <div
        className="relative z-10 w-full"
        style={{
          maxWidth: 400,
          background: '#1a1d27',
          borderRadius: 20,
          border: '0.5px solid rgba(255,255,255,0.08)',
          padding: '2.5rem 2rem',
          boxShadow: '0 0 0 0.5px rgba(139,92,246,0.12) inset',
        }}
      >
        {/* Logo */}
        <div className="text-center mb-8">
          <span
            style={{
              display: 'block',
              fontSize: '3.75rem',
              marginBottom: '0.75rem',
              animation: 'chessPulse 3s ease-in-out infinite',
              filter: 'drop-shadow(0 0 18px rgba(139,92,246,0.45))',
            }}
          >
            ♟
          </span>

          <h1 style={{ fontSize: '1.75rem', fontWeight: 600, color: '#fff', letterSpacing: '-0.02em' }}>
            Chess
          </h1>

          <p style={{ marginTop: '0.3rem', fontSize: '0.78rem', letterSpacing: '0.06em', color: 'rgba(255,255,255,0.35)', textTransform: 'uppercase' }}>
            <span
              style={{
                display: 'inline-block',
                width: 6, height: 6,
                borderRadius: '50%',
                background: '#10b981',
                marginRight: 6,
                verticalAlign: 'middle',
                position: 'relative', top: -1,
                animation: 'blink 2s ease-in-out infinite',
              }}
            />
            Real-time multiplayer
          </p>
        </div>

        {/* Username field */}
        <div className="mb-5">
          <label style={labelStyle}>Your name</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            placeholder="Enter username"
            autoFocus
            style={inputStyle}
            onFocus={(e) => Object.assign(e.target.style, inputFocusStyle)}
            onBlur={(e)  => Object.assign(e.target.style, inputStyle)}
          />
        </div>

        {/* Create game */}
        <button
          onClick={() => handleStart(null)}
          disabled={!hasUser || loading}
          style={{
            ...primaryBtnStyle,
            ...(!hasUser || loading ? disabledStyle : {}),
          }}
          onMouseEnter={(e) => { if (hasUser && !loading) Object.assign(e.currentTarget.style, primaryBtnStyle, primaryBtnHoverStyle) }}
          onMouseLeave={(e) => { if (hasUser && !loading) Object.assign(e.currentTarget.style, primaryBtnStyle) }}
        >
          {loading ? (
            <>
              <span style={spinnerStyle} />
              Connecting…
            </>
          ) : (
            '♟ Create new game'
          )}
        </button>

        {/* Divider */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', margin: '1.25rem 0' }}>
          <div style={{ flex: 1, height: '0.5px', background: 'rgba(255,255,255,0.08)' }} />
          <span style={{ fontSize: '0.78rem', color: 'rgba(255,255,255,0.25)', letterSpacing: '0.05em' }}>
            or join existing
          </span>
          <div style={{ flex: 1, height: '0.5px', background: 'rgba(255,255,255,0.08)' }} />
        </div>

        {/* Join room */}
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <input
            type="text"
            value={roomInput}
            onChange={(e) => setRoomInput(e.target.value)}
            placeholder="Room ID"
            style={{ ...inputStyle, flex: 1 }}
            onFocus={(e) => Object.assign(e.target.style, inputFocusStyle)}
            onBlur={(e)  => Object.assign(e.target.style, inputStyle)}
          />
          <button
            onClick={() => handleStart(roomInput.trim())}
            disabled={!hasUser || !hasRoom || loading}
            style={{
              ...secondaryBtnStyle,
              ...(!hasUser || !hasRoom || loading ? disabledStyle : {}),
            }}
            onMouseEnter={(e) => { if (hasUser && hasRoom && !loading) Object.assign(e.currentTarget.style, secondaryBtnStyle, secondaryBtnHoverStyle) }}
            onMouseLeave={(e) => { if (hasUser && hasRoom && !loading) Object.assign(e.currentTarget.style, secondaryBtnStyle) }}
          >
            Join
          </button>
        </div>

        {/* Footer note */}
        <p style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.73rem', color: 'rgba(255,255,255,0.18)' }}>
          Games are private — share your Room ID to invite
        </p>
      </div>
    </div>
  )
}

/* ── Shared style objects ─────────────────────────────────────── */

const labelStyle = {
  display: 'block',
  fontSize: '0.78rem',
  color: 'rgba(255,255,255,0.4)',
  marginBottom: '0.5rem',
  letterSpacing: '0.04em',
  textTransform: 'uppercase',
}

const inputStyle = {
  width: '100%',
  background: '#0f1117',
  border: '0.5px solid rgba(255,255,255,0.1)',
  borderRadius: 10,
  padding: '0.75rem 1rem',
  color: '#fff',
  fontSize: '0.95rem',
  fontFamily: 'inherit',
  outline: 'none',
  caretColor: '#8b5cf6',
  transition: 'border-color 0.2s, box-shadow 0.2s',
}

const inputFocusStyle = {
  ...inputStyle,
  borderColor: 'rgba(139,92,246,0.55)',
  boxShadow: '0 0 0 3px rgba(139,92,246,0.12)',
}

const primaryBtnStyle = {
  width: '100%',
  padding: '0.8rem 1rem',
  background: '#7c3aed',
  color: '#fff',
  border: 'none',
  borderRadius: 10,
  fontSize: '0.95rem',
  fontWeight: 500,
  fontFamily: 'inherit',
  cursor: 'pointer',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  gap: 8,
  letterSpacing: '0.01em',
  transition: 'background 0.2s, box-shadow 0.2s, transform 0.1s',
}

const primaryBtnHoverStyle = {
  background: '#8b5cf6',
  boxShadow: '0 4px 16px rgba(139,92,246,0.35)',
}

const secondaryBtnStyle = {
  padding: '0.8rem 1.1rem',
  background: 'transparent',
  color: 'rgba(255,255,255,0.7)',
  border: '0.5px solid rgba(255,255,255,0.12)',
  borderRadius: 10,
  fontSize: '0.95rem',
  fontFamily: 'inherit',
  cursor: 'pointer',
  whiteSpace: 'nowrap',
  transition: 'all 0.2s',
}

const secondaryBtnHoverStyle = {
  background: 'rgba(255,255,255,0.06)',
  borderColor: 'rgba(255,255,255,0.22)',
  color: '#fff',
}

const disabledStyle = {
  opacity: 0.35,
  cursor: 'not-allowed',
}

const spinnerStyle = {
  display: 'inline-block',
  width: 14, height: 14,
  border: '2px solid rgba(255,255,255,0.3)',
  borderTopColor: '#fff',
  borderRadius: '50%',
  animation: 'spin 0.7s linear infinite',
}