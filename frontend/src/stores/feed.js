import { defineStore } from 'pinia';
import api from '../services/api';

export const useFeedStore = defineStore('feed', {
  state: () => ({
    pins: [],
    isLoading: false,
    error: null,
    page: 0,
    hasNext: false
  }),
  actions: {
    async fetchFeed() {
      this.isLoading = true;
      this.error = null;
      this.page = 0;
      try {
        const response = await api.get('/feed?page=0&size=20');
        this.pins = response.data.content;
        this.hasNext = response.data.hasNext;
      } catch (err) {
        this.error = 'Ocorreu um erro ao carregar o feed. Tente novamente mais tarde.';
        console.error('Erro ao buscar feed:', err);
      } finally {
        this.isLoading = false;
      }
    },
    async loadMore() {
      if (this.isLoading || !this.hasNext) return;
      
      this.isLoading = true;
      this.error = null;
      const nextPage = this.page + 1;
      
      try {
        const response = await api.get(`/feed?page=${nextPage}&size=20`);
        this.pins = [...this.pins, ...response.data.content];
        this.page = nextPage;
        this.hasNext = response.data.hasNext;
      } catch (err) {
        this.error = 'Ocorreu um erro ao carregar mais pins.';
        console.error('Erro ao carregar mais feed:', err);
      } finally {
        this.isLoading = false;
      }
    }
  }
});
