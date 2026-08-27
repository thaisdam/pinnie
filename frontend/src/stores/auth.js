import { defineStore } from 'pinia';
import api from '../services/api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isAuthenticated: false,
    loading: false,
    error: null,
    isInitializing: true
  }),
  actions: {
    setUser(userData) {
      this.user = userData;
      this.isAuthenticated = true;
    },
    clearUser() {
      this.user = null;
      this.isAuthenticated = false;
    },
    async fetchCurrentUser() {
      try {
        const response = await api.get('/users/me');
        this.setUser(response.data);
      } catch (error) {
        this.clearUser();
      } finally {
        this.isInitializing = false;
      }
    },
    async register(userData) {
      this.loading = true;
      this.error = null;
      try {
        await api.post('/auth/register', userData);
      } catch (error) {
        if (error.response) {
          const status = error.response.status;
          if (status === 409) {
            this.error = 'Este usuário ou e-mail já está cadastrado.';
          } else if (status === 400 && error.response.data && error.response.data.message) {
            this.error = error.response.data.message;
          } else {
            this.error = 'Não foi possível realizar o cadastro. Tente novamente.';
          }
        } else {
          this.error = 'Não foi possível conectar ao servidor.';
        }
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async login(credentials) {
      this.loading = true;
      this.error = null;
      try {
        // Obter o token CSRF primeiro
        await api.get('/csrf');
        // Efetuar login com as credenciais
        await api.post('/auth/login', credentials);
        // Em caso de sucesso, buscar o perfil
        await this.fetchCurrentUser();
      } catch (error) {
        if (error.response) {
          const status = error.response.status;
          if (status === 401) {
            this.error = 'E-mail ou senha inválidos.';
          } else if (status === 403) {
            this.error = 'Sua conta está desativada ou bloqueada.';
          } else if (status === 400 && error.response.data && error.response.data.message) {
            this.error = error.response.data.message;
          } else {
            this.error = 'Ocorreu um erro inesperado ao tentar fazer login.';
          }
        } else {
          this.error = 'Não foi possível conectar ao servidor.';
        }
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async logout() {
      this.loading = true;
      this.error = null;
      try {
        await api.post('/auth/logout');
      } catch (error) {
        console.error('Erro ao efetuar logout', error);
      } finally {
        this.clearUser();
        this.loading = false;
      }
    },
    async updateProfile(profileData) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.put('/users/me/profile', profileData);
        this.setUser(response.data);
      } catch (error) {
        this.error = error.response?.data?.message || 'Erro ao atualizar perfil';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async updateAvatar(file) {
      this.loading = true;
      this.error = null;
      try {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post('/users/me/avatar', formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        this.setUser(response.data);
      } catch (error) {
        this.error = error.response?.data?.message || 'Erro ao atualizar foto de perfil';
        throw error;
      } finally {
        this.loading = false;
      }
    },
    async updatePassword(passwordData) {
      this.loading = true;
      this.error = null;
      try {
        await api.put('/users/me/password', passwordData);
      } catch (error) {
        this.error = error.response?.data?.message || 'Erro ao atualizar senha';
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
});
