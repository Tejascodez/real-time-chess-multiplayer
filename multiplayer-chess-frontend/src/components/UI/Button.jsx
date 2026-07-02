export function Button({ children, onClick, variant = 'primary', disabled, className = '' }) {

  const base = 'px-6 py-3 rounded-lg font-semibold transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed'

  const variants = {
    primary:   'bg-accent hover:bg-accent-light text-white',
    secondary: 'bg-surface hover:bg-surface-hover text-white border border-gray-700',
    success:   'bg-green-600 hover:bg-green-700 text-white',
    danger:    'bg-transparent hover:bg-red-500/10 text-red-400 border border-red-500/30 hover:border-red-500/60',
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