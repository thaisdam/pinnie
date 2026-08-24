<template>
  <main class="home-view container">
    
    <!-- Loading State -->
    <div v-if="feedStore.isLoading && feedStore.pins.length === 0" class="loading-state">
      <div class="loader"></div>
      <p>Buscando inspirações...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="feedStore.error && feedStore.pins.length === 0" class="error-state">
      <h2>Ops!</h2>
      <p>{{ feedStore.error }}</p>
      <button class="retry-btn" @click="feedStore.fetchFeed">Tentar novamente</button>
    </div>

    <div v-else-if="!feedStore.isLoading && feedStore.pins.length === 0" class="empty-state">
      <h2>Ainda não há nada aqui.</h2>
      <p>Seja o primeiro a adicionar inspirações ao Pinnie!</p>
    </div>

    <!-- Content State (Masonry Grid e Scroll Infinito) -->
    <div v-else class="content-state">
      <div class="masonry-grid">
        <PinCard 
          v-for="pin in feedStore.pins" 
          :key="pin.id" 
          :pin="pin" 
        />
      </div>

      <!-- Gatilho do Scroll Infinito -->
      <div ref="bottomTrigger" class="infinite-scroll-trigger"></div>

      <!-- Loading Adicional Discreto -->
      <div v-if="feedStore.isLoading && feedStore.pins.length > 0" class="loading-more">
        <div class="loader loader-small"></div>
      </div>

      <!-- Fim do Feed -->
      <div v-if="!feedStore.hasNext && feedStore.pins.length > 0" class="end-of-feed">
        <p>Você chegou ao fim das inspirações</p>
      </div>
    </div>

  </main>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useFeedStore } from '../stores/feed';
import PinCard from '../components/PinCard.vue';

const feedStore = useFeedStore();
const bottomTrigger = ref(null);
let observer = null;

onMounted(async () => {
  // Carga inicial
  await feedStore.fetchFeed();

  // Configurar IntersectionObserver
  observer = new IntersectionObserver((entries) => {
    const target = entries[0];
    if (target.isIntersecting && !feedStore.isLoading && feedStore.hasNext) {
      feedStore.loadMore();
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
.home-view {
  min-height: 80vh;
}

/* Masonry Grid via CSS Columns */
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

/* States */
.loading-state, .error-state, .empty-state {
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

.retry-btn {
  background-color: var(--color-surface);
  color: var(--color-text);
  font-weight: 600;
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: 20px;
  border: 1px solid var(--color-border);
  transition: background-color var(--transition-fast);
}

.retry-btn:hover {
  background-color: #efefef;
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
