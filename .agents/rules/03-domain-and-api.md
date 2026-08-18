# Modelo de Domínio, Regras de Negócio e APIs

## 1. Conceitos Principais
- **User:** Representa um usuário registrado na plataforma.
- **Board (Painel):** Coleção criada por um usuário para organizar Pins (ex: Ideias de Decoração, Looks, Receitas).
- **Pin:** Conteúdo visual publicado na plataforma (imagem, título, descrição e link de fonte original).
- **BoardPin:** Tabela associativa (N:N) que relaciona Pins a Boards. Um Pin pode estar em vários Boards.

---

## 2. Regras de Negócio Iniciais

### Pins
- Um Pin obrigatoriamente possui uma imagem.
- Pode possuir uma fonte original (`sourceUrl`), mas a fonte **não** deve substituir a visualização do Pin no Pinnie.

### Boards
- Um usuário pode criar múltiplos Boards.
- Boards podem ser **públicos** ou **privados**.
- Somente o proprietário do Board pode editar ou excluir o seu Board.

### Pins Salvos
- Um Pin pode ser salvo em múltiplos Boards.
- Um usuário não pode adicionar Pins em Boards privados de outros usuários.

### Autorização
- Todas as validações e verificações de propriedade devem ser feitas no **backend**. Nunca confiar apenas no frontend.

---

## 3. Entidades Iniciais (Mapeamento)

- **User:** `id` (UUID), `username`, `email`, `passwordHash`, `displayName`, `enabled`, `createdAt`, `updatedAt`.
- **Board:** `id` (UUID), `name`, `description`, `isPrivate`, `coverImageUrl`, `userId`, `createdAt`, `updatedAt`.
- **Pin:** `id` (UUID), `title`, `description`, `imageUrl`, `sourceUrl`, `userId`, `createdAt`, `updatedAt`.
- **BoardPin:** `boardId`, `pinId`, `createdAt`.

---

## 4. API REST Pretendida

### Autenticação (`/api/auth`)
- `POST /api/auth/register` — Cadastro de usuário
- `POST /api/auth/login` — Login e emissão de cookie JWT
- `POST /api/auth/logout` — Limpeza de cookies de sessão
- `GET /api/csrf` — Obtenção do token CSRF

### Usuários (`/api/users`)
- `GET /api/users/me` — Perfil do usuário autenticado
- `GET /api/users/{id}` — Visualizar usuário
- `PUT /api/users/me` — Atualizar perfil

### Boards (`/api/boards`)
- `POST /api/boards` — Criar Board
- `GET /api/boards` — Listar Boards do usuário
- `GET /api/boards/{id}` — Detalhes do Board
- `PUT /api/boards/{id}` — Editar Board
- `DELETE /api/boards/{id}` — Remover Board

### Pins (`/api/pins`)
- `POST /api/pins` — Criar Pin (com upload)
- `GET /api/pins/{id}` — Visualizar Pin
- `PUT /api/pins/{id}` — Editar Pin
- `DELETE /api/pins/{id}` — Deletar Pin

### Board Pins (`/api/boards/{boardId}/pins/{pinId}`)
- `POST /api/boards/{boardId}/pins/{pinId}` — Salvar Pin no Board
- `DELETE /api/boards/{boardId}/pins/{pinId}` — Remover Pin do Board

### Feed & Busca (`/api/feed`, `/api/search`)
- `GET /api/feed?page=0&size=20` — Feed público paginado (ordenado por data)
- `GET /api/search?q=` — Busca básica por termo

---

## 5. Feed e Interface
- O Feed inicial será paginado, exibindo Pins públicos recentes (sem IA no MVP).
- A interface frontend utilizará um **Masonry Grid** (layout em colunas dinâmicas) com visual clean e identidade própria.
