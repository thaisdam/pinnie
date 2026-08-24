<template>
  <main class="pin-create-view container">
    <div class="create-card">
      <h1 class="page-title">Criar Pin</h1>
      
      <form @submit.prevent="handleSubmit" class="create-form">
        <!-- Esquerda: Upload de Imagem -->
        <div class="image-upload-col">
          <div 
            class="upload-box" 
            :class="{ 'has-image': imagePreview }"
            @click="triggerFileInput"
            @dragover.prevent
            @drop.prevent="handleDrop"
          >
            <input 
              type="file" 
              ref="fileInput" 
              @change="handleFileChange" 
              accept="image/*" 
              class="hidden-input" 
            />
            
            <img v-if="imagePreview" :src="imagePreview" alt="Preview" class="preview-image" />
            
            <div v-else class="upload-placeholder">
              <i class="ph ph-upload-simple upload-icon"></i>
              <p>Clique ou arraste uma imagem aqui</p>
              <span class="upload-hint">Recomendamos usar arquivos .jpg ou .png de alta qualidade.</span>
            </div>
          </div>
          <p v-if="errors.file" class="error-text">{{ errors.file }}</p>
        </div>

        <!-- Direita: Formulário de Informações -->
        <div class="form-col">
          <div class="form-group">
            <label for="title">Título</label>
            <input 
              id="title" 
              v-model="formData.title" 
              type="text" 
              placeholder="Adicione um título"
              maxlength="100"
            />
            <p v-if="errors.title" class="error-text">{{ errors.title }}</p>
          </div>

          <div class="form-group">
            <label for="description">Descrição</label>
            <textarea 
              id="description" 
              v-model="formData.description" 
              placeholder="Fale um pouco sobre o que é este Pin"
              rows="4"
            ></textarea>
          </div>

          <div class="form-group">
            <label for="link">Link externo</label>
            <input 
              id="link" 
              v-model="formData.link" 
              type="url" 
              placeholder="Adicione um link de destino (opcional)"
            />
            <p v-if="errors.link" class="error-text">{{ errors.link }}</p>
          </div>

          <div class="form-group">
            <label for="altText">Texto alternativo</label>
            <input 
              id="altText" 
              v-model="formData.altText" 
              type="text" 
              placeholder="Explique o que as pessoas podem ver no Pin"
              maxlength="255"
            />
          </div>

          <!-- Mensagem Geral de Erro -->
          <div v-if="submitError" class="alert-error">
            {{ submitError }}
          </div>

          <div class="form-actions">
            <button 
              type="submit" 
              class="btn-submit" 
              :disabled="isSubmitting"
            >
              {{ isSubmitting ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </div>
      </form>
    </div>
  </main>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import api from '../services/api';

const router = useRouter();
const fileInput = ref(null);

const selectedFile = ref(null);
const imagePreview = ref(null);

const formData = reactive({
  title: '',
  description: '',
  link: '',
  altText: ''
});

const errors = reactive({
  file: '',
  title: '',
  link: ''
});

const submitError = ref('');
const isSubmitting = ref(false);

const triggerFileInput = () => {
  fileInput.value.click();
};

const handleFileChange = (event) => {
  const file = event.target.files[0];
  processFile(file);
};

const handleDrop = (event) => {
  const file = event.dataTransfer.files[0];
  processFile(file);
};

const processFile = (file) => {
  if (!file) return;
  
  if (!file.type.startsWith('image/')) {
    errors.file = 'O arquivo precisa ser uma imagem.';
    return;
  }
  
  // Limites básicos de tamanho no frontend (ex: 10MB)
  if (file.size > 10 * 1024 * 1024) {
    errors.file = 'A imagem é muito grande (máximo 10MB).';
    return;
  }
  
  errors.file = '';
  selectedFile.value = file;
  
  if (imagePreview.value) {
    URL.revokeObjectURL(imagePreview.value);
  }
  imagePreview.value = URL.createObjectURL(file);
};

const validateForm = () => {
  let isValid = true;
  errors.file = '';
  errors.title = '';
  errors.link = '';

  if (!selectedFile.value) {
    errors.file = 'Uma imagem é obrigatória.';
    isValid = false;
  }
  
  if (formData.title && formData.title.length > 100) {
    errors.title = 'O título não pode ter mais de 100 caracteres.';
    isValid = false;
  }
  
  if (formData.link) {
    const urlPattern = /^(https?:\/\/.*)?$/;
    if (!urlPattern.test(formData.link)) {
      errors.link = 'Formato de URL inválido (deve começar com http:// ou https://)';
      isValid = false;
    }
  }

  return isValid;
};

const handleSubmit = async () => {
  submitError.value = '';
  
  if (!validateForm()) {
    return;
  }

  isSubmitting.value = true;

  try {
    // 1. Enviar a imagem (Two-Step Upload)
    const formDataToSend = new FormData();
    formDataToSend.append('file', selectedFile.value);

    const uploadResponse = await api.post('/pins/upload', formDataToSend, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    const uploadId = uploadResponse.data.uploadId;

    // 2. Criar o Pin com o ID do upload e os dados do formulário
    const pinPayload = {
      title: formData.title || null,
      description: formData.description || null,
      link: formData.link || null,
      altText: formData.altText || null,
      uploadId: uploadId
    };

    const pinResponse = await api.post('/pins', pinPayload);

    // 3. Sucesso! Redirecionar para os detalhes do Pin criado
    router.push(`/pin/${pinResponse.data.id}`);

  } catch (err) {
    console.error('Erro na criação do Pin:', err);
    if (err.response && err.response.data && err.response.data.message) {
      submitError.value = err.response.data.message;
    } else {
      submitError.value = 'Ocorreu um erro ao criar o Pin. Tente novamente.';
    }
  } finally {
    isSubmitting.value = false;
  }
};

onUnmounted(() => {
  // Limpar a URL temporária para evitar memory leak
  if (imagePreview.value) {
    URL.revokeObjectURL(imagePreview.value);
  }
});
</script>

<style scoped>
.pin-create-view {
  padding: var(--spacing-xl) var(--spacing-md);
  display: flex;
  justify-content: center;
}

.create-card {
  background-color: var(--color-background);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-md);
  padding: var(--spacing-xl);
  width: 100%;
  max-width: 900px;
}

.page-title {
  margin-bottom: var(--spacing-lg);
  font-size: 1.5rem;
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--color-border);
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

@media (min-width: 768px) {
  .create-form {
    flex-direction: row;
  }
}

/* Upload Box */
.image-upload-col {
  flex: 1;
  max-width: 100%;
}

@media (min-width: 768px) {
  .image-upload-col {
    max-width: 400px;
  }
}

.upload-box {
  background-color: var(--color-surface);
  border: 2px dashed var(--color-border);
  border-radius: var(--border-radius);
  height: 450px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  position: relative;
  transition: all var(--transition-fast);
}

.upload-box:hover {
  background-color: #efefef;
  border-color: #ccc;
}

.upload-box.has-image {
  border: none;
  background-color: transparent;
}

.hidden-input {
  display: none;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--spacing-lg);
  color: var(--color-text-light);
}

.upload-icon {
  font-size: 2rem;
  margin-bottom: var(--spacing-sm);
}

.upload-hint {
  font-size: 0.75rem;
  margin-top: var(--spacing-lg);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* Form inputs */
.form-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--color-text);
}

input, textarea {
  font-family: inherit;
  padding: 12px var(--spacing-md);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  font-size: 1rem;
  transition: border-color var(--transition-fast);
  background-color: var(--color-surface);
}

input:focus, textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  background-color: var(--color-background);
}

textarea {
  resize: vertical;
}

.error-text {
  color: var(--color-primary);
  font-size: 0.75rem;
  margin-top: 4px;
}

.alert-error {
  background-color: #fde8e8;
  color: var(--color-primary);
  padding: var(--spacing-md);
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.form-actions {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
}

.btn-submit {
  background-color: var(--color-primary);
  color: white;
  padding: 12px 24px;
  border-radius: 24px;
  font-weight: 600;
  font-size: 1rem;
  transition: background-color var(--transition-fast);
}

.btn-submit:hover:not(:disabled) {
  background-color: var(--color-primary-hover);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
