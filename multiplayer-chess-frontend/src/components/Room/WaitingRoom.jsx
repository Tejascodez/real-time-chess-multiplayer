import { useGameStore } from '../../store/gameStore'

export function WaitingRoom() {

  const { roomId, assignedColor } = useGameStore()

  const copyRoomId = async () => {

    if (!roomId) return

    try {
      await navigator.clipboard.writeText(roomId)
      console.log('Room ID copied')
    } catch (err) {
      console.error('Failed to copy room ID', err)
    }
  }

  // Prevent rendering before room exists
  if (!roomId) {
    return (
      <div className="min-h-screen flex items-center justify-center text-white">
        Loading room...
      </div>
    )
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">

      <div
        className="
          bg-surface
          border border-gray-700
          rounded-2xl
          p-8
          w-full
          max-w-md
          text-center
          shadow-2xl
        "
      >

        {/* Spinner */}
        <div
          className="
            w-16 h-16
            border-4
            border-accent
            border-t-transparent
            rounded-full
            animate-spin
            mx-auto
            mb-6
          "
        />

        <h2 className="text-2xl font-bold mb-2">
          Waiting for opponent
        </h2>

        <p className="text-gray-400 mb-6">
          You are playing as{' '}
          <span className="text-white font-semibold">
            {assignedColor}
          </span>
        </p>

        {/* Room ID */}
        <div className="bg-primary rounded-xl p-4 mb-4">

          <p className="text-sm text-gray-400 mb-2">
            Share this Room ID
          </p>

          <div className="flex items-center gap-2">

            <code
              className="
                flex-1
                text-accent
                font-mono
                text-sm
                break-all
              "
            >
              {roomId}
            </code>

            <button
              onClick={copyRoomId}
              className="
                text-gray-400
                hover:text-white
                transition-colors
                text-sm
                px-2
              "
            >
              Copy
            </button>

          </div>
        </div>

        <p className="text-gray-600 text-sm">
          Game starts automatically when opponent joins
        </p>

      </div>
    </div>
  )
}