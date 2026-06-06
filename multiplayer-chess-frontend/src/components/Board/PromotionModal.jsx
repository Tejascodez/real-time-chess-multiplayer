import { Modal } from '../UI/Modal'
import { useGameStore } from '../../store/gameStore'

const PIECES = [
  { value: 'q', label: '♛', name: 'Queen'  },
  { value: 'r', label: '♜', name: 'Rook'   },
  { value: 'b', label: '♝', name: 'Bishop' },
  { value: 'n', label: '♞', name: 'Knight' },
]

export function PromotionModal({ isOpen, onSelect }) {

  const { assignedColor } = useGameStore()

  // Flip Unicode pieces for Black
  const pieces = assignedColor === 'BLACK'
    ? [
        { value: 'q', label: '♕', name: 'Queen'  },
        { value: 'r', label: '♖', name: 'Rook'   },
        { value: 'b', label: '♗', name: 'Bishop' },
        { value: 'n', label: '♘', name: 'Knight' },
      ]
    : PIECES

  return (
    <Modal isOpen={isOpen}>
      <h2 className="text-xl font-bold text-center mb-6">Choose promotion piece</h2>
      <div className="flex justify-center gap-4">
        {pieces.map(({ value, label, name }) => (
          <button
            key={value}
            onClick={() => onSelect(value)}
            className="flex flex-col items-center gap-2 p-4 rounded-xl
                       bg-primary hover:bg-accent/20 border border-gray-700
                       hover:border-accent transition-all duration-200"
          >
            <span className="text-5xl">{label}</span>
            <span className="text-xs text-gray-400">{name}</span>
          </button>
        ))}
      </div>
    </Modal>
  )
}