# Escopo, Fases de Desenvolvimento e MVP — Pinnie

Este documento evidencia o planejamento macro de software que norteou o projeto do Pinnie desde o levantamento de requisitos até a auditoria pré-publicação.

## 1. O Escopo (MVP)

A premissa da plataforma Pinnie era recriar os fluxos centrais de um sistema de descoberta visual baseado em imagens (estilo Pinterest), com identidade visual única e fluxos otimizados, englobando:

- **Módulo de Usuários:** Autenticação e Perfis (Bio, Avatar, Senha).
- **Módulo de Pastas (Boards):** Criação e organização, além do controle vital de privacidade (pastas públicas vs privadas).
- **Módulo de Pins (Imagens):** Feed infinito global, upload nativo redimensionado e buscas textuais rápidas.
- **Módulo Social:** Curtidas, Sistema robusto de Comentários e Seguidores.
- **Ambiente Contêinerizado:** Pronto para Cloud (Docker).

Foram deliberadamente excluídos do MVP: Inteligência artificial, microsserviços e mensageria distribuída (reservados para arquiteturas Enterprise e que gerariam overengineering neste escopo).

## 2. Roadmap Executado

O projeto foi organizado, conduzido e versionado nas seguintes fases de engenharia:

- **Fase 0 — Planejamento:** Requisitos, arquitetura macro, modelo de dados relacional e contratos REST API.
- **Fase 1 — Setup Inicial:** Configuração do Spring Boot, conexões, Flyway e esqueleto do Git.
- **Fase 2 — Auth e Core Backend:** Implementação de JWT via HTTP-Only e proteção CSRF moderna (Módulo Crítico).
- **Fase 3 — Domínio de Boards:** Construção da lógica de painéis e validação de donos do recurso.
- **Fase 4 — Domínio de Pins:** Criação da engine de processamento de imagem (`ImageProcessor` local).
- **Fase 5 — Associação N:N:** Salvar Pins em Boards com checagem rigorosa de visibilidade.
- **Fase 6 — Feed e Busca:** Implementação de busca com base no Postgres e rotas de exploração paginada.
- **Fase 7 — Frontend Core (Vue 3):** Consumo via Axios, Pinia para gerenciamento global e construção do "Masonry Grid" para os Pins.
- **Fase 8 — Expansão Social:** Módulo extra adicionando Curtidas, Comentários interativos e sistema de Seguir usuários.
- **Fase 9 — Perfil e Autogestão:** Área de perfil do usuário.
- **Fase 10 — Qualidade & Auditoria Geral:** Criação de suíte de Testes Unitários/Integração (com Testcontainers), revisão das permissões (`CORS`, `SameSite=Lax`) e blindagem para o ambiente de Produção.
