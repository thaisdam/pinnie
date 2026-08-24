<template>
  <div class="app-wrapper">
    <header class="app-header">
      <div class="logo-wrapper">
        <router-link to="/" class="app-logo">Pinnie</router-link>
      </div>
      
      <div class="search-bar-wrapper">
        <form @submit.prevent="handleSearch" class="search-form">
          <i class="ph ph-magnifying-glass search-icon"></i>
          <input 
            type="search" 
            v-model="searchQuery" 
            placeholder="Pesquisar..." 
            class="search-input"
          />
        </form>
      </div>

      <!-- Evita mostrar opções incorretas enquanto verifica sessão -->
      <nav v-if="authStore.isInitializing" class="app-nav">
        <!-- Espaço discreto -->
      </nav>
      
      <nav v-else class="app-nav">
        <router-link to="/" class="nav-link">Home</router-link>
        
        <template v-if="authStore.isAuthenticated">
          <router-link to="/boards" class="nav-link">Minhas Pastas</router-link>
          <router-link to="/pin/create" class="nav-link">Criar Pin</router-link>
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
import { onMounted, ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from './stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const searchQuery = ref('');

// Mantém o input atualizado com a URL caso o usuário recarregue a página
watch(() => route.query.q, (newQ) => {
  if (newQ) {
    searchQuery.value = newQ;
  } else if (route.name !== 'search') {
    searchQuery.value = '';
  }
});

const handleSearch = () => {
  const q = searchQuery.value.trim();
  if (q.length >= 2) {
    router.push({ name: 'search', query: { q } });
  }
};

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
/* Search Bar Styles */
.search-bar-wrapper {
  flex: 1;
  max-width: 800px;
  margin: 0 var(--spacing-lg);
  display: flex;
  align-items: center;
}

.search-form {
  display: flex;
  align-items: center;
  background-color: var(--color-surface);
  border-radius: 24px;
  padding: 8px 16px;
  width: 100%;
  border: 1px solid transparent;
  transition: all var(--transition-fast);
}

.search-form:focus-within {
  border-color: var(--color-primary);
  background-color: var(--color-background);
  box-shadow: 0 0 0 4px rgba(230, 0, 35, 0.1);
}

.search-icon {
  font-size: 1.1rem;
  color: var(--color-text-light);
  margin-right: 8px;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 1rem;
  color: var(--color-text);
  font-family: inherit;
}

.search-input::placeholder {
  color: var(--color-text-light);
}

/* Nav & Auth Styles */
.app-logo {
  font-family: 'Emilys Candy', cursive;
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.5px;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  text-decoration: none;
}

.app-nav {
  display: flex;
  gap: var(--spacing-md);
}

.nav-link {
  font-weight: 600;
  color: var(--color-text);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  transition: background-color var(--transition-fast);
}

.nav-link:hover, .nav-link.router-link-active {
  background-color: var(--color-surface);
}

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
