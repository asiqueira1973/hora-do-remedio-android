# DOCUMENTACAO-TECNICA.md

Documento obrigatório da seção 19 do `spec.md`. Descreve o sistema **como ele ficou implementado**,
não como foi planejado. Deve ser suficiente para retomar o projeto (ou trocar de agente de código)
sem engenharia reversa.

**Regra de manutenção:** toda mudança em schema, trigger ou lógica de tela atualiza este documento
no mesmo commit.

---

## 19.1 Modelo de dados explicado em português

### `remedios`

Uma linha por medicamento que a pessoa toma — não uma linha por dose ou por horário.

| Coluna | Significado de negócio |
| :---- | :---- |
| `id` | Identificador interno, usado por todas as telas para referenciar o remédio. |
| `nome` | Nome do remédio, digitado livremente (1 a 60 caracteres, sem ficar só em espaço). |
| `dose` | Texto livre ("50mg", "1 comprimido", "10 gotas"). **Nula quando a pessoa não preencheu** — o card e a exportação simplesmente omitem a linha de dose. |
| `forma` | Como a pessoa toma: `comprimido`, `capsula`, `gotas`, `xarope`, `injecao`, `pomada`, `spray`, `outro`. Só decide qual ícone aparece — não tem outro efeito no sistema. |
| `frequencia` | `diario` (todo dia, nos horários do bloco 5), `dias_semana` (só nos dias marcados em `dias_semana`), `intervalo_dias` (a cada N dias — mantido no schema mas **cortado da UI**, ver 19.6), `quando_precisar` (sem horário fixo, sem lembrete). |
| `intervalo_dias` | Só preenchido quando `frequencia = 'intervalo_dias'` (2 a 30). **Nula em qualquer outra frequência** — a constraint `ck_remedios_intervalo_coerente` garante isso no banco. |
| `dias_semana` | String de dígitos ("246" = segunda, quarta, sexta — 1=domingo...7=sábado), só quando `frequencia = 'dias_semana'`. **Nula em qualquer outra frequência** (`ck_remedios_dias_semana_coerente`). |
| `observacao` | Texto livre opcional ("Tomar junto com a comida"). **Nula quando não há observação** — omitida no card e na exportação. |
| `lembrete_ativo` | `1` = agenda notificação para os horários deste remédio; `0` = aparece na Home com o ícone de sino cortado, mas sem avisar. Sempre `0` quando `frequencia = 'quando_precisar'` (o formulário nunca deixa ligar o lembrete nesse caso, porque não há horário para agendar). |
| `cor` | Uma das 6 cores da paleta (16.3), escolhida pelo rodízio (11.8) ou manualmente. Só decide a cor de fundo do card. |
| `arquivado_em` | **Nula enquanto o remédio está ativo.** Preenchida com a data/hora de "Parar de tomar" — o remédio some da Home mas continua no banco, pronto para "Voltar a tomar" limpar este campo. |
| `criado_em` / `atualizado_em` | Carimbos de tempo. `atualizado_em` é mantido pela trigger `trg_remedios_atualizado_em`, não pela aplicação. |

### `horarios`

Um horário do dia em que um remédio é tomado. Um remédio "todo dia às 08:00 e às 20:00" gera duas
linhas aqui, uma para cada horário — nunca uma linha com dois horários juntos.

| Coluna | Significado |
| :---- | :---- |
| `id` | Identificador interno. |
| `remedio_id` | A quem este horário pertence. **Nunca nulo** — um horário sempre pertence a exatamente um remédio, e some junto com ele (cascade). |
| `hora_minuto` | Texto `"HH:MM"` (`ck_horarios_formato` garante o formato). |
| `ordem` | Reservado para uma ordenação manual futura; hoje os horários são sempre ordenados pelo próprio valor de `hora_minuto`. |
| `criado_em` | Carimbo de criação. |

### `notificacoes_agendadas`

Espelho local do que foi **realmente pedido ao iOS** — nunca a fonte de verdade sobre o que o
usuário quer (isso é `remedios`/`horarios`), só sobre o que o sistema operacional já aceitou agendar.
Existe para saber o que cancelar e para respeitar a cota de 64 (11.4).

| Coluna | Significado |
| :---- | :---- |
| `id` | Identificador interno. |
| `horario_id` | De qual horário este agendamento veio. Some junto com o horário (cascade). |
| `identificador_ios` | O identificador que `Notifications.scheduleNotificationAsync` devolveu — é o que permite cancelar exatamente aquele agendamento depois. |
| `disparo_em` | Data/hora do próximo disparo previsto, usada para ordenar por proximidade quando a cota de 64 estoura. **Nula** para gatilhos puramente repetitivos (`diario`/`dias_semana`), onde não há uma "próxima data" única e determinística guardada aqui — o cálculo de proximidade nesse caso é feito em memória por `calcularProximoDisparo` (`reconciliadorLembretes.ts`), não lido desta coluna. |
| `criado_em` | Carimbo de criação. |

### `preferencias`

Linha única (`id` sempre `1`, garantido por `ck_preferencias_linha_unica`) com as preferências do
app inteiro. Existe como tabela — e não em `AsyncStorage` — para reagir via `useLiveQuery` sem
nenhum mecanismo extra de state management (ver divergência 19.6 sobre `AsyncStorage`).

| Coluna | Significado |
| :---- | :---- |
| `id` | Sempre `1`. |
| `escala_fonte` | `'grande'` ou `'gigante'` (16.2). Controla o tamanho de texto do app inteiro via `PreferencesContext`. |
| `som_lembrete` | `1` = notificações tocam som; `0` = chegam silenciosas. Lido no momento de **agendar** (não de entregar), por isso mudar este valor precisa reagendar tudo. |
| `onboarding_concluido_em` | **Nula** até a pessoa terminar o onboarding (8.7) — enquanto nula, o app sempre abre em `/onboarding`. Preenchida ao tocar "Começar", e limpa de novo por "Apagar todos os meus dados" (que recria esta linha do zero). |
| `atualizado_em` | Mantida pela trigger `trg_preferencias_atualizado_em`. |

---

## 19.2 Relacionamento entre as tabelas

```
remedios (1) ──────< (0..6) horarios (1) ──────< (0..N) notificacoes_agendadas

preferencias (linha única, sem relação com as outras tabelas)
```

- Um remédio tem de zero a seis horários (limite de 6 do bloco 5, `MAXIMO_HORARIOS`); um horário
  pertence a exatamente um remédio e some junto com ele (`ON DELETE CASCADE`).
- Um horário tem zero ou mais notificações agendadas (zero quando `lembrete_ativo = 0` ou quando a
  cota de 64 não sobrou espaço para ele); uma notificação agendada pertence a exatamente um horário
  e some junto com ele (`ON DELETE CASCADE`).
- `preferencias` é uma tabela isolada, sem foreign key para nada — é configuração do app, não dado
  de remédio.

---

## 19.3 Regras que vivem no banco

### PRAGMAs (aplicados pela conexão em `src/db/client.ts`, não persistidos no arquivo)

| PRAGMA | Efeito |
| :---- | :---- |
| `foreign_keys = ON` | Sem isso, os `ON DELETE CASCADE` das foreign keys abaixo não fariam nada — SQLite ignora foreign keys por padrão. |
| `journal_mode = WAL` | Permite leitura e escrita concorrentes sem travar a UI durante uma escrita. |

### Constraints nomeadas

| Nome | Tabela | O que garante |
| :---- | :---- | :---- |
| `ck_remedios_nome_nao_vazio` | `remedios` | `nome` tem entre 1 e 60 caracteres depois de `trim` — nunca só espaço em branco. |
| `ck_remedios_forma` | `remedios` | `forma` só aceita um dos 8 valores do enum (7.6). |
| `ck_remedios_frequencia` | `remedios` | `frequencia` só aceita um dos 4 valores do enum. |
| `ck_remedios_cor` | `remedios` | `cor` só aceita uma das 6 cores da paleta. |
| `ck_remedios_intervalo_coerente` | `remedios` | `intervalo_dias` preenchido (2–30) **se e só se** `frequencia = 'intervalo_dias'`; nulo em qualquer outro caso. |
| `ck_remedios_dias_semana_coerente` | `remedios` | `dias_semana` preenchido (dígitos 1–7, 1 a 7 caracteres) **se e só se** `frequencia = 'dias_semana'`; nulo em qualquer outro caso. |
| `ck_remedios_lembrete_ativo` | `remedios` | `lembrete_ativo` só aceita `0` ou `1`. |
| `ck_horarios_formato` | `horarios` | `hora_minuto` só aceita o formato `HH:MM` (`00`–`29` na hora por causa do `GLOB`, mas a UI nunca gera nada fora de `00`–`23`). |
| `uq_horarios_remedio_hora` | `horarios` (índice único) | Um mesmo remédio não pode ter dois horários iguais. |
| `uq_notificacoes_identificador` | `notificacoes_agendadas` (índice único) | Nunca duas linhas apontando para o mesmo `identificador_ios`. |
| `ck_preferencias_linha_unica` | `preferencias` | `id` só pode ser `1` — impede uma segunda linha de preferências. |
| `ck_preferencias_escala` | `preferencias` | `escala_fonte` só aceita `'grande'` ou `'gigante'`. |
| `ck_preferencias_som` | `preferencias` | `som_lembrete` só aceita `0` ou `1`. |

### Índices (performance, sem regra de negócio)

`idx_remedios_ativos` (`remedios.arquivado_em`), `idx_horarios_remedio` (`horarios.remedio_id`),
`idx_horarios_hora` (`horarios.hora_minuto`), `idx_notificacoes_horario`
(`notificacoes_agendadas.horario_id`), `idx_notificacoes_disparo`
(`notificacoes_agendadas.disparo_em`).

### Triggers

| Nome | Tabela | Dispara | O que faz |
| :---- | :---- | :---- | :---- |
| `trg_remedios_atualizado_em` | `remedios` | Depois de qualquer `UPDATE` | Grava `datetime('now')` em `atualizado_em` da linha alterada — a aplicação nunca precisa lembrar de fazer isso manualmente. |
| `trg_preferencias_atualizado_em` | `preferencias` | Depois de qualquer `UPDATE` | Idem, para `preferencias.atualizado_em`. |

### Foreign keys com cascade

`horarios.remedio_id → remedios.id` e `notificacoes_agendadas.horario_id → horarios.id`, ambas
`ON DELETE CASCADE` — na prática só usadas por "Apagar todos os meus dados" (8.6), já que o fluxo
normal do produto é arquivar (`arquivado_em`), nunca apagar um remédio individual.

---

## 19.4 Mapa tela → tabelas

| # | Tela | Rota | Lê | Escreve | Funções principais |
| :---- | :---- | :---- | :---- | :---- | :---- |
| 1 | Onboarding 1 | `/onboarding` | `preferencias` (via `PreferencesContext`, só para o portão de navegação) | — | `PortaoOnboarding` (`app/_layout.tsx`) |
| 2 | Onboarding 2 | `/onboarding/privacidade` | — | `preferencias` | `repositorioPreferencias.concluirOnboarding` |
| 3 | Home | `/` | `remedios`, `horarios`, `preferencias` | — (delega escrita às telas de destino) | `repositorioRemedios.queryAtivos`, `agruparPorHorario`, `calcularCustoDeAgendamento`, `usePermissaoNotificacao` |
| 4 | Cadastro | `/remedio/novo` | `preferencias` (som, para o agendamento) | `remedios`, `horarios`, `notificacoes_agendadas` | `repositorioRemedios.criar`, `servicoLembretes.verificarPermissao`, `reconciliar` |
| 5 | Detalhe | `/remedio/[id]` | `remedios`, `horarios` | — | `repositorioRemedios.queryPorId`, `formatarFrequencia` |
| 6 | Edição | `/remedio/[id]/editar` | `remedios`, `horarios` | `remedios`, `horarios`, `notificacoes_agendadas` | `repositorioRemedios.atualizar`, `reconciliar` |
| 7 | Permissão de aviso | `/permissao-aviso` (modal) | — | — | `servicoLembretes.pedirPermissao`, `reconciliar` |
| 8 | Ajustes | `/ajustes` | `preferencias`, `remedios` (para o botão de compartilhar) | `preferencias` | `repositorioPreferencias.atualizarEscalaFonte/atualizarSomLembrete/apagarTudo`, `servicoLembretes.cancelarTudo` |
| 9 | Remédios que parei | `/ajustes/arquivados` | `remedios`, `horarios` | `remedios` (limpa `arquivado_em`), `notificacoes_agendadas` (via reconciliação) | `repositorioRemedios.queryArquivados/desarquivar`, `reconciliar` |
| 10 | Sobre o app | `/ajustes/sobre` | — | — | `Linking.openURL` |
| 11 | Erro de banco | componente global (`app/_layout.tsx`) | — | — | `inicializarBanco` |

---

## 19.5 Lógica das principais funcionalidades por tela

### Cadastro e edição (`MedicationForm.tsx`, compartilhado pelas telas 4 e 6)

- **Validado** só ao tocar em Salvar (`validarFormulario`, `src/features/medications/validarFormulario.ts`):
  nome vazio, zero horários quando a frequência exige, zero dias da semana quando `dias_semana`.
  Nunca durante a digitação.
- **Gravado** numa transação síncrona só, dentro de `repositorioRemedios.criar`/`.atualizar`
  (`src/repositories/medications.ts`): a linha de `remedios` e todas as linhas de `horarios` juntas —
  em edição, os horários antigos são apagados e os novos inseridos dentro da mesma transação, nunca
  um `UPDATE` linha a linha (mais simples e sem risco de "sobrar" um horário órfão).
- **Calculado**: a cor sugerida vem do rodízio (`repositorioRemedios.proximaCor`); o texto de
  frequência do card vem de `formatarFrequencia`.
- **Erro ao usuário**: campo com borda + texto de erro abaixo, nunca alert/toast; falha ao salvar no
  banco mostra `TEXTOS.cadastro.erroAoSalvar` sem fechar o formulário (os dados digitados não se
  perdem).

### Agendamento de notificações (a parte mais difícil de reconstruir só lendo o código)

O agendamento vive em duas camadas deliberadamente separadas:

1. **`src/features/reminders/agendamentoPuro.ts`** — zero import de banco ou de módulo nativo.
   - `calcularCustoDeAgendamento(remedios)`: soma quantas notificações cada remédio ativo vai
     consumir da cota de 64 do iOS (11.4): `diario` = nº de horários; `dias_semana` = dias × horários;
     `intervalo_dias` = `⌈30 / N⌉ × horários` (mantido pela função, nunca alimentado pela UI — ver
     19.6); `quando_precisar` = 0.
   - `montarAgendamentos(remedio)`: transforma um remédio em uma lista de `{ horarioId, gatilho,
     disparoEm }`, onde `gatilho` é `{ tipo: 'diario' | 'dias_semana' | 'data', hour, minute,
     weekday?, data? }` — ainda **não** é um trigger real do expo-notifications, só a descrição pura
     do que precisa ser agendado.
2. **`src/features/reminders/servicoLembretes.ts`** — traduz o gatilho puro em
   `Notifications.SchedulableNotificationTriggerInput` real (`paraGatilhoNativo`), monta o
   conteúdo da notificação (`conteudoNotificacao`, título fixo `"Hora do remédio 💊"` + corpo
   `"{nome} — {dose}"` ou só `"{nome}"` sem dose — 15.3), e grava/apaga
   `notificacoes_agendadas` em `agendarDoRemedio`/`cancelarDoRemedio`/`cancelarTudo`. O som
   (`preferencias.som_lembrete`) é lido **no momento de agendar**, não de entregar — por isso mudar
   o som em Ajustes precisa reagendar tudo, não só marcar uma flag.

**Reconciliação** (`src/features/reminders/reconciliadorLembretes.ts`, rotina 13-A.3): roda na
abertura do app e ao voltar do background depois de mais de 1 hora (`app/_layout.tsx`). Sempre
começa do zero: `servicoLembretes.cancelarTudo()` (cancela **tudo** no iOS + esvazia
`notificacoes_agendadas`), busca todos os remédios ativos, ordena por proximidade do próximo
disparo (`calcularProximoDisparo`, matemática pura de datas em JS), agenda as 64 mais próximas e
descarta o resto silenciosamente — sem travar, sem erro, sem perder o cadastro do remédio em si
(só o aviso dele que não roda). "Cancelar tudo e reagendar tudo" é a estratégia deliberada em toda
mudança (arquivar, editar, mudar o som, apagar tudo) — nunca uma edição pontual de uma notificação
já agendada, porque o expo-notifications não oferece isso de forma confiável.

**Fluxo de permissão** (8.4): `servicoLembretes.verificarPermissao()` roda ao salvar um remédio com
`lembrete_ativo = 1`; se `'undetermined'`, a tela de cadastro/edição navega para
`/permissao-aviso` **antes** de chamar `reconciliar()`. A tela explicativa, ao continuar, chama
`pedirPermissao()` (o diálogo nativo do iOS) e só então reconcilia. Negar a permissão não desfaz o
cadastro — o remédio já foi salvo antes de chegar nessa tela — só deixa a faixa
`home.avisosDesligados` visível (`usePermissaoNotificacao`, que reage a `AppState` para pegar
mudanças feitas nos Ajustes do iOS).

### Exportação (`src/features/export/exportadorLista.ts`)

`gerarTexto(remedios)` é uma função pura que reaproveita `agruparPorHorario` (o mesmo agrupamento da
Home) e `formatarFrequencia` (o mesmo texto do card) para montar o texto exato da 14.1 — cada linha
de conteúdo separada por uma linha em branco, sem exceção, conferido caractere por caractere pelo
teste `22.1.3`. `compartilhar(texto)` chama a API `Share` do React Native (não `expo-sharing`, que é
voltado a compartilhar arquivos por URI — ver 19.6).

### Ajustes (`app/ajustes/index.tsx`)

- **Tamanho das letras**: grava `preferencias.escala_fonte` direto; a prévia de cada botão usa
  `obterEstiloTipografico('corpo', escala)` chamado manualmente para as duas escalas ao mesmo tempo
  (não depende da escala ativa do app) — é por isso que dá para ver "Grande" e "Gigante" lado a lado
  sem trocar a tela inteira de tamanho.
- **Som do aviso**: grava `preferencias.som_lembrete` e chama `reconciliar()` em seguida — as linhas
  de `notificacoes_agendadas` ganham identificadores novos porque tudo é cancelado e reagendado do
  zero.
- **Apagar todos os meus dados**: duas confirmações (`Alert.alert` encadeados, texto não
  especificado na 15.5 — ver 19.6) e, ao confirmar a segunda, `servicoLembretes.cancelarTudo()`
  (cancela no iOS + limpa `notificacoes_agendadas`) seguido de `repositorioPreferencias.apagarTudo()`
  (apaga `remedios`, que arrasta `horarios` em cascade, apaga e recria `preferencias` via o mesmo
  `rodarSeed` do boot). O portão de onboarding (`PortaoOnboarding`, ver 19.6) detecta
  `onboarding_concluido_em` voltando a nulo e navega para `/onboarding` sozinho.

### Onboarding

Duas telas sem nenhum dado pessoal nem pedido de permissão. "Começar" grava
`onboarding_concluido_em`; a navegação de volta para a Home é responsabilidade do `PortaoOnboarding`
reativo em `app/_layout.tsx`, não de um `router.replace` isolado na própria tela de onboarding — ver
o porquê em 19.6.

---

## 19.6 Divergências em relação ao spec

### Redesenho visual para o padrão nativo da Apple (2026-08-23)

Decisão de Alexandre, aprovada a partir de mockups gerados nesta sessão (o app "tinha cara de IA"):
troca da identidade visual definida na seção 16 por uma mais próxima do Human Interface Guidelines
da Apple — listas agrupadas nativas, tipografia do sistema, azul de sistema no lugar do verde. A
16.8 marca cor/tipografia como "não editáveis pelo agente sem aprovação"; a aprovação aconteceu
explicitamente nesta sessão, então o que segue são os valores **atuais**, substituindo os da 16.2/16.4:

| Item | Valor da 16.2/16.4 (spec original) | Valor atual |
| :---- | :---- | :---- |
| Fonte | Nunito, empacotada no binário (`assets/fonts/`, removida) | Fonte do sistema (San Francisco no iOS) — `fontFamily` fica `undefined` no iOS, só `fontWeight` varia por token |
| `acao` | `#1E9E5A` (verde) | `#007AFF` (azul de sistema do iOS) |
| `perigo` | `#D93025` | `#FF3B30` (vermelho de sistema) |
| `atencao` | `#B25E00` | `#FF9500` (laranja de sistema) |
| `neutro_fundo` | `#F5F5F7` | `#F2F2F7` (cinza de "grouped list" do iOS) |
| `neutro_borda` | `#D6D6DB` | `#E5E5EA` |
| Raio de canto | 20pt cards / 16pt botões | 14pt em tudo |
| Sombra | y:2 blur:8 opacidade 0.08 | y:1 blur:3 opacidade 0.06 — listas agrupadas do iOS quase não usam sombra |
| Card de remédio | Fundo cheio na cor do remédio | Fundo branco + círculo de 48pt na cor do remédio à esquerda, chevron à direita — o padrão de linha de lista do iOS |
| Botão destrutivo | Texto e borda `perigo`, fundo branco | Só texto `perigo`, sem borda nem fundo — linha de lista destrutiva nativa |
| "+ Adicionar remédio" | FAB flutuante (círculo/pílula com sombra, canto inferior direito) | Barra larga ancorada embaixo, mesma altura (`ALTURAS.fab`) |

Além disso, uma nova cor semântica foi adicionada (não existia na 16.4): `neutro_texto_secundario`
(`#6E6E73`) — formaliza o cinza secundário que antes estava espalhado como `'#6B6B70'` literal em
vários componentes.

**O que não mudou:** os 6 tokens de `PALETA_REMEDIOS` (16.3) — `coral`, `azul`, `verde`, `amarelo`,
`lilas`, `pessego` — continuam com os hex originais. Isso importa especialmente para o token
`azul`: como a nova cor de ação também é azul (`#007AFF`), a regra dura da 16.4 ("nenhuma das seis
cores de identificação pode coincidir com as semânticas") só continua valendo porque
`PALETA_REMEDIOS.azul.detalhe` (`#2F7FE0`) é um azul visualmente distinto do azul de ação — não são
o mesmo valor. Também não mudou: o piso de 20pt em qualquer texto (princípio 0.1, ainda respeitado
em todo lugar, incluindo os cabeçalhos de seção em maiúsculas que o iOS normalmente renderiza bem
menores) e a altura mínima de toque de 60pt.

Telas com reestruturação de layout (não só cor): Home (`app/index.tsx`), formulário de
cadastro/edição (`MedicationForm.tsx` — blocos viraram listas agrupadas, "Como você toma?" e "Com
que frequência?" viraram linhas com marca de seleção via `SeletorOpcao` redesenhado), Detalhe do
remédio (`app/remedio/[id]/index.tsx` — herói centralizado + duas listas agrupadas, "Voltar" virou
link de navegação no topo em vez de botão), Ajustes (`app/ajustes/index.tsx` — seções com
cabeçalho maiúsculo e grupos brancos). Os mockups em Design Components desta sessão (Início,
Cadastro, Detalhe, Ajustes) documentam a direção visual antes da implementação.

### `intervalo_dias` cortado da experiência do usuário (resolve a pergunta aberta 25.8)

Decisão tomada no início da implementação, confirmada por Alexandre: ninguém que ele conhece usa
"a cada X dias" de fato. Por isso:

- O **schema** mantém as colunas `remedios.intervalo_dias` e a constraint
  `ck_remedios_intervalo_coerente` **exatamente como especificado** na seção 7 — cortar colunas do
  modelo de dados violaria a instrução de seguir a seção 7 literalmente, e mantê-las custa zero.
- O **bloco 4 do cadastro** (8.2) mostra 3 opções de frequência, não 4: falta "A cada X dias".
- `calcularCustoDeAgendamento` e `montarAgendamentos` (`agendamentoPuro.ts`) **continuam corretos**
  para `frequencia = 'intervalo_dias'` e são cobertos pelos testes obrigatórios 22.1.1/22.1.2 —
  é barato manter e deixa a v2 pronta para religar essa frequência sem reescrever a lógica de
  agendamento — mas nada na UI gera esse valor, então esse ramo nunca executa em produção.

### `AsyncStorage` não foi usado para `escala_fonte`/`onboarding_concluido_em`

A seção 4.3 lista `@react-native-async-storage/async-storage` para "preferências de UI (tamanho de
fonte, onboarding visto)". Como a seção 7.5 já modela essas duas preferências como colunas da tabela
`preferencias` (SQLite), usar `AsyncStorage` em paralelo criaria duas fontes de verdade para o mesmo
dado. Optou-se por manter tudo em `preferencias` (SQLite), o que também dá reatividade automática
via `useLiveQuery` sem nenhum código extra de sincronização — `@react-native-async-storage/async-storage`
não chegou a ser instalado. Divergência de implementação, não de intenção: a preferência continua
sobrevivendo a reinstalação de app do mesmo jeito, só que numa tabela em vez de num arquivo separado.

### `expo-sharing` não foi usado na exportação (8.5)

A seção 4.3 lista `expo-sharing` **ou** a API `Share` do React Native para a share sheet. `expo-sharing`
é voltada a compartilhar um arquivo a partir de uma URI local; a exportação da 14.1 é texto puro, sem
arquivo — por isso `compartilhar()` (`exportadorLista.ts`) usa `Share.share({ message: texto })`, que
já está dentro da opção permitida pela própria 4.3 e evita escrever um arquivo temporário à toa.

### Confirmações e microcópias sem texto exato na 15.5

A 15.5 não define o texto literal de alguns diálogos que o spec pede, mas não escreve por extenso:
os dois passos de confirmação de "Apagar todos os meus dados" (8.6, critério "confirmação dupla em
texto claro") e os botões do seletor de hora nativo do bloco 5 do cadastro. Esses textos foram
escritos seguindo o tom da 15.2 (direto, sem culpar o usuário, sem tecnicismo) e estão comentados no
próprio `src/constants/textos.ts` como não especificados literalmente pela 15.5.

### Política de Privacidade (resolve a pergunta aberta 25.6)

O link de "Sobre o app" aponta para uma página do Notion fornecida por Alexandre nesta sessão
(`TEXTOS.sobre.urlPoliticaPrivacidade`). Página de suporte e demais itens do Anexo B (categoria na
App Store, nome disponível, checagem jurídica) continuam em aberto, fora do escopo desta
implementação.

### Portão de onboarding reativo (`PortaoOnboarding`, `app/_layout.tsx`)

Não é uma divergência de comportamento visível, mas vale registrar a decisão de arquitetura: a
navegação entre onboarding e Home não fica só na ação que dispara a mudança (tocar "Começar" ou
"Apagar todos os meus dados") — um componente à parte, `PortaoOnboarding`, observa
`preferencias.onboarding_concluido_em` via `useLiveQuery`/Context e navega nos dois sentidos (para o
onboarding quando ele está vazio, de volta para a Home quando ele acabou de ser preenchido). A razão:
a escrita no banco e a propagação reativa do `useLiveQuery` não são perfeitamente síncronas — sem o
segundo sentido, uma tela que já chamou `router.replace('/')` podia ser jogada de volta ao
onboarding pelo portão, que ainda via o valor antigo, e ficar presa lá (bug encontrado e corrigido
durante o teste manual desta sessão).

### Teste automatizado do épico 8.2/8.3 (`repositorioRemedios.criar`/`.atualizar`, item 22.1.6 do spec)

Não foi possível escrever este teste como um teste Jest isolado: `repositorioRemedios` importa
`src/db/client.ts`, que abre uma conexão real via `expo-sqlite` (`openDatabaseSync`) — um módulo
nativo sem contraparte executável fora de um dispositivo/simulador iOS, e sem mock funcional no
`jest-expo` atual. Diferente do agendamento de lembretes (que por isso ganhou o módulo puro
`agendamentoPuro.ts`), extrair a lógica de transação de `criar`/`atualizar` para fora do Drizzle não
foi feito por não haver função pura equivalente a isolar — a transação **é** a chamada ao driver.
Esse comportamento foi verificado manualmente e repetidamente ao longo da implementação: cadastro,
edição (incluindo troca de horário), arquivamento e "voltar a tomar" foram exercitados no simulador
com inspeção direta do arquivo `.db` via `sqlite3`, confirmando que a transação grava `remedios` e
todos os `horarios` juntos, sem linha órfã. As constraints do banco em si (item 22.1.5) **foram**
automatizadas rodando as migrations SQL reais num SQLite em memória via `node:sqlite`
(`src/db/__tests__/constraints.test.ts`) — essa parte não depende do driver `expo-sqlite`.
