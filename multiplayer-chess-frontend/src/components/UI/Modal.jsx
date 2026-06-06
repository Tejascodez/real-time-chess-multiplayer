export function Modal({ isOpen, children }) {
  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black/70 flex items-center justify-center z-50">
      <div className="bg-surface border border-gray-700 rounded-2xl p-8 max-w-md w-full mx-4 shadow-2xl">
        {children}
      </div>
    </div>
  )
}