import { createRouter, createWebHistory } from 'vue-router';
import { watch } from 'vue';
import HomeView from '../views/HomeView.vue';
import LoginView from '../views/LoginView.vue';
import RegisterView from '../views/RegisterView.vue';
import { useAuthStore } from '../stores/auth';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/pin/create',
      name: 'pin-create',
      component: () => import('../views/PinCreateView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/boards',
      name: 'boards',
      component: () => import('../views/BoardsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/my-pins',
      name: 'my-pins',
      component: () => import('../views/MyPinsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/boards/:id',
      name: 'board-detail',
      component: () => import('../views/BoardDetailView.vue')
    },
    {
      path: '/search',
      name: 'search',
      component: () => import('../views/SearchView.vue')
    },
    {
      path: '/pin/:id',
      name: 'pin-detail',
      component: () => import('../views/PinView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/:id',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { requiresGuest: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminDashboard.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    }
  ]
});

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();

  const handleRouting = () => {
    // Se a rota exige que o usuário seja visitante (ex: Login/Register)
    // E o usuário já está autenticado, manda pra Home.
    if (to.meta.requiresGuest && authStore.isAuthenticated) {
      next({ name: 'home' });
    } 
    else if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      next({ name: 'login' });
    } 
    else if (to.meta.requiresAdmin && !authStore.isAdmin) {
      next({ name: 'home' });
    }
    else {
      next();
    }
  };

  // Se a store ainda estiver inicializando (verificando o token com o backend)
  // precisamos aguardar a conclusão antes de decidir a rota.
  if (authStore.isInitializing) {
    const unwatch = watch(
      () => authStore.isInitializing,
      (isInitializing) => {
        if (!isInitializing) {
          unwatch(); // Para de observar a variável
          handleRouting(); // Toma a decisão da rota
        }
      }
    );
  } else {
    // Se já inicializou, processa imediatamente
    handleRouting();
  }
});

export default router;
