<template>
  <main class="pin-view-container container">
    
    <!-- Loading State -->
    <div v-if="isLoading" class="state-container">
      <div class="loader"></div>
      <p>Carregando Pin...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="state-container error-state">
      <h2>Ops!</h2>
      <p>{{ error }}</p>
      <router-link to="/" class="btn-back">Voltar para o Feed</router-link>
    </div>

    <!-- Pin Content -->
    <div v-else-if="pin" class="pin-layout">
      <!-- Botão Voltar mobile/desktop -->
      <div class="top-bar">
        <router-link to="/" class="btn-back-icon" aria-label="Voltar">
          ← Voltar
        </router-link>
      </div>

      <div class="pin-card-max">
        
        <!-- Esquerda: Imagem -->
        <div class="pin-image-col">
          <img 
            :src="fullImageUrl" 
            :alt="pin.altText || pin.title || 'Imagem do Pin'" 
            class="pin-full-image"
          />
        </div>

        <!-- Direita: Informações -->
        <div class="pin-info-col">
          <div class="pin-actions">
            <!-- Futuro botão salvar pode entrar aqui -->
          </div>
          
          <h1 v-if="pin.title" class="pin-title">{{ pin.title }}</h1>
          <p v-if="pin.description" class="pin-description">{{ pin.description }}</p>
          
          <div v-if="pin.link" class="pin-link">
            <a :href="pin.link" target="_blank" rel="noopener noreferrer" class="external-link">
              Visitar link ↗
            </a>
          </div>

          <div class="pin-creator">
            <div class="creator-avatar"></div>
            <div class="creator-info">
              <span class="creator-label">Criador(a)</span>
              <span class="creator-id">Usuário do Pinnie</span>
            </div>
          </div>
        </div>

      </div>
    </div>
    
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import api from '../services/api';

const route = useRoute();
const pinId = route.params.id;

const pin = ref(null);
const isLoading = ref(true);
const error = ref(null);

const fullImageUrl = computed(() => {
  if (!pin.value) return '';
  const baseUrl = import.meta.env.VITE_BACKEND_URL || '';
  return `${baseUrl}${pin.value.imageUrl}`;
});

const fetchPin = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await api.get(`/pins/${pinId}`);
    pin.value = response.data;
  } catch (err) {
    console.error('Erro ao buscar o Pin:', err);
    if (err.response && err.response.status === 404) {
      error.value = 'Pin não encontrado. Ele pode ter sido excluído.';
    } else {
      error.value = 'Ocorreu um erro ao tentar carregar o Pin.';
    }
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  fetchPin();
});
</script>

<style scoped>
.pin-view-container {
  padding: var(--spacing-lg) var(--spacing-md);
  min-height: 80vh;
}

/* States */
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
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

.btn-back {
  background-color: var(--color-surface);
  color: var(--color-text);
  font-weight: 600;
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: 20px;
  border: 1px solid var(--color-border);
  transition: background-color var(--transition-fast);
  text-decoration: none;
}

.btn-back:hover {
  background-color: #efefef;
}

/* Layout */
.top-bar {
  margin-bottom: var(--spacing-md);
}

.btn-back-icon {
  font-weight: 600;
  color: var(--color-text);
  padding: var(--spacing-sm) 0;
  display: inline-block;
  transition: color var(--transition-fast);
}

.btn-back-icon:hover {
  color: var(--color-primary);
}

.pin-card-max {
  display: flex;
  flex-direction: column;
  background-color: var(--color-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  max-width: 1000px;
  margin: 0 auto;
}

@media (min-width: 768px) {
  .pin-card-max {
    flex-direction: row;
    align-items: stretch;
  }
}

/* Columns */
.pin-image-col {
  flex: 1;
  background-color: var(--color-surface);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.pin-full-image {
  width: 100%;
  height: auto;
  max-height: 80vh;
  object-fit: contain;
  display: block;
}

.pin-info-col {
  flex: 1;
  padding: var(--spacing-xl);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  max-width: 500px;
}

/* Typography & Info */
.pin-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-text);
  line-height: 1.2;
}

.pin-description {
  font-size: 1rem;
  color: var(--color-text);
  line-height: 1.5;
}

.external-link {
  display: inline-block;
  background-color: var(--color-surface);
  color: var(--color-text);
  font-weight: 600;
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: 20px;
  border: 1px solid var(--color-border);
  transition: background-color var(--transition-fast);
}

.external-link:hover {
  background-color: #efefef;
}

/* Creator Mockup */
.pin-creator {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border);
}

.creator-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
}

.creator-info {
  display: flex;
  flex-direction: column;
}

.creator-label {
  font-size: 0.75rem;
  color: var(--color-text-light);
}

.creator-id {
  font-weight: 600;
  color: var(--color-text);
}
</style>
