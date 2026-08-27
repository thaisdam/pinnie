<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Editar Perfil</h2>
        <button class="close-btn" @click="close">&times;</button>
      </div>

      <div class="modal-body">
        <!-- Tabs para alternar entre Dados e Senha -->
        <div class="modal-tabs">
          <button :class="{ active: activeTab === 'profile' }" @click="activeTab = 'profile'">Perfil</button>
          <button :class="{ active: activeTab === 'password' }" @click="activeTab = 'password'">Senha</button>
        </div>

        <!-- Erros globais -->
        <div v-if="authStore.error" class="error-message">
          {{ authStore.error }}
        </div>
        <div v-if="successMessage" class="success-message">
          {{ successMessage }}
        </div>

        <!-- Tab: Perfil -->
        <form v-if="activeTab === 'profile'" @submit.prevent="submitProfile" class="edit-form">
          
          <div class="avatar-section">
            <div class="avatar-preview">
              <img v-if="avatarPreview || currentUser?.avatarUrl" :src="avatarPreview || fullAvatarUrl" alt="Avatar" />
              <div v-else class="avatar-placeholder">
                {{ currentUser?.displayName?.charAt(0).toUpperCase() || 'U' }}
              </div>
            </div>
            <div class="avatar-actions">
              <label class="btn btn-secondary">
                Alterar foto
                <input type="file" accept="image/*" @change="onFileChange" class="hidden-input" />
              </label>
            </div>
          </div>

          <div class="form-group">
            <label for="displayName">Nome</label>
            <input type="text" id="displayName" v-model="profileForm.displayName" required />
          </div>

          <div class="form-group">
            <label for="bio">Biografia</label>
            <textarea id="bio" v-model="profileForm.bio" rows="3" placeholder="Conte um pouco sobre você..."></textarea>
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="close">Cancelar</button>
            <button type="submit" class="btn btn-primary" :disabled="authStore.loading">
              {{ authStore.loading ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>

        <!-- Tab: Senha -->
        <form v-if="activeTab === 'password'" @submit.prevent="submitPassword" class="edit-form">
          <div class="form-group">
            <label for="oldPassword">Senha atual</label>
            <input type="password" id="oldPassword" v-model="passwordForm.oldPassword" required />
          </div>

          <div class="form-group">
            <label for="newPassword">Nova senha</label>
            <input type="password" id="newPassword" v-model="passwordForm.newPassword" required minlength="6" />
          </div>

          <div class="form-group">
            <label for="confirmPassword">Confirmar nova senha</label>
            <input type="password" id="confirmPassword" v-model="passwordForm.confirmPassword" required minlength="6" />
          </div>

          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="close">Cancelar</button>
            <button type="submit" class="btn btn-primary" :disabled="authStore.loading">
              {{ authStore.loading ? 'Atualizando...' : 'Atualizar Senha' }}
            </button>
          </div>
        </form>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '../stores/auth';

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  }
});

const emit = defineEmits(['close']);
const authStore = useAuthStore();
const currentUser = computed(() => authStore.user);

const fullAvatarUrl = computed(() => {
  if (!currentUser.value?.avatarUrl) return null;
  const baseUrl = import.meta.env.VITE_BACKEND_URL || '';
  return `${baseUrl}${currentUser.value.avatarUrl}`;
});

const activeTab = ref('profile');
const successMessage = ref('');
const avatarFile = ref(null);
const avatarPreview = ref(null);

const profileForm = ref({
  displayName: '',
  bio: ''
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

watch(() => props.isOpen, (newVal) => {
  if (newVal && currentUser.value) {
    profileForm.value.displayName = currentUser.value.displayName || '';
    profileForm.value.bio = currentUser.value.bio || '';
    passwordForm.value.oldPassword = '';
    passwordForm.value.newPassword = '';
    passwordForm.value.confirmPassword = '';
    successMessage.value = '';
    authStore.error = null;
    avatarFile.value = null;
    avatarPreview.value = null;
    activeTab.value = 'profile';
  }
});

function close() {
  emit('close');
}

function onFileChange(e) {
  const file = e.target.files[0];
  if (!file) return;
  avatarFile.value = file;
  avatarPreview.value = URL.createObjectURL(file);
}

async function submitProfile() {
  successMessage.value = '';
  try {
    if (avatarFile.value) {
      await authStore.updateAvatar(avatarFile.value);
    }
    await authStore.updateProfile({
      displayName: profileForm.value.displayName,
      bio: profileForm.value.bio
    });
    successMessage.value = 'Perfil atualizado com sucesso!';
    setTimeout(close, 1500);
  } catch (error) {
    // erro tratado na store
  }
}

async function submitPassword() {
  successMessage.value = '';
  authStore.error = null;
  
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    authStore.error = 'As novas senhas não coincidem!';
    return;
  }

  try {
    await authStore.updatePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    });
    successMessage.value = 'Senha alterada com sucesso!';
    passwordForm.value.oldPassword = '';
    passwordForm.value.newPassword = '';
    passwordForm.value.confirmPassword = '';
    setTimeout(close, 1500);
  } catch (error) {
    // erro tratado na store
  }
}
</script>

<style scoped>
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
  border-radius: 16px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--color-text-light);
}

.modal-tabs {
  display: flex;
  border-bottom: 1px solid var(--color-border);
  padding: 0 1.5rem;
}

.modal-tabs button {
  background: none;
  border: none;
  padding: 1rem;
  font-weight: 600;
  color: var(--color-text-light);
  cursor: pointer;
  border-bottom: 2px solid transparent;
}

.modal-tabs button.active {
  color: var(--color-text);
  border-bottom-color: var(--color-primary);
}

.modal-body {
  padding: 1.5rem;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #eee;
}

.avatar-preview img {
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
  font-size: 2rem;
  color: var(--color-text-light);
  font-weight: bold;
}

.hidden-input {
  display: none;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 500;
  font-size: 0.875rem;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  font-family: inherit;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 24px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}

.btn-primary {
  background-color: var(--color-primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.btn-secondary:hover:not(:disabled) {
  background-color: var(--color-border);
}

.error-message {
  color: #d32f2f;
  background-color: #fdeded;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}

.success-message {
  color: #0b8043;
  background-color: #e6f4ea;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}
</style>
