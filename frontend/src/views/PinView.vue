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
            <!-- Funcionalidade Salvar e Curtir (Apenas logados) -->
            <div class="action-buttons-row" v-if="authStore.isAuthenticated">
              <button 
                class="btn-icon btn-like" 
                @click="toggleLike" 
                :disabled="isLiking"
                :class="{'btn-liked': pin.likedByMe}"
                title="Curtir"
              >
                {{ pin.likedByMe ? '♥' : '♡' }} <span class="likes-count">{{ pin.likesCount || 0 }}</span>
              </button>

              <div class="save-wrapper">
                <select v-model="selectedBoardId" class="board-select">
                  <option disabled value="">Selecione a Pasta</option>
                  <option v-for="board in myBoards" :key="board.id" :value="board.id">
                    {{ board.name }}
                  </option>
                </select>
                <button 
                  class="btn-save" 
                  @click="savePinToBoard" 
                  :disabled="isSaving || !selectedBoardId || isSaved"
                  :class="{'btn-saved': isSaved}"
                >
                  {{ saveStatusText }}
                </button>
              </div>
            </div>
            <p v-if="saveError" class="save-error">{{ saveError }}</p>
          </div>
          
          <h1 v-if="pin.title" class="pin-title">{{ pin.title }}</h1>
          <p v-if="pin.description" class="pin-description">{{ pin.description }}</p>
          
          <div v-if="pin.link" class="pin-link">
            <a :href="pin.link" target="_blank" rel="noopener noreferrer" class="external-link">
              Visitar link ↗
            </a>
          </div>

          <div class="pin-creator" v-if="creatorProfile">
            <img :src="creatorAvatarUrl" alt="Avatar" class="creator-avatar" />
            <div class="creator-info">
              <span class="creator-id">{{ creatorProfile.displayName || creatorProfile.username }}</span>
              <span class="creator-followers">{{ creatorProfile.followersCount }} seguidores</span>
            </div>
            
            <button 
              v-if="authStore.isAuthenticated && authStore.user.id !== creatorProfile.id"
              class="btn-follow"
              :class="{'btn-following': creatorProfile.followedByMe}"
              @click="toggleFollow"
              :disabled="isFollowing"
            >
              {{ creatorProfile.followedByMe ? 'Seguindo' : 'Seguir' }}
            </button>
          </div>
          <div class="pin-creator" v-else>
            <div class="creator-avatar"></div>
            <div class="creator-info">
              <span class="creator-label">Criador(a)</span>
              <span class="creator-id">Usuário do Pinnie</span>
            </div>
          </div>
          
          <!-- Seção de Comentários -->
          <div class="comments-section">
            <h2>Comentários</h2>
            
            <div v-if="authStore.isAuthenticated" class="comment-form">
              <textarea 
                v-model="newCommentText" 
                maxlength="500" 
                placeholder="Adicione um comentário..."
                :disabled="isSubmittingComment"
              ></textarea>
              <div class="form-footer">
                <span class="char-count">{{ newCommentText.length }}/500</span>
                <button class="btn-submit" @click="submitComment" :disabled="isSubmittingComment || !newCommentText.trim()">
                  {{ isSubmittingComment ? 'Enviando...' : 'Comentar' }}
                </button>
              </div>
            </div>
            
            <div class="comments-list">
              <CommentItem 
                v-for="comment in comments" 
                :key="comment.id" 
                :comment="comment" 
                :pinOwnerId="pin.userId || ''"
                @delete="deleteComment" 
              />
              
              <div v-if="comments.length === 0 && !isLoadingComments" class="no-comments">
                Ainda não há comentários.
              </div>
              
              <button v-if="hasMoreComments" class="btn-load-more" @click="loadMoreComments" :disabled="isLoadingComments">
                {{ isLoadingComments ? 'Carregando...' : 'Carregar mais' }}
              </button>
            </div>
          </div>

        </div>

      </div>
    </div>
    
    <!-- Modal de Confirmação de Exclusão -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal-content">
        <h3>Excluir Comentário?</h3>
        <p>Esta ação não pode ser desfeita.</p>
        <div class="modal-actions">
          <button class="btn-cancel" @click="cancelDelete">Cancelar</button>
          <button class="btn-delete" @click="confirmDeleteComment">Excluir</button>
        </div>
      </div>
    </div>
    
  </main>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import api from '../services/api';
import CommentItem from '../components/CommentItem.vue';

const route = useRoute();
const pinId = route.params.id;
const authStore = useAuthStore();

const pin = ref(null);
const isLoading = ref(true);
const error = ref(null);

// Lógica de Salvar Pin
const myBoards = ref([]);
const selectedBoardId = ref('');
const isSaving = ref(false);
const isSaved = ref(false);
const saveError = ref('');

const saveStatusText = computed(() => {
  if (isSaved.value) return 'Salvo!';
  if (isSaving.value) return 'Salvando...';
  return 'Salvar';
});

const fullImageUrl = computed(() => {
  if (!pin.value) return '';
  const baseUrl = import.meta.env.VITE_BACKEND_URL || '';
  return `${baseUrl}${pin.value.imageUrl}`;
});

const fetchMyBoards = async () => {
  if (!authStore.isAuthenticated || !authStore.user) return;
  try {
    const response = await api.get(`/users/${authStore.user.id}/boards?size=100`);
    myBoards.value = response.data.content;
    if (myBoards.value.length > 0) {
      selectedBoardId.value = myBoards.value[0].id;
    }
  } catch (err) {
    console.error('Erro ao buscar as pastas do usuario:', err);
  }
};

const savePinToBoard = async () => {
  if (!selectedBoardId.value) return;
  
  isSaving.value = true;
  saveError.value = '';
  try {
    await api.post(`/boards/${selectedBoardId.value}/pins/${pinId}`);
    isSaved.value = true;
  } catch (err) {
    console.error('Erro ao salvar pin:', err);
    saveError.value = 'Não foi possível salvar (talvez já esteja nesta pasta).';
  } finally {
    isSaving.value = false;
  }
};

const fetchCreatorProfile = async (creatorId) => {
  try {
    const response = await api.get(`/users/${creatorId}`);
    creatorProfile.value = response.data;
  } catch (err) {
    console.error('Erro ao buscar perfil do criador:', err);
  }
};

const fetchPin = async () => {
  isLoading.value = true;
  error.value = null;
  try {
    const response = await api.get(`/pins/${pinId}`);
    pin.value = response.data;
    if (pin.value.userId) {
      await fetchCreatorProfile(pin.value.userId);
    }
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

// --- Lógica de Liking e Following ---
const isLiking = ref(false);
const isFollowing = ref(false);
const creatorProfile = ref(null);

const creatorAvatarUrl = computed(() => {
  if (!creatorProfile.value) return '';
  if (creatorProfile.value.avatarUrl) {
    const baseUrl = import.meta.env.VITE_BACKEND_URL || '';
    return `${baseUrl}${creatorProfile.value.avatarUrl}`;
  }
  return `https://ui-avatars.com/api/?name=${creatorProfile.value.displayName || creatorProfile.value.username}&background=random`;
});

const toggleLike = async () => {
  if (!authStore.isAuthenticated) return;
  isLiking.value = true;
  try {
    if (pin.value.likedByMe) {
      await api.delete(`/pins/${pinId}/like`);
      pin.value.likedByMe = false;
      pin.value.likesCount--;
    } else {
      await api.post(`/pins/${pinId}/like`);
      pin.value.likedByMe = true;
      pin.value.likesCount++;
    }
  } catch (err) {
    console.error('Erro ao curtir/descurtir:', err);
    alert('Erro ao curtir: ' + (err.response?.data?.message || err.response?.status || err.message));
  } finally {
    isLiking.value = false;
  }
};

const toggleFollow = async () => {
  if (!authStore.isAuthenticated || !creatorProfile.value) return;
  isFollowing.value = true;
  try {
    if (creatorProfile.value.followedByMe) {
      await api.delete(`/users/${creatorProfile.value.id}/follow`);
      creatorProfile.value.followedByMe = false;
      creatorProfile.value.followersCount--;
    } else {
      await api.post(`/users/${creatorProfile.value.id}/follow`);
      creatorProfile.value.followedByMe = true;
      creatorProfile.value.followersCount++;
    }
  } catch (err) {
    console.error('Erro ao seguir/deixar de seguir:', err);
    alert('Erro ao seguir: ' + (err.response?.data?.message || err.response?.status || err.message));
  } finally {
    isFollowing.value = false;
  }
};

// --- Lógica de Comentários ---
const comments = ref([]);
const newCommentText = ref('');
const isSubmittingComment = ref(false);
const isLoadingComments = ref(false);
const currentPage = ref(0);
const hasMoreComments = ref(false);

const fetchComments = async (page = 0, append = false) => {
  isLoadingComments.value = true;
  try {
    const response = await api.get(`/pins/${pinId}/comments?page=${page}&size=5`);
    if (append) {
      comments.value = [...comments.value, ...response.data.content];
    } else {
      comments.value = response.data.content;
    }
    hasMoreComments.value = !response.data.last;
    currentPage.value = page;
  } catch (err) {
    console.error('Erro ao carregar comentários:', err);
  } finally {
    isLoadingComments.value = false;
  }
};

const loadMoreComments = () => {
  if (!isLoadingComments.value && hasMoreComments.value) {
    fetchComments(currentPage.value + 1, true);
  }
};

const submitComment = async () => {
  if (!newCommentText.value.trim() || isSubmittingComment.value) return;
  
  isSubmittingComment.value = true;
  try {
    await api.post(`/pins/${pinId}/comments`, {
      text: newCommentText.value.trim()
    });
    // Limpa o formulário e recarrega os comentários do zero (sem optimistic update)
    newCommentText.value = '';
    await fetchComments(0, false);
  } catch (err) {
    console.error('Erro ao enviar comentário:', err);
    alert('Erro ao enviar comentário: ' + (err.response?.data?.message || err.response?.status || err.message));
  } finally {
    isSubmittingComment.value = false;
  }
};

const showDeleteModal = ref(false);
const commentToDelete = ref(null);

const deleteComment = (commentId) => {
  commentToDelete.value = commentId;
  showDeleteModal.value = true;
};

const cancelDelete = () => {
  showDeleteModal.value = false;
  commentToDelete.value = null;
};

const confirmDeleteComment = async () => {
  if (!commentToDelete.value) return;
  
  try {
    await api.delete(`/pins/${pinId}/comments/${commentToDelete.value}`);
    // Recarrega os comentários do zero (sem optimistic update)
    await fetchComments(0, false);
  } catch (err) {
    console.error('Erro ao excluir comentário:', err);
    alert('Erro ao excluir comentário');
  } finally {
    showDeleteModal.value = false;
    commentToDelete.value = null;
  }
};

onMounted(() => {
  fetchPin();
  fetchMyBoards();
  fetchComments(0, false);
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

/* Save Component */
.pin-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.save-wrapper {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  background-color: var(--color-background);
  padding: 8px;
  border-radius: 30px;
  border: 1px solid var(--color-border);
}

.board-select {
  flex: 1;
  padding: 8px;
  border: none;
  background-color: transparent;
  font-family: inherit;
  font-weight: 600;
  outline: none;
  cursor: pointer;
}

.btn-save {
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 10px 24px;
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-save:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-save.btn-saved {
  background-color: #222;
  opacity: 1;
}

.save-error {
  color: var(--color-primary);
  font-size: 0.75rem;
  margin-top: -4px;
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

/* Creator and Social Styles */
.action-buttons-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-sm);
}

.btn-like {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--color-text);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all var(--transition-fast);
}

.btn-like:hover:not(:disabled) {
  background: #f0f0f0;
}

.btn-liked {
  color: #ff4444;
  border-color: #ff4444;
}

.creator-followers {
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.btn-follow {
  margin-left: auto;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.btn-follow:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-following {
  background-color: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.btn-following:hover:not(:disabled) {
  background-color: #efefef;
}

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
  object-fit: cover;
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

/* Comments Section */
.comments-section {
  margin-top: var(--spacing-xl);
  border-top: 1px solid var(--color-border);
  padding-top: var(--spacing-md);
}

.comments-section h2 {
  font-size: 1.2rem;
  margin-bottom: var(--spacing-md);
  font-weight: 700;
}

.comment-form {
  margin-bottom: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.comment-form textarea {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  padding: var(--spacing-sm);
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
  background-color: var(--color-surface);
  color: var(--color-text);
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-count {
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.btn-submit {
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color var(--transition-fast);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-submit:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.no-comments {
  color: var(--color-text-light);
  font-style: italic;
  font-size: 0.9rem;
  margin-bottom: var(--spacing-md);
}

.btn-load-more {
  width: 100%;
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  padding: 10px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  color: var(--color-text);
  transition: background-color var(--transition-fast);
}

.btn-load-more:hover:not(:disabled) {
  background-color: #efefef;
}

.btn-load-more:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Modal de Exclusão */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: var(--color-surface);
  padding: var(--spacing-xl);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-lg);
  max-width: 400px;
  width: 90%;
  text-align: center;
}

.modal-content h3 {
  margin-bottom: var(--spacing-sm);
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text);
}

.modal-content p {
  margin-bottom: var(--spacing-lg);
  color: var(--color-text-light);
}

.modal-actions {
  display: flex;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.modal-actions button {
  flex: 1;
  padding: 10px;
  border-radius: 20px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: opacity var(--transition-fast);
}

.modal-actions button:hover {
  opacity: 0.8;
}

.btn-cancel {
  background-color: var(--color-background);
  color: var(--color-text);
  border: 1px solid var(--color-border) !important;
}

.btn-delete {
  background-color: #ff4444;
  color: white;
}
</style>
