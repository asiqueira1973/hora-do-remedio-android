# DOCUMENTACAO-TECNICA.md

Documento obrigatório da seção 19 do `spec.md`, aplicado ao **port Android**. Descreve o sistema
**como ele ficou implementado** neste repositório — não como foi planejado, e não repete o que já
está em [`DOCUMENTACAO-TECNICA-ios.md`](DOCUMENTACAO-TECNICA-ios.md) exceto onde o Android diverge.
Deve ser suficiente para retomar o projeto (ou trocar de agente de código) sem engenharia reversa.

**Regra de manutenção:** toda mudança em schema, trigger ou lógica de tela atualiza este documento
no mesmo commit.

---

## 19.1 Modelo de dados explicado em português

O schema é **byte-a-byte o mesmo** do iOS (mesmos nomes de tabela, coluna, constraint, índice e
trigger — `spec-ios.md` 7.2-7.7, recriado em SQL bruto por `AppDatabase.recriarComConstraints`
porque o Room só gera `CREATE TABLE` simples a partir das entidades e não sabe declarar CHECK/FK
nomeada/trigger). Para o significado de negócio de cada coluna, ver
[`DOCUMENTACAO-TECNICA-ios.md` §19.1](DOCUMENTACAO-TECNICA-ios.md#191-modelo-de-dados-explicado-em-português)
— vale integralmente aqui. Só o *mecanismo* de aplicar o schema muda (ver 19.3).

Entidades Room (`dados/RemedioEntity.kt`, `HorarioEntity.kt`, `NotificacaoAgendadaEntity.kt`,
`PreferenciasEntity.kt`) usam `@ColumnInfo` para mapear `nomeCamelCase` → `coluna_snake_case`,
mantendo o nome de coluna idêntico ao iOS mesmo com Kotlin preferindo camelCase. `identificador_ios`
manteve esse nome de propósito mesmo no Android (onde guarda o `requestCode` do `PendingIntent`, não
um identificador do iOS) — é só um identificador opaco de agendamento, e renomear quebraria a
paridade de schema entre plataformas sem ganho nenhum (comentário em `NotificacaoAgendadaEntity.kt`).

`RemedioComHorarios` (`@Embedded` + `@Relation`) é um join Room que junta um remédio com seus
horários numa query só — usado pela Home e pela exportação para não fazer N+1 queries.

---

## 19.2 Relacionamento entre as tabelas

Idêntico ao iOS — ver [`DOCUMENTACAO-TECNICA-ios.md` §19.2](DOCUMENTACAO-TECNICA-ios.md#192-relacionamento-entre-as-tabelas).

---

## 19.3 Regras que vivem no banco

### Como a DDL completa chega ao SQLite (diferente do iOS)

O Drizzle do iOS gera migrations SQL versionadas em arquivo. Room não tem CHECK/trigger/FK nomeada
no seu DSL de entidades — por isso `AppDatabase.kt` usa uma estratégia diferente: o
`RoomDatabase.Callback.onCreate` **dropa e recria as 4 tabelas com DDL bruta completa**
(`recriarComConstraints`), rodando **depois** que o Room já criou as tabelas simples dele (que só
existem por um instante). Isso é seguro porque a validação de schema do Room (`RoomOpenHelper`) só
compara nome/tipo/`NOT NULL`/PK de coluna via `PRAGMA table_info` — CHECK, UNIQUE extra e trigger
não entram nessa comparação, então a DDL bruta pode ser estritamente mais restritiva sem o Room
reclamar. É por isso que nenhuma coluna usa `DEFAULT` em SQL: a entidade Kotlin já preenche todo
valor no insert, e um `DEFAULT` que a entidade não declarou quebraria essa validação.

Como `version = 1` no `@Database` e não há uma segunda versão publicada ainda, não existe
`Migration` real neste projeto — qualquer mudança de schema até aqui significa editar
`recriarComConstraints` e reinstalar (apagando o banco). Isso muda no dia em que uma versão for
publicada com usuários reais: a partir daí, mudar o schema exige uma `Migration` de verdade.

### PRAGMAs (aplicados em `Callback.onOpen`, por conexão — não persistidos no arquivo)

| PRAGMA | Efeito |
| :---- | :---- |
| `foreign_keys = ON` | Sem isso, os `ON DELETE CASCADE` das foreign keys abaixo não fariam nada. |
| `recursive_triggers = OFF` | **Divergência do iOS**: o SQLite empacotado no Android liga `recursive_triggers` por padrão (o do iOS não). Sem desligar, o `UPDATE` que o próprio trigger `trg_remedios_atualizado_em` faz dispara o trigger de novo, e de novo, até `"too many levels of trigger recursion"` — crash real, encontrado no emulador ao concluir o onboarding e corrigido nesta implementação. |
| `journal_mode = WRITE_AHEAD_LOGGING` | Via `RoomDatabase.Builder.setJournalMode`, só no banco real (`obter()`) — o banco em memória dos testes (`construirEmMemoriaParaTeste`) não aceita WAL. |

### Constraints, índices, triggers e foreign keys

Nomes e comportamento idênticos ao iOS — ver a tabela completa em
[`DOCUMENTACAO-TECNICA-ios.md` §19.3](DOCUMENTACAO-TECNICA-ios.md#193-regras-que-vivem-no-banco).
Uma pegadinha do SQLite encontrada e documentada só nesta implementação (a constraint em si é
idêntica dos dois lados, o comportamento também seria o mesmo no iOS — só não tinha sido notado lá):

> `ck_remedios_intervalo_coerente` e `ck_remedios_dias_semana_coerente` seguem o padrão `(A) OR (B)`
> onde `A` depende de uma coluna condicional (`intervalo_dias`/`dias_semana`) estar preenchida. Se
> essa coluna for `NULL` mas a `frequencia` exigir ela preenchida, `A` avalia para `NULL` (não
> `FALSE`, por causa da lógica de três valores do SQL) e `NULL OR FALSE` continua `NULL` — CHECK só
> rejeita em `FALSE` explícito, então a linha passaria batida. Nunca acontece pelo app
> (`CadastroViewModel` valida os dois campos antes de salvar), mas um `INSERT` bruto de fora do app
> (uma migração de dados futura, por exemplo) driblaria esse CHECK sem erro nenhum. Documentado com
> comentário na própria DDL (`AppDatabase.kt`) e coberto por
> `AppDatabaseConstraintsTest.frequenciaDiasSemanaComDigitoForaDoIntervalo1a7EhRejeitada` (que testa
> o caso que **é** pego — dígito inválido, não ausência).

---

## 19.4 Mapa tela → tabelas

| # | Tela | Rota (Navigation Compose) | Lê | Escreve | Arquivo(s) principais |
| :---- | :---- | :---- | :---- | :---- | :---- |
| 1 | Onboarding | `onboarding` | — | `preferencias` | `telas/onboarding/OnboardingScreen.kt` |
| 2 | Home | `home` | `remedios`+`horarios` (join), `preferencias` (escala) | — (delega às telas de destino) | `telas/home/HomeScreen.kt`, `HomeViewModel.kt`, `AgrupamentoHome.kt` |
| 3 | Cadastro | `cadastro/novo` | `preferencias` (indireto, via agendamento) | `remedios`, `horarios`, `notificacoes_agendadas` | `telas/cadastro/CadastroScreen.kt`, `CadastroViewModel.kt` |
| 4 | Detalhe | `detalhe/{id}` | `remedios`, `horarios` | — | `telas/detalhe/DetalheScreen.kt`, `DetalheViewModel.kt` |
| 5 | Edição | `cadastro/editar/{id}` | `remedios`, `horarios` | `remedios`, `horarios`, `notificacoes_agendadas` | `telas/cadastro/CadastroScreen.kt` (mesmo formulário de 3, `remedioIdParaEditar != null`) |
| 6 | Permissão de aviso | `permissao/{id}` | — | — (dispara agendamento adiado) | `telas/permissao/PermissaoAvisoScreen.kt`, `PermissaoAvisoViewModel.kt` |
| 7 | Ajustes | `ajustes` | `preferencias`, `remedios` (para compartilhar) | `preferencias` | `telas/ajustes/AjustesScreen.kt`, `AjustesViewModel.kt` |
| 8 | Remédios que parei | `ajustes/arquivados` | `remedios` (`arquivado_em IS NOT NULL`) | `remedios` (limpa `arquivado_em`), `notificacoes_agendadas` (via reagendamento) | `telas/arquivados/ArquivadosScreen.kt`, `ArquivadosViewModel.kt` |

Sem tela dedicada de "Sobre o app" nem "Erro de banco" separadas do iOS ainda — ver 19.6.

---

## 19.5 Lógica das principais funcionalidades por tela

### Cadastro e edição (`CadastroScreen.kt` + `CadastroViewModel.kt`, compartilhado pelas telas 3 e 5)

- **Validado** só ao tocar em Salvar (`CadastroViewModel.salvar`): nome vazio, zero dias da semana
  quando `frequencia = dias_semana`, zero horários quando a frequência exige. Nunca durante a
  digitação.
- **Gravado** numa transação só (`Repositorio.criarRemedio`/`.atualizarRemedioComHorarios`, via
  `androidx.room.withTransaction`): a linha de `remedios` e todas as linhas de `horarios` juntas —
  em edição, os horários antigos são apagados e os novos inseridos dentro da mesma transação, nunca
  um `UPDATE` linha a linha.
- **Mudar frequência para "Quando precisar" durante a edição** pede confirmação antes
  (`confirmandoQuandoPrecisar` em `CadastroScreen`, texto exato de `spec-ios.md` 8.3: *"Esse remédio
  vai sair dos horários e os avisos vão parar. Continuar?"*) — só quando `ehEdicao=true`; no
  cadastro novo troca direto, porque não há nada a perder ainda.
- **Calculado**: a cor sugerida vem do rodízio (`CorRemedio.proxima`, lendo
  `buscarUltimaCorCriada()`).
- **Erro ao usuário**: campo com `isError=true` + texto abaixo (`ErroTexto`), nunca
  `Toast`/`Snackbar` para erro de validação.

### Agendamento de notificações (arquitetura totalmente diferente do iOS — a parte mais arriscada do port)

O iOS usa `Notifications.scheduleNotificationAsync` com triggers **nativamente repetitivos**
(`UNCalendarNotificationTrigger` "toda terça às 08:00", entregue pelo sistema operacional sem o app
precisar fazer nada depois de agendar uma vez) e tem uma cota dura de 64 notificações pendentes por
app (`spec-ios.md` 11.4). **Nenhuma dessas duas coisas existe da mesma forma no Android**:

- `AlarmManager` não tem um gatilho nativo "toda terça". Cada alarme é **um disparo único**
  (`setExactAndAllowWhileIdle`), e o próprio `LembreteReceiver` — ao disparar — calcula a **próxima**
  ocorrência e reagenda a si mesmo (`proximoDisparoMillis`, `lembretes/CalculoAgendamento.kt`, função
  pura testada em JVM puro por `CalculoAgendamentoTest.kt`). Isso significa que "agendar um lembrete"
  no Android é, na prática, uma cadeia infinita de "dispara → reagenda o próximo → dispara → ...".
- Não há cota de 64 no Android (`AlarmManager` não impõe um teto assim) — `calcularCustoDeAgendamento`
  foi portado por completude e paridade de teste com o iOS, mas nada no Android trunca agendamentos.
- `notificacoes_agendadas` é o mesmo "espelho do que foi realmente pedido ao sistema" do iOS, mas
  aqui `identificador_ios` guarda o `requestCode`/`PendingIntent` do Android, não um id do iOS (ver
  19.1). `disparo_em` é sempre preenchido (diferente do iOS, onde fica nulo para gatilhos repetitivos
  puros) — como cada alarme Android é um disparo único, sempre há uma próxima data concreta.
- **Sobrevivência a reboot**: alarmes do `AlarmManager` **não sobrevivem** a reiniciar o aparelho
  (diferente das notificações locais do iOS, que persistem sozinhas). `BootReceiver.kt` escuta
  `ACTION_BOOT_COMPLETED` e chama `AgendadorLembretes.reconciliarTudo()` — mecanismo que o spec
  chama de "não pode quebrar" (`spec.md` §6): sem ele, os avisos somem silenciosamente depois que o
  idoso reinicia o aparelho, sem nenhum erro visível.
- **`SCHEDULE_EXACT_ALARM`** (Android 12+) é uma permissão especial: só declarar no manifesto não
  basta, e chamar `setExactAndAllowWhileIdle` sem o usuário ter concedido derruba o app com
  `SecurityException` (crash real, encontrado no emulador e corrigido). `podeAgendarExato()`
  (`AgendadorLembretes.kt`) checa antes de cada chamada e cai para `set()` (aproximado, sem garantia
  de horário exato) em vez de crashar — a tela de permissão (ver abaixo) tenta levar o usuário a
  conceder de verdade, mas o app nunca trava se ele recusar.
- **Reconciliação não é "cancelar tudo e reagendar tudo a cada abertura"** como no iOS
  (`reconciliadorLembretes.ts`, rodando na abertura e após >1h em background). No Android, cada
  operação (`agendarDoRemedio`/`cancelarDoRemedio`) já mantém `notificacoes_agendadas` em sincronia
  incrementalmente a cada cadastro/edição/arquivamento/"voltar a tomar" — um "cancelar tudo e
  reagendar tudo" só acontece em dois pontos: `BootReceiver` (depois de reiniciar o aparelho, onde
  não há outra forma de saber o que ainda está pendente) e `AjustesViewModel.mudarSom` (trocar o
  canal de som exige reagendar, porque o canal de notificação é decidido no momento de **agendar**,
  não de entregar — ver Ajustes abaixo). Essa é uma divergência arquitetural deliberada, não um corte
  de escopo — ver 19.6.
- **Conteúdo da notificação** (`NotificationHelper.mostrar`): título fixo (`notificacao_titulo`,
  strings.xml) + corpo `"{nome} — {dose}"` ou só `"{nome}"` sem dose — mesmo formato da 15.3 do iOS.
  Ícone próprio (`R.drawable.ic_notificacao`, sino monocromático) desde que o placeholder de sistema
  (`android.R.drawable.ic_dialog_info`) foi substituído nesta sessão. Dois canais
  (`lembretes_remedio_com_som`/`lembretes_remedio_silencioso`) em vez de um, porque o Android não
  deixa mudar o som de um `NotificationChannel` depois de criado — `mostrar` escolhe o canal pelo
  valor **atual** de `preferencias.som_lembrete` a cada disparo.

### Fluxo de permissão (`telas/permissao/`, spec-ios.md 8.4 "Fluxo de permissão")

`CadastroViewModel.salvar` calcula `precisaPermissao = lembreteFinal &&
PermissoesLembrete.precisaExplicarPermissao(context)` (`POST_NOTIFICATIONS` não concedida OU
`SCHEDULE_EXACT_ALARM` não concedida) e **pula o agendamento** quando `true`, devolvendo
`(id, precisaPermissao)` para quem chamou. `RootView` desvia para a rota `permissao/{id}` antes de
voltar para a Home. `PermissaoAvisoScreen`:

1. "Continuar" pede `POST_NOTIFICATIONS` (diálogo real do Android 13+, via
   `ActivityResultContracts.RequestPermission()`) — só se `SDK_INT >= TIRAMISU` e ainda não
   concedida.
2. Depois (callback do diálogo, ou direto se não precisava dele), se `SCHEDULE_EXACT_ALARM` ainda
   não foi concedida, abre a tela de sistema `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` — essa
   permissão **não tem diálogo in-app** no Android, só essa tela de configurações.
3. Em qualquer caso (concedeu ou não), `PermissaoAvisoViewModel.finalizarAgendamento` busca o remédio
   já salvo e chama `agendarDoRemedio` — o remédio já foi salvo com `lembrete_ativo=1` mesmo que a
   pessoa recuse tudo (mesma regra do iOS: negar não desfaz o cadastro).
4. "Agora não" pula os diálogos e chama `finalizarAgendamento` direto (que vai simplesmente falhar
   silenciosamente em agendar de verdade se a permissão não estiver lá — `podeAgendarExato()` degrada
   para `set()` aproximado).

A Home mostra a faixa "Os avisos estão desligados" (`home_avisos_desligados`, strings.xml) quando
`PermissoesLembrete.notificacoesPermitidas(context)` é falso — reverificado a cada `ON_RESUME`
(`DisposableEffect` + `LifecycleEventObserver` em `HomeScreen.kt`), porque a pessoa pode ter mudado a
permissão nos Ajustes do sistema sem fechar o app. Tocar na faixa abre
`Settings.ACTION_APP_NOTIFICATION_SETTINGS` escopado ao app.

### Exportação (`exportacao/ExportadorLista.kt`)

`gerarTexto(remediosAtivos)` é uma função pura, porte caractere por caractere de
`exportadorLista.ts` (iOS) — mesmo agrupamento por horário, mesmos emojis por faixa, mesma junção
`\n\n` entre **todas** as linhas de conteúdo, mesmo rodapé fixo. `compartilhar()`
(`exportacao/Compartilhar.kt`) abre a share sheet nativa do Android
(`Intent.ACTION_SEND` + `Intent.createChooser`) com o texto puro — equivalente ao `Share.share` do
React Native, sem escrever nenhum arquivo temporário.

### Ajustes (`telas/ajustes/AjustesScreen.kt` + `AjustesViewModel.kt`)

- **Tamanho das letras**: grava `preferencias.escala_fonte` direto — `RootView` já observa esse
  campo via Flow e aplica em `HoraDoRemedioTheme(escala=...)` para o app inteiro, sem precisar de
  nenhum outro código de propagação.
- **Som do aviso**: grava `preferencias.som_lembrete` e chama `agendador.reconciliarTudo()` em
  seguida — **divergência do iOS**: no Android o som é uma propriedade do `NotificationChannel`
  (imutável depois de criado), não da notificação individual, então trocar o som exige recriar
  literalmente todos os `PendingIntent` pendentes para que passem a apontar para o canal certo da
  próxima vez que dispararem. No iOS o som é por notificação, então o mesmo botão só muda uma flag.
- **Apagar todos os meus dados**: `agendador.cancelarTudo()` (cancela os alarmes de todos os
  remédios ativos) seguido de `Repositorio.apagarTudo()` (`remedios.apagarTudo()` cascade-apaga
  `horarios`+`notificacoes_agendadas` via `PRAGMA foreign_keys=ON`, depois
  `preferencias.resetarParaDefaults()` — sem onboarding concluído). `RootView` navega de volta para
  `onboarding` explicitamente na callback (`aoApagarTudo`), diferente do portão reativo do iOS — ver
  19.6.

### Onboarding

Uma única tela (`OnboardingScreen.kt`), sem nenhum dado pessoal nem pedido de permissão (a permissão
só é pedida no primeiro remédio com lembrete, ver acima). "Começar" grava
`preferencias.onboarding_concluido_em` e navega direto (`navController.navigate("home") {
popUpTo("onboarding") { inclusive = true } }`) — sem um portão reativo separado (ver 19.6).

---

## 19.6 Divergências em relação ao spec (e em relação ao próprio port iOS)

Este projeto é um **port nativo**, não uma tradução literal linha a linha — `spec.md` (o spec
genérico da 8.0-8.7) é a fonte de verdade de produto, `spec-ios.md` documenta como o iOS resolveu
cada detalhe de plataforma, e este Android resolve os mesmos requisitos de produto com as primitivas
nativas do Android, que em vários pontos são estruturalmente diferentes o suficiente para exigir uma
arquitetura diferente, não só uma sintaxe diferente. As divergências abaixo são as que importam para
quem for dar manutenção — cada uma tem uma razão técnica, nenhuma é um corte de escopo silencioso.

### Arquitetura de agendamento reconstruída do zero (não é um port de `servicoLembretes.ts`)

A diferença mais profunda do projeto — detalhada em 19.5 acima. Resumo para quem está escaneando
esta seção: `AlarmManager` não tem gatilho nativo repetitivo, `expo-notifications`/`UNUserNotification`
tem; alarmes não sobrevivem a reboot, notificações locais do iOS sobrevivem; não há cota de 64 no
Android. Isso levou a um desenho com auto-reagendamento no `LembreteReceiver` e um `BootReceiver`
dedicado, nenhum dos dois com equivalente no iOS.

### Reconciliação incremental, não "cancelar tudo e reagendar tudo" a cada abertura do app

O iOS reconcilia (cancela tudo, reagenda as 64 mais próximas) toda vez que o app abre ou volta do
background após 1h — é a estratégia deliberada dele para qualquer mudança. O Android faz cada
operação (criar, editar, arquivar, desarquivar) manter `notificacoes_agendadas` em sincronia direta e
incrementalmente, chamando `agendarDoRemedio`/`cancelarDoRemedio` pontualmente; `reconciliarTudo()`
(que faz o "cancela tudo, reagenda tudo" completo) só roda depois de `BOOT_COMPLETED` e ao trocar o
som do lembrete — os dois únicos casos em que reconstruir do zero é necessário ou mais simples que
uma atualização incremental. Razão: sem cota de 64 para gerenciar, não há motivo de negócio para
recalcular tudo a cada abertura — só custo de bateria/CPU a mais.

### Sem portão de onboarding reativo (`PortaoOnboarding`)

O iOS tem um componente à parte observando `preferencias.onboarding_concluido_em` para navegar nos
dois sentidos, porque a escrita no banco e a propagação reativa do `useLiveQuery` não são
perfeitamente síncronas lá. No Android, `RootView` já decide a `startDestination` do `NavHost` lendo
`preferencias.observar()` como `State` (`collectAsState`), e cada ação que muda esse estado
(`concluirOnboarding`, `apagarTudo`) já navega explicitamente na própria callback
(`navController.navigate(...)`) em vez de confiar em um observador separado. Não foi encontrado o bug
de "preso no onboarding" que motivou o portão reativo do iOS — se aparecer, é o primeiro lugar a
olhar.

### Sem tela "Sobre o app" e sem tela de erro de banco dedicada

O `spec-ios.md` 8.6 lista "Sobre o app" (versão, política de privacidade) como parte de Ajustes, e
`spec.md` 12.4 pede uma tela de erro se a migration do banco falhar — nenhuma das duas foi
implementada ainda neste port. Não são cortes deliberados de escopo, são pendências de
implementação: `AjustesScreen.kt` não tem a linha "Sobre o app" e `RootView` não trata falha de
`prepararNaAbertura()` (que hoje não pode falhar de forma realista, já que não há migration real —
ver 19.3 — mas passará a poder no dia em que uma existir).

### `expo-sharing`/`Share` viram `Intent.ACTION_SEND` (equivalente direto, sem ganho ou perda)

Mesma decisão do iOS (texto puro, sem arquivo) — só a API nativa muda. Ver
`exportacao/Compartilhar.kt`.

### `AsyncStorage` nunca existiu como opção no Android

O equivalente Android (`SharedPreferences`/`DataStore`) nem chegou a ser considerado — desde o
início, `escala_fonte`/`onboarding_concluido_em` foram para `preferencias` (Room), pela mesma razão
que o iOS explica na divergência equivalente dele: uma tabela reativa via Flow é mais simples que
duas fontes de verdade.

### `intervalo_dias` cortado da experiência do usuário — mesma decisão do iOS, herdada, não reavaliada

Ver [`DOCUMENTACAO-TECNICA-ios.md` §19.6](DOCUMENTACAO-TECNICA-ios.md#intervalo_dias-cortado-da-experiência-do-usuário-resolve-a-pergunta-aberta-258)
— vale integralmente aqui: schema mantido por paridade (`ck_remedios_intervalo_coerente`,
`RemedioEntity.intervaloDias`), bloco 4 do cadastro mostra 3 opções, `calcularCustoDeAgendamento` e
`proximoDisparoMillis` continuam corretos para `intervalo_dias` (testados em
`CalculoAgendamentoTest.kt`) mas nada na UI Android gera esse valor.

### Confirmações e microcópias sem texto exato na 15.5 (mesma situação do iOS)

Idem à divergência equivalente do iOS — os textos onde a 15.5 não especifica literalmente foram
escritos seguindo o mesmo tom (direto, sem culpar o usuário) e vivem em `strings.xml`, comentados
onde relevante.

### `CHECK` com coluna condicional `NULL` não rejeita (achado, não corrigido — ver 19.3)

Documentado em detalhe em 19.3 acima. Vale repetir aqui por ser uma divergência de **comportamento
real do banco**, mesmo que a constraint em si seja texto idêntico ao iOS: nunca foi testado lá com
esse caso específico, então não se sabe se o SQLite do iOS (mesmo motor, mesma versão de
comportamento CHECK+NULL) tem o mesmo furo — matematicamente deveria ter, já que é a mesma DDL e o
mesmo SQLite.

---

## 19.7 O que este documento não cobre ainda

- Assinatura de release / Play Console (`keystore.properties`, ficha da loja, telas de marketing) —
  ver "Falta" no [`CLAUDE.md`](CLAUDE.md).
- Comportamento observado em device físico com o app em background por muitas horas (o emulador não
  é confiável para isso) — maior item pendente de verificação antes de publicar.
