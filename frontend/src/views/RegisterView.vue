<template>
  <main class="register-view container">
    <div class="auth-card">
      <h1>Criar Conta</h1>
      
      <div v-if="successMessage" class="success-message">
        {{ successMessage }}
      </div>
      
      <div v-if="authStore.error && !successMessage" class="error-message">
        {{ authStore.error }}
      </div>
      <div v-if="localError && !successMessage" class="error-message">
        {{ localError }}
      </div>

      <form v-if="!successMessage" @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="username">Nome de Usuário</label>
          <input 
            type="text" 
            id="username" 
            v-model="username" 
            placeholder="Ex: joao_silva"
            :disabled="authStore.loading"
          />
        </div>

        <div class="form-group">
          <label for="displayName">Nome de Exibição</label>
          <input 
            type="text" 
            id="displayName" 
            v-model="displayName" 
            placeholder="Ex: João da Silva"
            :disabled="authStore.loading"
          />
        </div>

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
            placeholder="No mínimo 6 caracteres"
            :disabled="authStore.loading"
          />
        </div>
        
        <button type="submit" class="btn-primary" :disabled="authStore.loading">
          {{ authStore.loading ? 'Criando conta...' : 'Cadastrar' }}
        </button>
      </form>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const username = ref('');
const displayName = ref('');
const email = ref('');
const password = ref('');
const localError = ref('');
const successMessage = ref('');

const handleRegister = async () => {
  localError.value = '';
  
  if (!username.value || !displayName.value || !email.value || !password.value) {
    localError.value = 'Por favor, preencha todos os campos obrigatórios.';
    return;
  }

  if (password.value.length < 6) {
    localError.value = 'A senha deve ter no mínimo 6 caracteres.';
    return;
  }
  
  try {
    await authStore.register({
      username: username.value,
      displayName: displayName.value,
      email: email.value,
      password: password.value
    });
    
    successMessage.value = 'Cadastro realizado com sucesso! Redirecionando para login...';
    
    // Aguarda um momento para o usuário ler a mensagem de sucesso e redireciona para o login
    setTimeout(() => {
      router.push('/login');
    }, 2000);
    
  } catch (err) {
    console.error('Falha no registro:', err);
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

.success-message {
  background-color: #e8f5e9;
  color: #2e7d32;
  padding: var(--spacing-sm);
  border-radius: 8px;
  margin-bottom: var(--spacing-md);
  font-size: 0.9rem;
  font-weight: 500;
}
</style>
