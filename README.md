<div align="center">

# Finance Chat Bot

### Suas finanças pessoais, resolvidas numa conversa.

*Mande uma mensagem como você mandaria para um amigo. O bot anota, organiza e avisa quando o orçamento apertar.*

![Telegram](https://img.shields.io/badge/Canal-Telegram-26A5E4?style=for-the-badge&logo=telegram&logoColor=white)
![Status](https://img.shields.io/badge/Status-em%20desenvolvimento-orange?style=for-the-badge)
![Custo](https://img.shields.io/badge/Custo-zero-brightgreen?style=for-the-badge)
![Licença](https://img.shields.io/badge/Licen%C3%A7a-MIT-blue?style=for-the-badge)

<img src="docs/images/mockup-registro.svg" alt="Conversa com o Finance Chat Bot registrando gastos" width="320">

</div>

---

## A ideia

Quase todo mundo já tentou controlar os gastos e desistiu. Planilha esquecida, aplicativo cheio de telas, categorias para preencher, botões para clicar. O controle financeiro morre porque **dá trabalho demais registrar**.

O Finance Chat Bot parte de uma pergunta simples: e se registrar um gasto fosse tão fácil quanto mandar uma mensagem?

```
gastei 45 no mercado
```

Pronto. O bot entende, guarda, coloca na categoria certa e responde mostrando como aquele gasto afeta o seu mês. Sem abrir app, sem planilha, sem formulário.

## Para quem é

- Para quem **quer saber para onde o dinheiro vai**, mas não tem paciência para planilhas.
- Para quem já **tentou apps de finanças e abandonou** depois de duas semanas.
- Para quem fala do jeito brasileiro: *"gastei 50 conto"*, *"paguei 89,90 na farmácia ontem"*, *"R$ 1.200 de aluguel"*.
- Para quem prefere **ter os próprios dados** em vez de entregá-los a terceiros.

## O que o bot faz

| | |
|---|---|
| **Anota por texto livre** | Escreva despesas e receitas do seu jeito, o bot entende. |
| **Organiza sozinho** | Coloca cada gasto na categoria certa e **aprende com as suas correções**. |
| **Cuida do orçamento** | Defina um limite por categoria e receba um aviso ao chegar em 80% e em 100%. |
| **Mostra o panorama** | Peça o resumo do mês a qualquer momento. |
| **Chega sem ser chamado** | No dia 1 de cada mês, um relatório completo do mês anterior aparece na conversa. |
| **Perdoa erros** | Registrou errado? Desfaça com um comando, sem perder o histórico. |

## Como vai ficar

> As telas abaixo são ilustrações do resultado esperado. O projeto está em construção.

### Registrar um gasto e ver o impacto na hora

Cada mensagem recebe uma resposta clara. Quando o orçamento da categoria está perto do limite, o bot avisa na própria confirmação.

<div align="center">
<img src="docs/images/mockup-registro.svg" alt="Registro de despesas e receitas por mensagem" width="340">
</div>

### Um bot que aprende com você

Se o bot não souber a categoria, ele pergunta uma vez, com botões. A resposta vira regra, e na próxima vez ele já sabe.

<div align="center">
<img src="docs/images/mockup-categoria.svg" alt="Bot perguntando a categoria com botões e aprendendo a resposta" width="340">
</div>

### O relatório que chega sozinho

Todo dia 1, você recebe o retrato do mês: quanto entrou, quanto saiu, onde o dinheiro foi e como ficou em relação ao orçamento.

<div align="center">
<img src="docs/images/mockup-relatorio.svg" alt="Relatório mensal automático e comando de resumo" width="340">
</div>

## Por que isso importa

**Consistência vence perfeição.** O melhor método de controle financeiro é o que você realmente usa. Reduzindo o registro a uma frase, o hábito deixa de pesar.

**Saber é o primeiro passo.** Quem enxerga quanto gasta com delivery, transporte ou assinaturas consegue decidir o que mudar. O bot transforma gastos soltos em informação que você consegue usar.

**Avisar antes, não depois.** Descobrir no fim do mês que o orçamento estourou não ajuda. O alerta em 80% chega enquanto ainda dá tempo de ajustar.

**Feito para o Brasil.** Entende "conto", PIX, valores como `1.200,50` e o jeito brasileiro de escrever.

**Seus dados, seu controle.** A proposta é que cada pessoa possa rodar o próprio bot, com as próprias informações sob o próprio controle.

## Tecnologias

<div align="center">

<img src="https://skillicons.dev/icons?i=java,spring,postgres,docker,githubactions,maven,git,idea&perline=8" alt="Java, Spring, PostgreSQL, Docker, GitHub Actions, Maven, Git e IntelliJ IDEA">

<br><br>

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![Telegram](https://img.shields.io/badge/Telegram-26A5E4?style=flat-square&logo=telegram&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

</div>

> Os detalhes técnicos (arquitetura, decisões e como rodar) serão documentados aqui quando o projeto estiver pronto.

## Status do projeto

Em desenvolvimento, por etapas:

- [ ] Bot respondendo no Telegram
- [ ] Registro de gastos e receitas por mensagem
- [ ] Categorias automáticas que aprendem com o uso
- [ ] Orçamento com alertas
- [ ] Resumo e relatório mensal automático
- [ ] Polimento e instruções para qualquer pessoa rodar o próprio bot

## Ideias para o futuro

Gastos recorrentes (aluguel, assinaturas), importação de extrato bancário, relatório em PDF com gráficos, entendimento de mensagens ainda mais livres com IA e suporte a outros canais, como o WhatsApp.

## Autor

Feito por **Guilherme Paiva Alves**  
GitHub: [@Guui360gpa](https://github.com/Guui360gpa)

## Licença

Este projeto será distribuído sob a licença MIT.
