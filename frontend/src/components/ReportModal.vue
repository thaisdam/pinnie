<template>
  <div class="modal-overlay" v-if="show" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Reportar {{ targetType === 'PIN' ? 'Pin' : 'Usuário' }}</h3>
        <button class="btn-close" @click="close">&times;</button>
      </div>
      
      <div class="modal-body">
        <p class="warning-text">A moderação do Pinnie leva denúncias muito a sério. Por favor, forneça um motivo detalhado para a nossa equipe avaliar.</p>
        
        <form @submit.prevent="submitReport" class="report-form">
          <div class="form-group">
            <label>Motivo da denúncia:</label>
            <textarea 
              v-model="reason" 
              required 
              minlength="10" 
              maxlength="500" 
              rows="4" 
              placeholder="Descreva o que há de errado com este conteúdo..."
              :disabled="loading"
            ></textarea>
          </div>
          
          <div class="error-msg" v-if="error">{{ error }}</div>
          
          <div class="modal-actions">
            <button type="button" class="btn-secondary" @click="close" :disabled="loading">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="loading || reason.length < 10">
              <span v-if="loading">Enviando...</span>
              <span v-else>Enviar Denúncia</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import api from '../services/api';

const props = defineProps({
  show: Boolean,
  targetType: {
    type: String,
    required: true // 'PIN' or 'USER'
  },
  targetId: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['close', 'success']);

const reason = ref('');
const loading = ref(false);
const error = ref('');

const close = () => {
  reason.value = '';
  error.value = '';
  emit('close');
};

const submitReport = async () => {
  if (reason.value.trim().length < 10) {
    error.value = 'O motivo deve ter pelo menos 10 caracteres.';
    return;
  }
  
  loading.value = true;
  error.value = '';
  
  try {
    await api.post('/reports', {
      targetType: props.targetType,
      targetId: props.targetId,
      reason: reason.value.trim()
    });
    emit('success');
    close();
  } catch (err) {
    if (err.response?.status === 409) {
      error.value = 'Você já enviou uma denúncia para este conteúdo que ainda está pendente.';
    } else if (err.response?.status === 400) {
      error.value = 'Dados inválidos ou você está tentando denunciar a si mesmo.';
    } else {
      error.value = 'Ocorreu um erro ao enviar a denúncia. Tente novamente.';
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-content {
  background-color: var(--color-surface);
  border-radius: var(--border-radius-lg);
  width: 90%;
  max-width: 500px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
}

.modal-header {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: var(--color-error);
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--color-text-light);
}

.modal-body {
  padding: var(--spacing-lg);
}

.warning-text {
  font-size: 0.9rem;
  color: var(--color-text-light);
  margin-bottom: var(--spacing-md);
}

.form-group {
  margin-bottom: var(--spacing-md);
}

.form-group label {
  display: block;
  margin-bottom: var(--spacing-xs);
  font-weight: 600;
}

.form-group textarea {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: var(--spacing-sm);
  font-family: inherit;
  resize: vertical;
}

.form-group textarea:focus {
  outline: none;
  border-color: var(--color-primary);
}

.error-msg {
  color: var(--color-error);
  font-size: 0.9rem;
  margin-bottom: var(--spacing-md);
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-sm);
}

.btn-secondary {
  padding: 8px 16px;
  border: 1px solid var(--color-border);
  background: transparent;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
}

.btn-primary {
  padding: 8px 16px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
}

.btn-primary:disabled, .btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
