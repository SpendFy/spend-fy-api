# SpendFy API — Changelog para o Frontend

> **Data:** Março 2026
> **Branch:** `feature/update-system`
> **Base URL:** `http://localhost:8080/api`
> **Auth:** Todas as rotas (exceto `/auth/**`) exigem header `Authorization: Bearer <token>`

---

## ⚠️ BREAKING CHANGES — Leia primeiro

Estas alterações **quebram** integrações existentes e precisam de correção imediata no frontend.

---

### 1. `GET /api/transacoes` — Agora retorna objeto paginado

O endpoint que antes retornava um array simples agora retorna um objeto de paginação do Spring.

**Antes:**
```json
[
  { "id": 1, "tipo": "DESPESA", "status": "CONFIRMADA", ... },
  { "id": 2, "tipo": "RECEITA", "status": "CONFIRMADA", ... }
]
```

**Agora:**
```json
{
  "content": [
    { "id": 1, "tipo": "DESPESA", "status": "CONFIRMADA", ... },
    { "id": 2, "tipo": "RECEITA", "status": "CONFIRMADA", ... }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false,
  "empty": false
}
```

**Parâmetros de paginação (todos opcionais):**
| Param | Tipo | Padrão | Descrição |
|---|---|---|---|
| `page` | int | `0` | Número da página (começa em 0) |
| `size` | int | `20` | Itens por página |
| `sort` | string | `data` | Campo de ordenação |
| `sort` | string | `data,desc` | Ordenação decrescente por data |

**O que precisa mudar no frontend:**
- Todo lugar que faz `GET /transacoes` e trata a resposta como array → trocar `.data` por `.data.content`
- Usar `.data.totalPages`, `.data.number` e `.data.size` para controlar paginação real

**Exemplo paginado:**
```
GET /api/transacoes?page=0&size=10&sort=data,desc
```

---

### 2. `TransacaoResponse` — Campos novos

A resposta de transação agora inclui os campos `recorrencia` e `dataProximaOcorrencia`:

**Antes:**
```json
{
  "id": 1,
  "tipo": "DESPESA",
  "data": "2026-03-24",
  "valor": 150.00,
  "descricao": "Academia",
  "observacao": "Mensalidade",
  "status": "CONFIRMADA",
  "idUsuario": 1,
  "idConta": 1,
  "nomeConta": "Nubank",
  "idCategoria": 3,
  "nomeCategoria": "Saúde",
  "dataCadastro": "2026-03-24T10:00:00",
  "dataAtualizacao": "2026-03-24T10:00:00"
}
```

**Agora:**
```json
{
  "id": 1,
  "tipo": "DESPESA",
  "data": "2026-03-24",
  "valor": 150.00,
  "descricao": "Academia",
  "observacao": "Mensalidade",
  "status": "CONFIRMADA",
  "recorrencia": "MENSAL",
  "dataProximaOcorrencia": "2026-04-24",
  "idUsuario": 1,
  "idConta": 1,
  "nomeConta": "Nubank",
  "idCategoria": 3,
  "nomeCategoria": "Saúde",
  "dataCadastro": "2026-03-24T10:00:00",
  "dataAtualizacao": "2026-03-24T10:00:00"
}
```

| Campo novo | Tipo | Pode ser nulo? | Descrição |
|---|---|---|---|
| `recorrencia` | string (enum) | Não | `NENHUMA` se a transação não se repete |
| `dataProximaOcorrencia` | date (`yyyy-MM-dd`) | Sim | `null` se `recorrencia = NENHUMA` |

---

### 3. `OrcamentoResponse` — Campo `percentualUtilizado` substituído

O campo calculado do orçamento mudou de nome e foi expandido.

**Antes:**
```json
{
  "id": 1,
  "valorLimite": 500.00,
  "percentualUtilizado": 64.00,
  ...
}
```

**Agora:**
```json
{
  "id": 1,
  "valorLimite": 500.00,
  "valorGasto": 320.00,
  "valorRestante": 180.00,
  "dataInicio": "2026-03-01",
  "dataFim": "2026-03-31",
  "idUsuario": 1,
  "idCategoria": 2,
  "nomeCategoria": "Alimentação",
  "dataCadastro": "2026-03-01T00:00:00",
  "dataAtualizacao": "2026-03-24T10:00:00"
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `valorGasto` | BigDecimal | Total de despesas na categoria dentro do período do orçamento |
| `valorRestante` | BigDecimal | `valorLimite - valorGasto` (negativo = orçamento estourado) |

> ⚠️ O campo `percentualUtilizado` **não existe mais**. Para calcular o percentual no frontend: `(valorGasto / valorLimite) * 100`.

**Onde impacta:**
- `GET /api/orcamentos`
- `GET /api/orcamentos/{id}`
- O objeto `orcamentosAtivos` retornado dentro de `GET /api/dashboard`

---

## ✅ ALTERAÇÕES EM ENDPOINTS EXISTENTES

---

### `GET /api/transacoes` — Filtros avançados

Além da paginação, aceita filtros como query params. Todos opcionais e combináveis entre si.

| Param | Tipo | Exemplo | Descrição |
|---|---|---|---|
| `tipo` | enum | `DESPESA` | Filtra por tipo (`DESPESA` ou `RECEITA`) |
| `status` | enum | `CONFIRMADA` | Filtra por status (`CONFIRMADA`, `PENDENTE`, `CANCELADA`) |
| `categoriaId` | Long | `3` | Filtra pela categoria específica |
| `contaId` | Long | `1` | Filtra pela conta específica |
| `dataInicio` | date | `2026-01-01` | Transações a partir desta data (formato ISO `yyyy-MM-dd`) |
| `dataFim` | date | `2026-01-31` | Transações até esta data (formato ISO `yyyy-MM-dd`) |
| `page` | int | `0` | Página |
| `size` | int | `20` | Itens por página |
| `sort` | string | `data,desc` | Ordenação |

**Exemplos de uso:**
```
# Despesas do mês de março, 10 por página, mais recentes primeiro
GET /api/transacoes?tipo=DESPESA&dataInicio=2026-03-01&dataFim=2026-03-31&size=10&page=0&sort=data,desc

# Todas as transações da conta 1, pendentes
GET /api/transacoes?contaId=1&status=PENDENTE

# Transações da categoria "Alimentação" (id=2) do último mês
GET /api/transacoes?categoriaId=2&dataInicio=2026-02-01&dataFim=2026-02-28
```

---

### `POST /api/transacoes` e `PUT /api/transacoes/{id}` — Campo recorrência

O body agora aceita o campo opcional `recorrencia`. Se omitido, assume `NENHUMA`.

**Body completo:**
```json
{
  "tipo": "DESPESA",
  "data": "2026-03-24",
  "valor": 150.00,
  "descricao": "Academia",
  "observacao": "Mensalidade mensal",
  "status": "CONFIRMADA",
  "recorrencia": "MENSAL",
  "idConta": 1,
  "idCategoria": 3
}
```

**Valores aceitos para `recorrencia`:**
| Valor | Descrição | Comportamento automático |
|---|---|---|
| `NENHUMA` | Não recorrente (padrão) | Nenhum |
| `DIARIA` | Repete todo dia | Nova transação criada todos os dias |
| `SEMANAL` | Repete toda semana | Nova transação criada toda semana |
| `MENSAL` | Repete todo mês | Nova transação criada todo mês |
| `ANUAL` | Repete todo ano | Nova transação criada todo ano |

> Quando `recorrencia != NENHUMA`, o campo `dataProximaOcorrencia` é preenchido automaticamente pelo backend e o servidor criará novas transações nos períodos subsequentes (ver seção de comportamentos automáticos).

**Campos obrigatórios:**

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| `tipo` | `DESPESA` \| `RECEITA` | ✅ | — |
| `data` | date `yyyy-MM-dd` | ✅ | — |
| `valor` | decimal | ✅ | Mínimo `0.01` |
| `status` | `CONFIRMADA` \| `PENDENTE` \| `CANCELADA` | ✅ | — |
| `idConta` | Long | ✅ | Conta deve pertencer ao usuário |
| `idCategoria` | Long | ✅ | Categoria deve pertencer ao usuário |
| `descricao` | string | ❌ | Máximo 100 caracteres |
| `observacao` | string | ❌ | Máximo 255 caracteres |
| `recorrencia` | enum | ❌ | Padrão: `NENHUMA` |

**Validação de saldo:** Se `tipo = DESPESA`, o backend verifica automaticamente se há saldo suficiente na conta. Se não houver, retorna `400 Bad Request`:
```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Saldo insuficiente na conta Nubank. Saldo disponível: R$ 230.00",
  "timestamp": "2026-03-24T10:00:00"
}
```

---

### `DELETE /api/transacoes/{id}` — Hard delete

A transação é **permanentemente removida** do banco de dados. Resposta: `204 No Content`.

---

### `GET /api/contas` e `GET /api/contas/{id}` — Novo campo `saldoAtual`

O campo `saldoAtual` é calculado em tempo real pelo backend somando o saldo inicial com todas as receitas e subtraindo todas as despesas daquela conta.

**Response:**
```json
{
  "id": 1,
  "nome": "Nubank",
  "tipo": "Corrente",
  "saldoInicial": 1000.00,
  "saldoAtual": 750.50,
  "idUsuario": 1,
  "dataCadastro": "2026-01-15T09:00:00",
  "dataAtualizacao": "2026-03-24T10:00:00"
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `saldoAtual` | BigDecimal | Calculado: `saldoInicial + receitas - despesas` da conta |
| `tipo` | string | Texto livre (ex: `"Corrente"`, `"Poupança"`) — **não é enum** |

> `saldoAtual` reflete o estado em tempo real — toda vez que uma transação é criada, editada ou deletada, o saldo se atualiza na próxima consulta da conta.

**Request para criar/editar conta:**
```json
{
  "nome": "Nubank",
  "tipo": "Corrente",
  "saldoInicial": 1000.00
}
```

| Campo | Tipo | Obrigatório | Validação |
|---|---|---|---|
| `nome` | string | ✅ | Máximo 50 caracteres, único por usuário |
| `tipo` | string | ✅ | Máximo 30 caracteres, texto livre |
| `saldoInicial` | decimal | ✅ | Mínimo `0.00` |

---

## 🆕 NOVOS ENDPOINTS

---

### Dashboard — `GET /api/dashboard`

Visão consolidada das finanças do mês atual em uma única chamada. Ideal para usar na tela inicial — substitui 3 a 4 chamadas separadas.

**Autenticação:** Requerida

**Response completo:**
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
    },
    {
      "idCategoria": 1,
      "nome": "Lazer",
      "cor": "#f59e0b",
      "total": 280.00,
      "percentualDoTotal": 16.01
    }
  ],
  "orcamentosAtivos": [
    {
      "id": 1,
      "valorLimite": 1000.00,
      "valorGasto": 850.00,
      "valorRestante": 150.00,
      "dataInicio": "2026-03-01",
      "dataFim": "2026-03-31",
      "idUsuario": 1,
      "idCategoria": 2,
      "nomeCategoria": "Alimentação",
      "dataCadastro": "2026-03-01T00:00:00",
      "dataAtualizacao": "2026-03-24T10:00:00"
    }
  ]
}
```

**Descrição de cada campo:**

| Campo | Tipo | Descrição |
|---|---|---|
| `saldoTotal` | BigDecimal | Soma do `saldoAtual` de **todas** as contas do usuário |
| `totalReceitasMes` | BigDecimal | Total de receitas com transações no mês calendário atual |
| `totalDespesasMes` | BigDecimal | Total de despesas com transações no mês calendário atual |
| `saldoMes` | BigDecimal | `totalReceitasMes - totalDespesasMes` |
| `topCategorias` | array | Top 5 categorias com maior gasto no mês atual, ordenadas decrescentemente |
| `topCategorias[].total` | BigDecimal | Total gasto nesta categoria no mês |
| `topCategorias[].percentualDoTotal` | BigDecimal | `(total / totalDespesasMes) * 100` |
| `orcamentosAtivos` | array | Orçamentos cujo período inclui a data de hoje |

> O período do mês é calculado automaticamente do 1º ao último dia do mês corrente. Não há parâmetros.

---

### Alertas — `GET /api/alertas`

Retorna todos os alertas **não lidos** do usuário autenticado, do mais recente para o mais antigo.

**Autenticação:** Requerida

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
    "tipo": "ORCAMENTO_ESTOURADO",
    "mensagem": "O orçamento de Transporte foi ultrapassado! Gasto: R$ 420.00 de R$ 400.00",
    "lido": false,
    "idReferencia": 3,
    "criadoEm": "2026-03-23T08:00:00"
  },
  {
    "id": 1,
    "tipo": "SALDO_BAIXO",
    "mensagem": "Saldo baixo na conta \"Carteira\": R$ 45.00",
    "lido": false,
    "idReferencia": 5,
    "criadoEm": "2026-03-22T08:00:00"
  }
]
```

**Tipos de alerta (`tipo`):**

| Valor | Quando dispara | `idReferencia` aponta para |
|---|---|---|
| `ORCAMENTO_80_PERCENT` | ≥ 80% do limite do orçamento consumido | ID do orçamento |
| `ORCAMENTO_ESTOURADO` | ≥ 100% do limite do orçamento consumido | ID do orçamento |
| `SALDO_BAIXO` | Saldo de qualquer conta < R$ 100,00 | ID da conta |
| `DESPESA_INCOMUM` | Reservado para uso futuro | — |

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | Long | ID do alerta |
| `tipo` | string (enum) | Tipo do alerta (ver tabela acima) |
| `mensagem` | string | Mensagem legível para exibir ao usuário |
| `lido` | boolean | Sempre `false` neste endpoint (só retorna não lidos) |
| `idReferencia` | Long | ID do recurso que gerou o alerta (orçamento ou conta) |
| `criadoEm` | datetime | Timestamp de criação (`yyyy-MM-ddTHH:mm:ss`) |

> **Frequência de geração:** Automaticamente todo dia às **08h00** pelo servidor. O frontend não gera alertas — apenas os consulta.
> **Deduplicação:** O mesmo alerta não é gerado duas vezes em 24h para o mesmo recurso.

---

### Alertas — `PATCH /api/alertas/{id}/lido`

Marca um alerta específico como lido. Após isso, ele não aparece mais no `GET /api/alertas`.

**Autenticação:** Requerida
**Path param:** `id` — ID do alerta

**Response:** `200 OK` com o alerta atualizado:
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

**Erros possíveis:**
- `404 Not Found` — Alerta não existe
- `400 Bad Request` — Alerta pertence a outro usuário

> **Sugestão de UX:** Ao clicar em uma notificação ou ao fechar o dropdown de alertas, disparar `PATCH /api/alertas/{id}/lido` para cada alerta visualizado.

---

### Insights — `GET /api/insights/previsao`

Previsão de gastos por categoria para o mês atual, calculada com base na **média ponderada dos últimos 3 meses** (quanto mais recente o mês, maior o peso na média).

**Autenticação:** Requerida

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
| `idCategoria` | Long | ID da categoria |
| `nomeCategoria` | string | Nome da categoria |
| `mediaMensal` | BigDecimal | Média ponderada dos 3 meses anteriores (mês -1 peso 3, mês -2 peso 2, mês -3 peso 1) |
| `previsaoMesAtual` | BigDecimal | Igual à `mediaMensal` (projeção para o mês corrente) |
| `gastoAtualMes` | BigDecimal | Quanto já foi gasto nesta categoria no mês atual |
| `diferenca` | BigDecimal | `gastoAtualMes - previsaoMesAtual` — positivo = acima do previsto, negativo = abaixo |

> Categorias sem nenhum gasto nos últimos 3 meses **não aparecem** na lista.
> A lista é ordenada por `gastoAtualMes` decrescente (categorias com mais gasto primeiro).

---

### Insights — `GET /api/insights/score`

Score financeiro do usuário de **0 a 100** para o mês atual, baseado em 3 critérios:

- **30 pts** — Relação receita/despesa (poupou ≥ 30% = 30 pts; apenas balanço positivo = 15 pts)
- **40 pts** — Aderência a orçamentos (proporcional ao % de orçamentos vigentes respeitados; sem orçamentos = 20 pts padrão)
- **30 pts** — Histórico de saldo positivo (10 pts por mês com receitas > despesas, nos últimos 3 meses)

**Autenticação:** Requerida

**Response:**
```json
{
  "score": 72,
  "classificacao": "BOM",
  "fatoresPositivos": [
    "Você poupou mais de 30% da sua renda este mês",
    "75% dos orçamentos estão dentro do limite",
    "Saldo positivo nos últimos 2 meses"
  ],
  "fatoresNegativos": [
    "Suas despesas superaram suas receitas este mês"
  ]
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `score` | int | Pontuação de 0 a 100 |
| `classificacao` | string | Categoria do score (ver tabela abaixo) |
| `fatoresPositivos` | string[] | Lista de aspectos positivos identificados |
| `fatoresNegativos` | string[] | Lista de pontos de atenção |

**Tabela de classificações:**
| Faixa de pontos | Classificação |
|---|---|
| 80 – 100 | `EXCELENTE` |
| 60 – 79 | `BOM` |
| 40 – 59 | `REGULAR` |
| 0 – 39 | `ATENÇÃO` |

---

### Insights — `GET /api/insights/relatorio-mensal`

Gera um resumo financeiro personalizado do mês usando **Inteligência Artificial (Claude da Anthropic)**. O texto é gerado em português, com tom encorajador, com base nos dados reais do mês.

**Autenticação:** Requerida

**Response:**
```json
{
  "resumo": "Você teve um mês muito positivo! Suas receitas superaram as despesas em 43%, e o saldo positivo já se mantém por 3 meses consecutivos. Fique atento ao orçamento de Alimentação, que ficou 8% acima do planejado.",
  "destaques": [
    "Você poupou mais de 30% da sua renda este mês",
    "Saldo positivo nos últimos 3 meses",
    "O orçamento de Alimentação foi ultrapassado"
  ],
  "totalReceitas": 5000.00,
  "totalDespesas": 1749.25,
  "score": 85
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `resumo` | string | Texto em português gerado pela IA (2-3 frases) |
| `destaques` | string[] | Fatores positivos + negativos do score combinados |
| `totalReceitas` | BigDecimal | Total de receitas do mês atual |
| `totalDespesas` | BigDecimal | Total de despesas do mês atual |
| `score` | int | Score financeiro do mês (0-100) |

> **Configuração necessária no servidor:** A variável de ambiente `ANTHROPIC_API_KEY` precisa estar definida. Se não estiver, o campo `resumo` retornará uma mensagem padrão sem IA mas os demais campos continuam corretos.

---

### Classificação com IA — `POST /api/transacoes/classificar`

Sugere a categoria mais adequada para uma transação com base na descrição, usando IA. As categorias sugeridas são as **categorias reais do usuário** (não categorias genéricas).

**Autenticação:** Requerida

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
  "justificativa": "Categoria sugerida com base na descrição: \"iFood - pedido de pizza\""
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `idCategoria` | Long | ID da categoria sugerida (pertence ao usuário) |
| `nomeCategoria` | string | Nome da categoria |
| `cor` | string | Cor em hex da categoria |
| `justificativa` | string | Texto explicando o motivo da sugestão |

> **Fallback:** Se a IA não reconhecer nenhuma categoria compatível, retorna a primeira categoria cadastrada do usuário (nunca retorna erro por causa da IA).
> **Sugestão de UX:** Chamar este endpoint ao usuário terminar de digitar a descrição (debounce de ~500ms) e pré-selecionar a categoria no formulário. O usuário pode aceitar ou trocar.
> **Configuração necessária no servidor:** `ANTHROPIC_API_KEY` deve estar definida.

---

## 📋 RESUMO COMPLETO DE TODOS OS ENDPOINTS

### Endpoints existentes com comportamento alterado

| Método | Rota | O que mudou |
|---|---|---|
| `GET` | `/api/transacoes` | Resposta paginada + filtros avançados por query param |
| `POST` | `/api/transacoes` | Aceita campo opcional `recorrencia`; valida saldo disponível |
| `PUT` | `/api/transacoes/{id}` | Aceita campo opcional `recorrencia` |
| `GET` | `/api/contas` | Response inclui `saldoAtual` calculado dinamicamente |
| `GET` | `/api/contas/{id}` | Response inclui `saldoAtual` calculado dinamicamente |
| `GET` | `/api/orcamentos` | Response inclui `valorGasto` e `valorRestante` (substituem `percentualUtilizado`) |
| `GET` | `/api/orcamentos/{id}` | Response inclui `valorGasto` e `valorRestante` |

### Novos endpoints

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| `GET` | `/api/dashboard` | ✅ | Dashboard consolidado do mês atual |
| `GET` | `/api/alertas` | ✅ | Lista alertas não lidos |
| `PATCH` | `/api/alertas/{id}/lido` | ✅ | Marca alerta como lido |
| `GET` | `/api/insights/previsao` | ✅ | Previsão de gastos por categoria |
| `GET` | `/api/insights/score` | ✅ | Score financeiro do mês (0-100) |
| `GET` | `/api/insights/relatorio-mensal` | ✅ | Resumo do mês gerado por IA |
| `POST` | `/api/transacoes/classificar` | ✅ | Sugere categoria via IA pela descrição |

### Endpoints inalterados

| Método | Rota |
|---|---|
| `POST` | `/api/auth/login` |
| `POST` | `/api/auth/register` |
| `DELETE` | `/api/transacoes/{id}` |
| `GET` | `/api/transacoes/{id}` |
| `POST` | `/api/contas` |
| `PUT` | `/api/contas/{id}` |
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

---

## 🔁 Comportamentos automáticos do servidor (sem ação do frontend)

### Transações recorrentes

Toda noite à **01h00**, o servidor verifica todas as transações com `recorrencia != NENHUMA` cuja `dataProximaOcorrencia <= hoje` e cria automaticamente novas transações. O campo `dataProximaOcorrencia` avança para o próximo ciclo.

**O frontend não precisa fazer nada** — as novas transações aparecerão normalmente nas listagens. O usuário apenas vê o efeito: mais transações aparecendo com a mesma descrição e valor nos dias corretos.

### Geração de alertas

Todo dia às **08h00**, o servidor verifica automaticamente para todos os usuários:

1. **Orçamentos ativos** — Se o total de despesas na categoria do orçamento atingiu ≥ 80% → gera `ORCAMENTO_80_PERCENT`. Se atingiu ≥ 100% → gera `ORCAMENTO_ESTOURADO`.
2. **Contas** — Se o `saldoAtual` de qualquer conta ficou abaixo de R$ 100,00 → gera `SALDO_BAIXO`.

**Deduplicação:** O mesmo alerta (mesmo tipo + mesmo `idReferencia`) não é gerado mais de uma vez a cada 24h.

---

## 💡 Guia de implementação para o frontend

### ✅ Correções obrigatórias (breaking changes)

- [ ] `GET /api/transacoes` → Substituir `.data` por `.data.content` em todo lugar que lista transações
- [ ] `GET /api/transacoes` → Usar `.data.totalPages` / `.data.number` para paginação real
- [ ] `GET /api/orcamentos` → Substituir `percentualUtilizado` por `(valorGasto / valorLimite * 100)` calculado no frontend
- [ ] `TransacaoResponse` → O campo `recorrencia` agora sempre vem na resposta — garantir que o modelo/tipo aceite o novo campo

### 🆕 Novas funcionalidades sugeridas

- [ ] **Tela inicial — Dashboard:** Substituir múltiplas chamadas por 1 chamada a `GET /api/dashboard`. Exibir cards de saldo total, receitas e despesas do mês, gráfico de top 5 categorias, e lista de orçamentos ativos com barra de progresso.
- [ ] **Badge de notificações no header/sidebar:** Chamar `GET /api/alertas` ao carregar o app. Exibir badge com o número de alertas não lidos. Ao clicar, listar os alertas. Ao fechar ou clicar em um alerta, chamar `PATCH /api/alertas/{id}/lido`.
- [ ] **Página de Insights:** Três seções:
  - Score financeiro (gauge de 0-100, classificação colorida, lista de fatores)
  - Previsão de gastos (tabela ou gráfico de barras: previsto vs. atual por categoria)
  - Relatório mensal da IA (caixa de texto com o resumo gerado)
- [ ] **Formulário de nova transação:**
  - Adicionar campo `recorrencia` (select: Não se repete / Diária / Semanal / Mensal / Anual)
  - Botão "Sugerir categoria" que chama `POST /api/transacoes/classificar` com a descrição e pré-preenche o select de categoria
- [ ] **Filtros na listagem de transações:** Usar os query params do `GET /api/transacoes` para filtrar no servidor (tipo, status, período, categoria, conta) em vez de filtrar arrays no frontend
- [ ] **Paginação na listagem:** Usar `totalPages`, `number`, `size` para renderizar navegação de páginas

### ⚙️ Configuração necessária no servidor

Para as funcionalidades com IA (`/api/insights/relatorio-mensal` e `POST /api/transacoes/classificar`), a variável de ambiente `ANTHROPIC_API_KEY` precisa estar definida com uma chave válida da API da Anthropic. Sem ela, o endpoint de classificação ainda funciona mas pode retornar a primeira categoria cadastrada; o relatório mensal retorna mensagem padrão.

---

## 🔐 Mapeamento de erros HTTP

Todos os endpoints seguem o mesmo padrão de resposta de erro:

```json
{
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Saldo insuficiente na conta Nubank. Saldo disponível: R$ 230.00",
  "timestamp": "2026-03-24T10:00:00"
}
```

| Status | Quando ocorre |
|---|---|
| `400 Bad Request` | Validação falhou (campo obrigatório ausente, valor inválido, saldo insuficiente) |
| `401 Unauthorized` | Token JWT ausente ou expirado |
| `403 Forbidden` | Recurso pertence a outro usuário |
| `404 Not Found` | Recurso não encontrado pelo ID informado |
| `409 Conflict` | Duplicidade (ex: nome de conta já existe) |
| `500 Internal Server Error` | Erro inesperado no servidor |
