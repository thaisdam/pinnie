<template>
  <div class="app-wrapper">
    <header class="app-header">
      <router-link to="/" class="app-logo">Pinnie</router-link>
      
      <!-- Evita mostrar opções incorretas enquanto verifica sessão -->
      <nav v-if="authStore.isInitializing" class="app-nav">
        <!-- Espaço discreto -->
      </nav>
      
      <nav v-else class="app-nav">
        <router-link to="/" class="nav-link">Home</router-link>
        
        <template v-if="authStore.isAuthenticated">
          <span class="nav-user">Olá, {{ authStore.user?.displayName }}</span>
          <button @click="handleLogout" class="btn-logout" :disabled="authStore.loading">
            Sair
          </button>
        </template>
        
        <template v-else>
          <router-link to="/login" class="nav-link">Login</router-link>
          <router-link to="/register" class="nav-link btn-signup">Criar conta</router-link>
        </template>
      </nav>
    </header>

    <main class="app-content">
      <router-view></router-view>
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from './stores/auth';

const router = useRouter();
const authStore = useAuthStore();

// Dispara a verificação da sessão assim que a aplicação é montada no navegador
onMounted(() => {
  authStore.fetchCurrentUser();
});

const handleLogout = async () => {
  await authStore.logout();
  router.push('/');
};
</script>

<style scoped>
.nav-user {
  font-weight: 600;
  color: var(--color-text);
  margin-right: var(--spacing-sm);
  display: flex;
  align-items: center;
}

.btn-logout {
  background: transparent;
  border: 1px solid var(--color-border);
  padding: 6px 12px;
  border-radius: 16px;
  cursor: pointer;
  font-weight: 600;
  font-family: inherit;
  transition: all var(--transition-fast);
}

.btn-logout:hover:not(:disabled) {
  background: #f0f0f0;
}

.btn-logout:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-signup {
  background-color: var(--color-primary);
  color: white !important;
  padding: 8px 16px;
  border-radius: 20px;
  transition: background-color var(--transition-fast);
}

.btn-signup:hover {
  background-color: var(--color-primary-hover);
  text-decoration: none;
}
</style>
