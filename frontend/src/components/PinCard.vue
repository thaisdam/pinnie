<template>
  <div class="pin-card-wrapper">
    <div class="pin-card">
      <div class="image-container" :class="{ 'has-error': imageError }">
        <img 
          v-if="!imageError"
          :src="fullImageUrl" 
          :alt="pin.altText || pin.title || 'Pin image'"
          loading="lazy"
          @error="handleImageError"
          class="pin-image"
        />
        <div v-else class="image-fallback">
          <span class="fallback-icon">⚠️</span>
          <span class="fallback-text">Imagem indisponível</span>
        </div>
      </div>
      <div class="pin-info" v-if="pin.title">
        <h3 class="pin-title">{{ pin.title }}</h3>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  pin: {
    type: Object,
    required: true
  }
});

const imageError = ref(false);

const fullImageUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_BACKEND_URL || '';
  return `${baseUrl}${props.pin.imageUrl}`;
});

function handleImageError() {
  imageError.value = true;
}
</script>

<style scoped>
.pin-card-wrapper {
  break-inside: avoid;
  margin-bottom: var(--spacing-md);
}

.pin-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background-color: var(--color-background);
}

.image-container {
  width: 100%;
  border-radius: var(--border-radius);
  overflow: hidden;
  position: relative;
  background-color: var(--color-surface);
}

.image-container.has-error {
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border);
}

.pin-image {
  width: 100%;
  height: auto;
  display: block;
  transition: transform var(--transition-fast);
}

.pin-card:hover .pin-image {
  transform: scale(1.02);
}

.image-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: var(--color-text-light);
  gap: var(--spacing-sm);
}

.fallback-icon {
  font-size: 24px;
}

.fallback-text {
  font-size: 0.875rem;
}

.pin-info {
  padding: var(--spacing-sm) 0;
}

.pin-title {
  font-size: 0.875rem;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text);
}
</style>
