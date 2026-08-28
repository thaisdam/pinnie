<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h2>Painel de Moderação</h2>
      <p>Gerencie denúncias e usuários bloqueados.</p>
    </div>

    <div class="dashboard-content">
      <div v-if="loading" class="loading-state">
        Carregando denúncias...
      </div>
      
      <div v-else-if="error" class="error-state">
        {{ error }}
        <button @click="fetchReports" class="btn-retry">Tentar Novamente</button>
      </div>
      
      <div v-else-if="reports.length === 0" class="empty-state">
        <div class="empty-icon">🛡️</div>
        <h3>Tudo tranquilo por aqui!</h3>
        <p>Não há denúncias pendentes no momento.</p>
      </div>
      
      <div v-else class="reports-list">
        <div v-for="report in reports" :key="report.id" class="report-card">
          <div class="report-header">
            <span class="report-type" :class="report.targetType.toLowerCase()">
              {{ report.targetType === 'PIN' ? 'Denúncia de Pin' : 'Denúncia de Usuário' }}
            </span>
            <span class="report-date">{{ formatDate(report.createdAt) }}</span>
          </div>
          
          <div class="report-details">
            <div class="report-row">
              <strong>Autor da denúncia:</strong>
              <router-link :to="'/profile/' + report.reporter.id" class="reporter-link">
                {{ report.reporter.username }}
              </router-link>
            </div>
            
            <div class="report-row">
              <strong>Alvo (ID):</strong>
              <span class="target-id">{{ report.targetId }}</span>
            </div>
            
            <div class="report-reason">
              <strong>Motivo:</strong>
              <p>{{ report.reason }}</p>
            </div>
          </div>
          
          <div class="report-actions">
            <!-- Botões para acessar o alvo (abrir nova aba) -->
            <button v-if="report.targetType === 'PIN'" @click="openTarget(report)" class="btn-action view">
              Ver Pin
            </button>
            <button v-if="report.targetType === 'USER'" @click="openTarget(report)" class="btn-action view">
              Ver Usuário
            </button>

            <!-- Ações destrutivas -->
            <button v-if="report.targetType === 'PIN'" @click="deletePin(report)" class="btn-action danger" :disabled="processing === report.id">
              {{ processing === report.id ? 'Excluindo...' : 'Excluir Pin' }}
            </button>
            
            <button v-if="report.targetType === 'USER'" @click="blockUser(report)" class="btn-action danger" :disabled="processing === report.id">
              {{ processing === report.id ? 'Bloquear Usuário' : 'Bloquear Usuário' }}
            </button>

            <!-- Ações de resolução de denúncia -->
            <div class="resolve-group">
              <button @click="resolveReport(report.id, 'DISMISSED')" class="btn-action dismiss" :disabled="processing === report.id">
                Ignorar
              </button>
              <button @click="resolveReport(report.id, 'RESOLVED')" class="btn-action resolve" :disabled="processing === report.id">
                Marcar Resolvido
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../services/api';

const router = useRouter();
const reports = ref([]);
const loading = ref(true);
const error = ref('');
const processing = ref(null);

const fetchReports = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.get('/admin/reports?size=50&sort=createdAt,asc');
    reports.value = response.data.content;
  } catch (err) {
    error.value = 'Falha ao carregar as denúncias. Você tem permissão de administrador?';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchReports();
});

const formatDate = (isoString) => {
  const d = new Date(isoString);
  return d.toLocaleDateString() + ' às ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

const openTarget = (report) => {
  const routeName = report.targetType === 'PIN' ? 'pin-detail' : 'profile';
  const url = router.resolve({ name: routeName, params: { id: report.targetId } }).href;
  window.open(url, '_blank');
};

const resolveReport = async (id, status) => {
  if (!confirm(`Deseja realmente marcar esta denúncia como ${status}?`)) return;
  
  processing.value = id;
  try {
    await api.post(`/admin/reports/${id}/resolve?status=${status}`);
    // Remover da lista
    reports.value = reports.value.filter(r => r.id !== id);
  } catch (err) {
    alert('Erro ao resolver denúncia.');
  } finally {
    processing.value = null;
  }
};

const deletePin = async (report) => {
  if (!confirm('ATENÇÃO: Deseja EXCLUIR DEFINITIVAMENTE este Pin?')) return;
  
  processing.value = report.id;
  try {
    // 1. Excluir Pin
    await api.delete(`/admin/pins/${report.targetId}`);
    // 2. Marcar denúncia como resolvida automaticamente
    await api.post(`/admin/reports/${report.id}/resolve?status=RESOLVED`);
    
    alert('Pin excluído com sucesso.');
    reports.value = reports.value.filter(r => r.id !== report.id);
  } catch (err) {
    alert('Erro ao excluir Pin.');
  } finally {
    processing.value = null;
  }
};

const blockUser = async (report) => {
  if (!confirm('ATENÇÃO: Deseja BLOQUEAR DEFINITIVAMENTE o acesso deste usuário?')) return;
  
  processing.value = report.id;
  try {
    // 1. Bloquear usuário
    await api.post(`/admin/users/${report.targetId}/block`);
    // 2. Marcar denúncia como resolvida
    await api.post(`/admin/reports/${report.id}/resolve?status=RESOLVED`);
    
    alert('Usuário bloqueado com sucesso.');
    reports.value = reports.value.filter(r => r.id !== report.id);
  } catch (err) {
    alert('Erro ao bloquear usuário.');
  } finally {
    processing.value = null;
  }
};
</script>

<style scoped>
.admin-dashboard {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.dashboard-header {
  margin-bottom: var(--spacing-xl);
  border-bottom: 2px solid var(--color-border);
  padding-bottom: var(--spacing-md);
}

.dashboard-header h2 {
  color: var(--color-error);
  margin-bottom: 8px;
}

.dashboard-header p {
  color: var(--color-text-light);
}

.loading-state, .error-state, .empty-state {
  text-align: center;
  padding: 40px;
  background-color: var(--color-surface);
  border-radius: var(--border-radius);
}

.error-state {
  color: var(--color-error);
}

.btn-retry {
  display: block;
  margin: 20px auto 0;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--color-error);
  color: var(--color-error);
  border-radius: 20px;
  cursor: pointer;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 16px;
}

.reports-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.report-card {
  background-color: var(--color-surface);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border);
}

.report-type {
  font-weight: bold;
  font-size: 0.9rem;
  padding: 4px 8px;
  border-radius: 4px;
}

.report-type.pin {
  background-color: #e3f2fd;
  color: #1976d2;
}

.report-type.user {
  background-color: #fce4ec;
  color: #c2185b;
}

.report-date {
  font-size: 0.85rem;
  color: var(--color-text-light);
}

.report-details {
  margin-bottom: var(--spacing-lg);
}

.report-row {
  margin-bottom: 8px;
}

.reporter-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  margin-left: 8px;
}

.target-id {
  font-family: monospace;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
}

.report-reason {
  margin-top: 16px;
  background: #f9f9f9;
  padding: 12px;
  border-left: 4px solid var(--color-error);
  border-radius: 4px;
}

.report-reason p {
  margin: 0;
  white-space: pre-wrap;
}

.report-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  align-items: center;
}

.resolve-group {
  margin-left: auto;
  display: flex;
  gap: var(--spacing-sm);
}

.btn-action {
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  border: none;
  transition: opacity var(--transition-fast);
}

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-action.view {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  color: var(--color-text);
}

.btn-action.danger {
  background-color: #ffebee;
  color: #d32f2f;
}

.btn-action.dismiss {
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  color: var(--color-text-light);
}

.btn-action.resolve {
  background-color: #e8f5e9;
  color: #2e7d32;
}
</style>
