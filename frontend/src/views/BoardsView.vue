<template>
  <main class="boards-view container">
    
    <!-- Cabeçalho -->
    <header class="boards-header">
      <h1 class="page-title">Minhas Pastas</h1>
      <button 
        class="btn-primary" 
        @click="showCreateForm = !showCreateForm"
      >
        {{ showCreateForm ? 'Cancelar' : 'Criar Pasta' }}
      </button>
    </header>

    <!-- Formulário de Criação -->
    <div v-if="showCreateForm" class="create-form-container">
      <form @submit.prevent="createBoard" class="create-form">
        <div class="form-group">
          <label for="boardName">Nome da Pasta</label>
          <input 
            id="boardName" 
            v-model="newBoard.name" 
            type="text" 
            placeholder="Ex: Inspirações de Casamento" 
            maxlength="100"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="boardDescription">Descrição</label>
          <textarea 
            id="boardDescription" 
            v-model="newBoard.description" 
            placeholder="O que tem nessa pasta?"
            rows="2"
          ></textarea>
        </div>
        
        <div class="form-group checkbox-group">
          <input 
            id="boardPrivate" 
            v-model="newBoard.isPrivate" 
            type="checkbox" 
          />
          <label for="boardPrivate">Manter pasta secreta (privada)</label>
        </div>
        
        <div v-if="createError" class="error-text">{{ createError }}</div>
        
        <div class="form-actions">
          <button type="submit" class="btn-submit" :disabled="isCreating">
            {{ isCreating ? 'Criando...' : 'Salvar Pasta' }}
          </button>
        </div>
      </form>
    </div>

    <!-- Lista de Pastas -->
    <div v-if="isLoading" class="state-container">
      <div class="loader"></div>
    </div>
    
    <div v-else-if="error" class="state-container error-state">
      <p>{{ error }}</p>
      <button class="btn-retry" @click="fetchBoards">Tentar novamente</button>
    </div>

    <div v-else-if="boards.length === 0" class="state-container empty-state">
      <h2>Nenhuma pasta encontrada.</h2>
      <p>Crie sua primeira pasta para começar a salvar suas inspirações!</p>
    </div>

    <div v-else class="boards-grid">
      <router-link 
        v-for="board in boards" 
        :key="board.id" 
        :to="'/boards/' + board.id"
        class="board-card"
      >
        <div class="board-header">
          <h3 class="board-title">{{ board.name }}</h3>
          <i v-if="board.isPrivate" class="ph-fill ph-lock-key private-icon" title="Pasta Privada"></i>
        </div>
        <p class="board-desc">{{ board.description || 'Sem descrição' }}</p>
      </router-link>
    </div>

  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';

const authStore = useAuthStore();
const boards = ref([]);
const isLoading = ref(true);
const error = ref(null);

const showCreateForm = ref(false);
const isCreating = ref(false);
const createError = ref('');
const newBoard = ref({
  name: '',
  description: '',
  isPrivate: false
});

const fetchBoards = async () => {
  if (!authStore.user) return;
  
  isLoading.value = true;
  error.value = null;
  
  try {
    const userId = authStore.user.id;
    // O endpoint backend retorna um Page (content, pageable, etc)
    const response = await api.get(`/users/${userId}/boards?size=50`);
    boards.value = response.data.content;
  } catch (err) {
    console.error('Erro ao buscar pastas:', err);
    error.value = 'Não foi possível carregar as pastas.';
  } finally {
    isLoading.value = false;
  }
};

const createBoard = async () => {
  createError.value = '';
  if (!newBoard.value.name.trim()) {
    createError.value = 'O nome da pasta é obrigatório.';
    return;
  }

  isCreating.value = true;
  try {
    const response = await api.post('/boards', newBoard.value);
    
    // Adiciona reativamente no início da lista
    boards.value.unshift(response.data);
    
    // Reseta form
    newBoard.value = { name: '', description: '', isPrivate: false };
    showCreateForm.value = false;
  } catch (err) {
    console.error('Erro ao criar pasta:', err);
    createError.value = err.response?.data?.message || 'Erro ao criar pasta.';
  } finally {
    isCreating.value = false;
  }
};

onMounted(() => {
  fetchBoards();
});
</script>

<style scoped>
.boards-view {
  padding: var(--spacing-xl) var(--spacing-md);
  min-height: 80vh;
}

.boards-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--spacing-md);
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text);
}

.btn-primary {
  background-color: #FEFCAD;
  border: 1px solid #FEFCAD;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-primary:hover {
  background-color: #efefef;
}

/* Form Container */
.create-form-container {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.checkbox-group {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

label {
  font-weight: 600;
  font-size: 0.875rem;
}

input[type="text"], textarea {
  padding: 10px;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-family: inherit;
}

.error-text {
  color: var(--color-primary);
  font-size: 0.875rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-sm);
}

.btn-submit {
  background-color: var(--color-primary);
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
}

.btn-submit:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-submit:disabled {
  opacity: 0.7;
}

/* States */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  color: var(--color-text-light);
}

.loader {
  border: 4px solid var(--color-surface);
  border-top: 4px solid var(--color-primary);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  100% { transform: rotate(360deg); }
}

.btn-retry {
  margin-top: var(--spacing-md);
  padding: 8px 16px;
  border-radius: 16px;
  border: 1px solid var(--color-border);
  background-color: var(--color-surface);
  cursor: pointer;
}

/* Grid de Pastas */
.boards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: var(--spacing-md);
}

.board-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  cursor: pointer;
  text-decoration: none;
  color: inherit;
}

.board-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
  background-color: var(--color-surface-hover);
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.board-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
}

.private-icon {
  font-size: 1.2rem;
}

.board-desc {
  font-size: 0.875rem;
  color: var(--color-text-light);
  line-height: 1.4;
  margin: 0;
}
</style>
