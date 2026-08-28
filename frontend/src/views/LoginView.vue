<template>
  <main class="login-view container">
    <div class="auth-card">
      <h1>Entrar no Pinnie</h1>
      
      <div v-if="authStore.error" class="error-message">
        {{ authStore.error }}
      </div>
      <div v-if="localError" class="error-message">
        {{ localError }}
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label for="email">E-mail</label>
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            placeholder="Seu e-mail"
            :disabled="authStore.loading"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Senha</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            placeholder="Sua senha"
            :disabled="authStore.loading"
          />
        </div>
        
        <button type="submit" class="btn-primary" :disabled="authStore.loading">
          {{ authStore.loading ? 'Entrando...' : 'Entrar' }}
        </button>
      </form>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const authStore = useAuthStore();

onMounted(() => {
  authStore.error = null;
});

const email = ref('');
const password = ref('');
const localError = ref('');

const handleLogin = async () => {
  localError.value = '';
  
  if (!email.value || !password.value) {
    localError.value = 'Por favor, preencha todos os campos obrigatórios.';
    return;
  }
  
  try {
    await authStore.login({
      email: email.value,
      password: password.value
    });
    
    // Redirecionar para home em caso de sucesso
    router.push('/');
  } catch (err) {
    // Erros já são tratados e colocados em authStore.error pela store
    console.error('Falha no login:', err);
  }
};
</script>

<style scoped>
.auth-card {
  max-width: 400px;
  margin: 40px auto;
  padding: var(--spacing-xl);
  background: var(--color-surface);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  text-align: center;
}

h1 {
  margin-bottom: var(--spacing-lg);
  font-size: 1.8rem;
  letter-spacing: -0.5px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  text-align: left;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

label {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--color-text-light);
}

input {
  padding: var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  font-family: inherit;
  font-size: 1rem;
  transition: border-color var(--transition-fast);
}

input:focus {
  outline: none;
  border-color: var(--color-primary);
}

input:disabled {
  background-color: #f0f0f0;
  cursor: not-allowed;
}

.btn-primary {
  margin-top: var(--spacing-sm);
  background-color: var(--color-primary);
  color: white;
  padding: var(--spacing-md);
  border-radius: 24px;
  font-size: 1rem;
  font-weight: 600;
  transition: background-color var(--transition-fast);
}

.btn-primary:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-message {
  background-color: #fdeded;
  color: #d32f2f;
  padding: var(--spacing-sm);
  border-radius: 8px;
  margin-bottom: var(--spacing-md);
  font-size: 0.9rem;
  font-weight: 500;
}
</style>
