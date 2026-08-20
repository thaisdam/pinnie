# Roadmap, Fases e Escopo do MVP

## 1. Escopo do MVP Inicial

### O que ENTRA no MVP:
- Cadastro, Login, Logout e Perfil do Usuário;
- CRUD de Boards (Painéis públicos e privados);
- CRUD de Pins com Upload de Imagens;
- Associação de Pins em Boards;
- Feed paginado público;
- Busca básica por termo;
- Interface web responsiva em Vue 3;
- Suíte de Testes (Unitários e de Integração);
- Documentação com OpenAPI / Swagger;
- Containerização com Docker Compose.

### O que NÃO ENTRA no MVP (Recursos Futuros):
- Inteligência Artificial e Recomendação Personalizada;
- Sistema de Comentários, Notificações e Curtidas;
- Mensageria (Kafka/RabbitMQ) ou Caching complexo (Redis);
- Busca Semântica / Elasticsearch;
- Arquitetura de Microserviços ou Kubernetes.

---

## 2. Roadmap Completo do Projeto

- **Fase 0 — Planejamento:** Requisitos, arquitetura, modelo de dados e API.
- **Fase 1 (Módulo 1) — Inicialização:** Git, estrutura de arquivos, Spring Boot, Docker. *(Concluída)*
- **Fase 2 (Módulo 2) — Backend Base & User/Auth:** PostgreSQL, Flyway, User, Auth, JWT. *(Concluída)*
- **Fase 3 (Módulo 3) — Boards:** CRUD de Painéis e controle de visibilidade.
- **Fase 4 (Módulo 4) — Pins e Imagens:** CRUD de Pins e upload de imagens (estáticos).
- **Fase 5 (Módulo 5) — Salvar Pins em Boards:** Relacionamento BoardPin, salvar e remover Pins de Boards.
- **Fase 6 (Módulo 6) — Feed:** Paginação, ordenação e exibição no Masonry Grid.
- **Fase 7 — Frontend (Vue 3):** Layout, roteamento, estado (Pinia), consumo da API com Axios.
- **Fase 8 — Busca:** Busca textual e filtros por palavra-chave.
- **Fase 11 — Social:** Likes, Follows e Comentários (Pós-MVP).
- **Fase 12 — Qualidade:** Testes completos, segurança refinada e documentação final.
- **Fase 13 — Deploy:** Ambiente de Produção, HTTPS e Object Storage externo.
- **Fase 14 — Moderação:** Denúncias, bloqueios e painel administrativo.
- **Fase 15 — Recomendação:** Algoritmos de recomendação e personalização.
- **Fase 16 — IA:** Classificação de imagens e busca semântica multimodal.
