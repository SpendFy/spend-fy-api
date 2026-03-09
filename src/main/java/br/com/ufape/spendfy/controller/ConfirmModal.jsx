import { AlertTriangle, X } from 'lucide-react';

export default function ConfirmModal({ isOpen, title, message, onConfirm, onCancel, loading }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Overlay */}
      <div 
        className="fixed inset-0 bg-black/50 transition-opacity" 
        onClick={onCancel}
      />

      {/* Modal */}
      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 p-6 z-10">
        {/* Botão fechar */}
        <button
          onClick={onCancel}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <X size={20} />
        </button>

        {/* Ícone de alerta */}
        <div className="flex items-center justify-center w-12 h-12 bg-red-100 rounded-full mb-4 mx-auto">
          <AlertTriangle className="text-red-600" size={24} />
        </div>

        {/* Título */}
        <h3 className="text-lg font-bold text-gray-900 text-center mb-2">
          {title || 'Confirmar exclusão'}
        </h3>

        {/* Mensagem */}
        <p className="text-sm text-gray-500 text-center mb-6">
          {message || 'Tem certeza que deseja excluir este item? Esta ação não pode ser desfeita.'}
        </p>

        {/* Botões */}
        <div className="flex gap-3">
          <button
            onClick={onCancel}
            disabled={loading}
            className="flex-1 px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium transition-colors disabled:opacity-50"
          >
            Cancelar
          </button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className="flex-1 px-4 py-2.5 bg-red-600 text-white rounded-lg hover:bg-red-700 font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <div className="animate-spin rounded-full h-4 w-4 border-2 border-white border-t-transparent" />
                Excluindo...
              </>
            ) : (
              'Sim, excluir'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
