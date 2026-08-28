# 🚀 Backlog de Funcionalidades Futuras

Este documento guarda ideias e melhorias mapeadas para o futuro da plataforma Pinnie, para que não caiam no esquecimento.

## Sistema de Comentários

### 1. Curtir um Comentário ❤️
- **Nível de Esforço**: Fácil / Moderado
- **Backend**: 
  - Criar tabela `comment_likes` (relação N:M entre `User` e `Comment`).
  - Endpoint `POST /comments/{id}/like` e `DELETE /comments/{id}/like`.
- **Frontend**: 
  - Renderizar um ícone de coração nos comentários.
  - Alternar estado (vermelho/vazio) e atualizar a contagem de forma reativa.

### 2. Responder um Comentário (Threads) 💬
- **Nível de Esforço**: Moderado / Alto
- **Backend**:
  - Adicionar autorreferência na entidade `Comment` (ex: coluna `parent_comment_id`).
  - Retornar os comentários estruturados em árvore ou ordenados adequadamente.
- **Frontend**:
  - Transformar o componente de lista de comentários em recursivo para suportar aninhamento (indentação visual).
  - Lógica para exibir/ocultar respostas filhas.
  - Campo de input dinâmico atrelado ao comentário-pai (botão "Responder").

### 3. Marcar Alguém (@usuario) 🏷️
- **Nível de Esforço**: Alto
- **Backend**:
  - Endpoint para busca rápida de usuários por nome (`autocomplete`).
  - Lógica para processar menções ao salvar o comentário e criar registros em um novo **Sistema de Notificações**.
- **Frontend**:
  - Interface flutuante (dropdown) de autocomplete que é ativada ao digitar o caractere `@` no textarea.
  - Fazer o parse do texto salvo para renderizar as tags `@nome` em negrito e como links (redirecionando para o perfil).

## Moderação e Perfil

### 4. Denunciar Usuários 🚨
- **Nível de Esforço**: Moderado
- **Backend**:
  - Criar entidade `UserReport` com os campos: `reporter_id`, `reported_user_id`, `reason` (enum com motivos como Spam, Fake, Ofensivo), e `status` (Pendente, Resolvido).
  - Endpoint `POST /users/{id}/report`.
- **Frontend**:
  - Adicionar um botão/ícone de reticências (...) na página de Perfil do Usuário com a opção "Denunciar".
  - Criar um Modal simples com um formulário de rádio (motivos) e textarea (opcional).

### 5. Denunciar Pins (Posts) 🚩
- **Nível de Esforço**: Moderado
- **Backend**:
  - Criar entidade `PinReport` relacionando o `User` (denunciante) e o `Pin` (denunciado), mais motivo e descrição.
  - Endpoint `POST /pins/{id}/report`.
- **Frontend**:
  - Adicionar a opção "Denunciar Pin" no menu de ações do Pin (na PinView ou no PinCard).
  - Reutilizar ou criar um Modal de denúncia, seguido de um Toast/Alerta de "Denúncia recebida com sucesso".

### 6. Mudar Nome de Usuário (Username/Handle) ✏️
- **Nível de Esforço**: Moderado / Alto
- **Backend**:
  - Endpoint `PUT /users/me/username`.
  - Lógica estrita de validação: garantir que não contenha espaços/caracteres especiais e checar unicidade no banco (lançar exceção de conflito se já existir).
  - *Atenção:* Se o username estiver no payload do token JWT atual, pode ser necessário emitir um novo token ao finalizar a troca para evitar bugs de autenticação.
- **Frontend**:
  - Adicionar o campo "Nome de Usuário" no Modal de Edição de Perfil.
  - Tratar adequadamente erros 409 (Conflict) caso o usuário tente pegar um nome que já existe.
  - Atualizar o estado global (Pinia/auth.js) com o novo username em tempo real após o sucesso.
