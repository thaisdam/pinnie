<template>
  <main class="profile-view container" v-if="profileUser">
    
    <!-- Header do Perfil -->
    <div class="profile-header">
      <div class="avatar-container">
        <img v-if="profileUser.avatarUrl" :src="fullAvatarUrl" alt="Avatar" class="avatar-img" />
        <div v-else class="avatar-placeholder">
          {{ profileUser.displayName?.charAt(0).toUpperCase() || 'U' }}
        </div>
      </div>
      
      <h1 class="display-name">{{ profileUser.displayName }}</h1>
      <p class="username">@{{ profileUser.username }}</p>
      <p class="bio" v-if="profileUser.bio">{{ profileUser.bio }}</p>

      <div class="stats">
        <span><strong>{{ profileUser.followersCount }}</strong> seguidores</span>
        <span><strong>{{ profileUser.followingCount }}</strong> seguindo</span>
      </div>

      <div class="profile-actions">
        <!-- Se for o próprio usuário, mostrar Editar Perfil -->
        <button v-if="isMyProfile" class="btn btn-secondary" @click="isEditModalOpen = true">
          Editar Perfil
        </button>
        <!-- Senão, mostrar Seguir/Deixar de Seguir (se aplicável no MVP) -->
        <button v-else class="btn btn-primary">
          {{ profileUser.followedByMe ? 'Seguindo' : 'Seguir' }}
        </button>
      </div>
    </div>

    <!-- Abas de Navegação -->
    <div class="profile-tabs">
      <button :class="{ active: activeTab === 'pins' }" @click="activeTab = 'pins'">Criados</button>
      <button :class="{ active: activeTab === 'boards' }" @click="activeTab = 'boards'">Salvos</button>
    </div>

    <!-- Conteúdo: Pins Criados -->
    <div v-if="activeTab === 'pins'" class="tab-content">
      <div v-if="loadingContent" class="loading-state">Carregando pins...</div>
      <div v-else-if="userPins.length === 0" class="empty-state">
        <p>Ainda não há nada para mostrar! Pins que você criar viverão aqui.</p>
      </div>
      <div v-else class="masonry-grid">
        <PinCard v-for="pin in userPins" :key="pin.id" :pin="pin" />
      </div>
    </div>

    <!-- Conteúdo: Pastas Salvas -->
    <div v-if="activeTab === 'boards'" class="tab-content">
      <div v-if="loadingContent" class="loading-state">Carregando pastas...</div>
      <div v-else-if="userBoards.length === 0" class="empty-state">
        <p>Ainda não há pastas para mostrar.</p>
      </div>
      <div v-else class="boards-grid">
        <div v-for="board in userBoards" :key="board.id" class="board-card">
          <router-link :to="'/boards/' + board.id" class="board-link">
            <div class="board-cover">
              <!-- No futuro, colocar capa do board aqui -->
            </div>
            <div class="board-info">
              <h3>{{ board.name }}</h3>
              <p v-if="board.description">{{ board.description }}</p>
            </div>
          </router-link>
        </div>
      </div>
    </div>

    <!-- Modal de Edição -->
    <ProfileEditModal :isOpen="isEditModalOpen" @close="closeEditModal" />
  </main>
  
  <div v-else-if="loading" class="loading-state">
    <div class="loader"></div>
  </div>
  <div v-else class="error-state">
    <h2>Usuário não encontrado</h2>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import PinCard from '../components/PinCard.vue';
import ProfileEditModal from '../components/ProfileEditModal.vue';

const route = useRoute();
const authStore = useAuthStore();
const activeTab = ref('pins');
const isEditModalOpen = ref(false);

const profileUser = ref(null);
const loading = ref(true);
const loadingContent = ref(false);
const userPins = ref([]);
const userBoards = ref([]);

const isMyProfile = computed(() => {
  return authStore.user && profileUser.value && authStore.user.id === profileUser.value.id;
});

const fullAvatarUrl = computed(() => {
  if (!profileUser.value?.avatarUrl) return null;
  const baseUrl = import.meta.env.VITE_BACKEND_URL || '';
  return `${baseUrl}${profileUser.value.avatarUrl}`;
});

async function loadProfile() {
  loading.value = true;
  try {
    const userId = route.params.id;
    // Se userId for 'me', buscamos o profile do authStore.user.id
    const targetId = userId === 'me' ? authStore.user?.id : userId;
    
    if (targetId) {
      const response = await api.get(`/users/${targetId}`);
      profileUser.value = response.data;
      await loadTabContent();
    }
  } catch (error) {
    console.error('Erro ao carregar perfil', error);
  } finally {
    loading.value = false;
  }
}

async function loadTabContent() {
  if (!profileUser.value) return;
  loadingContent.value = true;
  try {
    if (activeTab.value === 'pins') {
      const res = await api.get(`/pins/user/${profileUser.value.id}?page=0&size=50`);
      userPins.value = res.data.content;
    } else if (activeTab.value === 'boards') {
      const res = await api.get(`/users/${profileUser.value.id}/boards?page=0&size=50`);
      userBoards.value = res.data.content;
    }
  } catch (error) {
    console.error(`Erro ao carregar ${activeTab.value}`, error);
  } finally {
    loadingContent.value = false;
  }
}

watch(activeTab, () => {
  loadTabContent();
});

watch(() => route.params.id, () => {
  loadProfile();
});

onMounted(() => {
  loadProfile();
});

function closeEditModal() {
  isEditModalOpen.value = false;
  // Atualiza os dados se for o próprio perfil, pois a store já atualizou
  if (isMyProfile.value && authStore.user) {
    profileUser.value = { ...profileUser.value, ...authStore.user };
  }
}
</script>

<style scoped>
.profile-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem 0;
  min-height: 80vh;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 2rem;
}

.avatar-container {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 1rem;
  background-color: #eee;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  color: var(--color-text-light);
  font-weight: bold;
}

.display-name {
  font-size: 2rem;
  margin: 0 0 0.25rem 0;
  color: var(--color-text);
}

.username {
  color: var(--color-text-light);
  margin: 0 0 1rem 0;
}

.bio {
  max-width: 400px;
  margin: 0 0 1rem 0;
  line-height: 1.5;
}

.stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 24px;
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-secondary {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover {
  background-color: #efefef;
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.profile-tabs {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
}

.profile-tabs button {
  background: none;
  border: none;
  padding: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  color: var(--color-text-light);
  cursor: pointer;
  border-bottom: 3px solid transparent;
}

.profile-tabs button.active {
  color: var(--color-text);
  border-bottom-color: var(--color-text);
}

.tab-content {
  width: 100%;
}

.empty-state {
  text-align: center;
  color: var(--color-text-light);
  padding: 3rem 0;
}

/* Masonry Grid */
.masonry-grid {
  column-count: 1;
  column-gap: var(--spacing-md);
  width: 100%;
}

@media (min-width: 640px) { .masonry-grid { column-count: 2; } }
@media (min-width: 1024px) { .masonry-grid { column-count: 3; } }
@media (min-width: 1280px) { .masonry-grid { column-count: 4; } }

/* Boards Grid */
.boards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
}

.board-card {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s;
}

.board-card:hover {
  transform: translateY(-4px);
}

.board-link {
  text-decoration: none;
  color: inherit;
  display: block;
}

.board-cover {
  height: 150px;
  background-color: #e9e9e9;
}

.board-info {
  padding: 1rem;
}

.board-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
}

.board-info p {
  margin: 0;
  color: var(--color-text-light);
  font-size: 0.9rem;
}
</style>
