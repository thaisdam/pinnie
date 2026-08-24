<template>
  <main class="board-detail-view container">
    
    <!-- Cabeçalho da Pasta -->
    <header class="board-header" v-if="board">
      <h1 class="page-title">{{ board.name }} <i v-if="board.isPrivate" class="ph-fill ph-lock-key" title="Privada"></i></h1>
      <p class="board-desc" v-if="board.description">{{ board.description }}</p>
    </header>

    <!-- Estados -->
    <div v-if="isLoading && pins.length === 0" class="state-container">
      <div class="loader"></div>
      <p>Carregando inspirações...</p>
    </div>

    <div v-else-if="error && pins.length === 0" class="state-container error-state">
      <h2>Ops!</h2>
      <p>{{ error }}</p>
      <button class="btn-retry" @click="fetchData">Tentar novamente</button>
    </div>

    <div v-else-if="!isLoading && pins.length === 0" class="state-container empty-state">
      <h2>Esta pasta ainda está vazia.</h2>
      <p>Navegue pelo Feed e salve alguns Pins aqui!</p>
      <router-link to="/" class="btn-primary">Explorar Feed</router-link>
    </div>

    <!-- Conteúdo (Masonry Grid) -->
    <div v-else class="content-state">
      <div class="masonry-grid">
        <PinCard 
          v-for="pinRef in pins" 
          :key="pinRef.id" 
          :pin="pinRef.pin" 
        />
      </div>

      <!-- Scroll Infinito Trigger (se a API retornasse paginação longa) -->
      <div v-if="hasNext" class="loading-more">
        <button class="btn-retry" @click="fetchPins" :disabled="isLoading">Carregar mais</button>
      </div>
    </div>

  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api';
import PinCard from '../components/PinCard.vue';

const route = useRoute();
const boardId = route.params.id;

const board = ref(null);
const pins = ref([]);
const isLoading = ref(true);
const error = ref(null);

const page = ref(0);
const hasNext = ref(false);

const fetchData = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    // 1. Busca os detalhes da pasta
    const boardResponse = await api.get(`/boards/${boardId}`);
    board.value = boardResponse.data;

    // 2. Busca os pins iniciais
    await fetchPins();
  } catch (err) {
    console.error('Erro ao buscar dados da pasta:', err);
    if (err.response && err.response.status === 403) {
      error.value = 'Esta pasta é privada ou você não tem acesso.';
    } else {
      error.value = 'Ocorreu um erro ao tentar carregar a pasta.';
    }
    isLoading.value = false;
  }
};

const fetchPins = async () => {
  isLoading.value = true;
  try {
    const pinsResponse = await api.get(`/boards/${boardId}/pins?page=${page.value}&size=30`);
    const data = pinsResponse.data;
    
    if (page.value === 0) {
      pins.value = data.content;
    } else {
      pins.value = [...pins.value, ...data.content];
    }
    
    hasNext.value = !data.last;
    if (hasNext.value) {
      page.value++;
    }
  } catch (err) {
    console.error('Erro ao buscar pins da pasta:', err);
    if (page.value > 0) {
      // Falha num carregamento subsequente
      error.value = 'Não foi possível carregar mais pins.';
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.board-detail-view {
  padding: var(--spacing-xl) var(--spacing-md);
  min-height: 80vh;
}

.board-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: var(--spacing-sm);
}

.board-desc {
  font-size: 1rem;
  color: var(--color-text-light);
  max-width: 600px;
  margin: 0 auto;
}

/* States */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 40vh;
  gap: var(--spacing-md);
  color: var(--color-text-light);
  text-align: center;
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

.btn-retry, .btn-primary {
  background-color: var(--color-surface);
  color: var(--color-text);
  font-weight: 600;
  padding: 10px 20px;
  border-radius: 20px;
  border: 1px solid var(--color-border);
  cursor: pointer;
  text-decoration: none;
  transition: background-color var(--transition-fast);
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
  border: none;
  margin-top: var(--spacing-md);
}

.btn-primary:hover {
  background-color: var(--color-primary-hover);
}

.btn-retry:hover {
  background-color: #efefef;
}

.btn-retry:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Masonry Grid (Reaproveitado da Home) */
.masonry-grid {
  column-count: 1;
  column-gap: var(--spacing-md);
  width: 100%;
}

@media (min-width: 640px) { .masonry-grid { column-count: 2; } }
@media (min-width: 1024px) { .masonry-grid { column-count: 3; } }
@media (min-width: 1280px) { .masonry-grid { column-count: 4; } }
@media (min-width: 1536px) { .masonry-grid { column-count: 5; } }

.loading-more {
  display: flex;
  justify-content: center;
  padding: var(--spacing-xl) 0;
}
</style>
