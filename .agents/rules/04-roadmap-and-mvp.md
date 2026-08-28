# Roadmap, Fases e Escopo do MVP

## 1. Escopo do MVP Inicial

### O que ENTRA no MVP:
- Cadastro, Login, Logout e Perfil do Usuário;
- CRUD de Boards (Painéis públicos e privados);
- CRUD de Pins com Upload de Imagens (via LocalStorage no backend, preparado para S3);
- Associação de Pins em Boards;
- Feed paginado público com scroll infinito;
- Busca básica por termo;
- Interface web responsiva em Vue 3 (CSS puro, sem frameworks adicionais de UI);
- Suíte de Testes (Unitários para Services);
- Documentação com OpenAPI / Swagger;
- Containerização básica.
- Sistema de Comentários, Curtidas e Seguidores (Implementados como features extras consolidadas).

### O que NÃO ENTRA no MVP (Recursos Futuros):
- Inteligência Artificial e Recomendação Personalizada;
- Mensageria (Kafka/RabbitMQ) ou Caching complexo (Redis);
- Busca Semântica / Elasticsearch;
- Arquitetura de Microserviços ou Kubernetes.

---

## 2. Roadmap Consolidado do Projeto (Atualizado na Auditoria Módulo 15)

- **Fase 0 — Planejamento:** Requisitos, arquitetura, modelo de dados e API. [CONCLUÍDO]
- **Fase 1 (Módulo 1) — Inicialização:** Git, estrutura de arquivos, Spring Boot. [CONCLUÍDO]
- **Fase 2 (Módulo 2) — Backend Base & User/Auth:** PostgreSQL, Flyway, User, Auth, JWT. [CONCLUÍDO]
- **Fase 3 (Módulo 3) — Boards:** CRUD de Painéis e controle de visibilidade. [CONCLUÍDO]
- **Fase 4 (Módulo 4) — Pins e Imagens:** CRUD de Pins e upload de imagens locais. [CONCLUÍDO]
- **Fase 5 (Módulo 5) — Salvar Pins em Boards:** Relacionamento BoardPin. [CONCLUÍDO]
- **Fase 6 (Módulo 6) — Feed:** Paginação, ordenação (FeedController). [CONCLUÍDO]
- **Fase 7 (Módulo 7) — Busca:** Busca textual (SearchController). [CONCLUÍDO]
- **Fase 8 (Módulo 8) — Frontend (Vue 3):** Layout, roteamento, Pinia, Axios. [CONCLUÍDO]
- **Fase 9 (Módulo 9) — Frontend Auth UI:** Telas de Login e Registro. [CONCLUÍDO]
- **Fase 10 (Módulo 10) — Frontend Core UI:** Home/Feed (Masonry Grid), Upload, Boards. [CONCLUÍDO]
- **Fase 13 (Módulo 13) — Comentários:** Backend e Frontend para comentários nos Pins. [CONCLUÍDO] *(Antecipado)*
- **Fase 14 (Módulo 14) — Social:** Likes e Follows. [CONCLUÍDO] *(Antecipado)*
- **Fase 11 (Módulo 14.5) — Perfil de Usuário:** Tela de Perfil, listas de pastas, alteração de avatar, nome, bio e senhas. [CONCLUÍDO] *(Adiado e renomeado)*
- **Fase 12 (Módulo 12) — Qualidade:** Testes unitários de serviços, correções de segurança (JWT HttpOnly, CSRF desativado com SameSite Lax) e ajustes de UI (Tema Amarelo). [CONCLUÍDO]

**>>> ATUALMENTE AQUI <<<**

- **Fase 15 (Módulo 15) — Deploy:** Ambiente de Produção, HTTPS, Docker Compose completo e migração para Object Storage externo. [PENDENTE]
- **Fase 16 (Módulo 16) — Moderação:** Denúncias, bloqueios e painel administrativo. [PENDENTE]
