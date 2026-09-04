# Domínio e Referência de API — Pinnie

Este documento expõe o desenho dos modelos de negócio, das tabelas transacionais e a organização dos contratos da API REST do Pinnie.

## 1. Entidades de Domínio e Banco de Dados

O banco de dados do Pinnie foi modelado com alta integridade referencial utilizando `UUID` como chave primária para ofuscar sequências (evitando ID guessing) e deleções em cascata.

### Principais Entidades
- **User:** Contém o Auth (Hashes do BCrypt), bio, avatar e flags de segurança (`enabled`).
- **Board:** O "Painel" ou pasta. Relaciona-se com o `User` (1:N) e possui flag crítica de visibilidade (`isPrivate`).
- **Pin:** O Post Visual. Hospeda os metadados físicos gerados pelo backend (width, height, content type) e os textos para busca (`title`, `description`). Relacionado ao `User` (1:N).
- **BoardPin:** Tabela Associativa (N:N). Reflete a ação de "Salvar". Um Pin de um usuário X pode ser salvo no Board de um usuário Y (desde que o Board não seja privado de outro usuário).
- **Comment, Like, Follow:** Estruturas relacionais que montam a rede social. Todas possuem constraints que asseguram a exclusão limpa quando a raiz (Usuário ou Pin) é deletada.

## 2. Design da API REST

A API foi projetada de forma RESTful, segregando claramente as responsabilidades de leitura (públicas) e mutação (privadas/autenticadas).

### Autenticação (`/api/auth`)
- `POST /api/auth/register`
- `POST /api/auth/login` (Anexa JWT no Cookie)
- `POST /api/auth/logout` (Invalida cookies de sessão)
- `GET /api/csrf` (Provê token inicial)

### Usuários (`/api/users`)
- Rotas para buscar perfil público via ID ou `username`.
- Alteração segura (via `PUT`) usando identificador do Cookie.
- Gestão de Follows (Seguir/Deixar de Seguir).

### Boards e Pins (`/api/boards`, `/api/pins`)
- CRUD completo.
- Deleção checa propriedade; A visualização (GET) de um Board intercepta se o `isPrivate` exige que o requisitante seja o dono.

### Busca e Feed
- `GET /api/feed?page=X&size=Y` — Traz Pins globais aleatórios/recentes (para Masonry Grid) paginados.
- `GET /api/search?q=TERMO` — Utiliza engines do Postgres para busca rápida (com eventual suporte a `pg_trgm`).

### Tratamento de Erros Globais
A API responde a exceções padronizadas pelo `@RestControllerAdvice`, emitindo JSONs estruturados (`status`, `error`, `message`, `path`) invés de StackTraces cruas de Java, prevenindo vazamento de stack interna.
