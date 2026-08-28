<template>
  <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Editar Pin</h2>
        <button class="btn-close" @click="$emit('close')">&times;</button>
      </div>

      <form @submit.prevent="submitEdit" class="edit-form">
        <div class="form-group">
          <label for="title">Título</label>
          <input 
            id="title" 
            v-model="formData.title" 
            type="text" 
            placeholder="Adicione um título"
          />
        </div>

        <div class="form-group">
          <label for="description">Descrição</label>
          <textarea 
            id="description" 
            v-model="formData.description" 
            placeholder="Conte mais sobre este Pin"
            rows="4"
          ></textarea>
        </div>

        <div class="form-group">
          <label for="link">Link</label>
          <input 
            id="link" 
            v-model="formData.link" 
            type="url" 
            placeholder="Adicione um link de destino"
          />
        </div>

        <div class="form-group">
          <label for="altText">Texto Alternativo</label>
          <input 
            id="altText" 
            v-model="formData.altText" 
            type="text" 
            placeholder="Descreva o que as pessoas podem ver no Pin"
          />
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="$emit('close')">Cancelar</button>
          <button type="submit" class="btn-save" :disabled="isSaving">
            {{ isSaving ? 'Salvando...' : 'Salvar Alterações' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import api from '../services/api';

const props = defineProps({
  show: Boolean,
  pin: Object
});

const emit = defineEmits(['close', 'success']);

const formData = ref({
  title: '',
  description: '',
  link: '',
  altText: ''
});

const isSaving = ref(false);
const error = ref('');

// Preenche o formulário quando o modal abrir e o pin existir
watch(() => props.show, (newVal) => {
  if (newVal && props.pin) {
    formData.value = {
      title: props.pin.title || '',
      description: props.pin.description || '',
      link: props.pin.link || '',
      altText: props.pin.altText || ''
    };
    error.value = '';
  }
});

const submitEdit = async () => {
  isSaving.value = true;
  error.value = '';
  
  try {
    const response = await api.put(`/pins/${props.pin.id}`, formData.value);
    emit('success', response.data);
  } catch (err) {
    console.error('Erro ao editar pin:', err);
    error.value = err.response?.data?.message || 'Não foi possível salvar as alterações.';
  } finally {
    isSaving.value = false;
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
}

.modal-content {
  background-color: var(--color-background);
  border-radius: var(--border-radius);
  width: 90%;
  max-width: 500px;
  padding: var(--spacing-xl);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--color-text-light);
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

label {
  font-weight: 600;
  font-size: 0.875rem;
}

input, textarea {
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-family: inherit;
  background-color: var(--color-surface);
  color: var(--color-text);
}

input:focus, textarea:focus {
  outline: none;
  border-color: var(--color-primary);
}

.error-message {
  color: var(--color-error, #ff4444);
  font-size: 0.875rem;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  margin-top: var(--spacing-md);
}

.btn-cancel, .btn-save {
  padding: 10px 20px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-cancel {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.btn-cancel:hover {
  background: var(--color-surface);
}

.btn-save {
  background: var(--color-primary);
  border: none;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
