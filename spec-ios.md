# SDD — Hora do Remédio

App iOS de lembrete de medicamentos para idosos ativos.

|  |  |
| :---- | :---- |
| **Versão do documento** | 1.1 |
| **Responsável de produto** | Alexandre Siqueira |
| **Status** | rascunho — aguarda revisão da seção 24 |
| **Data** | 26 de julho de 2026 (v1.1: resolvidas 25.3 e 25.7; adicionado o Anexo B com o roteiro de publicação) |
| **Produto** | Hora do Remédio (nome provisório — ver 1.4) |
| **Plataforma** | iOS, exclusivamente |

---

## Como usar este documento

Este é o documento-fonte do produto. Ele existe para ser colado num agente de código (Claude Code) e produzir a v1 do app sem que o agente precise inventar nenhuma decisão de produto, de dados ou de regra de negócio.

Quem consome: **Claude Code**, através do prompt de implementação entregue junto com este arquivo.

**Antes de gerar qualquer linha de código, leia a seção 24 (Assunções declaradas).** São decisões tomadas sem consulta, por serem de baixo impacto. Assunção não revisada vira verdade no minuto em que o agente começa a codar — e desfazer depois custa mais do que revisar agora.

A numeração das seções é estável. O prompt de implementação e todas as conversas futuras vão referenciar "seção 8.2", "regra 11.3". Não renumere.

---

## 0\. Princípios não-negociáveis

Estas são as regras de desempate. Quando houver dúvida de UX, de copy ou de trade-off técnico, resolve-se a favor destes princípios — nesta ordem de prioridade.

| \# | Princípio | O que significa na prática |
| :---- | :---- | :---- |
| 0.1 | **Legível a um braço de distância** | Nenhum texto do app abaixo de 20pt. Rótulos e conteúdo principal em 24pt ou mais. Se um elemento não cabe com fonte grande, o elemento sai — a fonte não diminui. |
| 0.2 | **Um dedo grosso, com pressa** | Toda área tocável tem no mínimo 60×60pt (acima dos 44pt da HIG da Apple). Nada de gestos como caminho único: swipe, long-press e arrastar sempre têm um botão visível equivalente. |
| 0.3 | **Nada some sem aviso, nada acontece em silêncio** | Toda ação destrutiva (excluir remédio, desligar lembrete) pede confirmação em linguagem clara. Toda ação bem-sucedida dá retorno visível na tela. |
| 0.4 | **Funciona sem internet, sempre** | Nenhuma funcionalidade do app pode depender de rede. Se algo só funciona online, não entra no produto. |
| 0.5 | **Simplicidade antes de complexidade prematura** | Na dúvida entre duas modelagens, vale a mais simples de explicar. O app é um lembrete de remédio, não um prontuário. |

**Como o princípio 0.1 se aplica de verdade:** ele exclui alternativas reais. Ele proíbe tabela densa, proíbe rodapé de texto explicativo em fonte menor, proíbe placeholder cinza-claro dentro de campo, e proíbe caber sete remédios na tela. Três cards grandes com rolagem é a resposta correta.

---

## 1\. Visão e problema

### 1.1 O produto em um parágrafo

**Hora do Remédio** é um app iOS para pessoas idosas independentes — que moram sozinhas ou com o cônjuge, cuidam da própria rotina e tomam de dois a oito medicamentos contínuos. O app faz três coisas: guarda a lista dos remédios com horário e frequência, avisa na hora de tomar, e gera um texto pronto para mandar por WhatsApp para um filho ou levar ao médico. Tudo funciona sem internet, e todos os dados ficam no aparelho.

### 1.2 A dor

Quem toma seis remédios em horários diferentes não esquece por falta de memória — esquece porque a informação está espalhada: uma caixa na cozinha, uma receita na gaveta, um horário que o cardiologista mudou mês passado. Quando um filho pergunta "pai, o que o senhor está tomando?", a resposta é uma foto tremida de quatro caixas em cima da mesa. E quando o idoso vai a um médico novo, ele leva a lista errada ou desatualizada.

Os apps de medicamento existentes falham com esse público por duas razões: são desenhados como prontuário clínico (dezenas de campos, jargão, telas densas) e usam a tipografia padrão do iOS, que é pequena demais para presbiopia.

### 1.3 Anti-escopo — o que este produto explicitamente **não** é

| Não é | Por quê |
| :---- | :---- |
| Um app de saúde/prontuário | Não registra pressão, glicemia, peso, sintomas, consultas ou exames. |
| Um app de adesão/compliance | **Não registra se a pessoa tomou o remédio.** Decisão do usuário (ver 3.2 e 24.1). Lembra e lista — não cobra, não pontua, não faz gráfico de aderência. |
| Um app de cuidador remoto | Não tem conta, não tem login, não sincroniza, não avisa o filho se o pai não tomou. O compartilhamento é manual e voluntário, feito pelo próprio idoso. |
| Uma fonte de informação médica | Não sugere dose, não avisa de interação medicamentosa, não consulta bula, não valida se o remédio existe. O app repete o que o usuário digitou. |
| Um app de farmácia | Não compra, não compara preço, não controla estoque nem avisa que a cartela está acabando (ver 28.3). |
| Multiplataforma | iOS apenas. Nenhuma decisão de arquitetura é tomada para preservar portabilidade Android. |

### 1.4 Nome do produto

Nome de trabalho e escolha atual: **Hora do Remédio**.

Motivo: autoexplicativo, fácil de falar em português, e um idoso entende o ícone na tela inicial sem ninguém explicar. Cabe embaixo do ícone do iOS em duas linhas.

Alternativas consideradas, caso o nome esteja ocupado na App Store ou você mude de ideia:

| Nome | Tom | Observação |
| :---- | :---- | :---- |
| Meus Remédios | Descritivo, posse | Muito fácil de achar na busca da App Store; alto risco de já existir |
| Minha Receita | Descritivo | Casa bem com o OCR de receita da v2 |
| Toma Certo | Coloquial, brasileiro | Curto, cabe no ícone; leve tom de cobrança |
| Na Hora Certa | Afetivo | Genérico fora do contexto |
| Cuida | Curto, afetivo | Precisa de subtítulo; menos literal |
| Remedinho | Carinhoso | Pode soar infantilizante para o público — cuidado |
| Caderneta | Familiar, analógico | Boa metáfora (a caderneta que fica na gaveta) |
| Bem Lembrado | Afetivo | Positivo, não cobra |

**Nome de exibição no iOS:** "Hora do Remédio". **Bundle identifier:** `com.dablab.horadoremedio` — segue o padrão real dos seus outros apps na mesma conta Apple Developer (`com.dablab.gearaapp`, `com.dablab.bemviver`, `com.dablab.tadandocerto`, conforme o roteiro de publicação da seção Anexo B), substituindo o palpite `br.com.dablab...` de uma versão anterior deste documento.

---

## 2\. Personas e cenários de uso

### 2.1 Persona primária — o idoso ativo

Homem ou mulher de 68 a 82 anos. Mora sozinho ou com o cônjuge. Usa iPhone há anos — sabe WhatsApp, sabe câmera, sabe ligar. **Não** sabe: gestos escondidos, menus de três pontinhos, arrastar para o lado, "puxar para atualizar". Nunca leu um tutorial de app na vida e não vai ler o seu.

Características que definem o desenho:

- **Presbiopia.** Lê sem óculos de longe, mas o celular ele afasta. Fonte pequena não é desconforto — é ilegível.  
- **Precisão motora reduzida.** Toque impreciso, às vezes dois toques sem querer. Botão pequeno vira erro, erro vira frustração, frustração vira app apagado.  
- **Baixa tolerância a erro.** Se ele apagar um remédio sem querer e não conseguir desfazer, ele para de mexer no app inteiro por medo de "estragar".  
- **Não pede ajuda na hora.** Ele tenta, não consegue, e desiste em silêncio. O app não tem segunda chance.

### 2.2 O momento mais difícil de uso — e por que ele define a arquitetura

**O cadastro do primeiro remédio, no dia em que o filho instalou o app e foi embora.**

É aqui que o produto morre ou vive. O idoso está sozinho, com quatro caixas na mesa, e precisa transcrever nome, dose, horário e frequência de cada uma. Se o formulário tiver dez campos, ele cadastra um remédio, se cansa e abandona. Se tiver três campos e um botão grande, ele cadastra os quatro.

Consequência direta na arquitetura: **o formulário de cadastro tem no máximo os campos da seção 8.2, um por tela ou em blocos muito separados, com frequência e horário oferecidos como escolhas prontas em vez de campos livres.** "Todo dia" é um botão, não uma configuração.

### 2.3 Persona secundária — o filho que instala

Tem 35–55 anos, mora em outra cidade ou no mesmo bairro. É ele quem baixa o app, cadastra os primeiros remédios junto com o pai numa visita de domingo, e é ele quem recebe a lista pelo WhatsApp depois.

Ele não é usuário do app — é usuário de **um** fluxo: receber a lista exportada e conseguir ler. Isso significa que o texto exportado (seção 14\) precisa fazer sentido sozinho, fora do app, sem contexto.

### 2.4 Contexto físico de uso

| Momento | Contexto | Implicação de desenho |
| :---- | :---- | :---- |
| Manhã, ao acordar | Sem óculos, luz baixa, na cozinha | Contraste alto, fonte grande, modo claro legível com pouca luz |
| Na hora do lembrete | Celular no bolso ou em outro cômodo | A notificação precisa dizer **o nome do remédio e a dose** no texto — não "você tem 1 lembrete" |
| No consultório médico | Sentado, com pressa, médico esperando | A exportação precisa estar a dois toques da Home |
| Domingo, com o filho | Os dois na mesa, cadastrando junto | Cadastro precisa ser rápido de repetir para vários remédios seguidos |

---

## 3\. Escopo do MVP

### 3.1 Dentro do MVP (v1)

| \# | Item | Seção detalhada |
| :---- | :---- | :---- |
| 1 | Cadastro de remédio: nome, dose, forma, frequência, horários, observação | 8.2 |
| 2 | Tela principal com a lista dos remédios ativos, agrupada por horário | 8.1 |
| 3 | Edição e exclusão (arquivamento) de remédio | 8.3 |
| 4 | Lembrete por notificação local, opcional por remédio | 8.4 |
| 5 | Exportação da lista ativa como texto, via share sheet do iOS | 8.5 |
| 6 | Banco local SQLite, 100% offline | 7 |
| 7 | Tela de ajustes: tamanho de fonte, som do lembrete, sobre o app | 8.6 |
| 8 | Onboarding de duas telas \+ pedido de permissão de notificação em contexto | 8.7 |

### 3.2 Fora do MVP

| Item | Destino | Motivo / o que precisa acontecer antes |
| :---- | :---- | :---- |
| **OCR de receita médica** | **v2 — seção 28.1** | Decisão do usuário. Tecnicamente viável offline (Apple Vision, nativo, reconhece pt-BR sem rede — ver 4.6), mas o custo não está no OCR: está no parser de texto de receita e na tela de confirmação. Receita manuscrita tem acerto baixo o suficiente para frustrar. Entra na v2 já especificado. |
| **Registro de tomada ("já tomei")** | **Descartado do MVP — v2 na seção 28.2** | Decisão do usuário: só lembrar. Dobraria o modelo de dados (tabela de ocorrências), adicionaria estado na Home e criaria a pergunta chata "o que fazer com o dia que ele esqueceu de marcar". Ver 24.1. |
| Sincronização / conta / nuvem | Descartado | Viola o princípio 0.4. Reintroduz login, senha, recuperação de senha e LGPD de dados de saúde em servidor — tudo que o app evita por não ter backend. |
| Acesso do cuidador (o filho vê o app do pai) | v2 — seção 28.4 | Exige backend, conta e vínculo entre contas. Reavaliar só depois que a v1 tiver uso real. |
| Controle de estoque da cartela | v3 — seção 28.3 | Exige que o usuário informe quantidade por caixa e mantenha atualizado. Alto atrito para o público. |
| Alerta de interação medicamentosa | Descartado | Exige base de dados clínica licenciada e assume responsabilidade médica. Fora do anti-escopo (1.3). |
| Múltiplos perfis (marido e mulher no mesmo aparelho) | v2 — seção 28.5 | Cada iPhone é de uma pessoa; o casal costuma ter dois aparelhos. Só entra se aparecer demanda real. |
| Android / iPad | Descartado para o MVP | iOS-only por decisão do usuário. iPad roda em modo compatibilidade, sem layout dedicado. |
| Widget de tela inicial | v2 — seção 28.6 | Alto valor para o público (ver sem abrir o app), mas exige extensão nativa e um segundo alvo de build. |
| Apple Health / HealthKit | Descartado | Não agrega ao caso de uso e adiciona revisão de privacidade da Apple. |
| Modo escuro | Descartado do MVP | Ver 16.7. Modo claro apenas, com contraste alto. |

---

## 4\. Arquitetura

### 4.1 Visão geral

App React Native local-first, distribuído via App Store, **sem backend, sem servidor, sem API**. Todo o estado vive em SQLite dentro do sandbox do aplicativo. Nenhuma requisição de rede é feita pelo app em nenhum momento do MVP.

┌───────────────────────────────────────────────┐

│  iPhone (iOS 16+)                             │

│                                               │

│  ┌─────────────────────────────────────────┐  │

│  │  App (Expo SDK 54 / RN 0.81)            │  │

│  │                                         │  │

│  │  Telas (Expo Router)                    │  │

│  │        ↕                                │  │

│  │  Camada de dados (Drizzle ORM)          │  │

│  │        ↕                                │  │

│  │  expo-sqlite  →  horadoremedio.db       │  │

│  │                                         │  │

│  │  Serviço de lembretes                   │  │

│  │        ↓                                │  │

│  │  expo-notifications → UNUserNotification│  │

│  └─────────────────────────────────────────┘  │

│                                               │

│  Sem rede. Sem servidor. Sem conta.           │

└───────────────────────────────────────────────┘

### 4.2 Stack — versões fixas

| Camada | Escolha | Versão | Por quê | Alternativa descartada |
| :---- | :---- | :---- | :---- | :---- |
| Framework | **Expo SDK 54** | 54.x (**fixar — não usar a mais recente**) | Exigência explícita do usuário. Traz React Native 0.81 e React 19.1, com XCFrameworks pré-compilados para iOS (build local muito mais rápido). | SDK 55+: descartado por instrução do usuário. RN puro (bare): descartado — perderia expo-notifications, expo-sqlite e EAS Build integrados sem ganho nenhum. |
| Runtime RN | React Native | 0.81 (vem com o SDK 54\) | Amarrado ao SDK. | — |
| React | React | 19.1 (vem com o SDK 54\) | Amarrado ao SDK. | — |
| Linguagem | TypeScript | strict: true | O spec depende de tipos que espelham o schema; sem strict o agente de código perde a rede de segurança. | JS puro: descartado. |
| Navegação | **Expo Router** | v6 (vem com o SDK 54\) | Roteamento por arquivos, padrão do SDK 54, menos configuração manual. | React Navigation cru: descartado — mais boilerplate para o mesmo resultado. |
| Banco local | **expo-sqlite** | versão do SDK 54 | SQLite é o padrão de fato para local-first em Expo, roda offline por definição, e é o único que sobrevive a backup/restore do iOS sem trabalho extra. | AsyncStorage: descartado — sem query, sem relação, sem migração; vira JSON gigante. MMKV: descartado — ótimo para chave-valor, ruim para lista relacional. WatermelonDB: descartado — resolve sincronização, e não há o que sincronizar. |
| ORM / camada de dados | **Drizzle ORM** (`drizzle-orm/expo-sqlite`) \+ `drizzle-kit` | atual compatível com SDK 54 | Migrations versionadas e empacotadas no bundle (obrigatório: não há como rodar migration por CLI num aparelho), tipos gerados a partir do schema, e `useLiveQuery` para a Home reagir a mudanças sem gerenciamento manual de estado. | SQL cru: descartado — sem tipos e com migração manual frágil. |
| Notificações | **expo-notifications** | versão do SDK 54 | Único caminho suportado para notificação local agendada em Expo. **Atenção:** exports deprecados foram removidos no SDK 54 — usar a API atual, não exemplos antigos de blog. | Notifee: descartado — exige config nativa adicional sem ganho para lembrete simples. |
| Estado de UI | React state \+ Context | — | O app tem quase nenhum estado global: o banco é a fonte de verdade e `useLiveQuery` propaga mudança. | Redux/Zustand/Jotai: descartados por 0.5. Um Context de preferências basta. |
| Estilo | StyleSheet \+ tokens de tema próprios | — | Sem dependência de framework de estilo; o design system daqui (seção 16\) é pequeno e específico. | NativeWind: descartado — camada a mais para \~15 componentes. |
| Fonte | **Nunito** via `expo-font` (empacotada, não baixada) | — | Arredondada, alegre, ótima legibilidade em corpo grande — casa com as referências visuais (16.1). Empacotada no binário porque o app não usa rede (0.4). | SF Pro (sistema): descartada — correta, porém neutra demais para o tom alegre pedido. |
| Ícones | `@expo/vector-icons` (empacotado no SDK) | — | Já vem no SDK, sem download em runtime. | Ícones remotos/SVG por URL: descartados por 0.4. |
| Build e distribuição | **EAS Build** \+ EAS Submit | — | Build iOS na nuvem sem depender de Mac local configurado. | Xcode local: possível, mas exige Mac com Xcode 16.1+; deixado como alternativa. |

### 4.3 Bibliotecas auxiliares (todas do próprio SDK 54\)

| Pacote | Para quê |
| :---- | :---- |
| `expo-sqlite` | Banco local |
| `expo-notifications` | Agendamento e entrega de lembretes locais |
| `expo-sharing` / API `Share` do RN | Abrir a share sheet do iOS na exportação (8.5) |
| `expo-font` | Carregar a Nunito empacotada |
| `expo-haptics` | Retorno tátil ao salvar e ao excluir (reforça 0.3) |
| `expo-splash-screen` | Segurar a tela inicial até o banco migrar |
| `expo-build-properties` | Fixar `deploymentTarget` iOS (ver 4.5) |
| `expo-localization` | Detectar fuso do aparelho para as regras de virada de dia (11.6) |
| `@react-native-async-storage/async-storage` | **Somente** preferências de UI (tamanho de fonte, onboarding visto). Nunca dado de remédio. |

**Nenhuma biblioteca fora desta lista sem aprovação.** Se o agente de código achar que precisa de outra, ele deve perguntar (ver 25).

### 4.4 Onde o código vive e onde roda

- **Repositório:** Git, repositório novo e privado. Nome sugerido: `hora-do-remedio`. Criação do repositório: ver 27\.  
- **Build:** EAS Build, perfis `development` (dev client), `preview` (TestFlight interno) e `production` (App Store).  
- **Distribuição de teste:** TestFlight. O primeiro testador real é o pai do usuário — teste com o público-alvo desde a primeira build (ver 27.5).  
- **Deploy:** App Store Connect. Não há servidor para publicar.

### 4.5 Compatibilidade de plataforma

| Item | Decisão | Observação |
| :---- | :---- | :---- |
| Plataforma | iOS apenas | `platforms: ["ios"]` no `app.json` |
| iOS mínimo | **16.0** | Fixado via `expo-build-properties` (`ios.deploymentTarget: "16.0"`). Escolhido para (a) já estar pronto para o Apple Vision do OCR da v2 sem nova quebra de compatibilidade e (b) cobrir do iPhone 8 em diante, que é o aparelho realista do público. **Premissa a validar:** confirmar o `deploymentTarget` padrão do SDK 54 antes de fixar — se o padrão já for maior, use o padrão. |
| Orientação | Retrato travado | Simplifica o layout de fonte grande; ninguém consulta remédio deitado. |
| iPad | Sem layout dedicado | Roda em modo compatibilidade. Não é caso de uso. |
| Idioma | pt-BR apenas | Sem i18n no MVP. Strings em português direto no código, centralizadas (ver 15.1). |

### 4.6 Premissas técnicas verificadas (julho de 2026\)

| Premissa | Status | Fonte |
| :---- | :---- | :---- |
| Expo SDK 54 \= React Native 0.81 \+ React 19.1, lançado em 10/09/2025 | ✅ verificado | Changelog Expo (Anexo A) |
| Expo SDK 54 exige Xcode 16.1+ e Node 20.19.4+ | ✅ verificado | Changelog Expo |
| `expo-notifications` removeu exports deprecados no SDK 54 | ✅ verificado — **usar a API atual** | Changelog Expo |
| iOS limita a **64 notificações locais pendentes por app**; o sistema mantém as 64 mais próximas e descarta o resto. Notificação repetitiva conta como **uma** | ✅ verificado — **é a restrição que define a seção 11.4** | Apple Developer Forums / documentação UNUserNotificationCenter |
| Apple Vision (`VNRecognizeTextRequest`) reconhece **pt-BR** e roda **100% no dispositivo, sem rede** | ✅ verificado — habilita o OCR offline da v2 | Apple Developer Forums |
| Drizzle ORM suporta `expo-sqlite` com migrations empacotadas e `useLiveQuery` | ✅ verificado | Documentação Drizzle |

### 4.7 Projeto de referência

Não há projeto de referência a reaproveitar: este é um app novo, isolado, sem backend e sem relação com os outros sistemas do usuário (que são web \+ Supabase). A convenção de idioma da seção 18 é o único elo com o padrão dos demais projetos.

---

## 5\. Perfis de usuário e permissões

**O app não tem perfis, não tem conta e não tem login.** Existe exatamente um usuário: quem segura o aparelho. Não há matriz de permissões, não há RLS, não há primeiro administrador, não há escopo por organização.

Consequências que o agente de código deve tratar como regra:

| Regra | Detalhe |
| :---- | :---- |
| 5.1 | Não implemente tela de login, cadastro, senha ou recuperação de senha. |
| 5.2 | Não implemente autenticação biométrica (Face ID) no MVP. Ver 20.5 e 28.7. |
| 5.3 | O controle de acesso aos dados é o próprio sandbox do iOS \+ o passcode do aparelho. |
| 5.4 | Não existe nenhuma credencial, chave de API ou segredo no projeto. Se algum arquivo `.env` for criado, ele está errado. |

**Seções 6 (Autenticação) e 13 (Contratos de API) não se aplicam** e ficam registradas como não aplicáveis para que a ausência não pareça esquecimento. A seção 13 é substituída pelos contratos dos módulos internos (13-A).

---

## 6\. Autenticação e identidade

**Não aplicável.** Ver seção 5\. Nenhum código de autenticação deve ser escrito.

---

## 7\. Modelo de dados

Banco: **SQLite** local, arquivo `horadoremedio.db`, no diretório de documentos do app.

**Convenção de idioma (obrigatória):** tabelas, colunas, enums, índices e triggers em **português**, `snake_case`. Ver seção 18.1.

### 7.1 Diagrama de relacionamento

  remedios                      horarios

  ─────────                     ─────────

  id (PK)  ─────────────┐       id (PK)

  nome                  └──\<    remedio\_id (FK, cascade)

  dose                          hora\_minuto     ("08:00")

  forma (enum)                  ordem

  frequencia (enum)             UNIQUE (remedio\_id, hora\_minuto)

  intervalo\_dias

  dias\_semana                   notificacoes\_agendadas

  observacao                    ──────────────────────

  lembrete\_ativo                id (PK)

  cor                           horario\_id (FK, cascade)

  arquivado\_em                  identificador\_ios  (UNIQUE)

  criado\_em                     disparo\_em

  atualizado\_em                 criado\_em

  preferencias  (tabela de linha única, id \= 1\)

  ────────────

  id (PK, sempre 1\)

  escala\_fonte

  som\_lembrete

  onboarding\_concluido\_em

  atualizado\_em

Cardinalidades em linguagem de negócio:

- Um remédio tem **um ou mais** horários; um horário pertence a **exatamente um** remédio. Remédio sem horário não existe — o cadastro obriga pelo menos um.  
- Um horário tem **zero ou mais** notificações agendadas; uma notificação agendada pertence a **exatamente um** horário. Zero acontece quando o lembrete está desligado ou quando o horário ficou fora da janela das 64 (regra 11.4).  
- `preferencias` não se relaciona com nada. É uma linha só, criada no seed.

### 7.2 Tabela `remedios`

O remédio que a pessoa toma. Uma linha por medicamento, não por dose.

| Coluna | Tipo | Nulo? | Default | Descrição |
| :---- | :---- | :---- | :---- | :---- |
| `id` | INTEGER PK AUTOINCREMENT | não | — | Identificador interno. |
| `nome` | TEXT | não | — | Nome como a pessoa escreveu. Ex.: "Losartana". 1 a 60 caracteres, sem validação contra base de medicamentos (ver 1.3). |
| `dose` | TEXT | **sim** | NULL | Texto livre curto: "50mg", "1 comprimido", "10 gotas". Nulo quando a pessoa não soube ou não quis informar — o campo é opcional de propósito (2.2). Máx. 40 caracteres. |
| `forma` | TEXT (enum) | não | `'comprimido'` | Forma farmacêutica. Define o ícone do card (16.5). Valores em 7.6. |
| `frequencia` | TEXT (enum) | não | `'diario'` | Padrão de repetição. Valores e semântica em 7.6 e 11.1. |
| `intervalo_dias` | INTEGER | **sim** | NULL | Só preenchido quando `frequencia = 'intervalo_dias'`. Nulo em todas as outras frequências. Domínio: 2 a 30\. |
| `dias_semana` | TEXT | **sim** | NULL | Só preenchido quando `frequencia = 'dias_semana'`. Nulo nas demais. Formato: dígitos de 1 a 7 ordenados, sem separador, onde 1 \= domingo e 7 \= sábado. Ex.: `"246"` \= segunda, quarta e sexta. Ver 11.2. |
| `observacao` | TEXT | **sim** | NULL | Texto livre do usuário: "tomar em jejum", "com leite". Nulo quando não informado. Máx. 200 caracteres. Aparece no card e na exportação. |
| `lembrete_ativo` | INTEGER (0/1) | não | `1` | 1 \= agenda notificações para todos os horários deste remédio; 0 \= o remédio aparece na lista mas não avisa. Ver 8.4. |
| `cor` | TEXT (enum) | não | atribuída no cadastro | Cor do card. Sorteada em rodízio na ordem da paleta (16.3) no momento do cadastro, e editável depois. Existe para identificação visual rápida, não carrega significado clínico — ver 16.4. |
| `arquivado_em` | TEXT (ISO 8601\) | **sim** | NULL | Nulo \= remédio ativo. Preenchido \= arquivado (soft delete, ver 7.5). |
| `criado_em` | TEXT (ISO 8601\) | não | `datetime('now')` | — |
| `atualizado_em` | TEXT (ISO 8601\) | não | `datetime('now')` | Mantido pela trigger `trg_remedios_atualizado_em` (7.7). |

**Constraints e índices**

| Nome | Tipo | Definição |
| :---- | :---- | :---- |
| `ck_remedios_nome_nao_vazio` | CHECK | `length(trim(nome)) BETWEEN 1 AND 60` |
| `ck_remedios_forma` | CHECK | `forma IN (...)` com os valores de 7.6 |
| `ck_remedios_frequencia` | CHECK | `frequencia IN (...)` com os valores de 7.6 |
| `ck_remedios_cor` | CHECK | `cor IN (...)` com os valores de 16.3 |
| `ck_remedios_intervalo_coerente` | CHECK | `(frequencia = 'intervalo_dias' AND intervalo_dias BETWEEN 2 AND 30) OR (frequencia <> 'intervalo_dias' AND intervalo_dias IS NULL)` |
| `ck_remedios_dias_semana_coerente` | CHECK | `(frequencia = 'dias_semana' AND dias_semana GLOB '[1-7]*' AND length(dias_semana) BETWEEN 1 AND 7) OR (frequencia <> 'dias_semana' AND dias_semana IS NULL)` |
| `ck_remedios_lembrete_ativo` | CHECK | `lembrete_ativo IN (0, 1)` |
| `idx_remedios_ativos` | ÍNDICE | `(arquivado_em)` — a Home filtra por `arquivado_em IS NULL` em toda abertura |

Não há unique em `nome`: a pessoa pode legitimamente ter dois cadastros com o mesmo nome e doses diferentes (ex.: "Insulina" de manhã e à noite com doses distintas). Bloquear isso geraria erro incompreensível para o público.

### 7.3 Tabela `horarios`

O horário do dia em que aquele remédio é tomado. Uma linha por horário.

| Coluna | Tipo | Nulo? | Default | Descrição |
| :---- | :---- | :---- | :---- | :---- |
| `id` | INTEGER PK AUTOINCREMENT | não | — | — |
| `remedio_id` | INTEGER FK → `remedios.id` | não | — | `ON DELETE CASCADE`. |
| `hora_minuto` | TEXT | não | — | Horário local no formato `"HH:MM"`, 24 horas, zero à esquerda. Ex.: `"08:00"`, `"19:30"`. Guardado como texto por ser comparável e ordenável em SQLite, e por não carregar fuso — ver 11.6. |
| `ordem` | INTEGER | não | `0` | Ordem de exibição dentro do remédio. Recalculada por horário crescente ao salvar. |
| `criado_em` | TEXT (ISO 8601\) | não | `datetime('now')` | — |

**Constraints e índices**

| Nome | Tipo | Definição |
| :---- | :---- | :---- |
| `uq_horarios_remedio_hora` | UNIQUE | `(remedio_id, hora_minuto)` — impede cadastrar o mesmo horário duas vezes no mesmo remédio |
| `ck_horarios_formato` | CHECK | `hora_minuto GLOB '[0-2][0-9]:[0-5][0-9]'` |
| `idx_horarios_remedio` | ÍNDICE | `(remedio_id)` |
| `idx_horarios_hora` | ÍNDICE | `(hora_minuto)` — a Home agrupa por horário (8.1) |

**Regra de cardinalidade:** todo remédio tem no mínimo 1 e no máximo 6 horários. O máximo é de produto, não técnico: mais de seis tomadas por dia do mesmo remédio é caso clínico fora do público-alvo, e cada horário consome cota das 64 notificações (11.4). Validado na camada de aplicação, não no banco (ver 11.3).

### 7.4 Tabela `notificacoes_agendadas`

Espelho local do que foi realmente agendado no iOS. **Existe por uma razão específica:** o app precisa saber quais identificadores cancelar quando o usuário edita ou arquiva um remédio, e precisa saber o que já está agendado para respeitar a cota de 64 (11.4). Sem esta tabela, a única forma de descobrir seria consultar o iOS a cada operação e reconciliar às cegas.

| Coluna | Tipo | Nulo? | Default | Descrição |
| :---- | :---- | :---- | :---- | :---- |
| `id` | INTEGER PK AUTOINCREMENT | não | — | — |
| `horario_id` | INTEGER FK → `horarios.id` | não | — | `ON DELETE CASCADE`. |
| `identificador_ios` | TEXT | não | — | O identificador devolvido por `expo-notifications` ao agendar. É por ele que o cancelamento acontece. |
| `disparo_em` | TEXT (ISO 8601\) | **sim** | NULL | Quando essa notificação vai disparar. Nulo quando o agendamento é repetitivo (calendário diário), porque não há uma data única — ver 11.4. |
| `criado_em` | TEXT (ISO 8601\) | não | `datetime('now')` | — |

**Constraints e índices**

| Nome | Tipo | Definição |
| :---- | :---- | :---- |
| `uq_notificacoes_identificador` | UNIQUE | `(identificador_ios)` |
| `idx_notificacoes_horario` | ÍNDICE | `(horario_id)` |
| `idx_notificacoes_disparo` | ÍNDICE | `(disparo_em)` |

Esta tabela é **derivada** — se ela for perdida ou ficar inconsistente, a rotina de reconciliação (13-A.3) reconstrói tudo a partir de `remedios` \+ `horarios`. Ela nunca é fonte de verdade sobre o que o usuário quer, só sobre o que o sistema fez.

### 7.5 Tabela `preferencias`

Linha única, `id` sempre 1\. Existe como tabela (e não em AsyncStorage) apenas para o que precisa sobreviver junto do banco em backup/restore.

| Coluna | Tipo | Nulo? | Default | Descrição |
| :---- | :---- | :---- | :---- | :---- |
| `id` | INTEGER PK | não | `1` | Sempre 1\. |
| `escala_fonte` | TEXT (enum) | não | `'grande'` | `'grande'` ou `'gigante'`. Ver 16.2 e 17.3. |
| `som_lembrete` | INTEGER (0/1) | não | `1` | 1 \= notificação com som; 0 \= silenciosa. |
| `onboarding_concluido_em` | TEXT (ISO 8601\) | **sim** | NULL | Nulo \= onboarding ainda não foi visto. |
| `atualizado_em` | TEXT (ISO 8601\) | não | `datetime('now')` | — |

**Constraints**

| Nome | Tipo | Definição |
| :---- | :---- | :---- |
| `ck_preferencias_linha_unica` | CHECK | `id = 1` |
| `ck_preferencias_escala` | CHECK | `escala_fonte IN ('grande', 'gigante')` |
| `ck_preferencias_som` | CHECK | `som_lembrete IN (0, 1)` |

### 7.6 Enums

**`remedios.forma`** — forma farmacêutica; define o ícone do card.

| Valor | Rótulo na UI | Ícone sugerido |
| :---- | :---- | :---- |
| `comprimido` | Comprimido | pílula |
| `capsula` | Cápsula | cápsula |
| `gotas` | Gotas | conta-gotas |
| `xarope` | Xarope | frasco |
| `injecao` | Injeção | seringa |
| `pomada` | Pomada | bisnaga |
| `spray` | Spray / bombinha | spray |
| `outro` | Outro | genérico |

**`remedios.frequencia`** — padrão de repetição. Semântica completa em 11.1.

| Valor | Rótulo na UI | Campos que exige |
| :---- | :---- | :---- |
| `diario` | Todo dia | nenhum |
| `dias_semana` | Alguns dias da semana | `dias_semana` |
| `intervalo_dias` | A cada X dias | `intervalo_dias` |
| `quando_precisar` | Quando precisar | nenhum — **e não gera lembrete** (11.1) |

**`remedios.cor`** — ver paleta em 16.3: `coral`, `azul`, `verde`, `amarelo`, `lilas`, `pessego`.

**`preferencias.escala_fonte`** — `grande` (padrão), `gigante`.

### 7.7 Lógica que vive no banco

Pouca, de propósito (0.5). Toda regra de negócio real mora na aplicação (ver 11, coluna "onde"). O banco cuida apenas de integridade e carimbo de tempo.

| Nome | Tipo | Tabela | Quando dispara | O que faz |
| :---- | :---- | :---- | :---- | :---- |
| `trg_remedios_atualizado_em` | TRIGGER | `remedios` | `AFTER UPDATE` | Grava `datetime('now')` em `atualizado_em`. Evita depender de a aplicação lembrar de preencher. |
| `trg_preferencias_atualizado_em` | TRIGGER | `preferencias` | `AFTER UPDATE` | Idem para `preferencias.atualizado_em`. |
| `PRAGMA foreign_keys = ON` | PRAGMA | — | Na abertura da conexão | **Obrigatório.** SQLite ignora foreign keys por padrão; sem isso o `ON DELETE CASCADE` de `horarios` e `notificacoes_agendadas` não funciona e o banco acumula órfãos silenciosamente. |
| `PRAGMA journal_mode = WAL` | PRAGMA | — | Na abertura da conexão | Leitura e escrita concorrentes sem travar a UI. |

Não há políticas (RLS) — SQLite local não tem esse conceito e não há multiusuário (seção 5).

### 7.8 Estratégia de exclusão

| Entidade | Estratégia | Motivo |
| :---- | :---- | :---- |
| `remedios` | **Soft delete** via `arquivado_em` | Remédio que a pessoa parou de tomar frequentemente volta (tratamento cíclico, médico que retoma). Arquivar permite "Ver remédios que parei" (8.3) e reativar em um toque, em vez de recadastrar tudo — o que, para este público, é a diferença entre usar e não usar (2.1). |
| `horarios` | **Hard delete**, com cascade | Horário não tem valor histórico próprio. Ao editar um remédio, os horários são reescritos. |
| `notificacoes_agendadas` | **Hard delete**, com cascade | Tabela derivada (7.4). |
| `preferencias` | Nunca excluída | Linha única, criada no seed. |

**Exclusão definitiva:** existe apenas em Ajustes → "Apagar todos os meus dados" (8.6), que apaga o arquivo do banco inteiro e cancela todas as notificações. É o mecanismo de direito ao apagamento da seção 20.4. Não existe exclusão definitiva de um remédio individual — arquivar é o caminho, e a UI nunca usa a palavra "excluir" (ver 15.4).

### 7.9 Migrations e seed

- Migrations geradas por `drizzle-kit generate` e **empacotadas no bundle** — não há como rodar migration por CLI num aparelho do usuário.  
- Aplicadas na inicialização do app, antes de qualquer tela, com a splash screen segurando (`expo-splash-screen`). Se a migration falhar, o app mostra a tela de erro da regra 12.4 — nunca uma tela em branco.  
- **Seed:** insere a linha `preferencias` com `id = 1` e os defaults, se ela não existir. Nenhum remédio de exemplo é criado — lista falsa confunde este público mais do que a tela vazia (que tem instrução clara, ver 8.1).  
- Cada migration é versionada em arquivo no repositório e nunca editada depois de publicada.

### 7.10 Trade-off declarado: `dias_semana` como string

`dias_semana` guarda `"246"` em vez de existir uma tabela `remedios_dias_semana`.

**Ganho:** o cadastro inteiro de um remédio vira duas tabelas em vez de três, e a leitura da Home não precisa de mais um join.

**O que isso impede:** consultar "quais remédios são tomados na quarta-feira" com SQL puro fica desajeitado (exige `LIKE`/`instr`). Se algum dia entrar uma tela de calendário semanal (28.6), essa consulta passa a ser feita em memória, sobre a lista já carregada — o que é aceitável porque o volume é de dezenas de linhas, não milhares (17.5).

Decisão consciente sob o princípio 0.5. Se a v2 exigir consulta por dia no banco, normalize aí.

---

## 8\. Épicos e critérios de aceite

Ordem de implementação: **8.0 → 8.1 → 8.2 → 8.3 → 8.4 → 8.5 → 8.6 → 8.7**. Cada épico deve ser validado contra seus critérios antes de começar o próximo.

Critério de aceite aqui é binário: ou o comportamento descrito acontece, ou não acontece. Não há "ficou fluido".

---

### 8.0 Fundação: projeto, banco e tema

**Objetivo:** ter o app abrindo, com banco migrado e o design system disponível, antes de qualquer tela de produto.

**Escopo**

1. Projeto Expo SDK 54, TypeScript strict, Expo Router v6, iOS only, retrato travado.  
2. `expo-build-properties` com `deploymentTarget` iOS conforme 4.5.  
3. Schema Drizzle completo da seção 7, migrations geradas e empacotadas.  
4. Inicialização: abre o banco, aplica `PRAGMA foreign_keys = ON` e `journal_mode = WAL`, roda migrations, roda o seed, e só então libera a splash.  
5. Tokens de tema da seção 16 (cores, tipografia, espaçamento, raio) num único módulo.  
6. Fonte Nunito empacotada e carregada via `expo-font`.  
7. Componentes base: `TextoGrande`, `BotaoPrimario`, `BotaoSecundario`, `Card`, `CampoTexto`, `SeletorOpcao`.

**Critérios de aceite**

- [ ] `npx expo run:ios` abre o app num iPhone com iOS 16 sem erro de build.  
- [ ] Na primeira abertura, o arquivo `horadoremedio.db` existe e contém as 4 tabelas da seção 7 com todas as constraints nomeadas exatamente como especificado.  
- [ ] `SELECT * FROM preferencias` devolve exatamente uma linha, com `id = 1`, `escala_fonte = 'grande'`, `som_lembrete = 1`, `onboarding_concluido_em` nulo.  
- [ ] Inserir um `horario` com `remedio_id` inexistente falha com erro de foreign key (prova que o PRAGMA está ativo).  
- [ ] Excluir um `remedio` remove em cascata seus `horarios` e as `notificacoes_agendadas` correspondentes.  
- [ ] Inserir um remédio com `frequencia = 'diario'` e `intervalo_dias = 5` é rejeitado pela constraint `ck_remedios_intervalo_coerente`.  
- [ ] A splash screen permanece visível até as migrations terminarem; em nenhum momento aparece tela branca ou lista vazia piscando.  
- [ ] Todo texto renderizado usa Nunito, e nenhum estilo do app declara `fontSize` menor que 20\.

---

### 8.1 Tela principal: a lista de remédios

**Objetivo:** a pessoa abre o app e vê, sem rolar e sem interpretar, o que ela toma hoje e a que horas.

**Fluxo**

1. O app abre direto nesta tela (é a rota inicial; não há tela de login nem dashboard intermediário).  
2. Cabeçalho: saudação por período do dia \+ data por extenso. Ex.: "Bom dia" / "Domingo, 26 de julho".  
3. Corpo: os remédios **ativos** (`arquivado_em IS NULL`), **agrupados por horário**, em ordem crescente de horário.  
4. Um remédio com três horários aparece **três vezes**, uma em cada grupo. Isso é deliberado: o modelo mental do idoso é "o que eu tomo às 8h", não "quais remédios eu tenho". Ver 11.7.  
5. Remédios com `frequencia = 'quando_precisar'` **não** entram nos grupos de horário — ficam num bloco separado no fim, com o título "Quando precisar".  
6. Botão flutuante grande e permanente no canto inferior direito: **\+ Adicionar remédio**.  
7. Tocar num card abre o detalhe/edição (8.3).

**Agrupamento por horário — formato do cabeçalho de grupo**

| Faixa | Rótulo do grupo |
| :---- | :---- |
| 00:00–04:59 | Madrugada — `HH:MM` |
| 05:00–11:59 | Manhã — `HH:MM` |
| 12:00–17:59 | Tarde — `HH:MM` |
| 18:00–23:59 | Noite — `HH:MM` |

Ex.: "Manhã — 08:00". O horário aparece sempre, porque é a informação operacional; a faixa existe para dar contexto rápido.

**Conteúdo do card**

| Elemento | Regra |
| :---- | :---- |
| Nome do remédio | Maior elemento do card, `titulo` (28pt) |
| Dose | Abaixo do nome, `corpo` (22pt). Omitido inteiramente se `dose` for nula — sem placeholder, sem traço |
| Ícone da forma | À esquerda, grande, na cor do remédio |
| Observação | Abaixo, `corpo` (22pt), com ícone de aviso. Omitida se nula |
| Faixa/fundo colorido | Na `cor` do remédio (16.3) |
| Indicador de lembrete desligado | Ícone de sino cortado \+ texto "sem aviso" quando `lembrete_ativo = 0` — **exceto** para `quando_precisar`, onde o aviso nunca existiu e o rótulo seria ruído (11.1) |
| Frequência | Só aparece quando **não** é `diario`. Ex.: "Seg, Qua, Sex" ou "A cada 3 dias". Diário não escreve nada — é o caso comum e ocuparia espaço à toa |

**Estados**

| Estado | O que a tela mostra |
| :---- | :---- |
| **Vazio** (nenhum remédio) | Ilustração simples, título "Você ainda não cadastrou nenhum remédio", texto "Toque no botão verde para cadastrar o primeiro", e uma seta apontando para o botão. Sem lista falsa, sem exemplo |
| **Carregando** | A splash cobre a migração inicial (8.0). Depois disso o carregamento é local e instantâneo — não implemente spinner, ele piscaria e confundiria |
| **Só "quando precisar"** | Sem grupos de horário; só o bloco "Quando precisar" |
| **Erro de banco** | Ver regra 12.4 |

**Critérios de aceite**

- [ ] Com 3 remédios cadastrados nos horários 08:00, 08:00 e 20:00, a tela mostra dois grupos: "Manhã — 08:00" com 2 cards e "Noite — 20:00" com 1 card.  
- [ ] Um remédio cadastrado com horários 08:00 e 20:00 aparece uma vez em cada grupo, com o mesmo nome, mesma cor e mesmo ícone.  
- [ ] Um remédio com `frequencia = 'quando_precisar'` não aparece em nenhum grupo de horário e aparece no bloco "Quando precisar".  
- [ ] Um remédio com `arquivado_em` preenchido não aparece em lugar nenhum desta tela.  
- [ ] Cadastrar um remédio novo faz ele aparecer na lista **sem** o usuário precisar recarregar, fechar ou puxar a tela (`useLiveQuery`).  
- [ ] Com zero remédios, aparece o estado vazio descrito, e o botão "+ Adicionar remédio" continua visível e tocável.  
- [ ] Um remédio com `dose` nula renderiza o card sem nenhuma linha vazia ou traço no lugar da dose.  
- [ ] Um remédio com `lembrete_ativo = 0` mostra o ícone de sino cortado com o texto "sem aviso".  
- [ ] O botão "+ Adicionar remédio" mede no mínimo 64×64pt e permanece visível com a lista rolada até o fim.  
- [ ] Nenhum texto desta tela renderiza abaixo de 20pt, verificado com a escala de fonte em `grande` e em `gigante`.

---

### 8.2 Cadastro de remédio

**Objetivo:** cadastrar um remédio completo em menos de um minuto, sozinho, sem ajuda — o momento mais difícil do produto (2.2).

**Fluxo — passo a passo, uma pergunta por bloco**

O formulário é uma tela única com rolagem, dividida em blocos muito separados visualmente. **Não** é um wizard de várias telas (o idoso perde o contexto do "onde estou") nem um formulário denso (ele desiste).

| \# | Bloco | Tipo de entrada | Obrigatório |
| :---- | :---- | :---- | :---- |
| 1 | "Qual o nome do remédio?" | Campo de texto, teclado com autocapitalize de palavras | **Sim** |
| 2 | "Qual a dose?" | Campo de texto curto, com exemplos abaixo ("Ex.: 50mg, 1 comprimido, 10 gotas") | Não |
| 3 | "Como você toma?" | Grade de botões grandes com ícone \+ rótulo, um por valor de `forma` | Sim (padrão: Comprimido) |
| 4 | "Com que frequência?" | 4 botões grandes empilhados, um por valor de `frequencia` | Sim (padrão: Todo dia) |
| 4a | Se "Alguns dias da semana" | 7 botões redondos grandes (D S T Q Q S S), alternáveis | Sim, mín. 1 dia |
| 4b | Se "A cada X dias" | Stepper grande com − e \+, de 2 a 30 | Sim |
| 5 | "A que horas?" | Lista de horários \+ botão "Adicionar horário". Cada horário abre o time picker nativo do iOS. Cada horário tem um botão de remover visível | **Sim, mín. 1** — exceto quando `frequencia = 'quando_precisar'`, quando o bloco some inteiro |
| 6 | "Quer ser lembrado?" | Switch grande com rótulo textual "Sim, me avise" / "Não precisa avisar" | Sim (padrão: ligado) |
| 7 | "Alguma observação?" | Campo de texto multilinha, com exemplos ("Ex.: tomar em jejum") | Não |
| 8 | Cor do card | 6 círculos grandes da paleta (16.3) | Sim (padrão: próxima cor do rodízio) |

Botão **"Salvar remédio"** fixo no rodapé, sempre visível, largura total.

**Regras de comportamento**

- O bloco 5 (horários) **desaparece** quando a frequência é "Quando precisar" — e junto com ele o bloco 6, porque não há o que lembrar (11.1).  
- Ao escolher "Todo dia" pela primeira vez, um horário `08:00` já vem sugerido — reduz o cadastro mínimo a nome \+ salvar.  
- Validação acontece **ao tocar em Salvar**, não enquanto digita. Campo ficando vermelho durante a digitação assusta o público.  
- Erro de validação: o app rola até o primeiro bloco com problema, o destaca, e mostra a mensagem em texto grande **abaixo do bloco** (nunca um alert modal, nunca um toast que some).  
- Ao salvar com sucesso: háptica de sucesso, a tela fecha, e a Home mostra o remédio novo já no lugar certo.  
- Botão "Cancelar" no topo. Se houver qualquer coisa preenchida, pede confirmação: "Sair sem salvar? O que você preencheu será perdido." (0.3)

**Critérios de aceite**

- [ ] Preencher apenas o nome e tocar em Salvar cria um remédio com `frequencia = 'diario'`, `forma = 'comprimido'`, um horário `08:00`, `lembrete_ativo = 1`, `dose` nula e `observacao` nula.  
- [ ] Tocar em Salvar com o nome vazio não cria nada, rola até o bloco 1, o destaca e mostra "Escreva o nome do remédio" abaixo dele.  
- [ ] Escolher "Quando precisar" faz os blocos 5 e 6 sumirem da tela, e o remédio é salvo com zero horários e `lembrete_ativo = 0`.  
- [ ] Escolher "Alguns dias da semana" e marcar segunda, quarta e sexta grava `dias_semana = '246'` e `intervalo_dias = NULL`.  
- [ ] Escolher "A cada X dias" com 3 grava `intervalo_dias = 3` e `dias_semana = NULL`.  
- [ ] Tentar adicionar um horário que já existe naquele remédio mostra "Esse horário já está na lista" e não duplica.  
- [ ] Adicionar 6 horários funciona; a tentativa de adicionar o 7º mostra "São no máximo 6 horários por remédio" e o botão "Adicionar horário" fica desabilitado (regra 11.3).  
- [ ] Salvar com `lembrete_ativo = 1` agenda as notificações do épico 8.4 e grava as linhas correspondentes em `notificacoes_agendadas`.  
- [ ] Todo campo tocável do formulário mede no mínimo 60pt de altura.  
- [ ] Com a escala de fonte em `gigante`, nenhum rótulo é cortado com reticências nem sobrepõe outro elemento.

---

### 8.3 Detalhe, edição e arquivamento

**Objetivo:** mudar o horário que o médico alterou, ou parar um remédio, sem medo de estragar nada.

**Fluxo**

1. Tocar num card da Home abre o **detalhe**: mesma informação do card, em tela cheia e ainda maior, com todos os horários do remédio juntos (não só o do grupo em que ele foi tocado).  
2. Três botões grandes: **Editar**, **Parar de tomar**, **Voltar**.  
3. **Editar** abre o mesmo formulário de 8.2, preenchido. O botão do rodapé diz "Salvar alterações".  
4. **Parar de tomar** pede confirmação: título "Parar de tomar Losartana?", texto "Ele sai da sua lista e os avisos param. Você pode voltar a tomar depois." Botões: "Sim, parar" e "Cancelar".  
5. Confirmado: grava `arquivado_em`, cancela todas as notificações do remédio, volta para a Home, e mostra por 5 segundos uma faixa com "Losartana foi arquivado" \+ botão **Desfazer**.  
6. Ajustes → "Remédios que parei" lista os arquivados, cada um com o botão **"Voltar a tomar"**, que limpa `arquivado_em` e reagenda os lembretes.

**Regras de edição**

- Editar horários **reescreve** as linhas de `horarios` daquele remédio e **reagenda do zero** as notificações (cancela as antigas por `identificador_ios`, agenda as novas). Nunca edita notificação existente — cancelar e recriar é a única operação confiável.  
- Mudar `frequencia` para `quando_precisar` apaga todos os horários e desliga o lembrete, com confirmação: "Esse remédio vai sair dos horários e os avisos vão parar. Continuar?"

**Critérios de aceite**

- [ ] Tocar num card na Home abre o detalhe daquele remédio com todos os seus horários listados.  
- [ ] Editar o horário de 08:00 para 09:00 e salvar: a Home passa a mostrar o card no grupo "Manhã — 09:00", `notificacoes_agendadas` não tem mais nenhuma linha com o identificador antigo, e há uma linha nova.  
- [ ] "Parar de tomar" preenche `arquivado_em`, remove o card da Home e cancela as notificações do remédio — nenhuma notificação dele dispara depois disso.  
- [ ] Tocar em "Desfazer" na faixa dentro dos 5 segundos limpa `arquivado_em`, o card volta à Home e as notificações são reagendadas.  
- [ ] Deixar a faixa sumir sozinha mantém o remédio arquivado.  
- [ ] "Remédios que parei" lista exatamente os remédios com `arquivado_em` preenchido, e "Voltar a tomar" os traz de volta com os horários originais intactos.  
- [ ] Nenhuma tela do app usa a palavra "excluir", "apagar" ou "deletar" para essa operação (regra 15.4).  
- [ ] Não existe caminho de swipe-para-excluir sem botão equivalente na tela (princípio 0.2).

---

### 8.4 Lembretes

**Objetivo:** avisar na hora certa, com o nome do remédio na notificação, e nunca avisar de um remédio que a pessoa parou de tomar.

Este é o épico com a restrição de plataforma mais dura do produto — leia a regra **11.4** (cota de 64\) antes de implementar.

**Fluxo de permissão**

1. O app **não** pede permissão de notificação na abertura. Pede no momento em que a pessoa salva o primeiro remédio com lembrete ligado — quando o motivo é óbvio.  
2. Antes do diálogo do iOS, uma tela própria explica: "Para avisar na hora do remédio, o app precisa da sua permissão. Na próxima tela, toque em Permitir."  
3. Se a pessoa negar: o remédio é salvo normalmente, com `lembrete_ativo` mantido em 1 no banco, e a Home mostra uma faixa no topo: "Os avisos estão desligados no iPhone. Toque aqui para ligar" → abre os Ajustes do iOS.

**Regra de agendamento**

Ver 11.1 e 11.4. Em resumo:

| Frequência | Como é agendado no iOS |
| :---- | :---- |
| `diario` | Um trigger de calendário **repetitivo** por horário (`hour`, `minute`, `repeats: true`). Conta como **1** na cota de 64 |
| `dias_semana` | Um trigger de calendário repetitivo **por dia da semana e por horário** (`weekday`, `hour`, `minute`, `repeats: true`). Consome (dias × horários) da cota |
| `intervalo_dias` | **Não** tem trigger nativo equivalente. Agenda datas concretas, uma a uma, para os próximos 30 dias, e reagenda na abertura do app (rotina 13-A.3) |
| `quando_precisar` | Não agenda nada |

**Conteúdo da notificação** — ver 15.3. Título e corpo precisam dizer o remédio e a dose; "Você tem 1 lembrete" é inaceitável (2.4).

**Critérios de aceite**

- [ ] Salvar o primeiro remédio com lembrete ligado mostra a tela explicativa antes do diálogo do iOS.  
- [ ] Negar a permissão salva o remédio mesmo assim e mostra a faixa "Os avisos estão desligados no iPhone" na Home.  
- [ ] Um remédio diário às 08:00 gera **uma** notificação agendada repetitiva e dispara às 08:00 de hoje e às 08:00 de amanhã sem o app ser aberto no meio.  
- [ ] A notificação recebida mostra no título o nome do remédio e no corpo a dose, conforme 15.3.  
- [ ] Um remédio de segunda/quarta/sexta às 08:00 e 20:00 gera 6 notificações agendadas, e nenhuma dispara na terça.  
- [ ] Um remédio "a cada 3 dias" às 09:00 gera 10 notificações agendadas cobrindo 30 dias, com datas espaçadas de 3 em 3 dias.  
- [ ] Arquivar um remédio remove todas as suas linhas de `notificacoes_agendadas` e cancela os pedidos no iOS.  
- [ ] Desligar `lembrete_ativo` na edição cancela as notificações e mantém o remédio na Home com o ícone de sino cortado.  
- [ ] Com remédios suficientes para passar de 64 agendamentos, o app agenda as 64 mais próximas, registra a situação e mostra o aviso da regra 11.4 — sem travar, sem erro e sem perder dado.  
- [ ] Reabrir o app dispara a reconciliação (13-A.3): o número de notificações pendentes no iOS bate com as linhas de `notificacoes_agendadas`.  
- [ ] Com `som_lembrete = 0`, a notificação chega sem som.

---

### 8.5 Exportar e compartilhar a lista

**Objetivo:** mandar a lista para o filho pelo WhatsApp, em dois toques a partir da Home.

**Fluxo**

1. Botão **"Compartilhar minha lista"** no cabeçalho da Home — ícone \+ rótulo escrito, nunca só o ícone.  
2. Tocar gera o texto (formato exato em 14.1) com os remédios **ativos** e abre a share sheet nativa do iOS.  
3. A pessoa escolhe WhatsApp, E-mail, Mensagens, ou "Copiar".  
4. Com zero remédios ativos, o botão fica desabilitado com o texto "Cadastre um remédio primeiro".

**Critérios de aceite**

- [ ] O botão está visível na Home sem rolagem e mede no mínimo 60pt de altura.  
- [ ] Tocar abre a share sheet nativa do iOS com o texto já montado.  
- [ ] O texto gerado bate **caractere por caractere** com o formato da seção 14.1, incluindo cabeçalho, ordem, quebras de linha e rodapé.  
- [ ] Remédios arquivados não aparecem no texto.  
- [ ] Remédios "quando precisar" aparecem na seção própria do texto, conforme 14.1.  
- [ ] Um remédio sem dose e sem observação não gera linha vazia nem separador solto no texto.  
- [ ] Compartilhar com o WhatsApp entrega o texto formatado e legível, com as quebras de linha preservadas.  
- [ ] Com zero remédios ativos, o botão aparece desabilitado com o texto especificado.

---

### 8.6 Ajustes

**Objetivo:** as poucas coisas que a pessoa pode querer mudar, e o caminho de saída dos dados.

**Itens da tela**, nesta ordem:

| Item | Comportamento |
| :---- | :---- |
| **Tamanho das letras** | Dois botões grandes: "Grande" e "Gigante", com prévia real do texto em cada um. Grava `preferencias.escala_fonte` e aplica na hora, no app inteiro |
| **Som do aviso** | Switch grande. Grava `preferencias.som_lembrete` e **reagenda todas as notificações** (o som faz parte do agendamento, não da entrega) |
| **Remédios que parei** | Abre a lista de arquivados (8.3) |
| **Compartilhar minha lista** | Mesmo do 8.5, repetido aqui por descoberta |
| **Sobre o app** | Versão, uma frase sobre privacidade ("Seus dados ficam só no seu iPhone. Nada é enviado para a internet."), um link para a Política de Privacidade (abre no navegador — ver 25.6 e Anexo B) e nada mais |
| **Apagar todos os meus dados** | Em vermelho, no fim. Confirmação dupla em texto claro. Apaga o banco, cancela todas as notificações, volta ao onboarding |

**Critérios de aceite**

- [ ] Mudar para "Gigante" aumenta o texto de toda a Home imediatamente, sem reiniciar o app.  
- [ ] A escolha de tamanho persiste depois de fechar e reabrir o app.  
- [ ] Desligar o som e receber um lembrete: a notificação chega sem som.  
- [ ] Desligar o som reagenda as notificações — as linhas de `notificacoes_agendadas` têm identificadores novos.  
- [ ] "Apagar todos os meus dados" pede duas confirmações distintas em texto claro antes de agir.  
- [ ] Após apagar: o banco está vazio (exceto a linha de `preferencias` recriada pelo seed), não há nenhuma notificação pendente no iOS, e o app abre no onboarding.  
- [ ] O link de Política de Privacidade em "Sobre o app" abre a URL da 25.6 no navegador padrão (`Linking.openURL`), sem sair definitivamente do app (o iOS volta ao Hora do Remédio ao fechar o navegador).

---

### 8.7 Onboarding

**Objetivo:** em duas telas, dizer o que o app faz e levar direto ao primeiro cadastro. Ninguém deste público lê a terceira tela.

**Fluxo**

1. Tela 1 — "Seus remédios, na hora certa": ilustração \+ uma frase. Botão "Continuar".  
2. Tela 2 — "Tudo fica só no seu iPhone": ilustração \+ a frase de privacidade. Botão "Começar".  
3. "Começar" grava `onboarding_concluido_em` e abre a Home no estado vazio.  
4. O onboarding **não** pede permissão de notificação (isso é do 8.4) e **não** pede nome, idade ou nada do usuário.

**Critérios de aceite**

- [ ] Na primeira abertura, o onboarding aparece; da segunda em diante, não.  
- [ ] "Começar" grava `onboarding_concluido_em` e leva à Home vazia.  
- [ ] O onboarding tem exatamente 2 telas e não pede nenhum dado pessoal nem permissão.  
- [ ] Após "Apagar todos os meus dados" (8.6), o onboarding aparece de novo.

---

## 9\. Fluxos principais ponta a ponta

Os épicos organizam a implementação; estes fluxos garantem que ela funciona junta. Cada um deve ser percorrido manualmente num aparelho real antes do release (26.4).

### 9.1 Primeiro uso completo — do ícone ao primeiro lembrete

1. A pessoa toca no ícone pela primeira vez. Splash segura enquanto o banco migra e o seed roda.  
2. Onboarding, tela 1 → "Continuar" → tela 2 → "Começar".  
3. Home no estado vazio, com a seta apontando para "+ Adicionar remédio".  
4. Toca no botão. Formulário abre com "Todo dia", "Comprimido", horário 08:00 e lembrete ligado já pré-selecionados.  
5. Digita "Losartana", digita "50mg", toca em Salvar.  
6. **Primeira vez com lembrete ligado** → tela explicativa da permissão → diálogo do iOS → Permitir.  
7. Notificação diária repetitiva agendada para 08:00. Linha gravada em `notificacoes_agendadas`.  
8. Home mostra "Manhã — 08:00" com um card coral escrito "Losartana / 50mg".  
9. Às 08:00 do dia seguinte, com o app fechado, chega a notificação "Hora do remédio 💊 / Losartana — 50mg".

**Caminho da exceção:** se no passo 6 a pessoa tocar em "Não permitir", o fluxo continua do passo 8 em diante, sem o passo 9, e a Home ganha a faixa "Os avisos estão desligados no iPhone. Toque aqui para ligar".

### 9.2 O médico mudou o horário

1. Home → toca no card "Losartana" → detalhe.  
2. "Editar" → formulário preenchido → bloco 5 → toca no horário 08:00 → time picker → 09:00 → "Salvar alterações".  
3. Notificações antigas canceladas por `identificador_ios`, linhas removidas, notificações novas agendadas e gravadas.  
4. Home mostra o card no grupo "Manhã — 09:00".

### 9.3 Parar um remédio e voltar a tomar

1. Home → card → detalhe → "Parar de tomar" → confirmação → "Sim, parar".  
2. `arquivado_em` preenchido, notificações canceladas, card sai da Home, faixa "Losartana foi arquivado / Desfazer" por 5 s.  
3. Duas semanas depois: Ajustes → "Remédios que parei" → "Voltar a tomar" → `arquivado_em` limpo, horários originais intactos, notificações reagendadas, card de volta na Home.

**Caminho da exceção:** se ele tocar em "Desfazer" dentro dos 5 s, o passo 3 acontece na hora.

### 9.4 Mandar a lista para o filho

1. Home → "Compartilhar minha lista".  
2. Texto gerado conforme 14.1 com todos os remédios ativos.  
3. Share sheet do iOS → WhatsApp → escolhe o contato → envia.  
4. O filho recebe um texto que se explica sozinho, sem precisar do app (2.3).

**Caminho da exceção:** com zero remédios ativos, o botão está desabilitado com "Cadastre um remédio primeiro" — não gera texto vazio.

### 9.5 O caminho de quem nunca vai usar o app sozinho

O filho instala, cadastra os seis remédios do pai numa visita, liga os lembretes e vai embora. O pai **nunca abre o app**.

Este fluxo precisa funcionar do mesmo jeito, e é a razão de duas decisões:

- Os lembretes usam triggers repetitivos do iOS sempre que possível (11.4), para funcionar por meses sem o app ser aberto.  
- A única exceção — `intervalo_dias`, que depende de reagendamento — tem janela de 30 dias, folgada o bastante para sobreviver a um pai que só abre o app uma vez por mês. Ver o risco 23.2.

---

## 10\. Inventário de telas

Onze telas. O rótulo entre colchetes indica a rota do Expo Router.

| \# | Tela | Rota | Mostra | Ações | Lê | Escreve |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| 1 | Onboarding 1 | `/onboarding` | Proposta do app | Continuar | — | — |
| 2 | Onboarding 2 | `/onboarding/privacidade` | Frase de privacidade | Começar | — | `preferencias` |
| 3 | **Home** | `/` | Remédios ativos agrupados por horário | Adicionar, abrir detalhe, compartilhar, abrir ajustes | `remedios`, `horarios`, `preferencias` | — |
| 4 | Cadastro | `/remedio/novo` | Formulário de 8 blocos | Salvar, cancelar | `preferencias` | `remedios`, `horarios`, `notificacoes_agendadas` |
| 5 | Detalhe | `/remedio/[id]` | Remédio em tela cheia | Editar, parar de tomar, voltar | `remedios`, `horarios` | — |
| 6 | Edição | `/remedio/[id]/editar` | Formulário preenchido | Salvar alterações, cancelar | `remedios`, `horarios` | `remedios`, `horarios`, `notificacoes_agendadas` |
| 7 | Permissão de aviso | `/permissao-aviso` (modal) | Explicação antes do diálogo do iOS | Continuar, agora não | — | — |
| 8 | Ajustes | `/ajustes` | Lista de opções | Todas as de 8.6 | `preferencias` | `preferencias`, `notificacoes_agendadas` |
| 9 | Remédios que parei | `/ajustes/arquivados` | Remédios com `arquivado_em` | Voltar a tomar | `remedios`, `horarios` | `remedios`, `notificacoes_agendadas` |
| 10 | Sobre o app | `/ajustes/sobre` | Versão e privacidade | Voltar | — | — |
| 11 | Erro de banco | componente global | Mensagem da regra 12.4 | Tentar de novo | — | — |

**Verificação de cobertura:** todo remédio cadastrado pode ser visto (3), aberto (5), editado (6), arquivado (5), desarquivado (9) e exportado (3). Nenhuma operação do modelo de dados ficou sem caminho na interface.

---

## 11\. Regras de negócio

Toda regra tem um número, um comportamento definido e um lugar onde é aplicada. **O agente de código não deve inferir nenhum valor desta seção.**

### 11.1 Semântica das frequências

| Valor | Significa | Gera lembrete? | Campos exigidos |
| :---- | :---- | :---- | :---- |
| `diario` | Todos os dias, nos horários cadastrados | Sim | nenhum |
| `dias_semana` | Só nos dias marcados, nos horários cadastrados | Sim | `dias_semana` |
| `intervalo_dias` | A cada N dias a partir de `criado_em`, nos horários cadastrados | Sim | `intervalo_dias` (2 a 30\) |
| `quando_precisar` | Sem periodicidade. Aparece em bloco separado na Home e na exportação | **Não, nunca** | nenhum |

**Onde:** aplicação (validação no formulário 8.2 \+ serviço de lembretes 13-A.2). Coerência entre `frequencia` e os campos dependentes também é garantida no banco pelas constraints `ck_remedios_intervalo_coerente` e `ck_remedios_dias_semana_coerente`.

### 11.2 Codificação dos dias da semana

| Dígito | Dia | `weekday` do iOS |
| :---- | :---- | :---- |
| 1 | Domingo | 1 |
| 2 | Segunda | 2 |
| 3 | Terça | 3 |
| 4 | Quarta | 4 |
| 5 | Quinta | 5 |
| 6 | Sexta | 6 |
| 7 | Sábado | 7 |

Armazenado como string ordenada crescente, sem separador: `"246"` \= segunda, quarta, sexta. A numeração foi escolhida para coincidir com `weekday` do `UNCalendarNotificationTrigger`, eliminando conversão.

**Rótulo na UI e na exportação:** abreviação de duas letras — Do, Se, Te, Qu, Qi, Sx, Sa? **Não.** Use: `Dom`, `Seg`, `Ter`, `Qua`, `Qui`, `Sex`, `Sáb`. Três letras, sem ambiguidade, é o que o público lê sem hesitar.

**Onde:** aplicação.

### 11.3 Limites e cardinalidades

| Regra | Valor | Onde é aplicada |
| :---- | :---- | :---- |
| Horários por remédio | mín. 1, máx. 6 (exceto `quando_precisar`, que tem 0\) | Aplicação (formulário 8.2) |
| Caracteres em `nome` | 1 a 60 | Aplicação \+ CHECK `ck_remedios_nome_nao_vazio` |
| Caracteres em `dose` | 0 a 40 | Aplicação |
| Caracteres em `observacao` | 0 a 200 | Aplicação |
| `intervalo_dias` | 2 a 30 | Aplicação \+ CHECK |
| Dias marcados em `dias_semana` | mín. 1, máx. 7 | Aplicação \+ CHECK |
| Remédios ativos simultâneos | **sem limite rígido**; acima de 20 mostra o aviso da 11.4 | Aplicação |
| Duração do "Desfazer" após arquivar | 5 segundos | Aplicação |
| Janela de agendamento de `intervalo_dias` | 30 dias à frente | Aplicação (13-A.2) |

### 11.4 Cota de 64 notificações do iOS — a regra que define o épico 8.4

**Restrição de plataforma verificada (4.6):** o iOS aceita no máximo **64 pedidos de notificação local pendentes por app**. O sistema mantém os 64 mais próximos e **descarta o resto em silêncio**. Um pedido repetitivo conta como **1**, não importa quantas vezes dispare.

Consequência direta na estratégia de agendamento:

| Frequência | Estratégia | Custo na cota |
| :---- | :---- | :---- |
| `diario` | 1 trigger de calendário repetitivo por horário (`hour`, `minute`, `repeats: true`) | **1 por horário** |
| `dias_semana` | 1 trigger repetitivo por combinação de dia × horário (`weekday`, `hour`, `minute`, `repeats: true`) | **dias × horários** |
| `intervalo_dias` | Não há trigger nativo. Agenda datas concretas para os próximos 30 dias | **⌈30 / N⌉ × horários** |
| `quando_precisar` | Não agenda | 0 |

**Cálculo do custo total (função `calcularCustoDeAgendamento`):** soma o custo de todos os remédios ativos com `lembrete_ativo = 1`.

**Comportamento quando o total passa de 64:**

1. Ordene os agendamentos por proximidade do próximo disparo.  
2. Agende os 64 primeiros. Registre-os em `notificacoes_agendadas`.  
3. **Não** agende o resto, e **não** grave linhas para eles.  
4. Mostre na Home uma faixa permanente: *"Você tem muitos remédios cadastrados. Alguns avisos podem não chegar. Abra o app uma vez por dia para manter os avisos em dia."*  
5. Registre o fato no log local para depuração.

**O que nunca pode acontecer:** perder dado de remédio, travar, ou mostrar erro técnico. O remédio continua na lista mesmo sem o aviso — o app degrada, não quebra.

**Priorização quando estoura:** por proximidade do próximo disparo, sem exceção. Não invente prioridade por remédio, por cor ou por ordem de cadastro.

**Onde:** aplicação, serviço de lembretes (13-A.2).

### 11.5 Reconciliação de notificações

Toda vez que o app é aberto (e ao voltar do background após mais de 1 hora), roda a rotina 13-A.3, que:

1. Lê as notificações pendentes de verdade no iOS.  
2. Compara com `notificacoes_agendadas`.  
3. Cancela tudo o que existe no iOS e não deveria existir (remédio arquivado, lembrete desligado, horário mudado).  
4. Agenda tudo o que deveria existir e não existe — respeitando a cota da 11.4.  
5. Reagenda a janela de 30 dias dos remédios com `intervalo_dias`.  
6. Sincroniza a tabela com o resultado real.

**Motivo:** o iOS pode descartar agendamentos silenciosamente (cota, restore de backup, mudança de fuso). A tabela é derivada (7.4); o iOS é a realidade. Na divergência, o iOS ganha e a tabela é reescrita.

**Onde:** aplicação.

### 11.6 Fuso horário e virada de dia

| Situação | Comportamento |
| :---- | :---- |
| Fuso de referência | Sempre o **fuso local do aparelho**. `hora_minuto` é horário de parede — "08:00" é 08:00 onde a pessoa estiver |
| Pessoa viaja para outro fuso | O lembrete das 08:00 passa a tocar às 08:00 do fuso novo. É o comportamento correto e esperado: quem toma remédio de manhã toma de manhã, não às 04:00 |
| Horário de verão | Idem — triggers de calendário do iOS acompanham a mudança automaticamente |
| Virada de dia | 00:00. Não existe "dia estendido" nem madrugada contando como o dia anterior |
| Saudação da Home | Bom dia (05:00–11:59), Boa tarde (12:00–17:59), Boa noite (18:00–04:59) |
| Cálculo de `intervalo_dias` | Baseado na **data local** de `criado_em`, ignorando a hora. Dia 1 é o dia do cadastro |

**Onde:** aplicação. `hora_minuto` é guardado sem fuso justamente para que essa regra funcione (7.3).

### 11.7 Ordenação e agrupamento na Home

| Regra | Comportamento |
| :---- | :---- |
| 11.7.1 | Agrupa por `hora_minuto`, crescente (00:00 → 23:59) |
| 11.7.2 | Um remédio com N horários aparece N vezes, uma por grupo |
| 11.7.3 | Dentro do grupo, ordena por `remedios.nome`, alfabética, sem diferenciar maiúsculas nem acentos |
| 11.7.4 | `quando_precisar` fica fora dos grupos, em bloco próprio no fim, ordenado por nome |
| 11.7.5 | A Home mostra **todos** os horários do dia, não só os futuros — a lista é referência, não fila de tarefas (1.3: não é app de adesão) |

### 11.8 Atribuição de cor no cadastro

Rodízio na ordem da paleta (16.3): `coral` → `azul` → `verde` → `amarelo` → `lilas` → `pessego` → volta ao início. A cor sugerida é a próxima após a do último remédio criado (incluindo arquivados). Editável pelo usuário no bloco 8 do cadastro.

**Onde:** aplicação.

### 11.9 O que explicitamente **não** é automático

Escrito aqui para o agente de código não inventar comportamento:

- **Não** existe cálculo de aderência, estatística, streak ou pontuação.  
- **Não** existe validação do nome contra base de medicamentos, correção ortográfica ou sugestão de nome.  
- **Não** existe sugestão automática de dose, horário "ideal" ou alerta de interação.  
- **Não** existe arquivamento automático por tempo sem uso.  
- **Não** existe notificação além do lembrete de horário — nada de "faz tempo que você não abre o app".  
- **Não** existe backup, sincronização ou envio de nada para lugar nenhum.  
- **Não** existe analytics, telemetria ou crash reporting no MVP (ver 21).

---

## 12\. Máquinas de estado e tratamento de erro

### 12.1 Ciclo de vida do remédio

        cadastro

           ↓

      ┌─ ATIVO ─┐

      │         │

"Parar de      "Voltar a

 tomar"         tomar"

      │         │

      └→ ARQUIVADO ┘

              ↓

      "Apagar todos os dados"

              ↓

          (removido)

| Estado | `arquivado_em` | Aparece na Home | Entra na exportação | Tem notificação |
| :---- | :---- | :---- | :---- | :---- |
| Ativo | NULL | Sim | Sim | Se `lembrete_ativo = 1` |
| Arquivado | preenchido | Não | Não | Nunca |

**Transições permitidas:** Ativo → Arquivado (8.3), Arquivado → Ativo (8.3), Arquivado → removido (só via 8.6). **Transições proibidas:** Ativo → removido diretamente. Não existe exclusão individual definitiva (7.8).

### 12.2 Ciclo de vida da notificação agendada

 (nada) → AGENDADA → DISPARADA → (nada)

             ↓

         CANCELADA → (linha removida)

Cancelamento acontece em: arquivamento do remédio, `lembrete_ativo` → 0, edição de horário, mudança do som (8.6), reconciliação (11.5), apagar dados (8.6).

O app **não** trata a notificação disparada: não há ação ao tocar nela além de abrir o app na Home. Não há botão de ação na notificação, porque não há registro de tomada (1.3).

### 12.3 Estado da permissão de notificação

| Estado | Significa | O que o app faz |
| :---- | :---- | :---- |
| `undetermined` | Nunca pediu | Pede no primeiro remédio com lembrete (8.4) |
| `granted` | Permitido | Agenda normalmente |
| `denied` | Negado | Salva os remédios normalmente; mostra a faixa persistente na Home com atalho para os Ajustes do iOS. **Nunca** insiste com pop-up repetido |
| `provisional` | Entrega silenciosa | Trata como `granted`, mas a faixa avisa que os avisos chegam sem som |

Verificado na abertura do app e ao voltar do background — a pessoa pode ter mudado nos Ajustes do iOS.

### 12.4 Tratamento de erro

| Falha | O que o usuário vê | O que o app faz por baixo |
| :---- | :---- | :---- |
| Migration falha na abertura | Tela cheia: "Não conseguimos abrir seus dados. Feche o app e abra de novo." \+ botão "Tentar de novo" | Loga o erro localmente. **Nunca** apaga o banco automaticamente |
| Erro ao salvar remédio | Abaixo do botão Salvar: "Não conseguimos salvar. Tente de novo." | Rollback da transação. O formulário mantém tudo preenchido |
| Erro ao agendar notificação | Faixa na Home: "Não conseguimos programar o aviso deste remédio." | O remédio é salvo mesmo assim. Nunca perde o cadastro por causa do aviso |
| Cota de 64 estourada | Faixa da regra 11.4 | Agenda as 64 mais próximas |
| Share sheet cancelada pelo usuário | Nada | Não é erro. Nenhuma mensagem |
| Banco corrompido/ilegível | Mesma tela da migration | Oferece "Apagar todos os dados e recomeçar" **só** depois da segunda falha seguida, com confirmação dupla |

**Regra geral de erro:** nenhuma mensagem de erro do app contém código, stack trace, nome de tabela, nome de biblioteca ou a palavra "erro" em inglês. Toda mensagem diz o que aconteceu e o que fazer, em uma frase, em português comum.

---

## 13\. Contratos de API

**Não aplicável.** O app não tem backend, não faz requisição de rede e não integra com terceiros (princípio 0.4, seção 5). Substituída pela 13-A.

## 13-A. Contratos dos módulos internos

Quatro módulos concentram toda a lógica que não é de tela. Nenhuma tela fala com o banco direto — tudo passa por aqui.

### 13-A.1 `repositorioRemedios` — acesso a dados

| Função | Recebe | Devolve | O que faz |
| :---- | :---- | :---- | :---- |
| `listarAtivos()` | — | `RemedioComHorarios[]` | Remédios com `arquivado_em IS NULL` \+ horários. Base da Home |
| `listarArquivados()` | — | `RemedioComHorarios[]` | Remédios arquivados, mais recentes primeiro |
| `buscarPorId(id)` | `number` | `RemedioComHorarios | null` | Detalhe (tela 5\) |
| `criar(dados)` | `NovoRemedio` | `RemedioComHorarios` | Insere remédio \+ horários **numa transação só**. Falha em qualquer parte desfaz tudo |
| `atualizar(id, dados)` | `number, NovoRemedio` | `RemedioComHorarios` | Atualiza o remédio e **reescreve** os horários, em transação |
| `arquivar(id)` | `number` | `void` | Preenche `arquivado_em`. Não apaga nada |
| `desarquivar(id)` | `number` | `void` | Limpa `arquivado_em` |
| `apagarTudo()` | — | `void` | Esvazia as 4 tabelas e recria a linha de `preferencias` (8.6) |

Regra: `criar` e `atualizar` **não** agendam notificação. Quem orquestra é a tela, chamando o repositório e depois o serviço de lembretes. Separar isso mantém o repositório testável sem mock do iOS.

### 13-A.2 `servicoLembretes` — agendamento

| Função | Recebe | Devolve | O que faz |
| :---- | :---- | :---- | :---- |
| `verificarPermissao()` | — | `EstadoPermissao` | Os 4 estados da 12.3 |
| `pedirPermissao()` | — | `EstadoPermissao` | Dispara o diálogo do iOS. Só chamado depois da tela explicativa (8.4) |
| `calcularCustoDeAgendamento(remedios)` | `RemedioComHorarios[]` | `number` | Custo na cota de 64, conforme a tabela da 11.4. Função pura — **testável isoladamente** |
| `montarAgendamentos(remedio)` | `RemedioComHorarios` | `Agendamento[]` | Traduz frequência \+ horários nos triggers do iOS. Função pura |
| `agendarDoRemedio(remedio)` | `RemedioComHorarios` | `void` | Agenda no iOS e grava em `notificacoes_agendadas` |
| `cancelarDoRemedio(remedioId)` | `number` | `void` | Cancela por `identificador_ios` e remove as linhas |
| `cancelarTudo()` | — | `void` | Cancela todos os pendentes e esvazia a tabela |

`calcularCustoDeAgendamento` e `montarAgendamentos` são puras de propósito: são as duas funções onde um erro produz lembrete que não toca, e ambas precisam de teste automatizado (22.1).

### 13-A.3 `reconciliadorLembretes` — a rotina da regra 11.5

| Função | Recebe | Devolve | O que faz |
| :---- | :---- | :---- | :---- |
| `reconciliar()` | — | `RelatorioReconciliacao` | Os 6 passos da 11.5 |

`RelatorioReconciliacao` traz: quantos foram cancelados, quantos agendados, custo total e se a cota estourou (o que liga a faixa da 11.4 na Home).

**Quando roda:** na abertura do app (depois da migration, antes da Home) e ao voltar do background após mais de 1 hora. Nunca bloqueia a interface — roda em background e a Home reage por `useLiveQuery`.

### 13-A.4 `exportadorLista` — geração do texto

| Função | Recebe | Devolve | O que faz |
| :---- | :---- | :---- | :---- |
| `gerarTexto(remedios)` | `RemedioComHorarios[]` | `string` | Monta o texto exato da 14.1. **Função pura, testável por comparação literal** |
| `compartilhar(texto)` | `string` | `void` | Abre a share sheet do iOS |

`gerarTexto` é pura porque o critério de aceite de 8.5 é comparação caractere por caractere — sem isso, não há como testar.

---

## 14\. Exportação

Não há importação no MVP. A exportação tem um formato só: texto puro, para a share sheet do iOS.

### 14.1 Formato exato do texto exportado

Reproduza literalmente, incluindo emojis, quebras de linha e pontuação.

💊 MEUS REMÉDIOS

Atualizado em 26/07/2026

🌅 MANHÃ — 08:00

• Losartana — 50mg

• Metformina — 850mg

  ⚠️ Tomar junto com a comida

🌇 TARDE — 14:00

• Omeprazol — 20mg

🌙 NOITE — 20:00

• Sinvastatina — 20mg

  📅 Seg, Qua, Sex

💊 QUANDO PRECISAR

• Dipirona — 1 comprimido

  ⚠️ Só se tiver dor

—

Lista gerada pelo app Hora do Remédio

**Regras de montagem**

| \# | Regra |
| :---- | :---- |
| 14.1.1 | Cabeçalho fixo: `💊 MEUS REMÉDIOS`, quebra, `Atualizado em DD/MM/AAAA` com a data local de hoje |
| 14.1.2 | Linha em branco entre o cabeçalho e o primeiro grupo, e entre grupos |
| 14.1.3 | Grupos na ordem crescente de horário; título `{emoji} {FAIXA} — {HH:MM}`, faixa em maiúsculas |
| 14.1.4 | Emojis por faixa: Madrugada 🌃, Manhã 🌅, Tarde 🌇, Noite 🌙 |
| 14.1.5 | Cada remédio: `• {nome} — {dose}`. **Sem dose:** só `• {nome}`, sem o travessão |
| 14.1.6 | Observação: linha seguinte, com 2 espaços de indentação, prefixo `⚠️` . Omitida se nula |
| 14.1.7 | Frequência diferente de `diario`: linha com 2 espaços, prefixo `📅` , com `Seg, Qua, Sex` ou `A cada 3 dias`. **Diário não escreve linha nenhuma** |
| 14.1.8 | Ordem das linhas extras: observação (⚠️) antes de frequência (📅) |
| 14.1.9 | Bloco `💊 QUANDO PRECISAR` no fim, só se houver algum. Sem horário no título |
| 14.1.10 | Rodapé fixo: linha em branco, `—`, quebra, `Lista gerada pelo app Hora do Remédio` |
| 14.1.11 | Só remédios ativos (`arquivado_em IS NULL`) |
| 14.1.12 | Um remédio com 3 horários aparece nos 3 grupos, igual à Home (11.7.2) |
| 14.1.13 | Encoding UTF-8. Sem BOM (é texto para app de mensagem, não CSV para Excel) |
| 14.1.14 | Sem espaços em branco no fim das linhas; sem linha em branco dupla |

**Assunto sugerido no compartilhamento** (usado por e-mail): `Meus remédios — DD/MM/AAAA`.

Não há nome de arquivo: a exportação é texto na share sheet, não arquivo. PDF fica para a v2 (28.8).

---

## 15\. Copy e tom de voz

**Este é o texto final do produto, não placeholder.** O agente de código deve usar exatamente estas strings.

### 15.1 Onde as strings ficam

Arquivo único: `src/constants/textos.ts`, objeto exportado, agrupado por tela. Nenhuma string literal de interface espalhada em componente. Não é i18n (o app é só pt-BR, 4.5) — é para o texto ser revisável num lugar só.

### 15.2 Princípios de escrita

| Regra | Sim | Não |
| :---- | :---- | :---- |
| Trate por "você", nunca por "senhor" | "Você ainda não cadastrou nenhum remédio" | "O senhor ainda não cadastrou" |
| Verbo no imperativo direto, sem "por favor" | "Escreva o nome do remédio" | "Por favor, insira o nome do medicamento" |
| Palavra comum, não termo técnico | "remédio" | "medicamento", "fármaco", "posologia" |
| Frase curta, uma ideia por frase | "Ele sai da sua lista e os avisos param." | Período composto com duas orações subordinadas |
| Nunca culpe o usuário | "Não conseguimos salvar. Tente de novo." | "Você preencheu os dados incorretamente" |
| Nunca infantilize | "Pronto\!" | "Uhuu\! Você conseguiu, parabéns\!\! 🎉🎉" |
| Emoji com moderação, nunca substituindo texto | "Hora do remédio 💊" | "💊⏰🔔" |
| Nunca cobre | "Losartana — 50mg" | "Você esqueceu de tomar\!" |

### 15.3 Textos das notificações

| Situação | Título | Corpo |
| :---- | :---- | :---- |
| Lembrete com dose | `Hora do remédio 💊` | `{nome} — {dose}` |
| Lembrete sem dose | `Hora do remédio 💊` | `{nome}` |
| Dois ou mais remédios no mesmo horário | `Hora do remédio 💊` | Uma notificação **por remédio**, não agrupada. Ver nota abaixo |

**Nota sobre agrupar:** notificações separadas por remédio custam mais cota (11.4), mas garantem que o nome de cada remédio apareça. Agrupar ("Você tem 3 remédios agora") violaria 2.4 — a pessoa precisa saber **o quê** tomar sem abrir o app. Se a cota estourar, a degradação é a da 11.4, não o agrupamento.

### 15.4 Vocabulário obrigatório

| Conceito | Palavra a usar | Palavras proibidas na interface |
| :---- | :---- | :---- |
| Arquivar remédio | "Parar de tomar" | excluir, apagar, deletar, remover, inativar |
| Desarquivar | "Voltar a tomar" | reativar, restaurar |
| Lembrete | "aviso" | notificação, push, alerta |
| Cadastrar | "Adicionar remédio" | criar, registrar, incluir |
| Remédio | "remédio" | medicamento, fármaco, droga |
| Horário | "hora" / "horário" | posologia, frequência de administração |
| Arquivados | "Remédios que parei" | arquivo, histórico, inativos |

Exceção: `arquivado_em` no banco continua se chamando assim (7.2). A regra vale para o texto que o usuário lê.

### 15.5 Strings principais

| Chave | Texto |
| :---- | :---- |
| `home.saudacao.manha` | Bom dia |
| `home.saudacao.tarde` | Boa tarde |
| `home.saudacao.noite` | Boa noite |
| `home.vazio.titulo` | Você ainda não cadastrou nenhum remédio |
| `home.vazio.texto` | Toque no botão verde para cadastrar o primeiro |
| `home.botaoAdicionar` | \+ Adicionar remédio |
| `home.botaoCompartilhar` | Compartilhar minha lista |
| `home.semAviso` | sem aviso |
| `home.quandoPrecisar` | Quando precisar |
| `home.avisosDesligados` | Os avisos estão desligados no iPhone. Toque aqui para ligar |
| `home.cotaEstourada` | Você tem muitos remédios cadastrados. Alguns avisos podem não chegar. Abra o app uma vez por dia para manter os avisos em dia. |
| `cadastro.nome.pergunta` | Qual o nome do remédio? |
| `cadastro.dose.pergunta` | Qual a dose? |
| `cadastro.dose.exemplo` | Ex.: 50mg, 1 comprimido, 10 gotas |
| `cadastro.forma.pergunta` | Como você toma? |
| `cadastro.frequencia.pergunta` | Com que frequência? |
| `cadastro.horarios.pergunta` | A que horas? |
| `cadastro.horarios.adicionar` | Adicionar horário |
| `cadastro.lembrete.pergunta` | Quer ser lembrado? |
| `cadastro.lembrete.sim` | Sim, me avise |
| `cadastro.lembrete.nao` | Não precisa avisar |
| `cadastro.observacao.pergunta` | Alguma observação? |
| `cadastro.observacao.exemplo` | Ex.: tomar em jejum |
| `cadastro.cor.pergunta` | Escolha uma cor |
| `cadastro.salvar` | Salvar remédio |
| `cadastro.salvarEdicao` | Salvar alterações |
| `cadastro.erro.nomeVazio` | Escreva o nome do remédio |
| `cadastro.erro.semHorario` | Escolha pelo menos uma hora |
| `cadastro.erro.semDiaSemana` | Escolha pelo menos um dia |
| `cadastro.erro.horarioRepetido` | Esse horário já está na lista |
| `cadastro.erro.maximoHorarios` | São no máximo 6 horários por remédio |
| `cadastro.erro.aoSalvar` | Não conseguimos salvar. Tente de novo. |
| `cadastro.sairSemSalvar.titulo` | Sair sem salvar? |
| `cadastro.sairSemSalvar.texto` | O que você preencheu será perdido. |
| `detalhe.editar` | Editar |
| `detalhe.parar` | Parar de tomar |
| `detalhe.voltar` | Voltar |
| `detalhe.pararConfirmacao.titulo` | Parar de tomar {nome}? |
| `detalhe.pararConfirmacao.texto` | Ele sai da sua lista e os avisos param. Você pode voltar a tomar depois. |
| `detalhe.pararConfirmacao.sim` | Sim, parar |
| `detalhe.arquivado.faixa` | {nome} foi arquivado |
| `detalhe.arquivado.desfazer` | Desfazer |
| `permissao.titulo` | Para avisar na hora do remédio, o app precisa da sua permissão |
| `permissao.texto` | Na próxima tela, toque em Permitir. |
| `permissao.continuar` | Continuar |
| `permissao.agoraNao` | Agora não |
| `ajustes.titulo` | Ajustes |
| `ajustes.tamanhoLetras` | Tamanho das letras |
| `ajustes.tamanhoGrande` | Grande |
| `ajustes.tamanhoGigante` | Gigante |
| `ajustes.somAviso` | Som do aviso |
| `ajustes.arquivados` | Remédios que parei |
| `ajustes.sobre` | Sobre o app |
| `sobre.politicaPrivacidade` | Política de Privacidade |
| `ajustes.apagarTudo` | Apagar todos os meus dados |
| `ajustes.privacidade` | Seus dados ficam só no seu iPhone. Nada é enviado para a internet. |
| `arquivados.vazio` | Você não parou de tomar nenhum remédio |
| `arquivados.voltarATomar` | Voltar a tomar |
| `onboarding.tela1.titulo` | Seus remédios, na hora certa |
| `onboarding.tela1.texto` | Cadastre o que você toma e o app avisa na hora. |
| `onboarding.tela1.botao` | Continuar |
| `onboarding.tela2.titulo` | Tudo fica só no seu iPhone |
| `onboarding.tela2.texto` | Nada é enviado para a internet. Funciona sem sinal. |
| `onboarding.tela2.botao` | Começar |
| `erro.banco.titulo` | Não conseguimos abrir seus dados |
| `erro.banco.texto` | Feche o app e abra de novo. |
| `erro.banco.botao` | Tentar de novo |
| `erro.agendamento` | Não conseguimos programar o aviso deste remédio. |

---

## 16\. Direção visual

### 16.1 Referências

As três referências enviadas pelo usuário definem a linguagem visual. O que se aproveita de cada uma:

| Referência | O que aproveitar | O que não aproveitar |
| :---- | :---- | :---- |
| Dashboard de saúde (glicemia) | Cards com raio grande, número/informação principal enorme, fundo cinza-clarinho, hierarquia limpa | A densidade: gráficos, múltiplos cards por linha, rótulos pequenos. Nada disso cabe aqui |
| App de hábitos (ranking) | Cards coloridos com ícone circular, uso alegre de cor por item, checklist com respiro | Gamificação, ranking, pódio, foto de perfil. O app não pontua ninguém (1.3) |
| App de notas (blocos pastel) | **A referência mais próxima do alvo:** blocos grandes de cor sólida, tipografia grande e arredondada, muito respiro, uma informação por bloco | Barra inferior flutuante escura com ícones pequenos; texto corrido em fonte pequena |

**Síntese:** cards grandes e coloridos como o app de notas, com a organização limpa do dashboard de saúde, e nada da gamificação do app de hábitos.

### 16.2 Tipografia

Fonte: **Nunito** (arredondada, alegre, ótima legibilidade em corpo grande), empacotada no binário.

| Token | Peso | `grande` (padrão) | `gigante` | Uso |
| :---- | :---- | :---- | :---- | :---- |
| `display` | 800 | 34pt | 40pt | Título de tela |
| `titulo` | 700 | 28pt | 34pt | Nome do remédio no card |
| `corpo` | 600 | 22pt | 26pt | Dose, observação, rótulos |
| `botao` | 700 | 24pt | 28pt | Texto de botão |
| `apoio` | 600 | 20pt | 22pt | Cabeçalho de grupo, data |

**`apoio` a 20pt é o piso absoluto do app** (princípio 0.1). Não existe token menor. Se o agente de código precisar de um, o layout está errado — corte conteúdo, não fonte.

Altura de linha: 1.35× o tamanho da fonte, em todos os tokens.

### 16.3 Paleta de cores dos remédios

Cores de identificação visual do card. Alegres e saturadas o bastante para diferenciar, claras o bastante para texto escuro por cima.

| Token | Fundo do card | Texto sobre o fundo | Detalhe/ícone |
| :---- | :---- | :---- | :---- |
| `coral` | `#FFD9D0` | `#3A2520` | `#E8654A` |
| `azul` | `#CFE4FF` | `#1C2B3D` | `#2F7FE0` |
| `verde` | `#CFF0D8` | `#1D3325` | `#2FA35C` |
| `amarelo` | `#FFEBB8` | `#3A3018` | `#E5A400` |
| `lilas` | `#E4D9FF` | `#2C2340` | `#7C5AE0` |
| `pessego` | `#FFE0C2` | `#3A2A18` | `#E07B2F` |

Contraste de todos os pares fundo/texto: mínimo 7:1 (WCAG AAA) — ver 17.3.

### 16.4 Cor semântica — separada da cor de marca

**Regra dura:** as seis cores da 16.3 são identificação, **não significado**. Um remédio coral não é mais urgente que um azul.

As cores semânticas abaixo são reservadas e **nunca** entram na paleta de cards:

| Token | Valor | Significado exclusivo |
| :---- | :---- | :---- |
| `acao` | `#1E9E5A` (verde forte) | Botão primário, confirmação, sucesso |
| `perigo` | `#D93025` (vermelho) | Ação destrutiva, "Apagar todos os dados" |
| `atencao` | `#B25E00` (âmbar escuro) | Faixas de aviso (permissão negada, cota estourada) |
| `neutro_texto` | `#1C1C1E` | Texto padrão sobre fundo claro |
| `neutro_fundo` | `#F5F5F7` | Fundo das telas |
| `neutro_superficie` | `#FFFFFF` | Superfície de card neutro, campo de formulário |
| `neutro_borda` | `#D6D6DB` | Bordas e divisores |

**Motivo de reservar:** se um card verde-menta convivesse com o verde de confirmação, o botão "Salvar" deixaria de se destacar. A cor de ação existe para ser a única daquele verde na tela.

### 16.5 Componentes e formas

| Item | Especificação |
| :---- | :---- |
| Raio de canto | 20pt em cards, 16pt em botões e campos |
| Altura mínima de toque | 60pt (princípio 0.2) |
| Botão primário | Fundo `acao`, texto branco, largura total, altura 64pt, raio 16pt |
| Botão secundário | Fundo `neutro_superficie`, borda 2pt `neutro_borda`, texto `neutro_texto` |
| Botão destrutivo | Texto e borda `perigo`, fundo branco. **Nunca** fundo vermelho cheio — assusta |
| Card de remédio | Fundo na cor do remédio, padding 20pt, raio 20pt, sem sombra pesada |
| Ícone da forma | 44pt, círculo branco 60pt de fundo, cor de detalhe do remédio |
| FAB "+ Adicionar" | 68pt de altura, pílula com ícone **e** texto. Nunca só o ícone |
| Espaçamento entre cards | 16pt |
| Margem lateral das telas | 20pt |
| Sombra | Sutil e única: `y: 2, blur: 8, opacidade 0.08` |
| Divisores | Evitar. Separe por espaço, não por linha |

### 16.6 Movimento

Animação mínima e funcional: transições de navegação padrão do iOS, faixa de "Desfazer" deslizando de baixo, e háptica ao salvar (sucesso) e ao arquivar (aviso). **Sem** animação decorativa, confete, bounce ou parallax — distrai e atrasa quem já tem dificuldade de mirar.

### 16.7 Modo escuro

**Não implementado no MVP.** Decisão registrada para não parecer esquecimento: o app força modo claro (`userInterfaceStyle: "light"` no `app.json`).

Motivo: manter o contraste AAA de 16.3 verificado em uma única combinação. Duplicar a paleta dobraria a verificação de contraste sem benefício para o público — idoso com presbiopia costuma ler melhor com texto escuro sobre fundo claro.

### 16.8 O que o agente de código pode decidir sozinho

Para não travar nem inventar em silêncio:

- ✅ **Livre:** ícones concretos do `@expo/vector-icons`, ilustrações dos estados vazios e do onboarding (formas geométricas simples na paleta), micro-ajustes de espaçamento, curvas de animação.  
- ❌ **Não é livre:** tamanho de fonte, valor de cor, altura mínima de toque, raio, qualquer string da 15.5, e qualquer regra da seção 11\.

---

## 17\. Requisitos não-funcionais

### 17.1 Performance — alvos numéricos

| Métrica | Alvo | Como medir |
| :---- | :---- | :---- |
| Abertura a frio até a Home usável | **\< 2,0 s** em iPhone SE 2ª geração | Cronômetro, 5 medições, mediana |
| Abertura a frio na **primeira** vez (com migration) | \< 3,5 s | Idem |
| Abrir o formulário de cadastro | \< 300 ms | Percepção \+ profiler |
| Salvar um remédio (banco \+ agendamento) | \< 500 ms | Do toque à Home atualizada |
| Gerar o texto de exportação (20 remédios) | \< 100 ms | Função pura, mensurável em teste |
| Rolagem da Home com 30 cards | 60 fps, sem frame drop visível | Profiler do RN |
| Reconciliação de notificações | \< 1,5 s, **sempre em background**, nunca bloqueando a interface | — |

Aparelho de referência para todos os alvos: **iPhone SE 2ª geração** (o mais fraco que roda iOS 16 e é realista no público).

### 17.2 Offline

| Requisito | Detalhe |
| :---- | :---- |
| 17.2.1 | **100% das funcionalidades** funcionam em modo avião. Sem exceção (princípio 0.4) |
| 17.2.2 | O app não faz **nenhuma** requisição de rede. Verificável: rodar em modo avião do primeiro uso ao lembrete, sem degradação |
| 17.2.3 | Fontes, ícones e ilustrações são empacotados no binário — nada baixado em runtime |
| 17.2.4 | Nenhuma permissão de rede é declarada além do padrão inevitável do iOS |

### 17.3 Acessibilidade

| Requisito | Alvo |
| :---- | :---- |
| Tamanho mínimo de fonte | **20pt** em qualquer elemento, em qualquer escala (0.1) |
| Contraste texto/fundo | **7:1 (WCAG AAA)** em todas as combinações da 16.3 e 16.4. Verificar par a par |
| Área mínima de toque | **60×60pt** (acima dos 44pt da HIG) |
| VoiceOver | Toda tela navegável. Todo botão com `accessibilityLabel` em português. Card de remédio lido como "Losartana, 50mg, às 8 horas" |
| Dynamic Type do iOS | O app usa a própria escala (16.2). Deve respeitar a escala do sistema **até 200%** sem quebrar o layout, mas nunca renderizar abaixo do piso de 20pt |
| Cor como informação | Nunca isolada. A cor do card é decorativa; toda informação tem texto ou ícone |
| Redução de movimento | Respeitar `prefers-reduced-motion` do iOS: desliga as transições da 16.6 |
| Toque acidental | Ações destrutivas sempre com confirmação (0.3). Nenhuma ação irreversível a um toque |

### 17.4 Bateria, dados e binário

| Requisito | Alvo |
| :---- | :---- |
| Consumo de dados | **Zero.** Não há requisição |
| Trabalho em background | Nenhum além das notificações agendadas pelo iOS. Sem background fetch, sem timer, sem localização |
| Bateria | Consumo desprezível — o app só roda quando aberto |
| Tamanho do binário | **\< 40 MB.** Com a fonte empacotada e sem imagem pesada, é folgado. Se passar, o problema é asset desnecessário |

### 17.5 Volume de dados

| Cenário | Volume |
| :---- | :---- |
| Uso típico | 4 a 8 remédios, 6 a 12 horários |
| Uso pesado realista | 20 remédios, 40 horários |
| Limite testado | 50 remédios, 100 horários — a Home deve continuar em 60 fps |
| Crescimento | Praticamente nulo: sem histórico, sem log, o banco fica em poucos KB para sempre |

O volume baixo é o que justifica várias simplificações (7.10, 11.7): não há problema de escala neste produto.

### 17.6 Compatibilidade

| Item | Requisito |
| :---- | :---- |
| iOS mínimo | 16.0 (ver 4.5) |
| Aparelhos | iPhone SE 2ª geração em diante |
| Orientação | Retrato apenas |
| iPad | Modo compatibilidade, sem layout dedicado |
| Idioma | pt-BR apenas |
| Modo escuro | Não suportado; app forçado em claro (16.7) |

---

## 18\. Convenções de código, documentação e repositório

O que faz o código continuar editável por prompt daqui a seis meses.

### 18.1 Idioma — banco em português, código em inglês

**Regra, não sugestão.**

| Camada | Idioma | Exemplos |
| :---- | :---- | :---- |
| Tabelas, colunas, enums, índices, triggers, constraints | **Português**, `snake_case` | `remedios`, `hora_minuto`, `arquivado_em`, `ck_remedios_frequencia`, `trg_remedios_atualizado_em` |
| Tipos que espelham o banco | **Português** nas propriedades | `type Remedio = { id: number; nome: string; horaMinuto: string }` — a propriedade mantém o nome do domínio, em camelCase |
| Código, funções, variáveis, arquivos, componentes | **Inglês** | `useMedications()`, `formatTime()`, `MedicationCard.tsx` |
| Comentários e documentação | **Português** | ver 18.2 |
| Strings de interface | **Português** | seção 15 |

**A fronteira fica na camada de acesso a dados** (13-A.1): o repositório fala português com o banco e devolve objetos cujas propriedades de domínio estão em português; o resto do app é código em inglês manipulando esses objetos.

Exceção: nenhuma. Este projeto não estende banco existente.

### 18.2 Comentários obrigatórios

Toda função, hook, componente com lógica, rotina de serviço e trigger leva **comentário de cabeçalho em português** dizendo:

1. o que faz;  
2. o que recebe;  
3. o que devolve;  
4. **qual regra do spec implementa, com o número da seção**.

Lógica não óbvia (cálculo, condição composta, transformação, contorno de limitação) leva comentário inline explicando **a intenção**, não a sintaxe.

/\*\*

 \* Calcula quantos pedidos de notificação um conjunto de remédios consome

 \* da cota do iOS.

 \*

 \* Recebe: lista de remédios ativos com seus horários.

 \* Devolve: número total de pedidos que seriam agendados.

 \*

 \* Implementa a regra 11.4 do spec: o iOS aceita no máximo 64 notificações

 \* locais pendentes por app e descarta o excedente em silêncio, então o

 \* app precisa saber o custo ANTES de agendar.

 \*/

export function calcularCustoDeAgendamento(remedios: RemedioComHorarios\[\]): number {

  return remedios.reduce((total, remedio) \=\> {

    if (\!remedio.lembreteAtivo) return total;

    switch (remedio.frequencia) {

      case 'quando\_precisar':

        return total;

      case 'diario':

        // Trigger de calendário repetitivo: 1 pedido por horário, para sempre (11.4)

        return total \+ remedio.horarios.length;

      case 'dias\_semana':

        // O iOS não repete "seg/qua/sex" num pedido só — precisa de um por

        // combinação dia × horário (11.4)

        return total \+ remedio.diasSemana.length \* remedio.horarios.length;

      case 'intervalo\_dias':

        // Não existe trigger nativo de "a cada N dias". Agendamos datas

        // concretas cobrindo a janela de 30 dias da regra 11.3, e a

        // reconciliação (11.5) renova essa janela a cada abertura.

        return total \+ Math.ceil(30 / remedio.intervaloDias) \* remedio.horarios.length;

    }

  }, 0);

}

Comentário ruim: `// incrementa o total`. Comentário certo: o de cima — ele explica **por que** `dias_semana` multiplica, que é exatamente o que ninguém lembra depois.

### 18.3 Estrutura de pastas

Módulo por feature, com componentes, hooks e tipos juntos.

hora-do-remedio/

├── app/                          \# rotas do Expo Router (seção 10\)

│   ├── \_layout.tsx

│   ├── index.tsx                 \# Home

│   ├── onboarding/

│   │   ├── index.tsx

│   │   └── privacidade.tsx

│   ├── remedio/

│   │   ├── novo.tsx

│   │   └── \[id\]/

│   │       ├── index.tsx

│   │       └── editar.tsx

│   ├── permissao-aviso.tsx

│   └── ajustes/

│       ├── index.tsx

│       ├── arquivados.tsx

│       └── sobre.tsx

├── src/

│   ├── db/

│   │   ├── schema.ts             \# schema Drizzle — tabelas em português (18.1)

│   │   ├── client.ts             \# abertura, PRAGMAs (7.7), migrations

│   │   ├── seed.ts

│   │   └── migrations/           \# geradas pelo drizzle-kit, empacotadas

│   ├── features/

│   │   ├── medications/          \# cards, formulário, hooks, validação

│   │   ├── reminders/            \# servicoLembretes, reconciliador (13-A.2/3)

│   │   ├── export/               \# exportadorLista (13-A.4)

│   │   └── settings/

│   ├── repositories/

│   │   └── medications.ts        \# repositorioRemedios (13-A.1)

│   ├── components/               \# TextoGrande, BotaoPrimario, Card, ...

│   ├── theme/

│   │   ├── colors.ts             \# 16.3 e 16.4

│   │   ├── typography.ts         \# 16.2

│   │   └── spacing.ts            \# 16.5

│   ├── constants/

│   │   └── textos.ts             \# todas as strings da 15.5

│   └── types/

│       └── domain.ts             \# tipos com propriedades em português (18.1)

├── assets/

│   ├── fonts/                    \# Nunito empacotada

│   └── images/

├── SCHEMA.md                     \# gerado do banco real (18.4)

├── DOCUMENTACAO-TECNICA.md       \# entregável da seção 19

├── README.md

├── app.json

└── spec.md                       \# este documento, versionado junto

### 18.4 Documentação viva

| Arquivo | Conteúdo | Quando atualiza |
| :---- | :---- | :---- |
| `SCHEMA.md` | Dump do schema real do SQLite (tabelas, colunas, tipos, constraints, triggers), gerado a partir do banco — não escrito à mão | A cada migration |
| `README.md` | Setup local, versão exigida de Node e Xcode, como rodar no simulador e no aparelho, como gerar migration, como rodar os testes, como buildar no EAS | A cada mudança de processo |
| `DOCUMENTACAO-TECNICA.md` | Seção 19 | A cada mudança de schema, trigger ou lógica de tela — **no mesmo commit** |
| `spec.md` | Este documento | A cada mudança de escopo |

### 18.5 Variáveis de ambiente

**O app não tem nenhuma variável de ambiente e nenhum segredo.** Não há `.env`, não há chave, não há token. Se algum aparecer no repositório, é erro.

Reservado para o futuro (não criar agora): a v2 com OCR (28.1) continua sem segredo, porque o Apple Vision é local. Só um backend de cuidador (28.4) mudaria isso.

### 18.6 Qualidade e commits

| Item | Regra |
| :---- | :---- |
| TypeScript | `strict: true`. Zero `any` — se precisar, comente o porquê |
| Lint | ESLint com o config do Expo \+ Prettier. Sem warning no `main` |
| Commits | Mensagens em português, imperativo: "adiciona tela de cadastro de remédio" |
| Escopo do commit | Um assunto por commit. Mudança de schema vem com a atualização do `SCHEMA.md` e do `DOCUMENTACAO-TECNICA.md` no mesmo commit |
| Branch | `main` sempre buildável |

---

## 19\. `DOCUMENTACAO-TECNICA.md` — entregável obrigatório

Ao terminar a implementação, o agente de código **deve** gerar `DOCUMENTACAO-TECNICA.md` na raiz, em português, descrevendo o sistema **como ele ficou de fato** — não como foi planejado. É o documento que permite retomar o projeto (ou trocar de agente) sem engenharia reversa.

Sem ele, a feature não está pronta (26.6).

Conteúdo mínimo, nesta ordem:

### 19.1 Modelo de dados explicado em português

Cada tabela: para que serve em linguagem de negócio, o que cada coluna significa (não só o tipo), quais valores cada enum aceita e o que cada valor quer dizer na prática. Para cada coluna nullable, em que caso ela fica nula.

### 19.2 Relacionamento entre as tabelas

Diagrama em texto e, para cada relação, uma frase de cardinalidade no vocabulário do negócio: "um remédio tem de um a seis horários; um horário pertence a exatamente um remédio e some junto com ele".

### 19.3 Regras que vivem no banco

Lista de **todas** as triggers, constraints nomeadas, índices e PRAGMAs criados, com **nome exato**, tabela, quando disparam e o que fazem em português. Deve dar para ler essa lista e entender tudo o que o app faz sem o código ver.

### 19.4 Mapa tela → tabelas

Para cada uma das 11 telas (seção 10): quais tabelas lê, quais escreve, e por quais funções de repositório/serviço passa. É o mapa que evita que uma mudança de schema quebre uma tela sem ninguém perceber.

### 19.5 Lógica das principais funcionalidades por tela

Para cada tela, em cada ação principal: o que é validado, o que é gravado, o que é calculado e **onde** (tela, serviço ou banco), e o que o usuário vê quando dá errado. Descreva com atenção especial o agendamento de notificações — é a parte do sistema mais difícil de reconstruir só lendo o código.

### 19.6 Divergências em relação ao spec

O que foi implementado diferente do especificado e **por quê**. Sem esta seção o documento envelhece mentindo.

**Regra de manutenção:** toda mudança em schema, trigger ou lógica de tela atualiza este documento no mesmo commit (18.6).

---

## 20\. Privacidade, segurança e conformidade

### 20.1 Que dados são coletados

| Dado | Natureza | Por quê |
| :---- | :---- | :---- |
| Nome do remédio, dose, forma | **Dado pessoal sensível de saúde** (LGPD, art. 5º, II) | É o produto |
| Horários e frequência | Sensível de saúde | Gerar os lembretes |
| Observações do usuário | Sensível de saúde (texto livre) | Instruções que a pessoa quer lembrar |
| Preferências de app | Não sensível | Tamanho de fonte, som |

**Não coletados:** nome, idade, e-mail, telefone, CPF, localização, contatos, foto, identificador de dispositivo, IDFA. O app não pergunta quem a pessoa é.

### 20.2 Onde os dados ficam

- Exclusivamente no arquivo SQLite dentro do **sandbox do aplicativo**, no aparelho.  
- **Nunca** saem do aparelho por iniciativa do app. A única saída possível é a exportação da seção 14 — **acionada manualmente pelo usuário**, para o destino que ele escolher na share sheet.  
- Podem ser incluídos no backup do iCloud do aparelho, se o usuário tiver backup ligado. É comportamento padrão do iOS, sob controle dele, e deve ser mencionado na tela "Sobre o app".

### 20.3 Quem acessa

Só quem tem o aparelho desbloqueado. Não há conta, servidor, administrador, suporte com acesso, nem funcionário que possa ver dado de usuário — porque não existe lugar nenhum para ver.

### 20.4 Retenção, exportação e apagamento

| Direito (LGPD) | Como é atendido |
| :---- | :---- |
| Acesso | Todo dado está visível na interface; nada é guardado escondido |
| Portabilidade | Exportação em texto legível (seção 14), a dois toques |
| Eliminação | Ajustes → "Apagar todos os meus dados" (8.6): apaga o banco e cancela todas as notificações. Desinstalar o app também remove tudo com o sandbox |
| Retenção | Indefinida enquanto o usuário quiser. O app não expira nem descarta nada sozinho |
| Consentimento | Não há tratamento por terceiro. O tratamento é local, pelo próprio titular, no próprio aparelho |

**Enquadramento LGPD:** como não há transmissão, não há controlador operando servidor e não há compartilhamento com terceiros, o app fica fora do escopo de tratamento remoto. Isso é consequência direta do princípio 0.4 — a arquitetura local-first é também a estratégia de conformidade. **Ver a pergunta aberta 25.4 sobre validação jurídica.**

### 20.5 Segurança

| Item | Decisão |
| :---- | :---- |
| Criptografia do banco | **Não implementada no MVP.** O sandbox do iOS \+ o passcode do aparelho já protegem contra acesso por outro app. SQLCipher adicionaria dependência nativa e chave para gerenciar, sem ganho real no modelo de ameaça (aparelho pessoal do idoso) |
| Face ID para abrir o app | **Não no MVP** (5.2). Barreira a mais para quem já tem dificuldade, protegendo contra uma ameaça que quase não existe aqui. Ver 28.7 |
| Segredos e chaves | **Nenhum existe** (18.5) |
| Rede | Nenhuma requisição — nenhuma superfície de ataque de rede |
| Logs | Log local só em desenvolvimento. Build de produção não registra nome de remédio em log |
| Dependências | Só as da seção 4.3, todas do próprio SDK. Rodar `npm audit` antes de cada release |

### 20.6 App Store — privacidade

| Item | Resposta |
| :---- | :---- |
| Coleta de dados (nutrition label) | **"Data Not Collected"** — nada sai do aparelho |
| Categoria | Medical **ou** Health & Fitness — ver pergunta aberta 25.5 |
| Classificação etária | 4+ |
| Política de privacidade | Obrigatória mesmo sem coleta. Página simples, uma frase: os dados ficam no aparelho, nada é enviado. Ver 25.6 |
| Disclaimer médico | Recomendado no "Sobre o app": *"Este app é um lembrete pessoal. Ele não substitui a orientação do seu médico."* Reduz risco de rejeição na categoria Medical |

### 20.7 IA e guardrails

**O MVP não embute nenhum modelo de IA.** Nada é gerado, sugerido ou inferido — o app repete o que o usuário digitou (1.3).

Quando o OCR entrar (28.1), valem estes guardrails, definidos desde já:

| Guardrail | Regra |
| :---- | :---- |
| G1 | **Nada é salvo sem confirmação humana explícita.** O OCR só preenche um formulário; quem grava é o toque do usuário em "Salvar" |
| G2 | Todo campo extraído entra **editável** e visualmente marcado como "veio da receita, confira" |
| G3 | O app **nunca** infere dose, frequência ou horário não escritos na receita. Campo não encontrado fica vazio, não chutado |
| G4 | Confiança baixa do reconhecimento (limiar da própria API do Vision, não julgamento de modelo) → o campo entra vazio, não com o palpite |
| G5 | Aviso fixo e não dispensável na tela de confirmação: *"Confira com a receita antes de salvar. A leitura automática pode errar."* |
| G6 | A imagem da receita **não** é armazenada. É processada em memória e descartada ao sair da tela |

G4 é guardrail de verdade porque a detecção não depende do modelo se autoavaliar — usa o score da API. Guardrail que pergunta ao modelo se ele acertou não é guardrail.

---

## 21\. Métricas de sucesso e telemetria

### 21.1 Telemetria

**Nenhuma.** Sem analytics, sem crash reporting, sem eventos, sem SDK de terceiro. Consequência de 0.4 e 20.6 — instalar analytics obrigaria a mudar o nutrition label para "Data Collected", que é exatamente o que este produto não quer.

Isso significa, honestamente, que **as métricas abaixo não são medidas por instrumento.** São verificadas por conversa e observação. Está certo assim: o produto tem um usuário conhecido e o custo de instrumentar é maior que o benefício.

### 21.2 Métricas de sucesso

| \# | Métrica | Alvo | Prazo | Como verificar |
| :---- | :---- | :---- | :---- | :---- |
| 21.2.1 | O pai do usuário cadastra **sozinho** um remédio novo, sem ajuda por telefone | 1 cadastro autônomo | 30 dias após instalar | Perguntar |
| 21.2.2 | O app continua instalado e com lembretes ligados | ainda instalado | 60 dias | Verificar no aparelho |
| 21.2.3 | A lista é compartilhada ao menos uma vez com um familiar ou levada ao médico | 1 uso real | 90 dias | Perguntar |
| 21.2.4 | Nenhum lembrete perdido relatado por falha do app | zero relatos | contínuo | Perguntar |

**Critério de fracasso, escrito de propósito:** se em 30 dias o pai não tiver conseguido cadastrar sozinho, o problema é o formulário de 8.2 — e a resposta é simplificar o cadastro, não adicionar tutorial.

---

## 22\. Estratégia de teste

Sem exagero, mas sem deixar de fora o que quebra em silêncio. O critério é simples: **falha que o usuário não percebe na hora precisa de teste automatizado.** Um lembrete que não toca é invisível até ser tarde.

### 22.1 Testes automatizados obrigatórios

Runner: Jest (o que vem com o preset do Expo).

| \# | O que testar | Por que é obrigatório |
| :---- | :---- | :---- |
| 22.1.1 | `calcularCustoDeAgendamento` — todos os casos de frequência, incluindo `quando_precisar` \= 0 e a multiplicação de `dias_semana` | Erro aqui \= lembrete descartado pelo iOS em silêncio (11.4) |
| 22.1.2 | `montarAgendamentos` — triggers corretos para cada frequência; `intervalo_dias` gerando `⌈30/N⌉` datas espaçadas certo | Idem |
| 22.1.3 | `gerarTexto` — comparação **literal** com o exemplo da 14.1, mais os casos: sem dose, sem observação, só "quando precisar", lista vazia | O critério de 8.5 é caractere por caractere |
| 22.1.4 | Codificação/decodificação de `dias_semana` (`"246"` ↔ segunda/quarta/sexta) | Erro silencioso de um dia de deslocamento |
| 22.1.5 | Constraints do banco: rejeitar `frequencia='diario'` com `intervalo_dias` preenchido; rejeitar horário duplicado; cascade funcionando | Garante que 7.2/7.3 foram implementadas de fato |
| 22.1.6 | `repositorioRemedios.criar` e `.atualizar` — transação desfaz tudo em caso de falha parcial | Remédio salvo sem horário é estado inválido |
| 22.1.7 | Regras de agrupamento e ordenação da Home (11.7) | Erro aqui esconde remédio da lista |

### 22.2 Validação manual em aparelho real

Estes não dá para automatizar de forma útil — precisam de iPhone, não de simulador.

| \# | Caso | Como |
| :---- | :---- | :---- |
| 22.2.1 | O lembrete diário dispara com o app **fechado**, no dia seguinte | Cadastrar às 21:00 para as 08:00, fechar o app, esperar |
| 22.2.2 | O lembrete dispara com o aparelho **bloqueado** e mostra nome e dose na tela de bloqueio | Observar |
| 22.2.3 | Negar a permissão e verificar a faixa da Home e o atalho para os Ajustes do iOS | Manual |
| 22.2.4 | Mudar o fuso do aparelho e conferir que o lembrete das 08:00 toca às 08:00 do fuso novo (11.6) | Ajustes do iOS |
| 22.2.5 | Estourar a cota: cadastrar remédios até passar de 64 agendamentos e conferir a degradação da 11.4 | Manual |
| 22.2.6 | Modo avião do primeiro uso ao lembrete, sem nenhuma degradação (17.2) | Manual |
| 22.2.7 | Compartilhar no WhatsApp real e ler o texto como um filho leria (2.3) | Manual |
| 22.2.8 | VoiceOver ligado, percorrer as 11 telas | Manual |
| 22.2.9 | Escala `gigante` em todas as telas, procurando texto cortado ou sobreposto | Manual |
| 22.2.10 | Fechar o app durante o salvamento e reabrir — nenhum dado parcial | Manual |
| 22.2.11 | Restaurar backup do iCloud num aparelho e conferir que a reconciliação (11.5) reagenda tudo | Manual, quando possível |
| 22.2.12 | **Teste com o usuário real** — o pai, sozinho, cadastrando um remédio, sem ninguém falar nada | Observar em silêncio e anotar onde ele trava |

O 22.2.12 é o teste mais importante deste documento. Ele deve acontecer **antes** do release, não depois.

### 22.3 Casos de borda a cobrir

| Caso | Comportamento esperado |
| :---- | :---- |
| Remédio com nome de 60 caracteres | Card quebra em duas linhas, não corta com reticências |
| Horário 00:00 | Grupo "Madrugada — 00:00", funciona normalmente |
| `intervalo_dias = 30` | Gera 1 agendamento na janela de 30 dias |
| 6 horários no mesmo remédio | Todos aparecem, o 7º é bloqueado (11.3) |
| Todos os remédios arquivados | Home no estado vazio; botão de compartilhar desabilitado |
| Só remédios "quando precisar" | Nenhum grupo de horário; bloco próprio; nenhuma notificação agendada |
| Observação de 200 caracteres | Card cresce, texto inteiro visível, sem truncar |
| Dois remédios de mesmo nome | Permitido, ambos aparecem (7.2) |
| Arquivar e desfazer duas vezes seguidas | Estado final consistente, sem notificação duplicada |
| Cadastrar às 23:59 um lembrete para 00:01 | Dispara em 2 minutos |

---

## 23\. Riscos e mitigação

| \# | Risco | Impacto | Prob. | Mitigação |
| :---- | :---- | :---- | :---- | :---- |
| 23.1 | **A cota de 64 do iOS estoura** com um usuário de muitos remédios em `dias_semana` (7 dias × 6 horários \= 42 num remédio só) e lembretes somem em silêncio | **Alto** — é a promessa central do produto | Média | Regra 11.4 implementada com o cálculo de custo *antes* de agendar, degradação explícita ao usuário, teste 22.1.1 e validação 22.2.5. Limite de 6 horários por remédio (11.3) contém o pior caso |
| 23.2 | **`intervalo_dias` depende de o app ser aberto** para renovar a janela. Um usuário como o do fluxo 9.5, que nunca abre o app, para de receber esses lembretes depois de 30 dias | Alto para quem usa essa frequência | Média | Janela de 30 dias (folgada), reconciliação em toda abertura (11.5). **Mitigação adicional a considerar:** se o uso real mostrar que ninguém abre o app, avaliar mudar a frequência para uma aproximação em `dias_semana`, ou aumentar a janela até o limite da cota. Reavaliar após 22.2.12 |
| 23.3 | **O idoso não consegue completar o cadastro sozinho** e abandona o app | **Fatal** — o produto não existe sem isso | Média | Toda a seção 2.2, o formulário de 8.2 com defaults preenchidos, e o teste 22.2.12 **antes** do release. Métrica 21.2.1 |
| 23.4 | Rejeição na App Store na categoria **Medical**, por parecer aconselhamento de saúde | Médio — atrasa o lançamento | Baixa | Disclaimer da 20.6, nutrition label "Data Not Collected", zero afirmação clínica no texto do app e da ficha da loja. Decidir a categoria em 25.5 |
| 23.5 | **Expo SDK 54 vai ficando velho** — em algum momento a Apple exige um SDK do Xcode que o RN 0.81 não acompanha | Médio, no médio prazo | Alta (é questão de tempo) | Decisão consciente do usuário. O app é pequeno e sem backend: o upgrade de SDK é dos mais simples que existem. Reavaliar a cada exigência nova da Apple |
| 23.6 | **Perda de dados** por falha de migration ou banco corrompido, sem backup próprio | Alto — o usuário perde a lista inteira | Baixa | Nunca apagar banco automaticamente (12.4), backup do iCloud do aparelho (20.2), e a exportação como cópia manual. **Aceito conscientemente:** backup próprio exigiria nuvem, que viola 0.4 |
| 23.7 | **Notificação silenciosamente descartada pelo iOS** por motivos fora do controle do app (restore de backup, mudança de fuso, Foco/Não Perturbe) | Médio | Média | Reconciliação em toda abertura (11.5). **Limite honesto:** contra o modo Foco do iOS não há o que fazer — é escolha do usuário no sistema |
| 23.8 | **OCR da v2 frustra** por acerto baixo em receita manuscrita e queima a confiança no app | Médio | **Alta**, se mal feito | Fora do MVP por decisão. Guardrails G1–G6 (20.7). Entrar primeiro só com receita **impressa**, e medir antes de prometer manuscrito |
| 23.9 | O usuário **muda de ideia sobre registro de tomada** depois da v1, e o modelo de dados não previu | Baixo | Média | Assunção 24.1 declarada, spec da v2 pronto em 28.2 com o modelo já desenhado — a mudança é aditiva, não destrutiva |
| 23.10 | Um familiar instala e cadastra **errado** (dose trocada), e o app repete o erro com autoridade de tela | Médio, com risco real à saúde | Baixa | O app nunca afirma nada clinicamente (1.3), disclaimer da 20.6, e a exportação (14) facilita a conferência por um terceiro — que é justamente a mitigação social do problema |
| 23.11 | Os alvos de performance da 17.1 foram fixados no **iPhone SE 2ª geração**, mas o único aparelho físico disponível para teste é um **iPhone 13** (bem mais potente) — um problema de performance no aparelho mais fraco pode passar despercebido até o pai testar | Médio | Média | Antes do release, emular limitação de CPU/memória no simulador do Xcode (Debug → Simulate Location não serve; usar o painel de condição de rede/CPU do Instruments) como aproximação. Se possível, pedir emprestado um iPhone SE ou 8 para uma checagem rápida antes do TestFlight ir para o pai |

---

## 24\. Assunções declaradas

Decisões tomadas sem perguntar, por serem de baixo impacto ou por não valerem uma rodada de conversa.

**Aviso: assunção não revisada vira verdade no minuto em que o agente começa a codar.** Revise esta seção antes de rodar o prompt de implementação.

| \# | Assunção | Por quê | Se você discordar |
| :---- | :---- | :---- | :---- |
| 24.1 | **Sem nenhum registro de tomada, nem opcional.** Você escolheu "só lembrar"; eu levei isso até o fim — não há botão "já tomei", nem histórico, nem contador | Meio-termo seria pior: um botão que às vezes é tocado gera histórico furado, e histórico furado gera a pergunta "por que o app diz que não tomei?" | É a mudança mais cara depois de pronta. O modelo está desenhado em 28.2 — se quiser já na v1, me avise **antes** de rodar o prompt |
| 24.2 | Máximo de **6 horários** por remédio | Contém o pior caso da cota de 64 (23.1) e cobre o uso realista | Diga o número. Acima de 8 a cota vira problema sério |
| 24.3 | Janela de **30 dias** para `intervalo_dias` | Equilíbrio entre custo de cota e sobreviver a um usuário que não abre o app | Posso aumentar, à custa de cota |
| 24.4 | **Soft delete** (arquivar) em vez de excluir remédio | Tratamento cíclico é comum; recadastrar afasta este público (2.1) | Se quiser exclusão definitiva individual, é só avisar |
| 24.5 | **Sem modo escuro** (16.7) | Um único conjunto de contraste verificado em AAA | Dá para adicionar, dobrando a verificação de contraste |
| 24.6 | **Sem analytics, sem crash reporting** (21.1) | Manteria o nutrition label em "Data Not Collected" | Se quiser crash reporting, ele muda a ficha de privacidade da App Store |
| 24.7 | Fonte **Nunito** | Arredondada e alegre, casa com as referências, ótima em corpo grande | Sugira outra; troca é barata agora, cara depois |
| 24.8 | Paleta de **6 cores pastel** com atribuição em rodízio (16.3, 11.8) | Diferenciação visual sem virar semáforo clínico | Posso trocar as cores ou o número |
| 24.9 | **Tratamento por "você"**, nunca "senhor" (15.2) | "Senhor" cria distância e sublinha a idade | Diga a preferência do seu pai |
| 24.10 | Notificação **por remédio**, nunca agrupada (15.3) | A pessoa precisa saber **o quê** tomar sem abrir o app | Agrupar economiza cota, ao custo da clareza |
| 24.11 | Duas telas de onboarding, **sem tutorial** | Ninguém deste público lê a terceira | Posso adicionar uma terceira, contra a evidência |
| 24.12 | **Sem Face ID** para abrir o app (20.5) | Barreira contra ameaça quase inexistente no aparelho pessoal | Fácil de adicionar depois (28.7) |
| 24.13 | **Sem criptografia** do banco (20.5) | Sandbox \+ passcode já cobrem o modelo de ameaça | SQLCipher é possível, com dependência nativa a mais |
| 24.14 | Exportação **só texto**, sem PDF (14) | Foi a sua escolha; texto cai direto no WhatsApp | PDF está especificado em 28.8 |
| 24.15 | iOS mínimo **16.0** (4.5) | Cobre iPhone SE 2ª geração em diante e já prepara o Vision da v2 | Se o seu pai tiver um aparelho mais antigo, **me diga agora** |
| 24.16 | Nome de exibição **"Hora do Remédio"** | Sua escolha entre as opções | Alternativas em 1.4 |
| 24.17 | Bundle id `com.dablab.horadoremedio` (**atualizado na v1.1** — corrige o palpite anterior `br.com.dablab...`) | Confirmado pelo padrão real dos seus apps já publicados (`com.dablab.gearaapp`, `com.dablab.bemviver`, `com.dablab.tadandocerto`), visto no roteiro de publicação que você enviou (Anexo B) | Se preferir outro, é só trocar antes de criar o App ID no Developer.apple.com |
| 24.21 | Fluxo de submissão à App Store reaproveita a mesma conta Apple Developer e o mesmo padrão de passos (Identifiers → App Store Connect → API Key → build → TestFlight → Review) que você já usou em mais de 10 apps via FlutterFlow | O processo de conta/loja é independente da ferramenta que gera o build; só o passo "gerar e enviar o build" muda de FlutterFlow para EAS Build (Anexo B) | Se preferir subir manualmente pelo Transporter em vez de `eas submit`, funciona igual — é só uma escolha no passo B.6 |
| 24.18 | Um remédio com vários horários **aparece várias vezes** na Home (11.7.2) | O modelo mental é "o que eu tomo às 8h" | A alternativa é um card por remédio com os horários listados dentro. Diga se preferir |
| 24.19 | Home mostra **todos** os horários do dia, inclusive os já passados (11.7.5) | A lista é referência, não fila de tarefas | Esconder os passados aproximaria de app de adesão, que está no anti-escopo |
| 24.20 | Sem widget, sem Apple Watch, sem Siri no MVP | Cada um é um alvo de build a mais | Widget está em 28.6 |

---

## 25\. Perguntas abertas

Só você pode decidir. **O agente de código deve perguntar em vez de assumir** ao cruzar com qualquer item desta seção.

| \# | Pergunta | Quem decide | Quando precisa estar decidido |
| :---- | :---- | :---- | :---- |
| 25.1 | ✅ **Bundle id resolvido** (24.17/24.21): `com.dablab.horadoremedio`, mesma conta Apple Developer usada em Geara, Bem Viver e Tá Dando Certo. **Ainda falta confirmar:** é a mesma conta/Team de sempre, certo? Se for uma conta nova, os passos do Anexo B mudam no primeiro item (criar a conta, US$ 99/ano) | Alexandre | Antes do primeiro build |
| 25.2 | **O nome "Hora do Remédio" está disponível na App Store?** Precisa ser verificado antes de virar identidade visual e ícone | Alexandre | Antes do ícone e da ficha da loja |
| 25.3 | ✅ **Parcialmente resolvido:** o aparelho de desenvolvimento e teste é o seu iPhone 13, com iOS 26.5.2. Isso confirma que o mínimo de iOS 16 (24.15) está confortável — seu aparelho de teste está muito acima do piso. **Ainda falta:** qual iPhone o seu pai usa de fato, para validar o alvo de performance da 17.1 (o piso continua sendo o iPhone SE 2ª geração; se não houver um à mão para testar, considerar emular limitação de CPU no simulador do Xcode antes do release — ver risco novo 23.11) | Alexandre | Antes do release para o pai (não bloqueia o início da 8.0) |
| 25.4 | **Validação jurídica do enquadramento LGPD** (20.4). O raciocínio "sem servidor, sem tratamento remoto" me parece sólido, mas eu não sou advogado — vale uma conferida se o app for público | Alexandre / assessoria | Antes de publicar |
| 25.5 | **Categoria na App Store: Medical ou Health & Fitness?** Medical tem revisão mais rigorosa (23.4); Health & Fitness é mais leve, mas pode ser questionado. Precisa de pesquisa nas diretrizes atuais | Alexandre | Antes de submeter |
| 25.6 | **Onde hospedar a política de privacidade e a página de suporte?** A Apple exige as duas URLs mesmo sem coleta de dados (20.6). Pelo seu próprio histórico (Anexo B): uma página do Notion **funcionou** para Política de Privacidade num dos seus apps, mas **já causou rejeição** quando usada como página de Suporte — a Apple quer algo que pareça "página real publicada" para suporte. Sugestão: Notion para a política, Carrd (como você já usa) para o suporte | Alexandre | Antes de submeter |
| 25.7 | ✅ **Resolvido: vai para a App Store de verdade**, não só TestFlight familiar. Isso eleva a prioridade de 25.4 (LGPD), 25.5 (categoria) e 25.6 (política de privacidade) — as três precisam de resposta antes da submissão, não são mais opcionais. Roteiro completo no Anexo B | Alexandre | Antes da submissão (ver Anexo B) |
| 25.8 | **A frequência "a cada X dias" é realmente usada** por alguém que você conhece? É a que traz o risco 23.2. Se ninguém usa, cortá-la simplifica bastante o épico 8.4 | Alexandre | Antes do épico 8.4 |

---

## 26\. Definition of Done

Só se diz "acabou" com todos os itens marcados.

### Funcional

- [ ] 26.1 Os 8 épicos da seção 8 implementados, com **todos** os critérios de aceite verificados um a um  
- [ ] 26.2 As 11 telas da seção 10 existem e são alcançáveis  
- [ ] 26.3 As regras da seção 11 implementadas nos lugares indicados na coluna "onde"  
- [ ] 26.4 Os 5 fluxos ponta a ponta da seção 9 percorridos manualmente em aparelho real

### Convenções e código

- [ ] 26.5 **Banco em português**, `snake_case`, sem nenhuma tabela, coluna, enum, índice, constraint ou trigger fora do padrão da 18.1  
- [ ] 26.6 **Todas** as funções, hooks, serviços e triggers com comentário de cabeçalho em português citando a regra do spec que implementam (18.2)  
- [ ] 26.7 `DOCUMENTACAO-TECNICA.md` gerado com as 6 seções da 19, **conferido contra o sistema implementado** — não contra este spec  
- [ ] 26.8 `SCHEMA.md` gerado a partir do banco real e batendo com a seção 7  
- [ ] 26.9 `README.md` com setup, versões, testes e build  
- [ ] 26.10 TypeScript strict sem erro; ESLint sem warning; zero `any` não comentado  
- [ ] 26.11 Nenhuma biblioteca fora da lista da 4.3 sem aprovação

### Não-funcional

- [ ] 26.12 Alvos de performance da 17.1 medidos no aparelho de referência  
- [ ] 26.13 Modo avião do primeiro uso ao lembrete, sem nenhuma degradação (17.2)  
- [ ] 26.14 Nenhum `fontSize` abaixo de 20 em nenhum estilo do projeto (verificável por busca)  
- [ ] 26.15 Nenhuma área tocável abaixo de 60×60pt  
- [ ] 26.16 Contraste ≥ 7:1 conferido par a par nas cores da 16.3 e 16.4  
- [ ] 26.17 VoiceOver percorre as 11 telas com rótulos em português (22.2.8)  
- [ ] 26.18 Escala `gigante` sem texto cortado ou sobreposto em nenhuma tela (22.2.9)  
- [ ] 26.19 Binário abaixo de 40 MB

### Testes

- [ ] 26.20 Os 7 testes automatizados obrigatórios da 22.1 passando  
- [ ] 26.21 Os 12 casos manuais da 22.2 executados em aparelho real  
- [ ] 26.22 Os casos de borda da 22.3 verificados  
- [ ] 26.23 **Teste 22.2.12 realizado com o usuário real** — o pai cadastrando sozinho, em silêncio, com as observações anotadas

### Privacidade e publicação

- [ ] 26.24 Nenhuma requisição de rede no app inteiro (verificável com proxy ou inspetor)  
- [ ] 26.25 Nenhum `.env`, chave ou segredo no repositório (18.5)  
- [ ] 26.26 "Apagar todos os meus dados" apaga o banco **e** cancela todas as notificações (8.6)  
- [ ] 26.27 Nutrition label "Data Not Collected" preenchido  
- [ ] 26.28 Disclaimer médico presente em "Sobre o app" (20.6)  
- [ ] 26.29 Política de privacidade e página de suporte publicadas, vinculadas em App Store Connect, e a política também linkada dentro do app em "Sobre o app" (25.6, Anexo B)  
- [ ] 26.30 Build de produção instalada via TestFlight e testada em aparelho real

---

## 27\. Próximos passos operacionais

1. **Revisar a seção 24 (assunções)** — especialmente 24.1 (sem registro de tomada), 24.15 (iOS 16\) e 24.18 (remédio repetido na Home). É o único passo obrigatório antes de codar.  
2. **25.3 e 25.7 já respondidas** (v1.1): você vai testar no seu iPhone 13/iOS 26.5.2, e o app vai para a App Store de verdade. Isso já eleva a prioridade de 25.1, 25.4, 25.5 e 25.6 — resolva-as com calma durante a implementação, mas antes de chegar ao passo 7\.  
3. **Criar o repositório** `hora-do-remedio`, privado, e commitar este `spec.md` como primeiro commit. O spec versionado junto do código é o que mantém os dois em sincronia.  
4. **Rodar o prompt de implementação** (`prompt-implementacao.md`) no Claude Code. Aprovar o plano antes de deixar codar.  
5. **Ordem de execução:** banco e schema (8.0) primeiro, sempre. Só depois as telas. Schema mal resolvido contamina tudo o que vem em cima.  
6. **Buildar no TestFlight assim que 8.1 e 8.2 estiverem prontos** — antes de terminar o resto. O teste 22.2.12 com o seu pai vale mais cedo, com o app incompleto, do que tarde e completo. Como o app vai para a loja de verdade, esse primeiro build já pode passar pelo fluxo completo do **Anexo B** (App ID, app no App Store Connect, chave de API) — o trabalho de conta não muda entre TestFlight e a loja pública, só o botão final.  
7. **Seguir o roteiro do Anexo B** para a submissão à App Store — ele já resolve 25.1 (bundle id) e reaproveita sua conta de developer; só falta decidir 25.2 (nome disponível), 25.4 (checagem jurídica opcional), 25.5 (categoria) e 25.6 (onde hospedar política e suporte) ao longo do caminho.

---

## 28\. Roadmap: próximas releases

Tudo o que saiu do MVP, especificado com o detalhe preservado — a próxima release começa com o spec pronto.

### 28.1 OCR de receita médica (v2) — **avaliação de viabilidade**

**Veredito: viável offline, e a parte difícil não é o OCR.**

**Viabilidade técnica (verificada, 4.6):** o **Apple Vision** (`VNRecognizeTextRequest`) roda 100% no dispositivo, sem rede, é nativo do iOS (nada a empacotar, zero impacto no binário) e reconhece **pt-BR** oficialmente. Atende ao princípio 0.4 sem concessão. Bibliotecas Expo que expõem isso: `expo-text-extractor` e `expo-mlkit-ocr` (esta com `iosEngine: "vision"`). Sendo o app iOS-only, o Vision é a escolha certa — ML Kit só faria sentido para paridade com Android.

**Onde está o custo real, em ordem:**

1. **Parsear texto de receita.** O Vision devolve linhas soltas de texto, não campos. Transformar `"Losartana 50mg - tomar 1 comprimido de 12/12h"` em `{nome, dose, frequencia, horarios}` é um parser de linguagem natural com heurística de posologia em português — e é aqui que mora o trabalho.  
2. **Receita manuscrita.** Acerto baixo. Letra de médico é o caso adversarial clássico de OCR. **Recomendação: a v2 entra só com receita impressa/digital**, e manuscrito é medido antes de ser prometido (risco 23.8).  
3. **Tela de confirmação.** Precisa mostrar cada campo extraído, editável, com o mesmo padrão de fonte grande — é praticamente um formulário 8.2 com estado extra.

**Escopo da v2:**

| \# | Item |
| :---- | :---- |
| 28.1.1 | Botão "Ler receita" na Home, ao lado de "Adicionar remédio" |
| 28.1.2 | Captura por câmera (`expo-camera`) ou escolha da galeria, com guia de enquadramento e instrução em fonte grande |
| 28.1.3 | Reconhecimento local via Apple Vision, `recognitionLanguages: ["pt-BR"]`, nível `accurate` |
| 28.1.4 | Parser de posologia em português: reconhecer padrões de dose (`50mg`, `1 comprimido`, `10 gotas`), de intervalo (`de 8/8h`, `de 12 em 12 horas`, `1x ao dia`, `2 vezes ao dia`) e de forma |
| 28.1.5 | Conversão de intervalo em horários concretos sugeridos (ex.: `8/8h` → 06:00, 14:00, 22:00), sempre editáveis |
| 28.1.6 | Tela de confirmação listando os remédios encontrados, cada um com todos os campos editáveis, marcados como "veio da receita" |
| 28.1.7 | Seleção do que salvar: a pessoa pode desmarcar o que não quer, e salvar vários remédios de uma vez |
| 28.1.8 | Guardrails G1 a G6 da seção 20.7, sem exceção |
| 28.1.9 | Caminho de escape sempre visível: "Prefiro digitar" leva ao formulário 8.2 em branco |
| 28.1.10 | Modelo de dados: `remedios.origem` (`'manual'` | `'receita'`) e `remedios.confirmado_em`. Aditivo, não quebra o schema da v1 |

**Alvo de qualidade para liberar:** em 10 receitas **impressas** reais, ≥ 80% dos nomes de remédio extraídos corretamente e **zero** campo salvo sem confirmação. Se não bater, a feature não sai.

### 28.2 Registro de tomada (v2, se você mudar de ideia)

Especificado desde já porque é a mudança mais provável e o modelo precisa ser aditivo.

| \# | Item |
| :---- | :---- |
| 28.2.1 | Tabela nova `tomadas`: `id`, `horario_id` (FK), `data_prevista` (ISO), `marcada_em` (ISO), `criado_em`. Uma linha **só quando a pessoa marca** — ausência de linha significa "não marcado", nunca "não tomou" |
| 28.2.2 | Card da Home ganha um botão circular grande de confirmação à direita. Um toque marca, um toque desmarca |
| 28.2.3 | Card marcado: opacidade reduzida e um ✓ grande. Nunca sumir da lista (contraria 11.7.5) |
| 28.2.4 | Marcação vale só para o dia corrente. Vira o dia, tudo volta ao normal, **sem cobrança, sem histórico exposto, sem "você esqueceu"** |
| 28.2.5 | Ação de confirmação direto na notificação (`UNNotificationAction`) — marca sem abrir o app |
| 28.2.6 | **Nada de estatística, gráfico, percentual de aderência ou streak.** Isso continua no anti-escopo (1.3) mesmo com o registro existindo |
| 28.2.7 | Retenção: apagar automaticamente tomadas com mais de 90 dias — não há motivo para guardar mais |

Decisão de desenho preservada: mesmo com registro, o app **não cobra**. A diferença entre "lembrete com confirmação" e "app de aderência" é essa.

### 28.3 Controle de estoque da cartela (v3)

Campos `quantidade_atual` e `quantidade_por_caixa` em `remedios`, decremento a cada tomada marcada (depende de 28.2), e aviso "Está acabando" quando faltar menos de 5 dias. **Alto atrito** — exige o usuário manter a contagem atualizada. Só entra se aparecer demanda real.

### 28.4 Acesso do cuidador (v2/v3)

O filho vê a lista do pai no próprio celular. **Muda a arquitetura inteira:** exige backend, conta, vínculo entre contas, e reintroduz tudo o que as seções 5, 6 e 20 hoje descartam — inclusive tratamento remoto de dado sensível de saúde sob LGPD. Reavaliar só depois de a v1 ter uso real. Alternativa muito mais barata que resolve 80% do caso: **compartilhamento agendado** — o app gera o texto da 14.1 automaticamente uma vez por semana e oferece o envio. Sem backend, sem conta.

### 28.5 Múltiplos perfis no mesmo aparelho (v2)

Marido e mulher no mesmo iPhone. Tabela `pessoas` e `remedios.pessoa_id`, com um seletor grande no topo da Home. Só entra com demanda real — o padrão é cada um ter o seu aparelho.

### 28.6 Widget de tela inicial (v2)

**Alto valor para este público:** ver os remédios de hoje sem abrir o app. Widget pequeno e médio, mostrando o próximo horário e os remédios daquele horário. Exige extensão nativa (WidgetKit) e um segundo alvo de build — daí estar fora do MVP.

### 28.7 Face ID (v2)

`expo-local-authentication`, **opcional e desligado por padrão**, em Ajustes. Só faz sentido se o app passar a guardar mais coisa que a lista de remédios.

### 28.8 Exportação em PDF (v2)

Além do texto, gerar um PDF em fonte grande, pronto para imprimir e levar ao médico ou colar na geladeira. `expo-print` com HTML local (sem rede, mantendo 0.4). Mesmo conteúdo da 14.1, em layout de página A4, com cabeçalho e data.

### 28.9 Lembrete de consulta médica (v3)

Extensão natural, mas amplia o produto de "lembrete de remédio" para "agenda de saúde". Avaliar com cuidado contra o anti-escopo (1.3) antes de aceitar.

---

## 29\. Protocolo de mudanças incrementais

Depois da v1 pronta, siga isto para um ajuste pequeno não virar reescrita do app.

1. **Commit de checkpoint antes de qualquer mudança.** Sem isso não existe `git diff` confiável para revisar depois.  
2. **Não cole o spec inteiro de novo.** Cole só o trecho novo ou alterado, ou um spec adicional específico da feature. Colar o documento todo faz o agente reconsiderar decisões já implementadas.  
3. **Escreva a instrução como diff, não como recomeço.** Deixe explícito que o projeto já existe, funciona, e está publicado.  
4. **Delimite nominalmente o que não deve mudar** — cite os arquivos e as telas que ficam intocados.  
5. **Peça a lista de arquivos que serão criados e alterados antes do código**, e aprove essa lista.  
6. **Revise o `git diff` contra o plano aprovado.** Arquivo que passou do escopo: reverta e refaça o pedido mais específico.  
7. **Exija a atualização do `DOCUMENTACAO-TECNICA.md` no mesmo commit** da mudança.

### 29.1 Modelo de prompt para mudança incremental

O projeto Hora do Remédio já está implementado e funcionando (Expo SDK 54,

iOS only, SQLite local). Não é um projeto novo.

MUDANÇA PEDIDA

\<descreva só o que muda, em uma ou duas frases\>

CONTEXTO DO SPEC

\<cole apenas a seção do spec.md que muda, com o número da seção\>

NÃO DEVE MUDAR

\- O schema das tabelas remedios, horarios e preferencias

\- O serviço de lembretes (src/features/reminders/) e a regra 11.4 da cota de 64

\- As strings existentes em src/constants/textos.ts

\- \<acrescente o que for específico desta mudança\>

CONVENÇÕES (mantidas)

\- Banco em português, snake\_case. Código e variáveis em inglês.

\- Toda função nova ou alterada leva comentário de cabeçalho em português

  dizendo o que faz e qual regra do spec implementa.

ANTES DE CODAR

Me mostre a lista de arquivos que você vai criar e alterar, com uma linha

sobre o que muda em cada um. Espere minha aprovação.

AO TERMINAR

Atualize o DOCUMENTACAO-TECNICA.md e o SCHEMA.md (se o schema mudou) no

mesmo commit.

---

## Anexo A — Fontes

Consultadas em **26 de julho de 2026**.

| Assunto | Fonte |
| :---- | :---- |
| Expo SDK 54: RN 0.81, React 19.1, lançamento 10/09/2025, Xcode 16.1+, Node 20.19.4+, XCFrameworks pré-compilados, remoção de exports deprecados em `expo-notifications` | [Expo Changelog — SDK 54](https://expo.dev/changelog/sdk-54) · [SDK 54 beta](https://expo.dev/changelog/sdk-54-beta) · [expo.dev/sdk/54](https://expo.dev/sdk/54) |
| Limite de 64 notificações locais pendentes por app; sistema mantém as 64 mais próximas; repetitiva conta como uma | [Apple Developer Forums — UNNotificationRequest Scheduling Limitation](https://developer.apple.com/forums/thread/765490) · [Apple Developer Forums — 64-notification limit](https://developer.apple.com/forums/thread/811171) · [UNUserNotificationCenter](https://developer.apple.com/documentation/usernotifications/unusernotificationcenter) |
| Apple Vision `VNRecognizeTextRequest` com suporte a pt-BR, on-device | [Apple Developer Forums — Recognize text from image](https://developer.apple.com/forums/thread/651054) |
| OCR offline em Expo com Apple Vision / ML Kit | [expo-text-extractor](https://github.com/pchalupa/expo-text-extractor) · [expo-mlkit-ocr](https://www.npmjs.com/package/expo-mlkit-ocr) |
| Drizzle ORM com expo-sqlite: migrations empacotadas, `useLiveQuery` | [Drizzle — Expo SQLite](https://orm.drizzle.team/docs/sqlite/connect-expo-sqlite) · [Expo — SQLite](https://docs.expo.dev/versions/latest/sdk/sqlite/) |
| `expo-notifications` — API atual | [Expo — Notifications](https://docs.expo.dev/versions/latest/sdk/notifications/) |

---

---

## Anexo B — Roteiro de publicação na App Store

Adaptado do seu roteiro pessoal (*"Roteiro para apps na AppStore.pdf"*), que você já usou em mais de 10 apps publicados via FlutterFlow (Geara, Bem Viver, Tá Dando Certo, entre outros). A boa notícia: **quase tudo se repete.** A parte que muda é só como o build chega até a Apple — no FlutterFlow, o próprio FlutterFlow enviava; aqui, o build sai do **EAS Build** e sobe por `eas submit` ou manualmente. O resto — conta de developer, criação do App ID, criação do app no App Store Connect, política de privacidade, idade, preço — é o mesmo processo, na mesma conta.

### B.1 O que é idêntico ao seu roteiro antigo

| Passo | Onde | Igual ao que você já fez |
| :---- | :---- | :---- |
| Conta de Apple Developer (US$ 99/ano) | developer.apple.com | Reaproveite a conta já usada em Geara/Bem Viver/Tá Dando Certo — não crie uma nova, a menos que decida separar esta conta por algum motivo (25.1) |
| Criar o App ID | Developer.apple.com → Certificates, Identifiers & Profiles → Identifiers → **\+** | Idêntico. Platform: iOS. Bundle ID: **Explicit**, `com.dablab.horadoremedio` (24.17) |
| Criar o app no App Store Connect | appstoreconnect.apple.com → Apps → **\+** → New App | Idêntico. Platform iOS, Name "Hora do Remédio", Primary Language Portuguese (Brazil), Bundle ID \= o que você criou acima, SKU \= pode repetir o bundle id, User Access Full Access |
| Anotar o **Apple ID** do app (número, em App Information) | App Store Connect → o app → App Information | Idêntico — você vai precisar dele para configurar o EAS (equivalente ao "App ID" que o FlutterFlow também pedia) |
| Gerar a **chave de API** (App Store Connect API Key) | App Store Connect → Users and Access → Integrations → **\+** | Idêntico ao que você fez para o FlutterFlow: nome da chave, acesso **App Manager**, baixar o `.p8` (só dá uma vez), anotar **Key ID** e **Issuer ID** |
| Content Rights | App Information | Idêntico: "No, it does not contain, show, or access third-party content" |
| Encryption compliance ("Missing Compliance") | No build, dentro de App Store Connect | Idêntico: "None of the algorithms mentioned above" — o app não implementa criptografia própria (20.5) |
| Age Rating | App Information → Set Up Age Ratings | Preencher o questionário. Esperado 4+, mas responda com atenção ao bloco "Medical/Treatment Information" do novo questionário (visível no seu roteiro) — o app **não** dá orientação médica, só lembra o que o usuário digitou (1.3), então a resposta correta ali costuma ser "não" |
| Preço | App Store Connect → Pricing and Availability | Igual: US$/R$ 0,00. **Decisão nova para este app** (não estava no roteiro antigo): como o conteúdo é só em português, considere restringir a disponibilidade a Brasil \+ Portugal em vez de "All Countries" — evita usuário estrangeiro instalando um app que ele não vai entender. Fica a seu critério |
| TestFlight | App Store Connect → TestFlight | Idêntico: criar grupo interno, adicionar testadores por e-mail (você mesmo primeiro, depois o pai) |
| Submeter para revisão | App Store Connect → Distribution → Submit to App Review | Idêntico |
| Acompanhar aprovação | E-mail da Apple | Idêntico — esperar "Review of your submission has been completed" |

### B.2 O que **não** se aplica mais (o app não tem conta)

O seu checklist antigo (página 1 do roteiro) resolvia problemas de rejeição ligados a **conta de usuário**. O Hora do Remédio não tem conta nenhuma (seção 5\) — então boa parte desse checklist desaparece sozinha, o que só reduz trabalho:

| Item do checklist antigo | Situação no Hora do Remédio |
| :---- | :---- |
| App com opção para deletar a conta | Não há conta. O equivalente é "Apagar todos os meus dados" (8.6), que já está no MVP e cobre a mesma preocupação da Apple (usuário controla e apaga o próprio dado) |
| App com opção para trocar a senha | Não há conta, não há senha |
| App com opção para sair do app (logout) | Não há conta para sair |
| Opção para entrada sem conta (guest) | O app **inteiro** já é "modo convidado" — mais simples que qualquer um dos seus apps anteriores nesse ponto |
| App com página de Termo de Uso | Provavelmente dispensável: sem conta, sem conteúdo de terceiros, sem compra. Use o **Apple's Standard License Agreement** padrão (a opção que já vem marcada em App Information → License Agreement) em vez de escrever um termo próprio |
| Mensagem de permissão para câmera/galeria/microfone/etc. | Não se aplica ao MVP (v1). Só passa a valer quando o OCR da v2 entrar (28.1) — nesse momento, adicionar `NSCameraUsageDescription` e `NSPhotoLibraryUsageDescription` no `Info.plist`, em português, explicando por que o app pede a câmera |

O que **continua** valendo do checklist antigo: **Política de Privacidade** (agora como link, não como tela dentro do app — ver B.3) e a mensagem de permissão de **notificação**, que o app já trata na tela explicativa da regra 8.4/12.3.

### B.3 Política de privacidade e página de suporte — a lição que vale ouro do seu roteiro

Duas observações concretas do seu próprio histórico, direto do PDF que você mandou:

1. **Página de Suporte no Notion foi rejeitada** uma vez ("Já tive app rejeitado porque tentei usar uma página de suporte no Notion. Precisa ser uma página real publicada"). Você resolveu com **Carrd** (carrd.co) e funcionou desde então.  
2. **Página de Política de Privacidade no Notion foi aceita** em outro app, com link público (`notion.so/...?source=copy_link`).

A distinção não é 100% previsível (comportamento de revisão da Apple varia), mas o padrão dos seus próprios dados é claro: **use Carrd para Suporte, Notion está liberado para Política de Privacidade.** Recomendação para o Hora do Remédio:

- **Suporte:** nova página Carrd, ex. `horadoremedio.carrd.co`, com o mesmo formato que você já usa — um parágrafo dizendo que dúvidas ou pedidos relacionados aos dados podem ser enviados para o seu e-mail, resposta em até 48h úteis.  
- **Política de Privacidade:** pode ser Notion, publicada com link público. Conteúdo real desta vez (diferente do texto genérico do seu template antigo, que fala em "nome e e-mail", "localização", "contatos" — nada disso se aplica aqui): a política deste app deve dizer explicitamente que **nenhum dado sai do aparelho**, que não há conta, e listar os dados de saúde que ficam salvos localmente (remédio, dose, horário — seção 20.1). Reaproveitar o texto genérico do seu template seria impreciso e poderia até chamar atenção da revisão por inconsistência com o "Data Not Collected" do nutrition label.  
- **Link também dentro do app:** diferente dos seus apps anteriores (que tinham a política acessível "from login and profile screens" porque tinham tela de login), aqui o único lugar natural é "Sobre o app" (Ajustes → Sobre) — já adicionado ao spec nesta revisão (seção 8.6, critério de aceite novo).

### B.4 App Privacy — a parte mais simples deste app

Nos seus apps anteriores, o wizard de App Privacy exigia mapear cada dado coletado (e-mail, nome, localização) numa lista de categorias da Apple. Aqui não: siga o wizard e marque **"Data Not Collected"** em tudo — é literalmente verdade (seção 20.6), porque não há coleta, não há analytics, não há SDK de terceiro. É o único app do seu portfólio onde essa resposta é direta em vez de uma lista longa de marcações.

### B.5 Notas para o revisor da Apple (App Review Information)

Adaptando o texto que você já usa (que menciona itens que causaram rejeição no passado — login de teste, acesso anônimo). Como este app não tem login, o quadro fica mais simples: **desmarque "Sign-in required"** (não há tela de login para desmarcar sequer) e use notas assim:

Thank you for reviewing the Hora do Remédio app.

No Account Required

This app has no login, no account, and no sign-up. It opens directly

to the main screen. There is nothing to sign in with.

Main Features in This Version:

• Register recurring medications with dose, frequency and time

• View the active medication list on the home screen

• Optional local reminders (iOS notifications) per medication

• Export the active medication list as text, via the native iOS

  share sheet (e.g. to send via WhatsApp or email to a family member)

Data & Privacy:

• All data is stored locally on the device only (SQLite). Nothing is

  sent over the network — the app makes zero network requests.

• There is no analytics, no crash reporting, and no third-party SDK.

• Users can delete all their data at any time from Settings ("Apagar

  todos os meus dados").

• Privacy Policy is linked from Settings → About, and from the App

  Store listing.

Platform & Testing Notes:

• This build targets iPhone only, portrait orientation, light mode only

• No special hardware is required

• The app is fully functional offline (airplane mode) — there is no

  backend of any kind

• Built with Expo (React Native) and a local SQLite database

If you need anything else, feel free to reach me at

alexandre@dablab.com.br

Thanks again for your time and review.

Alexandre Siqueira

Sem credenciais de teste para preencher — é a primeira vez, no seu histórico, que essa parte do formulário fica realmente vazia.

### B.6 Como o build sai do EAS até a Apple (a parte que substitui o FlutterFlow)

O conceito é o mesmo do seu fluxo antigo — uma chave de API com acesso **App Manager** autoriza o envio do build — só muda onde essa chave é configurada.

1. **Gerar o build:** `eas build --platform ios --profile production`. O EAS pergunta pelas credenciais de assinatura (certificado \+ provisioning profile) na primeira vez; deixe o EAS gerenciar automaticamente (opção padrão), é o caminho mais simples.  
2. **Enviar para a Apple**, duas opções equivalentes:  
   - **Opção A — `eas submit` (recomendada, é o equivalente direto ao "Deploy to App Store" do FlutterFlow):** `eas submit --platform ios`. Na primeira vez, o EAS pede a mesma chave de API do passo B.1 (Issuer ID, Key ID, arquivo `.p8`) — os mesmos três dados que você já colava no FlutterFlow, agora colados no EAS. Depois de configurada uma vez, fica salva no projeto (`eas.json` / credenciais do EAS) e os próximos envios são um comando só.  
   - **Opção B — Transporter (mais manual, se preferir controlar cada passo):** baixe o `.ipa` gerado pelo `eas build`, abra o app **Transporter** (Mac App Store, gratuito, da própria Apple) e arraste o arquivo para enviar. Sem chave de API — usa seu login da Apple diretamente. Mais passos manuais, mas nenhuma configuração de credencial adicional.  
3. **Associar o build ao app** em App Store Connect → Distribution → Build → **\+**, exatamente como no passo "Add Build" do seu roteiro antigo (mesma tela, mesmo fluxo).  
4. **Missing Compliance** → "None of the algorithms" (B.1).  
5. **Preencher screenshots.** Diferente do FlutterFlow (que tinha o "Screenshot Mode" embutido no preview), aqui a forma mais simples é rodar o app no Simulador do Xcode nos tamanhos exigidos (iPhone 6.9" e 6.5" no mínimo) e usar `Cmd+S` para capturar direto do simulador, nos passos do formulário (Home, cadastro, exportação). Overlay de status bar (hora "9:41", bateria cheia) pode ser adicionado depois com uma ferramenta como o `fastlane frameit`, mas é opcional para a v1.  
6. **Submit to App Review**, como sempre.

### B.7 Para versões seguintes (depois da v1 aprovada)

Igual ao seu roteiro antigo ("fica mais simples... basicamente subir um novo Build para novas releases"): rode `eas build` \+ `eas submit` de novo com a versão incrementada em `app.json` (`version` e `ios.buildNumber`), associe o novo build à nova versão em App Store Connect, preencha "What to Test" (se for para o TestFlight) ou "What's New in This Version" (se for direto para a loja), e submeta. Não repete nenhum dos passos de configuração inicial (App ID, chave de API, política de privacidade) — isso é feito uma vez só.

### B.8 Checklist final antes de apertar "Submit to App Review"

- [ ] B.8.1 App ID criado em developer.apple.com com `com.dablab.horadoremedio`  
- [ ] B.8.2 App criado em App Store Connect, nome "Hora do Remédio", idioma principal Português (Brasil)  
- [ ] B.8.3 Chave de API gerada (App Manager), `.p8` salvo em local seguro, Key ID e Issuer ID anotados  
- [ ] B.8.4 Build enviado via `eas submit` (ou Transporter) e associado à versão em App Store Connect  
- [ ] B.8.5 Missing Compliance preenchido ("None of the algorithms")  
- [ ] B.8.6 Age Rating preenchido, com atenção ao bloco de conteúdo médico (esperado: sem conteúdo objetável)  
- [ ] B.8.7 App Privacy: "Data Not Collected" em tudo  
- [ ] B.8.8 Privacy Policy URL preenchida (Notion ou outro, com o conteúdo real da seção 20 — não o template genérico)  
- [ ] B.8.9 Support URL preenchida (Carrd — não Notion, ver B.3)  
- [ ] B.8.10 Categoria escolhida (25.5 — Medical ou Health & Fitness)  
- [ ] B.8.11 Preço definido (US$ 0,00) e disponibilidade de países decidida  
- [ ] B.8.12 Screenshots de iPhone 6.9" e 6.5" enviados (mínimo 3, das telas principais: Home, cadastro, exportação)  
- [ ] B.8.13 Descrição, subtítulo e keywords preenchidos em português  
- [ ] B.8.14 App Review Information preenchida com o texto de B.5, "Sign-in required" **desmarcado**  
- [ ] B.8.15 Disclaimer médico presente na descrição da loja e em "Sobre o app" (20.6)  
- [ ] B.8.16 Testado no TestFlight, no seu iPhone 13, antes de submeter para revisão

---

**Fim do documento.** Versão 1.1 — 26 de julho de 2026\.  
