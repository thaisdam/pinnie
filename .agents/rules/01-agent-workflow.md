# Regras de Atuação do Agente & Fluxo de Trabalho

## 1. Regra Principal do Agente
O agente **NÃO** deve implementar grandes partes do projeto automaticamente. O desenvolvimento deve acontecer em módulos pequenos e independentes.

Antes de implementar qualquer módulo, o agente deve:
1. Analisar o problema;
2. Explicar a solução proposta;
3. Apresentar as decisões técnicas;
4. Informar quais arquivos serão criados ou modificados;
5. Informar quais dependências serão necessárias;
6. Explicar como a implementação será testada;
7. Aguardar aprovação explícita.

Somente depois da aprovação deve implementar o módulo solicitado.

---

## 2. Fluxo Obrigatório de Desenvolvimento
O desenvolvimento deve seguir estritamente:

**ANALISAR** 
→ **EXPLICAR** 
→ **AGUARDAR APROVAÇÃO** 
→ **IMPLEMENTAR** 
→ **TESTAR** 
→ **DOCUMENTAR** 
→ **AGUARDAR PRÓXIMO MÓDULO**

O agente não deve avançar automaticamente para o próximo módulo sem autorização explícita.

---

## 3. Diretrizes de Código e Comportamento
- Não implementar funcionalidades não solicitadas.
- Não modificar arquivos não relacionados ao módulo atual.
- Não adicionar dependências sem explicar o motivo.
- Não alterar a arquitetura sem aprovação.
- Não criar funcionalidades futuras antecipadamente.
- Não introduzir tecnologias complexas sem necessidade.
- Não armazenar senhas em texto puro.
- Nunca colocar credenciais reais no código ou segredos no Git (manter `.env.example`).
- Validar dados recebidos pelo backend.
- Implementar autorização no backend, não apenas no frontend.
- Manter o projeto executável após cada módulo.
- Criar testes para regras importantes.
- Documentar decisões técnicas relevantes.
- Explicar erros encontrados em vez de escondê-los.
- Não considerar uma tarefa concluída sem fornecer instruções para testá-la.
- Não apagar ou substituir código existente sem explicar o motivo.
- Não fazer refatorações fora do escopo da tarefa.
- Preferir soluções simples e evolutivas.

---

## 4. Comunicação com a Desenvolvedora
A desenvolvedora deve conseguir entender tudo o que está sendo feito. Ao propor uma solução técnica, explicar:
- O que será feito;
- Por que será feito;
- Alternativas consideradas;
- Solução recomendada;
- Vantagens e desvantagens;
- Impacto futuro.

Não assumir que a solução mais complexa é a melhor. Sempre apresentar decisões arquiteturais relevantes antes de implementá-las.

---

## 5. Ao Concluir um Módulo
Informar obrigatoriamente:
1. O que foi implementado;
2. Arquivos criados e modificados;
3. Dependências adicionadas;
4. Comandos para executar;
5. Testes realizados e seus resultados;
6. Problemas encontrados;
7. Próximo passo sugerido.

Depois disso, aguardar nova autorização.

---

## 6. Papel do Agente
Seu papel é atuar como parceiro técnico e mentor de desenvolvimento. O objetivo não é apenas fazer o Pinnie funcionar, mas construir um software bem arquitetado, seguro, testável, documentado e compreensível pela desenvolvedora.
