# Finance Chat Bot: guia da estrutura de pacotes

Este guia explica o que vai em cada pacote, o que **não** pode ir, e em qual etapa do roadmap cada coisa aparece. Ele segue o Documento do Projeto (seção 4). Itens marcados com **(sugestão)** não estão no documento: são caminhos que eu recomendo, e você pode decidir diferente.

Raiz do código: `src/main/java/br/com/guilherme/financebot/`

---

## 1. A ideia central: Arquitetura Hexagonal (Ports and Adapters)

O sistema é dividido em camadas concêntricas. O que está no centro é o mais importante (regras de negócio) e o que está na borda é o mais descartável (Telegram, banco de dados).

```
        ┌──────────────────────────────────────────────┐
        │  adapter (borda: Telegram, scheduler, JPA)   │
        │   ┌──────────────────────────────────────┐   │
        │   │  application (casos de uso, ports)   │   │
        │   │   ┌──────────────────────────────┐   │   │
        │   │   │  domain (regras de negócio)  │   │   │
        │   │   └──────────────────────────────┘   │   │
        │   └──────────────────────────────────────┘   │
        └──────────────────────────────────────────────┘
```

### A regra de dependência (a mais importante do projeto)

As dependências só apontam **para dentro**:

| Pacote | Pode usar | NÃO pode usar |
|---|---|---|
| `domain` | Só Java puro (`java.*`) | Spring, JPA, Telegram, `application`, `adapter` |
| `application` | `domain` e Java puro | Spring (idealmente), JPA, Telegram, `adapter` |
| `adapter` | `application`, `domain`, Spring, JPA, HTTP | (livre, é a borda) |
| `config` | Todos (monta o quebra-cabeça) | (livre) |

Teste prático: se um `import` em `domain` começar com `org.springframework`, `jakarta.persistence` ou algo do Telegram, a arquitetura foi violada. Na Etapa 5 você audita isso.

> Nota sobre `application`: o documento diz que o domínio não conhece Spring. Para `application`, o mais limpo é também evitar Spring, mas é comum aceitar `@Service` e `@Transactional` aqui por praticidade. Decida e documente em um ADR **(sugestão)**.

### Ports e Adapters em uma frase

- **Port** = uma interface Java que descreve uma necessidade ou uma oferta ("preciso salvar uma transação", "ofereço registrar transação").
- **Adapter** = a classe concreta que conecta uma tecnologia a essa interface (JPA salva, Telegram envia).

### Fluxo de uma mensagem ("gastei 45 no mercado")

```
Telegram ──► adapter/in/telegram        (recebe o update via long polling)
               │  grava update_id (idempotência)
               ▼
           application/parser           (Chain: ComandoParser → GastoRegexParser)
               │  gera um comando/DTO interpretado
               ▼
           application/port/in          (RegistrarTransacaoUseCase)
               ▼
           application/service          (implementa o caso de uso)
               │  usa domain (Dinheiro, Transacao)
               │  usa application/categorizacao (Strategy)
               │  chama port/out: TransacaoRepository.salvar(...)
               │  publica TransacaoRegistradaEvent
               ▼
           adapter/out/persistence      (JPA grava no PostgreSQL)
               ...
           application/port/out         (NotificadorPort.enviar(...))
               ▼
           adapter/out/telegram         (TelegramNotificador responde ao usuário)
```

---

## 2. Pacote `domain`: o coração (Java puro)

**Objetivo:** guardar as regras de negócio e os conceitos do mundo financeiro, sem saber nada de tecnologia.

**Regras:**
- Sem anotações de framework (`@Entity`, `@Service`, `@Component`, etc.).
- Sem Lombok **(sugestão)**: como é Java puro e você está aprendendo, escreva construtores e getters à mão ou use `record` onde fizer sentido.
- Testável com JUnit puro, sem subir contexto Spring.

### 2.1 `domain/model`

Entidades e tipos do negócio.

| Classe | O que representa | Detalhes importantes |
|---|---|---|
| `Usuario` | Quem usa o bot | Identificado por `telegramId`; tem `nome` e `fusoHorario` (padrão `America/Sao_Paulo`) |
| `Transacao` | Uma despesa ou receita | Tem tipo, valor (`Dinheiro`), descrição, categoria, `ocorridaEm`, mensagem original e `desfeitaEm` (soft delete) |
| `Categoria` | Agrupador (Alimentação, Transporte...) | Pertence a um usuário; nome único por usuário |
| `Orcamento` | Limite mensal de uma categoria | Valor limite sempre maior que zero |
| `TipoTransacao` | Enum `DESPESA`, `RECEITA` | Simples e sem lógica de framework |

**Não entra aqui:** colunas, `@Table`, `@Id`. Isso é problema do adapter de persistência (que terá entidades JPA **separadas**, com mappers).

**Etapa:** 1 (criação) e 3 (Orcamento ganha comportamento).

### 2.2 `domain/valueobject`

Objetos definidos pelo valor, não por identidade.

| Classe | Função |
|---|---|
| `Dinheiro` | Envolve `BigDecimal` com regras: soma, subtração, comparação, arredondamento (2 casas) e formatação brasileira (`R$ 1.200,50`) |

**Por que existe:** `double` gera erros de arredondamento (0,1 + 0,2 ≠ 0,3). `Dinheiro` garante que ninguém no sistema use `double` para valores monetários. Deve ser **imutável**: toda operação devolve um novo objeto.

**Dica de TDD:** é a primeira coisa que você implementa na Etapa 1. Teste criação, soma, subtração, comparação, arredondamento e formatação antes de escrever a classe.

### 2.3 `domain/service`

Regras de negócio que não pertencem a uma entidade só (serviços de domínio).

| Classe | Função |
|---|---|
| `CalculadoraOrcamento` | Dado um orçamento e as transações do mês, calcula o percentual consumido e diz se cruzou 80% ou 100%; **ignora transações desfeitas** |

**Diferença para `application/service`:** o serviço de domínio só calcula e decide (lógica pura, sem repositório). O serviço de aplicação orquestra (busca dados, chama o domínio, salva, notifica).

**Etapa:** 3.

---

## 3. Pacote `application`: os casos de uso

**Objetivo:** orquestrar o que o sistema faz. Cada caso de uso responde a "o que o usuário quer fazer?". Conhece o `domain`, mas não conhece Telegram nem banco.

### 3.1 `application/port/in` (o que o app oferece)

Interfaces que descrevem as **ações** disponíveis. Quem chama (o adapter do Telegram, o scheduler) usa essas interfaces.

| Interface | Ação |
|---|---|
| `RegistrarTransacaoUseCase` | Registrar despesa ou receita |
| `DesfazerUltimaTransacaoUseCase` | Desfazer a última transação (soft delete) |
| `GerarResumoUseCase` | Gerar resumo do mês (totais, saldo, por categoria) |
| `DefinirOrcamentoUseCase` | Definir limite de uma categoria |

Cada interface normalmente tem **um único método**. Os parâmetros costumam ser pequenos objetos de comando (`record`) **(sugestão)**.

**Etapas:** 1 (Registrar), 3 (Desfazer, Definir), 4 (Resumo).

### 3.2 `application/port/out` (o que o app precisa)

Interfaces que descrevem as **necessidades** do app em relação ao mundo externo. Quem implementa são os adapters `out`.

| Interface | Necessidade |
|---|---|
| `TransacaoRepository` | Salvar e buscar transações (por usuário e período, última não desfeita) |
| `OrcamentoRepository` | Salvar e buscar orçamentos |
| `NotificadorPort` | Enviar mensagem ao usuário (sem saber que é Telegram) |

Outras portas vão surgir conforme a necessidade, por exemplo repositórios de usuário, categoria, regras, updates processados e alertas enviados **(sugestão)**. Todas seguem o mesmo padrão.

**Por que isso importa:** nos testes você troca `NotificadorPort` por uma versão falsa que guarda as mensagens em uma lista. Assim testa o fluxo inteiro sem Telegram e sem banco.

### 3.3 `application/parser` (padrão Chain of Responsibility)

Interpreta o texto que o usuário mandou.

| Classe | Função |
|---|---|
| `InterpretadorMensagem` | Ponto de entrada: recebe o texto e passa pela cadeia |
| `ComandoParser` | Reconhece comandos que começam com `/` (`/hoje`, `/resumo`...) |
| `GastoRegexParser` | Reconhece frases de gasto/receita com regex ("gastei 45 no mercado", "uber 23,50", "R$ 1.200 aluguel") |

**Como funciona a cadeia:** cada parser tenta tratar a mensagem. Se não reconhece, passa para o próximo. Se nenhum reconhece ("bom dia"), o resultado é "não reconhecido" e o bot responde com ajuda. No futuro entra um `LlmParser` no final da cadeia, sem mexer nos outros.

**Cuidados:** formato brasileiro (`1.200,50`: ponto de milhar, vírgula decimal), palavras como "ontem" e acentos. Escreva a tabela da seção 7 do documento como testes parametrizados **antes** de implementar.

**Etapa:** 1.

### 3.4 `application/categorizacao` (padrão Strategy)

Decide a categoria de uma transação.

| Classe | Função |
|---|---|
| `CategorizadorStrategy` | Interface comum: recebe descrição e usuário, devolve categoria (ou vazio) |
| `RegraDoUsuarioStrategy` | Consulta as regras aprendidas (`regra_categoria`) |
| `DicionarioPadraoStrategy` | Dicionário embutido (mercado, uber, farmácia, aluguel...) |

**Ordem de tentativa:** regra do usuário → dicionário padrão → "Sem categoria" (o bot pergunta por botões inline). A escolha do usuário vira uma nova regra, então o bot aprende com o uso.

**Etapa:** 2.

### 3.5 `application/service`

Implementações dos casos de uso (`port/in`). É onde a orquestração acontece.

Exemplo de `RegistrarTransacaoService`: recebe o comando, categoriza, cria a `Transacao`, salva via `TransacaoRepository`, publica o evento `TransacaoRegistradaEvent` e devolve o resultado para responder ao usuário.

**Onde ficam os eventos:** o `TransacaoRegistradaEvent` (Observer) pode ficar em `domain` ou `application` e ser publicado por meio de uma porta **(sugestão)**, para o `application` não depender diretamente do Spring. O listener que verifica o orçamento e usa `alerta_enviado` também fica aqui.

**Etapas:** 1 a 4.

---

## 4. Pacote `adapter`: a borda do sistema

**Objetivo:** traduzir entre o mundo externo e o núcleo. Aqui é permitido usar Spring, JPA, HTTP, JSON.

### 4.1 `adapter/in` (quem chama o app)

**`adapter/in/telegram`**
- Faz o **long polling** (`getUpdates`) e recebe os updates.
- Aplica a **idempotência**: grava `update_id` em `update_processado` antes de processar e ignora se já existir.
- Cria o usuário no primeiro contato (`telegram_id`).
- Chama o `InterpretadorMensagem` e os use cases.
- Trata `callback_query` dos botões inline (Etapa 2).
- Conteúdo típico: cliente da Bot API (`RestClient` ou biblioteca TelegramBots), DTOs dos updates, o loop de polling, o "controller" do Telegram.
- Mais tarde, a versão webhook entra aqui como alternativa ao polling.

**`adapter/in/scheduler`**
- Tarefas agendadas com `@Scheduled`: relatório mensal no dia 1, lembretes.
- Deve calcular "dia 1" e o mês anterior **no fuso de cada usuário**, evitar envio duplicado e usar um `Clock` injetável para ser testável.

**Etapas:** 0 (pong), 1, 2, 4.

### 4.2 `adapter/out` (o que o app chama)

**`adapter/out/persistence`**
- **Entidades JPA** (`@Entity`), separadas das classes de `domain/model`.
- **Mappers** que convertem entidade JPA ↔ modelo de domínio.
- Interfaces Spring Data JPA e as classes que implementam `TransacaoRepository`, `OrcamentoRepository` etc.
- Aqui, e só aqui, o Lombok é aceitável **(sugestão)**.

**`adapter/out/telegram`**
- `TelegramNotificador`: implementa `NotificadorPort` chamando a Bot API para enviar mensagens (e teclados inline).

**Etapas:** 1 em diante.

---

## 5. Pacote `config`

**Objetivo:** montar a aplicação. Beans, propriedades e segurança.

Exemplos de conteúdo:
- Classes `@Configuration` que criam os beans (por exemplo, ligar use cases às suas implementações).
- `@ConfigurationProperties` para ler token do bot, intervalo de polling etc. de variáveis de ambiente.
- Bean de `Clock` (permite testar agendamento).
- Habilitar `@EnableScheduling`.

Os arquivos `application.yml` e as migrations do Flyway **não** ficam neste pacote: ficam em `src/main/resources` (`application.yml` e `db/migration/V1__...sql`).

**Etapa:** 0 em diante.

---

## 6. Padrões de projeto: onde cada um aparece

| Padrão | Onde | Para quê |
|---|---|---|
| Chain of Responsibility | `application/parser` | Cada parser tenta tratar a mensagem; fácil adicionar um `LlmParser` depois |
| Strategy | `application/categorizacao` | Categorizadores intercambiáveis |
| Observer | `TransacaoRegistradaEvent` + listener | Verificar orçamento sem acoplar os serviços |
| Port/Adapter | `port/in`, `port/out`, `adapter/*` | Telegram e banco são detalhes de borda |
