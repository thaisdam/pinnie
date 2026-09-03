<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Editar Pasta</h2>
        <button class="btn-close" @click="close">&times;</button>
      </div>

      <form @submit.prevent="saveBoard">
        <div class="form-group">
          <label for="boardName">Nome da Pasta</label>
          <input 
            type="text" 
            id="boardName" 
            v-model="formData.name" 
            maxlength="100" 
            required 
          />
        </div>

        <div class="form-group">
          <label for="boardDescription">Descrição</label>
          <textarea 
            id="boardDescription" 
            v-model="formData.description" 
            rows="3"
          ></textarea>
        </div>

        <div class="form-group checkbox-group">
          <input 
            type="checkbox" 
            id="boardPrivate" 
            v-model="formData.isPrivate" 
          />
          <label for="boardPrivate">Manter pasta secreta (privada)</label>
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div class="modal-actions">
          <button type="button" class="btn-delete" @click="confirmDelete" :disabled="isLoading">
            Excluir Pasta
          </button>
          
          <div class="right-actions">
            <button type="button" class="btn-cancel" @click="close" :disabled="isLoading">
              Cancelar
            </button>
            <button type="submit" class="btn-primary" :disabled="isLoading">
              {{ isLoading ? 'Salvando...' : 'Salvar Alterações' }}
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import api from '../services/api';
import { useRouter } from 'vue-router';

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  board: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['close', 'updated', 'deleted']);
const router = useRouter();

const isLoading = ref(false);
const error = ref('');
const formData = ref({
  name: '',
  description: '',
  isPrivate: false
});

watch(() => props.isOpen, (newVal) => {
  if (newVal && props.board) {
    formData.value = {
      name: props.board.name || '',
      description: props.board.description || '',
      isPrivate: props.board.isPrivate || false
    };
    error.value = '';
  }
});

const close = () => {
  emit('close');
};

const saveBoard = async () => {
  if (!formData.value.name.trim()) {
    error.value = 'O nome da pasta é obrigatório.';
    return;
  }

  isLoading.value = true;
  error.value = '';

  try {
    const response = await api.put(`/boards/${props.board.id}`, formData.value);
    emit('updated', response.data);
    close();
  } catch (err) {
    error.value = err.response?.data?.message || 'Erro ao atualizar a pasta.';
  } finally {
    isLoading.value = false;
  }
};

const confirmDelete = async () => {
  if (confirm('Tem certeza que deseja excluir esta pasta? Esta ação não pode ser desfeita.')) {
    isLoading.value = true;
    error.value = '';
    try {
      await api.delete(`/boards/${props.board.id}`);
      emit('deleted');
      close();
      router.push('/boards');
    } catch (err) {
      error.value = 'Erro ao excluir a pasta.';
    } finally {
      isLoading.value = false;
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--color-surface);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--color-text);
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  cursor: pointer;
  color: var(--color-text-light);
  line-height: 1;
}

.btn-close:hover {
  color: var(--color-text);
}

.form-group {
  margin-bottom: var(--spacing-lg);
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-weight: 600;
  margin-bottom: var(--spacing-sm);
  color: var(--color-text);
}

.form-group input[type="text"],
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  font-family: inherit;
  font-size: 1rem;
}

.form-group input[type="text"]:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px rgba(247, 184, 1, 0.2);
}

.checkbox-group {
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

.checkbox-group input[type="checkbox"] {
  width: 18px;
  height: 18px;
}

.checkbox-group label {
  margin-bottom: 0;
  cursor: pointer;
}

.error-message {
  color: var(--color-error);
  margin-bottom: var(--spacing-md);
  font-size: 0.9rem;
}

.modal-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--spacing-xl);
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border);
}

.right-actions {
  display: flex;
  gap: var(--spacing-md);
}

.btn-cancel {
  background: none;
  border: none;
  font-weight: 600;
  cursor: pointer;
  color: var(--color-text-light);
}

.btn-cancel:hover {
  color: var(--color-text);
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-delete {
  background-color: transparent;
  color: var(--color-error);
  border: 1px solid var(--color-error);
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
}

.btn-delete:hover:not(:disabled) {
  background-color: #ffebee;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
