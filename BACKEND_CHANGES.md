# SpendFy API — Changelog para o Frontend

> **Data:** Março 2026
> **Branch:** `feature/update-system`
> **Base URL:** `http://localhost:8080/api`
> **Auth:** Todas as rotas (exceto `/auth/**`) exigem header `Authorization: Bearer <token>`

---

## ⚠️ BREAKING CHANGES — Leia primeiro

Estas alterações **quebram** integrações existentes e precisam ser corrigidas no frontend imediatamente.

---

### 1. `GET /api/transacoes` — Agora retorna objeto paginado

**Antes:**
```json
[
  { "id": 1, "tipo": "DESPESA", ... },
  { "id": 2, "tipo": "RECEITA", ... }
]
```

**Agora:**
```json
{
  "content": [
    { "id": 1, "tipo": "DESPESA", ... },
    { "id": 2, "tipo": "RECEITA", ... }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

**Parâmetros de paginação (todos opcionais):**
| Param | Tipo | Padrão | Descrição |
|---|---|---|---|
| `page` | int | 0 | Número da página (começa em 0) |
| `size` | int | 20 | Itens por página |
| `sort` | string | `data` | Campo de ordenação |

**Onde ajustar no frontend:**
Qualquer lugar que faz `GET /transacoes` e trata a resposta como array — trocar por `.data.content`.

---

### 2. Campo `tipo` em Conta — Agora é enum

**Antes:** String livre (`"Corrente"`, `"Poupança"`, etc.)
**Agora:** Enum com valores fixos em maiúsculas:

| Valor aceito | Descrição |
|---|---|
| `CORRENTE` | Conta corrente |
| `POUPANCA` | Conta poupança |
| `INVESTIMENTO` | Conta de investimento |
| `CARTEIRA` | Carteira / dinheiro em espécie |
| `OUTRO` | Outros tipos |

> ⚠️ O valor `"OUTROS"` (com S) **não é aceito** — use `"OUTRO"` (sem S).

**Impacto:** `POST /api/contas` e `PUT /api/contas/{id}` rejeitarão valores fora dessa lista com `400 Bad Request`. A resposta JSON retornará `"tipo": "CORRENTE"` (em maiúsculas), não `"Corrente"`.

---

### 3. Campo `status` em Transação — Agora é enum

**Antes:** String livre (`"CONFIRMADA"`, etc.)
**Agora:** Enum com dois valores:

| Valor aceito | Descrição |
|---|---|
| `PAGO` | Transação efetivada |
| `PENDENTE` | Transação ainda não efetivada |

> ⚠️ `"CONFIRMADA"` **não existe mais**. Migrar para `"PAGO"`.

---

### 4. Campo `tipo` em Transação — Agora é enum (compatível, mas verifique)

Os valores continuam os mesmos, mas agora são enums tipados:

| Valor | Descrição |
|---|---|
| `DESPESA` | Saída de dinheiro |
| `RECEITA` | Entrada de dinheiro |

A resposta JSON continua retornando `"DESPESA"` / `"RECEITA"` como string — sem impacto se o frontend já usa esses valores.

---

### 5. `DELETE /api/transacoes/{id}` — Agora é soft delete

A transação **não é removida do banco**. O campo `deletedAt` é preenchido e a transação some de todas as listagens automaticamente. Para o frontend, o comportamento é idêntico — a resposta continua sendo `204 No Content`.

---

## ✅ ALTERAÇÕES EM ENDPOINTS EXISTENTES

### `GET /api/transacoes` — Filtros avançados

Além da paginação, agora aceita filtros como query params. Todos são opcionais e combináveis:

| Param | Tipo | Exemplo | Descrição |
|---|---|---|---|
| `tipo` | enum | `DESPESA` | Filtra por tipo |
| `status` | enum | `PAGO` | Filtra por status |
| `categoriaId` | Long | `3` | Filtra por categoria |
| `contaId` | Long | `1` | Filtra por conta |
| `dataInicio` | date | `2026-01-01` | A partir desta data (formato ISO) |
| `dataFim` | date | `2026-01-31` | Até esta data (formato ISO) |

**Exemplo de requisição com filtros:**
```
GET /api/transacoes?tipo=DESPESA&dataInicio=2026-03-01&dataFim=2026-03-31&size=10&page=0
```

---

### `POST /api/transacoes` e `PUT /api/transacoes/{id}` — Campo recorrência

O body agora aceita o campo opcional `recorrencia`. Se omitido, assume `NENHUMA`.

**Body completo atualizado:**
```json
{
  "tipo": "DESPESA",
  "data": "2026-03-24",
  "valor": 150.00,
  "descricao": "Academia",
  "observacao": "Mensalidade",
  "status": "PAGO",
  "recorrencia": "MENSAL",
  "idConta": 1,
  "idCategoria": 3
}
```

**Valores de `recorrencia`:**
| Valor | Descrição |
|---|---|
| `NENHUMA` | Não recorrente (padrão) |
| `DIARIA` | Repete todo dia |
| `SEMANAL` | Repete toda semana |
| `MENSAL` | Repete todo mês |
| `ANUAL` | Repete todo ano |

**Resposta atualizada de Transação (`TransacaoResponse`):**
```json
{
  "id": 1,
  "tipo": "DESPESA",
  "data": "2026-03-24",
  "valor": 150.00,
  "descricao": "Academia",
  "observacao": "Mensalidade",
  "status": "PAGO",
  "recorrencia": "MENSAL",
  "idUsuario": 1,
  "idConta": 1,
  "nomeConta": "Nubank",
  "idCategoria": 3,
  "nomeCategoria": "Saúde",
  "dataCadastro": "2026-03-24T10:00:00",
  "dataAtualizacao": "2026-03-24T10:00:00"
}
```

---

### `GET /api/orcamentos` — Response atualizado com progresso

O `OrcamentoResponse` agora inclui dois campos calculados automaticamente pelo backend:

```json
{
  "id": 1,
  "valorLimite": 500.00,
  "valorGasto": 320.00,
  "percentualUtilizado": 64.00,
  "dataInicio": "2026-03-01",
  "dataFim": "2026-03-31",
  "idUsuario": 1,
  "idCategoria": 2,
  "nomeCategoria": "Alimentação",
  "dataCadastro": "2026-03-01T00:00:00",
  "dataAtualizacao": "2026-03-24T10:00:00"
}
```

| Campo novo | Tipo | Descrição |
|---|---|---|
| `valorGasto` | BigDecimal | Total gasto no período do orçamento |
| `percentualUtilizado` | BigDecimal | Percentual do limite já consumido (ex: `64.00` = 64%) |

---

### `GET /api/contas` — Response atualizado

O campo `tipo` agora retorna o enum em maiúsculas:

```json
{
  "id": 1,
  "nome": "Nubank",
  "tipo": "CORRENTE",
  "saldoInicial": 1000.00,
  "saldoAtual": 750.50,
  "idUsuario": 1,
  "dataCadastro": "2026-01-15T09:00:00",
  "dataAtualizacao": "2026-03-24T10:00:00"
}
```

O `saldoAtual` é calculado via SQL em tempo real (não é mais calculado in-memory com lazy loading).

---

## 🆕 NOVOS ENDPOINTS

---

### Dashboard — `GET /api/dashboard`

Visão consolidada das finanças do mês atual em uma única chamada. Substitui a necessidade de fazer múltiplas requisições na tela inicial.

**Response:**
```json
{
  "saldoTotal": 3250.75,
  "totalReceitasMes": 5000.00,
  "totalDespesasMes": 1749.25,
  "saldoMes": 3250.75,
  "topCategorias": [
    {
      "idCategoria": 2,
      "nome": "Alimentação",
      "cor": "#22c55e",
      "total": 850.00,
      "percentualDoTotal": 48.59
    },
    {
      "idCategoria": 5,
      "nome": "Transporte",
      "cor": "#3b82f6",
      "total": 320.00,
      "percentualDoTotal": 18.29
    }
  ],
  "orcamentosAtivos": [
    {
      "id": 1,
      "valorLimite": 1000.00,
      "valorGasto": 850.00,
      "percentualUtilizado": 85.00,
      "dataInicio": "2026-03-01",
      "dataFim": "2026-03-31",
      "idCategoria": 2,
      "nomeCategoria": "Alimentação"
    }
  ]
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `saldoTotal` | BigDecimal | Soma dos `saldoAtual` de todas as contas do usuário |
| `totalReceitasMes` | BigDecimal | Total de receitas no mês atual |
| `totalDespesasMes` | BigDecimal | Total de despesas no mês atual |
| `saldoMes` | BigDecimal | `totalReceitasMes - totalDespesasMes` |
| `topCategorias` | array | Top 5 categorias com mais gastos no mês atual |
| `orcamentosAtivos` | array | Orçamentos vigentes hoje (já com `valorGasto` e `percentualUtilizado`) |

---

### Alertas — `GET /api/alertas`

Retorna todos os alertas não lidos do usuário autenticado, ordenados do mais recente para o mais antigo.

**Response:**
```json
[
  {
    "id": 3,
    "tipo": "ORCAMENTO_80_PERCENT",
    "mensagem": "Você já utilizou 85.00% do orçamento de Alimentação",
    "lido": false,
    "idReferencia": 1,
    "criadoEm": "2026-03-24T08:00:00"
  },
  {
    "id": 2,
    "tipo": "SALDO_BAIXO",
    "mensagem": "Saldo baixo na conta \"Carteira\": R$ 45.00",
    "lido": false,
    "idReferencia": 5,
    "criadoEm": "2026-03-23T08:00:00"
  }
]
```

**Tipos de alerta (`tipo`):**
| Valor | Quando dispara |
|---|---|
| `ORCAMENTO_80_PERCENT` | Quando 80% ou mais do limite de um orçamento foi consumido |
| `ORCAMENTO_ESTOURADO` | Quando 100% ou mais do limite de um orçamento foi consumido |
| `SALDO_BAIXO` | Quando o saldo de qualquer conta cai abaixo de R$ 100,00 |
| `DESPESA_INCOMUM` | Reservado para uso futuro |

| Campo | Tipo | Descrição |
|---|---|---|
| `idReferencia` | Long | ID do orçamento ou da conta que gerou o alerta |
| `criadoEm` | LocalDateTime | Timestamp de criação |

> **Quando os alertas são gerados:** Automaticamente todo dia às 08h por um job agendado no servidor. O frontend não precisa fazer nada para gerá-los — apenas consultá-los.

---

### Alertas — `PATCH /api/alertas/{id}/lido`

Marca um alerta específico como lido.

**Response:** O alerta atualizado com `"lido": true`

```json
{
  "id": 3,
  "tipo": "ORCAMENTO_80_PERCENT",
  "mensagem": "Você já utilizou 85.00% do orçamento de Alimentação",
  "lido": true,
  "idReferencia": 1,
  "criadoEm": "2026-03-24T08:00:00"
}
```

> Sugestão de UX: usar este endpoint ao clicar no sino de notificações ou ao expandir um alerta.

---

### Insights — `GET /api/insights/previsao`

Previsão de gastos por categoria para o mês atual, calculada com base na **média ponderada dos últimos 3 meses** (mês mais recente tem peso maior).

**Response:**
```json
[
  {
    "idCategoria": 2,
    "nomeCategoria": "Alimentação",
    "mediaMensal": 780.00,
    "previsaoMesAtual": 780.00,
    "gastoAtualMes": 850.00,
    "diferenca": 70.00
  },
  {
    "idCategoria": 5,
    "nomeCategoria": "Transporte",
    "mediaMensal": 310.00,
    "previsaoMesAtual": 310.00,
    "gastoAtualMes": 250.00,
    "diferenca": -60.00
  }
]
```

| Campo | Tipo | Descrição |
|---|---|---|
| `mediaMensal` | BigDecimal | Média ponderada dos últimos 3 meses |
| `previsaoMesAtual` | BigDecimal | Projeção para o mês atual |
| `gastoAtualMes` | BigDecimal | Quanto já foi gasto neste mês |
| `diferenca` | BigDecimal | `gastoAtualMes - previsaoMesAtual` (positivo = acima do previsto) |

> Categorias sem histórico de gastos não aparecem na lista.
> A lista vem ordenada por `gastoAtualMes` decrescente.

---

### Insights — `GET /api/insights/score`

Score financeiro do usuário de 0 a 100, calculado com base em 3 critérios do mês atual:

- **30 pts** — Relação receita/despesa (poupou mais de 30% = 30 pts, balanço positivo = 15 pts)
- **40 pts** — Aderência a orçamentos (proporcional ao percentual de orçamentos respeitados)
- **30 pts** — Histórico de saldo positivo nos últimos 3 meses (10 pts por mês positivo)

**Response:**
```json
{
  "score": 72,
  "classificacao": "BOM",
  "fatoresPositivos": [
    "Você poupou mais de 30% da sua renda este mês",
    "75% dos orçamentos estão dentro do limite",
    "Saldo positivo nos últimos 3 meses"
  ],
  "fatoresNegativos": [
    "Mais da metade dos orçamentos foram ultrapassados"
  ]
}
```

**Classificações:**
| Faixa | Classificação |
|---|---|
| 80 – 100 | `EXCELENTE` |
| 60 – 79 | `BOM` |
| 40 – 59 | `REGULAR` |
| 0 – 39 | `ATENÇÃO` |

---

### Insights — `GET /api/insights/relatorio-mensal`

Gera um resumo financeiro personalizado do mês atual usando **Inteligência Artificial (Claude da Anthropic)**. O texto é gerado dinamicamente com base nos dados reais do usuário.

**Response:**
```json
{
  "resumo": "Você teve um ótimo mês! Suas receitas superaram as despesas em 43%, e você manteve todos os orçamentos sob controle — exceto Alimentação, que ficou 8% acima do limite. Continue assim!",
  "destaques": [
    "Você poupou mais de 30% da sua renda este mês",
    "Todos os orçamentos foram respeitados",
    "Saldo positivo nos últimos 3 meses"
  ],
  "totalReceitas": 5000.00,
  "totalDespesas": 1749.25,
  "score": 85
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `resumo` | String | Texto gerado pela IA (2-3 frases em português) |
| `destaques` | String[] | Lista combinada de fatores positivos e negativos do score |
| `totalReceitas` | BigDecimal | Total de receitas do mês |
| `totalDespesas` | BigDecimal | Total de despesas do mês |
| `score` | int | Score financeiro do mês |

> **Nota:** Se a variável de ambiente `ANTHROPIC_API_KEY` não estiver configurada no servidor, o campo `resumo` retornará uma mensagem padrão sem IA.

---

### Classificação com IA — `POST /api/transacoes/classificar`

Sugere automaticamente a categoria mais adequada para uma transação com base na descrição, usando Inteligência Artificial. Útil para pré-preencher o campo de categoria no formulário de nova transação.

**Request body:**
```json
{
  "descricao": "iFood - pedido de pizza"
}
```

**Response:**
```json
{
  "idCategoria": 2,
  "nomeCategoria": "Alimentação",
  "cor": "#22c55e",
  "justificativa": "A descrição menciona entrega de comida via iFood, que se enquadra em despesas com alimentação."
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `idCategoria` | Long | ID da categoria sugerida (categoria real do usuário) |
| `nomeCategoria` | String | Nome da categoria |
| `cor` | String | Cor da categoria (hex) |
| `justificativa` | String | Explicação da IA para a escolha |

> **Como usar no frontend:** Ao usuário digitar a descrição da transação, chamar este endpoint e pré-selecionar a categoria sugerida no `<select>`. O usuário pode aceitar ou trocar manualmente.

---

## 📋 RESUMO DE TODOS OS ENDPOINTS

### Endpoints existentes (comportamento atualizado)

| Método | Rota | O que mudou |
|---|---|---|
| `GET` | `/api/transacoes` | Resposta paginada + filtros por query param |
| `POST` | `/api/transacoes` | Aceita campo `recorrencia`; `tipo` e `status` agora são enums |
| `PUT` | `/api/transacoes/{id}` | Aceita campo `recorrencia`; `tipo` e `status` agora são enums |
| `DELETE` | `/api/transacoes/{id}` | Soft delete (transação marcada como deletada, não removida) |
| `POST` | `/api/contas` | Campo `tipo` agora é enum (`CORRENTE`, `POUPANCA`, `INVESTIMENTO`, `CARTEIRA`, `OUTRO`) |
| `PUT` | `/api/contas/{id}` | Campo `tipo` agora é enum |
| `GET` | `/api/orcamentos` | Response inclui `valorGasto` e `percentualUtilizado` |
| `GET` | `/api/orcamentos/{id}` | Response inclui `valorGasto` e `percentualUtilizado` |

### Novos endpoints

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/dashboard` | Dashboard consolidado (saldo, receitas/despesas do mês, top categorias, orçamentos ativos) |
| `GET` | `/api/alertas` | Lista alertas não lidos do usuário |
| `PATCH` | `/api/alertas/{id}/lido` | Marca alerta como lido |
| `GET` | `/api/insights/previsao` | Previsão de gastos por categoria |
| `GET` | `/api/insights/score` | Score financeiro de 0 a 100 |
| `GET` | `/api/insights/relatorio-mensal` | Resumo do mês gerado por IA |
| `POST` | `/api/transacoes/classificar` | Sugere categoria via IA com base na descrição |

### Endpoints inalterados

| Método | Rota |
|---|---|
| `POST` | `/api/auth/login` |
| `POST` | `/api/auth/register` |
| `GET` | `/api/contas` |
| `GET` | `/api/contas/{id}` |
| `DELETE` | `/api/contas/{id}` |
| `GET` | `/api/categorias` |
| `POST` | `/api/categorias` |
| `PUT` | `/api/categorias/{id}` |
| `DELETE` | `/api/categorias/{id}` |
| `POST` | `/api/orcamentos` |
| `PUT` | `/api/orcamentos/{id}` |
| `DELETE` | `/api/orcamentos/{id}` |
| `GET` | `/api/relatorios/pdf` |
| `GET` | `/api/relatorios/csv` |
| `GET` | `/api/transacoes/{id}` |

---

## 🔔 Comportamentos automáticos do servidor (sem ação do frontend)

### Transações recorrentes
Toda noite à **01h00**, o servidor verifica transações com recorrência ativa e cria automaticamente novas transações para o período seguinte. O campo `dataProximaOcorrencia` controla esse ciclo internamente. O frontend não precisa fazer nada — as novas transações aparecerão normalmente nas listagens.

### Geração de alertas
Todo dia às **08h00**, o servidor verifica:
- Orçamentos que atingiram 80% ou mais do limite → gera `ORCAMENTO_80_PERCENT`
- Orçamentos que atingiram 100% ou mais do limite → gera `ORCAMENTO_ESTOURADO`
- Contas com saldo abaixo de R$ 100,00 → gera `SALDO_BAIXO`

Os alertas têm **deduplicação de 24h** — o mesmo alerta não é gerado duas vezes no mesmo dia para o mesmo recurso.

---

## 💡 Sugestões de implementação para o frontend

### Checklist de correções obrigatórias

- [ ] `GET /api/transacoes` → trocar `.data` por `.data.content` em todos os lugares que listam transações
- [ ] Transações: trocar `status: "CONFIRMADA"` por `status: "PAGO"` no formulário
- [ ] Contas: trocar `tipo: "OUTROS"` por `tipo: "OUTRO"` no select do formulário
- [ ] Verificar se `jsonPath("$.tipo").value("Corrente")` em qualquer parte do código → trocar por `"CORRENTE"`

### Checklist de novas funcionalidades sugeridas

- [ ] **Dashboard:** Substituir as 2 chamadas paralelas (`/contas` + `/transacoes`) por 1 chamada a `/api/dashboard`
- [ ] **Sino de notificações no header/sidebar:** `GET /api/alertas` ao carregar, badge com a contagem, `PATCH /api/alertas/{id}/lido` ao abrir/fechar
- [ ] **Página de Insights:** Exibir score financeiro (com barra de progresso e fatores), previsão de gastos por categoria (tabela ou barras) e o relatório mensal da IA
- [ ] **Formulário de transação:** Adicionar campo `recorrencia` (select com NENHUMA / DIARIA / SEMANAL / MENSAL / ANUAL)
- [ ] **Formulário de transação:** Botão "Sugerir categoria" que chama `POST /api/transacoes/classificar` com a descrição digitada
- [ ] **Filtros na listagem de transações:** Usar os query params do `GET /api/transacoes` para filtrar por tipo, período, categoria e conta no servidor (em vez de filtrar no frontend)
- [ ] **Paginação:** Implementar paginação real na listagem de transações usando os campos `totalPages`, `number`, `size` do response
