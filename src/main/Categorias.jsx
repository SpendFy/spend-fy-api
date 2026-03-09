import { useState, useEffect } from 'react';
import api from '../api/axios';
import ConfirmModal from '../components/ConfirmModal';
import { Plus, Pencil, Trash2, Tags, X, Check, Loader2 } from 'lucide-react';

// Cores pré-definidas para seleção visual
const CORES_DISPONIVEIS = [
  { nome: 'vermelho', hex: '#EF4444' },
  { nome: 'laranja', hex: '#F97316' },
  { nome: 'amarelo', hex: '#EAB308' },
  { nome: 'verde', hex: '#22C55E' },
  { nome: 'azul', hex: '#3B82F6' },
  { nome: 'roxo', hex: '#8B5CF6' },
  { nome: 'rosa', hex: '#EC4899' },
  { nome: 'cinza', hex: '#6B7280' },
];

function getCorHex(nomeOuHex) {
  if (!nomeOuHex) return '#6B7280';
  const found = CORES_DISPONIVEIS.find(
    (c) => c.nome.toLowerCase() === nomeOuHex.toLowerCase()
  );
  return found ? found.hex : nomeOuHex.startsWith('#') ? nomeOuHex : '#6B7280';
}

export default function Categorias() {
  // ─── Estado ────────────────────────────────────────────────────
  const [categorias, setCategorias] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // Formulário
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState({ nome: '', cor: '' });
  const [formError, setFormError] = useState('');

  // Modal de exclusão
  const [deleteModal, setDeleteModal] = useState({ open: false, categoria: null });

  // ─── Fetch categorias ─────────────────────────────────────────
  const fetchCategorias = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await api.get('/categorias');
      setCategorias(response.data);
    } catch (err) {
      setError('Erro ao carregar categorias. Tente novamente.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategorias();
  }, []);

  // ─── Mostrar mensagem temporária ──────────────────────────────
  const showSuccess = (msg) => {
    setSuccessMsg(msg);
    setTimeout(() => setSuccessMsg(''), 3000);
  };

  // ─── Abrir form para criar ────────────────────────────────────
  const handleNewClick = () => {
    setEditingId(null);
    setFormData({ nome: '', cor: '' });
    setFormError('');
    setShowForm(true);
  };

  // ─── Abrir form para editar ───────────────────────────────────
  const handleEditClick = (categoria) => {
    setEditingId(categoria.id);
    setFormData({ nome: categoria.nome, cor: categoria.cor || '' });
    setFormError('');
    setShowForm(true);
  };

  // ─── Cancelar formulário ──────────────────────────────────────
  const handleCancelForm = () => {
    setShowForm(false);
    setEditingId(null);
    setFormData({ nome: '', cor: '' });
    setFormError('');
  };

  // ─── Salvar (criar ou atualizar) ──────────────��───────────────
  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    // Validação
    if (!formData.nome.trim()) {
      setFormError('O nome da categoria é obrigatório.');
      return;
    }

    if (formData.nome.trim().length < 2) {
      setFormError('O nome deve ter pelo menos 2 caracteres.');
      return;
    }

    setSaving(true);

    try {
      if (editingId) {
        // Atualizar
        await api.put(`/categorias/${editingId}`, {
          nome: formData.nome.trim(),
          cor: formData.cor || null,
        });
        showSuccess('Categoria atualizada com sucesso!');
      } else {
        // Criar
        await api.post('/categorias', {
          nome: formData.nome.trim(),
          cor: formData.cor || null,
        });
        showSuccess('Categoria criada com sucesso!');
      }

      handleCancelForm();
      fetchCategorias();
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Erro ao salvar categoria.';
      setFormError(msg);
    } finally {
      setSaving(false);
    }
  };

  // ─── Abrir modal de exclusão ──────────────────────────────────
  const handleDeleteClick = (categoria) => {
    setDeleteModal({ open: true, categoria });
  };

  // ─── Confirmar exclusão ───────────────────────────────────────
  const handleConfirmDelete = async () => {
    if (!deleteModal.categoria) return;

    setDeleting(true);
    try {
      await api.delete(`/categorias/${deleteModal.categoria.id}`);
      showSuccess('Categoria excluída com sucesso!');
      setDeleteModal({ open: false, categoria: null });
      fetchCategorias();
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        'Erro ao excluir categoria. Ela pode estar vinculada a transações.';
      setFormError('');
      setError(msg);
      setDeleteModal({ open: false, categoria: null });
    } finally {
      setDeleting(false);
    }
  };

  // ─── Render ───────────────────────────────────────────────────
  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-blue-100 rounded-lg text-blue-600">
            <Tags size={24} />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Categorias</h1>
            <p className="text-sm text-gray-500">
              Gerencie suas categorias de receitas e despesas
            </p>
          </div>
        </div>
        <button
          onClick={handleNewClick}
          className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg hover:bg-blue-700 transition-colors font-medium shadow-sm"
        >
          <Plus size={18} />
          Nova Categoria
        </button>
      </div>

      {/* Mensagem de sucesso */}
      {successMsg && (
        <div className="mb-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg text-sm flex items-center gap-2">
          <Check size={16} />
          {successMsg}
        </div>
      )}

      {/* Mensagem de erro global */}
      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
          {error}
        </div>
      )}

      {/* ─── Formulário de criação/edição ──────────────────────── */}
      {showForm && (
        <div className="mb-6 bg-white rounded-xl shadow-sm border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">
              {editingId ? 'Editar Categoria' : 'Nova Categoria'}
            </h2>
            <button
              onClick={handleCancelForm}
              className="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <X size={20} />
            </button>
          </div>

          {formError && (
            <div className="mb-4 bg-red-50 border-l-4 border-red-400 text-red-700 px-4 py-3 text-sm">
              {formError}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            {/* Nome */}
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Nome da categoria *
              </label>
              <input
                type="text"
                value={formData.nome}
                onChange={(e) => setFormData({ ...formData, nome: e.target.value })}
                placeholder="Ex: Alimentação, Transporte, Salário..."
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                autoFocus
              />
            </div>

            {/* Cor */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Cor (opcional)
              </label>
              <div className="flex flex-wrap gap-2">
                {CORES_DISPONIVEIS.map((cor) => (
                  <button
                    key={cor.nome}
                    type="button"
                    onClick={() => setFormData({ ...formData, cor: cor.nome })}
                    className={`w-8 h-8 rounded-full border-2 transition-all ${
                      formData.cor === cor.nome
                        ? 'border-gray-900 scale-110 ring-2 ring-offset-2 ring-gray-400'
                        : 'border-transparent hover:scale-110'
                    }`}
                    style={{ backgroundColor: cor.hex }}
                    title={cor.nome}
                  />
                ))}
                {/* Botão para limpar cor */}
                {formData.cor && (
                  <button
                    type="button"
                    onClick={() => setFormData({ ...formData, cor: '' })}
                    className="w-8 h-8 rounded-full border-2 border-dashed border-gray-300 flex items-center justify-center text-gray-400 hover:border-gray-400 transition-colors"
                    title="Remover cor"
                  >
                    <X size={14} />
                  </button>
                )}
              </div>
            </div>

            {/* Botões */}
            <div className="flex gap-3">
              <button
                type="button"
                onClick={handleCancelForm}
                className="px-4 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium transition-colors text-sm"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={saving}
                className="px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 text-sm"
              >
                {saving ? (
                  <>
                    <Loader2 size={16} className="animate-spin" />
                    Salvando...
                  </>
                ) : editingId ? (
                  'Atualizar'
                ) : (
                  'Criar Categoria'
                )}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* ─── Lista de categorias ───────────────────────────────── */}
      {loading ? (
        <div className="flex items-center justify-center py-16">
          <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-500" />
        </div>
      ) : categorias.length === 0 ? (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
          <div className="flex justify-center mb-4">
            <div className="p-4 bg-gray-100 rounded-full">
              <Tags size={32} className="text-gray-400" />
            </div>
          </div>
          <h3 className="text-lg font-semibold text-gray-700 mb-1">
            Nenhuma categoria encontrada
          </h3>
          <p className="text-sm text-gray-500 mb-4">
            Crie sua primeira categoria para organizar suas transações.
          </p>
          <button
            onClick={handleNewClick}
            className="inline-flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium text-sm"
          >
            <Plus size={16} />
            Criar primeira categoria
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <table className="w-full text-left">
            <thead className="bg-gray-50 border-b border-gray-100">
              <tr>
                <th className="px-6 py-4 font-semibold text-gray-600 text-sm">Cor</th>
                <th className="px-6 py-4 font-semibold text-gray-600 text-sm">Nome</th>
                <th className="px-6 py-4 font-semibold text-gray-600 text-sm text-right">
                  Ações
                </th>
              </tr>
            </thead>
            <tbody>
              {categorias.map((cat) => (
                <tr
                  key={cat.id}
                  className="border-b border-gray-50 last:border-none hover:bg-gray-50/50 transition-colors"
                >
                  {/* Cor */}
                  <td className="px-6 py-4">
                    <div
                      className="w-6 h-6 rounded-full border border-gray-200"
                      style={{ backgroundColor: getCorHex(cat.cor) }}
                      title={cat.cor || 'Sem cor'}
                    />
                  </td>
                  {/* Nome */}
                  <td className="px-6 py-4">
                    <span className="font-medium text-gray-900">{cat.nome}</span>
                  </td>
                  {/* Ações */}
                  <td className="px-6 py-4">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => handleEditClick(cat)}
                        className="p-2 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Editar"
                      >
                        <Pencil size={16} />
                      </button>
                      <button
                        onClick={() => handleDeleteClick(cat)}
                        className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="Excluir"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Contador */}
      {!loading && categorias.length > 0 && (
        <p className="mt-4 text-sm text-gray-500">
          {categorias.length} categoria{categorias.length !== 1 ? 's' : ''} cadastrada
          {categorias.length !== 1 ? 's' : ''}
        </p>
      )}

      {/* ─── Modal de confirmação de exclusão ──────────────────── */}
      <ConfirmModal
        isOpen={deleteModal.open}
        title="Excluir Categoria"
        message={`Tem certeza que deseja excluir a categoria "${deleteModal.categoria?.nome}"? Transações vinculadas a ela podem ser afetadas. Esta ação não pode ser desfeita.`}
        onConfirm={handleConfirmDelete}
        onCancel={() => setDeleteModal({ open: false, categoria: null })}
        loading={deleting}
      />
    </div>
  );
}
