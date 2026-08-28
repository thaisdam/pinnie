<template>
  <main class="my-pins-view container">
    
    <!-- Cabeçalho -->
    <header class="my-pins-header">
      <h1 class="page-title">Meus Pins</h1>
      <router-link to="/pin/create" class="btn-primary">
        Criar Pin
      </router-link>
    </header>

    <!-- Lista de Pins -->
    <div v-if="isLoading" class="state-container">
      <div class="loader"></div>
    </div>
    
    <div v-else-if="error" class="state-container error-state">
      <p>{{ error }}</p>
      <button class="btn-retry" @click="fetchMyPins">Tentar novamente</button>
    </div>

    <div v-else-if="pins.length === 0" class="state-container empty-state">
      <h2>Você ainda não tem nenhum Pin.</h2>
      <p>Que tal criar sua primeira inspiração agora mesmo?</p>
    </div>

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
      <div v-if="isLoadingMore" class="loading-more">
        <div class="loader loader-small"></div>
      </div>

      <!-- Fim da lista -->
      <div v-if="!hasNext && pins.length > 0" class="end-of-feed">
        <p>Você chegou ao fim dos seus Pins</p>
      </div>
    </div>

  </main>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import PinCard from '../components/PinCard.vue';

const authStore = useAuthStore();
const pins = ref([]);
const isLoading = ref(true);
const isLoadingMore = ref(false);
const error = ref(null);
const page = ref(0);
const hasNext = ref(true);

const bottomTrigger = ref(null);
let observer = null;

const fetchMyPins = async (isLoadMore = false) => {
  if (!authStore.user) return;
  
  if (isLoadMore) {
    isLoadingMore.value = true;
  } else {
    isLoading.value = true;
    error.value = null;
    page.value = 0;
    pins.value = [];
  }
  
  try {
    const userId = authStore.user.id;
    const response = await api.get(`/pins/user/${userId}?page=${page.value}&size=20`);
    
    if (isLoadMore) {
      pins.value = [...pins.value, ...response.data.content];
    } else {
      pins.value = response.data.content;
    }
    
    hasNext.value = !response.data.last;
    if (hasNext.value) {
      page.value++;
    }
  } catch (err) {
    console.error('Erro ao buscar seus pins:', err);
    if (!isLoadMore) {
      error.value = 'Não foi possível carregar seus pins.';
    }
  } finally {
    isLoading.value = false;
    isLoadingMore.value = false;
  }
};

onMounted(async () => {
  await fetchMyPins();

  observer = new IntersectionObserver((entries) => {
    const target = entries[0];
    if (target.isIntersecting && !isLoading.value && !isLoadingMore.value && hasNext.value) {
      fetchMyPins(true);
    }
  }, {
    rootMargin: '100px'
  });

  if (bottomTrigger.value) {
    observer.observe(bottomTrigger.value);
  }
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>

<style scoped>
.my-pins-view {
  padding: var(--spacing-xl) var(--spacing-md);
  min-height: 80vh;
}

.my-pins-header {
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
  background-color: var(--color-primary);
  color: white;
  padding: 10px 20px;
  border-radius: 24px;
  font-weight: 600;
  text-decoration: none;
  display: inline-block;
  transition: background-color var(--transition-fast);
}

.btn-primary:hover {
  background-color: var(--color-primary-hover);
}

/* States */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  color: var(--color-text-light);
  height: 40vh;
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

/* Masonry Grid */
.masonry-grid {
  column-count: 1;
  column-gap: var(--spacing-md);
  width: 100%;
}

@media (min-width: 640px) {
  .masonry-grid { column-count: 2; }
}

@media (min-width: 1024px) {
  .masonry-grid { column-count: 3; }
}

@media (min-width: 1280px) {
  .masonry-grid { column-count: 4; }
}

@media (min-width: 1536px) {
  .masonry-grid { column-count: 5; }
}

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
