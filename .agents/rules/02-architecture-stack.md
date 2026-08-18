# Arquitetura, Tecnologias e Infraestrutura

## 1. Sobre o Pinnie
O Pinnie é uma plataforma de descoberta, organização e compartilhamento de conteúdo visual, inspirada no conceito do Pinterest, mas com identidade e proposta próprias.
- Proposta principal: Experiência visual limpa, sem anúncios invasivos e sem redirecionamentos inesperados ao clicar em imagens.
- O usuário visualiza o conteúdo dentro do Pinnie e, se desejar, visita a fonte original.
- Inicialmente desenvolvido como um MVP evolutivo.

---

## 2. Stack Tecnológica

### Backend
- **Linguagem:** Java 17
- **Framework:** Spring Boot 3.x
- **Módulos:** Spring Web, Spring Security, Spring Data JPA
- **Banco de Dados:** PostgreSQL 16
- **Migrations:** Flyway
- **Validação:** Bean Validation (Jakarta Validation)
- **Documentação:** OpenAPI / Swagger
- **Build:** Maven

### Frontend
- **Framework:** Vue 3
- **Roteamento:** Vue Router
- **Estado:** Pinia

### Infraestrutura
- **Containers:** Docker & Docker Compose
- **Versionamento:** Git & GitHub

---

## 3. Visão Geral da Arquitetura

```
Frontend (Vue 3)
      ↓
REST API (JSON / Cookies HTTP-Only)
      ↓
Spring Boot 3
      ↓
PostgreSQL 16
```

### Armazenamento de Imagens
```
Frontend ──> Spring Boot ──> Storage Local (MVP) / Object Storage (Futuro) ──> URL salva no PostgreSQL
```
- A arquitetura deve permitir migrar de storage local para Object Storage (ex: S3/Cloud Storage) sem reescrever a lógica de negócio.
- Não armazenar arquivos binários de imagem diretamente no PostgreSQL (somente URLs e metadados).

---

## 4. Diretrizes de Infraestrutura e Banco de Dados

### Banco de Dados & Migrations
- Banco de dados relacional PostgreSQL.
- Migrações obrigatoriamente gerenciadas via **Flyway** (`V1__...sql`, `V2__...sql`).
- Não depender da criação automática de schemas pelo Hibernate (`ddl-auto: validate` em produção/dev).

### Docker & Docker Compose
- Manter a infraestrutura simples durante o desenvolvimento.
- Serviços iniciais: Backend, Frontend e PostgreSQL.
- Não adicionar Redis, Kafka, Elasticsearch ou Kubernetes sem uma necessidade real comprovada.

---

## 5. Segurança
- Autenticação Stateless baseada em **JWT** armazenado em **Cookies HttpOnly**.
- Criptografia de senhas com **BCrypt**.
- Proteção contra **CSRF** com `CookieCsrfTokenRepository` (`X-XSRF-TOKEN`).
- Validação estrita de entradas no backend.
- Controle de acesso por recurso (RBAC / Dono do Recurso).
- Validação rigorosa de uploads (tamanho, MIME type, extensão).
