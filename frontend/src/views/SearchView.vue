<template>
  <main class="search-view container">
    
    <header class="search-header">
      <h1 class="page-title">
        Resultados para "<span class="highlight">{{ currentQuery }}</span>"
      </h1>
    </header>

    <!-- Loading State Inicial -->
    <div v-if="isLoading && pins.length === 0" class="state-container">
      <div class="loader"></div>
      <p>Procurando inspirações...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error && pins.length === 0" class="state-container error-state">
      <h2>Ops!</h2>
      <p>{{ error }}</p>
      <button class="btn-retry" @click="fetchSearch">Tentar novamente</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="!isLoading && pins.length === 0" class="state-container empty-state">
      <h2>Não encontramos nada 😢</h2>
      <p>Tente usar palavras mais genéricas para a busca "<strong>{{ currentQuery }}</strong>".</p>
      <router-link to="/" class="btn-primary">Voltar ao Feed</router-link>
    </div>

    <!-- Content State (Masonry Grid e Scroll Infinito) -->
    <div v-else class="content-state">
      <div class="masonry-grid">
        <PinCard 
          v-for="pin in pins" 
          :key="pin.id" 
          :pin="pin" 
        />
      </div>

      <!-- Gatilho do Scroll Infinito -->
      <div ref="bottomTrigger" class="infinite-scroll-trigger"></div>

      <!-- Loading Adicional Discreto -->
      <div v-if="isLoading && pins.length > 0" class="loading-more">
        <div class="loader loader-small"></div>
      </div>

      <!-- Fim dos resultados -->
      <div v-if="!hasNext && pins.length > 0" class="end-of-feed">
        <p>Você chegou ao fim dos resultados</p>
      </div>
    </div>

  </main>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api';
import PinCard from '../components/PinCard.vue';

const route = useRoute();

const pins = ref([]);
const isLoading = ref(true);
const error = ref(null);
const currentQuery = ref('');

// Paginação
const page = ref(0);
const hasNext = ref(false);

// Scroll
const bottomTrigger = ref(null);
let observer = null;

const resetAndFetch = async (query) => {
  currentQuery.value = query;
  page.value = 0;
  pins.value = [];
  hasNext.value = false;
  
  if (!query || query.length < 2) {
    error.value = 'A busca exige pelo menos 2 caracteres.';
    isLoading.value = false;
    return;
  }
  
  await fetchSearch();
};

const fetchSearch = async () => {
  if (!currentQuery.value || currentQuery.value.length < 2) return;
  
  isLoading.value = true;
  error.value = null;
  
  try {
    const response = await api.get(`/search?q=${encodeURIComponent(currentQuery.value)}&page=${page.value}&size=20`);
    const data = response.data;
    
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
    console.error('Erro na busca:', err);
    if (err.response && err.response.status === 400) {
      error.value = err.response.data || 'Termo de busca inválido.';
    } else {
      error.value = 'Ocorreu um problema ao pesquisar. Tente novamente mais tarde.';
    }
  } finally {
    isLoading.value = false;
  }
};

const setupObserver = () => {
  observer = new IntersectionObserver((entries) => {
    const target = entries[0];
    if (target.isIntersecting && !isLoading.value && hasNext.value) {
      fetchSearch();
    }
  }, {
    rootMargin: '100px'
  });

  if (bottomTrigger.value) {
    observer.observe(bottomTrigger.value);
  }
};

// Escuta ativamente a mudança do parametro ?q= na URL
watch(
  () => route.query.q,
  async (newQ) => {
    if (newQ !== currentQuery.value) {
      await resetAndFetch(newQ || '');
    }
  }
);

onMounted(async () => {
  await resetAndFetch(route.query.q || '');
  setupObserver();
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
.search-view {
  padding: var(--spacing-md);
  min-height: 80vh;
}

.search-header {
  margin-bottom: var(--spacing-lg);
  padding: 0 var(--spacing-md);
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-text);
}

.highlight {
  color: var(--color-primary);
  font-style: italic;
}

/* States */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50vh;
  text-align: center;
  gap: var(--spacing-md);
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
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.btn-retry, .btn-primary {
  background-color: var(--color-surface);
  color: var(--color-text);
  font-weight: 600;
  padding: 10px 24px;
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

/* Masonry Grid (Reaproveitado) */
.masonry-grid {
  column-count: 1;
  column-gap: var(--spacing-md);
  width: 100%;
}

@media (min-width: 640px) { .masonry-grid { column-count: 2; } }
@media (min-width: 1024px) { .masonry-grid { column-count: 3; } }
@media (min-width: 1280px) { .masonry-grid { column-count: 4; } }
@media (min-width: 1536px) { .masonry-grid { column-count: 5; } }

/* Infinite Scroll e Estados Finais */
.infinite-scroll-trigger {
  height: 20px;
  width: 100%;
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: var(--spacing-md) 0 var(--spacing-xl) 0;
}

.loader-small {
  width: 24px;
  height: 24px;
  border-width: 3px;
}

.end-of-feed {
  text-align: center;
  padding: var(--spacing-lg) 0 var(--spacing-xl) 0;
  color: var(--color-text-light);
  font-weight: 500;
  font-size: 0.9rem;
}
</style>
