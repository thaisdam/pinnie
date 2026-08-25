<template>
  <div class="comment-item">
    <img 
      :src="authorAvatarUrl" 
      alt="Avatar" 
      class="comment-avatar"
    />
    <div class="comment-content">
      <div class="comment-header">
        <span class="comment-author">{{ comment.authorDisplayName || comment.authorUsername || 'Usuário' }}</span>
        <span class="comment-date">{{ formattedDate }}</span>
      </div>
      <p class="comment-text">{{ comment.text }}</p>
    </div>
    <div class="comment-actions" v-if="canDelete">
      <button class="btn-delete" @click="$emit('delete', comment.id)" title="Excluir comentário">
        &times;
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useAuthStore } from '../stores/auth';

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  pinOwnerId: {
    type: String,
    required: true
  }
});

defineEmits(['delete']);

const authStore = useAuthStore();

const authorAvatarUrl = computed(() => {
  const baseUrl = import.meta.env.VITE_BACKEND_URL || '';
  if (props.comment.authorAvatarUrl) {
    return `${baseUrl}${props.comment.authorAvatarUrl}`;
  }
  return 'https://ui-avatars.com/api/?name=User&background=random';
});

const formattedDate = computed(() => {
  if (!props.comment.createdAt) return '';
  const date = new Date(props.comment.createdAt);
  return date.toLocaleDateString();
});

const canDelete = computed(() => {
  if (!authStore.isAuthenticated || !authStore.user) return false;
  
  const currentUserId = authStore.user.id;
  const isCommentAuthor = props.comment.authorId === currentUserId;
  const isPinOwner = props.pinOwnerId === currentUserId;
  
  return isCommentAuthor || isPinOwner;
});
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
  align-items: flex-start;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  background-color: var(--color-surface);
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  gap: var(--spacing-sm);
  align-items: baseline;
  margin-bottom: 4px;
}

.comment-author {
  font-weight: 600;
  color: var(--color-text);
  font-size: 0.9rem;
}

.comment-date {
  font-size: 0.75rem;
  color: var(--color-text-light);
}

.comment-text {
  font-size: 0.95rem;
  color: var(--color-text);
  line-height: 1.4;
  margin: 0;
  word-break: break-word;
}

.comment-actions {
  margin-left: var(--spacing-sm);
}

.btn-delete {
  background: none;
  border: none;
  color: var(--color-text-light);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 4px;
  line-height: 1;
  transition: color var(--transition-fast);
}

.btn-delete:hover {
  color: #ff4444;
}
</style>
