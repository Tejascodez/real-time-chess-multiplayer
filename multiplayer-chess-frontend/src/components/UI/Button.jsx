export function Button({ children, onClick, variant = 'primary', disabled, className = '' }) {

  const base = 'px-6 py-3 rounded-lg font-semibold transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed'

  const variants = {
    primary:   'bg-accent hover:bg-red-600 text-white',
    secondary: 'bg-surface hover:bg-gray-700 text-white border border-gray-600',
    success:   'bg-green-600 hover:bg-green-700 text-white',
  }

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`${base} ${variants[variant]} ${className}`}
    >
      {children}
    </button>
  )
}