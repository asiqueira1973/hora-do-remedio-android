# CLAUDE.md — Hora do Remédio (Android)

Guia para o agente de código. Leia antes de mexer. Este é o **port Android** do app iOS
`Hora do Remédio` (lembrete de remédios para idosos). O `spec.md` (deste repositório) é a fonte de
verdade do port; `spec-ios.md`/`DOCUMENTACAO-TECNICA-ios.md`/`SCHEMA-ios.md` são cópias de
referência do projeto iOS, mantidas aqui só para consulta — nunca editadas.

## O que é

Lembrete de remédios para idosos, **local-first, 100% offline**, sem login. Kotlin + Jetpack
Compose + Room + `AlarmManager`. Material Design 3 (não é um clone visual do iOS — ver spec.md §2).
Phone-only, retrato apenas. `minSdk 26`, `targetSdk 35`.

## Regra de ouro

**Não alterar o projeto iOS.** Fica em `~/hora-do-remedio`, é uma base separada, publicada e
estável (já na versão 1.1 na App Store). Bases independentes; a paridade é de comportamento e de
texto de interface, não de código. A única exceção feita nesta sessão foi corrigir um bug de texto
("botão verde" → "botão azul") que ficou stale depois do redesenho visual do iOS — não mexer em
mais nada lá sem necessidade equivalente.

## Comandos

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21           # JDK 21 (AGP não aceita o JDK 25 do sistema)
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
./gradlew :app:assembleDebug        # compila o APK debug
./gradlew test                      # testes unitários JVM (20 testes — exportação, agrupamento, custo de agendamento)
./gradlew connectedDebugAndroidTest # testes instrumentados (9 testes — CHECK/FK/UNIQUE/trigger do banco, precisa de emulador/device rodando)
./gradlew lint                      # lint Android
```

Rodar no emulador (sem Android Studio):
```bash
export ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"
emulator -avd Pixel_10 -no-window -gpu swiftshader_indirect &
adb wait-for-device
./gradlew installDebug
adb shell am start -n com.dablab.horadoremedio.debug/com.dablab.horadoremedio.MainActivity
```

## Arquitetura (pastas em `app/src/main/java/com/dablab/horadoremedio/`)

- `dados/` — Room: `RemedioEntity`/`HorarioEntity`/`NotificacaoAgendadaEntity`/`PreferenciasEntity`
  (colunas `snake_case` via `@ColumnInfo`, espelhando o schema do iOS 1:1), DAOs, `AppDatabase`
  (CHECK/UNIQUE/trigger recriados em SQL bruto no `Callback.onCreate` — ver "Pegadinhas" abaixo),
  `Repositorio` (bootstrap na abertura), `Enums.kt` (Forma/Frequencia/Cor/EscalaFonte),
  `RemedioComHorarios` (`@Relation`, usado pela Home). **Feito (épico 8.0).**
- `lembretes/` — `CalculoAgendamento.kt` (funções puras: `proximoDisparoMillis`,
  `calcularCustoDeAgendamento` — **com testes JVM**, ver `app/src/test/.../lembretes/`),
  `AgendadorLembretes` (`AlarmManager.setExactAndAllowWhileIdle` por horário, com fallback pra
  `set()` aproximado quando `SCHEDULE_EXACT_ALARM` não foi concedido, + espelho em
  `notificacoes_agendadas`), `NotificationHelper` (canal + notificação, texto exato da tabela 15.3
  do spec-ios.md), `LembreteReceiver` (dispara + se reagenda sozinho), `BootReceiver`
  (`RECEIVE_BOOT_COMPLETED` — mecanismo crítico, ver "Pegadinhas"). **Motor pronto e chamado de
  verdade pelo cadastro/edição/arquivamento (épicos 8.2/8.3), mas o disparo real da notificação
  ainda não foi observado** — falta verificar num device real (o emulador não é confiável para
  `AlarmManager`/Doze).
- `designsystem/` — `Cores.kt` (ColorScheme M3 fixo + `CorRemedio` + `CorRemedio.proxima` (rodízio
  11.8), tradução 1:1 do redesenho iOS de 2026-08-23), `Tipografia.kt` (tokens display/titulo/
  corpo/botao/apoio, mesmos valores do `typography.ts` do iOS pós-redesenho), `Tema.kt`
  (`HoraDoRemedioTheme`). **Feito.**
- `telas/common/IconesForma.kt` — ícone por forma farmacêutica, compartilhado entre Home/Cadastro.
- `telas/home/` — `AgrupamentoHome.kt` (agrupamento por horário/faixa, 11.7 — **com testes JVM**),
  `Saudacao.kt` (saudação por período, 11.6 — pura), `HomeViewModel` (Flow de Room + StateFlow
  separada `Eagerly` pra exportação — ver "Pegadinhas"), `HomeScreen` (cabeçalho com saudação +
  botão compartilhar, lista agrupada, estado vazio, CTA, faixa de arquivamento + Desfazer).
  **Feito e verificado no emulador ponta a ponta.**
- `telas/cadastro/` — `CadastroViewModel` (form state + validação só ao salvar + modo edição via
  `remedioIdParaEditar`), `CadastroScreen` (8 blocos, TimePicker M3, seletor de dias da semana,
  seletor de cor). **Feito e verificado no emulador** (criar e editar).
- `telas/detalhe/` — `DetalheViewModel`/`DetalheScreen` — horários + status do aviso, Editar,
  Parar de tomar com confirmação. **Feito e verificado no emulador.**
- `telas/onboarding/OnboardingScreen.kt` — 2 telas, grava `onboarding_concluido_em`. **Feito e
  verificado no emulador.**
- `telas/RootView.kt` — `NavHost` (onboarding/home/cadastro-novo/cadastro-editar/detalhe), decide
  onboarding × Home pelas preferências, libera a splash, guarda o estado do "X foi arquivado" +
  Desfazer (hoisted aqui pra sobreviver à navegação de volta pra Home).
- `exportacao/` — `ExportadorLista.kt` (`gerarTexto`, porte 1:1 de `exportadorLista.ts` do iOS,
  **com teste de comparação literal** — 22.1.3), `Compartilhar.kt` (`Intent.ACTION_SEND`). **Feito
  e verificado no emulador** (épico 8.5).
- `telas/ajustes/` — `AjustesViewModel`/`AjustesScreen` — tamanho de letra (aplica na hora, todo o
  app), som do aviso (reagenda tudo), atalhos p/ arquivados/compartilhar, "Sobre o app" com link de
  privacidade, apagar tudo com confirmação dupla. **Feito e verificado no emulador.**
- `telas/arquivados/` — `ArquivadosViewModel`/`ArquivadosScreen` — lista arquivados, "Voltar a
  tomar" limpa `arquivado_em` e reagenda. **Feito e verificado no emulador.**
- `App.kt`, `MainActivity.kt` — bootstrap do banco, splash screen, cria os canais de notificação.

## Pegadinhas que NÃO podem ser quebradas

1. **`recursive_triggers` do SQLite do Android vem ligado por padrão** (diferente do iOS/SQLite
   "de fábrica"). Sem `PRAGMA recursive_triggers = OFF` no `onOpen` do `AppDatabase`, o próprio
   `UPDATE` que a trigger `atualizado_em` faz dispara a trigger de novo, recursivamente, até
   `SQLiteException: too many levels of trigger recursion` — **isso já crashou o app uma vez nesta
   sessão** (ao concluir o onboarding, que grava em `preferencias`). Não remover essa PRAGMA.
2. **CHECK/UNIQUE/trigger não vêm do `@Entity` do Room** — Room só gera `CREATE TABLE` simples.
   `AppDatabase.recriarComConstraints` faz `DROP TABLE` + `CREATE TABLE` completo (com CHECK) logo
   depois que o Room cria a tabela vazia, dentro do `Callback.onCreate`. As colunas do `CREATE
   TABLE` bruto **não podem ter `DEFAULT`** — o `TableInfo` que o Room usa para validar o schema em
   runtime compara valor de default coluna a coluna, e como as entidades não declaram
   `defaultValue` em `@ColumnInfo`, um `DEFAULT` no SQL bruto quebraria essa validação na abertura
   seguinte do banco. Os defaults ficam só no lado Kotlin (valor default do construtor da entidade).
3. **`SCHEDULE_EXACT_ALARM` é uma permissão "especial" (Android 12+)** — declarar no manifesto NÃO
   basta; sem o usuário conceder em Ajustes, `AlarmManager.setExactAndAllowWhileIdle` derruba o app
   com `SecurityException` (já crashou nesta sessão, ao salvar o 1º remédio). `AgendadorLembretes.
   podeAgendarExato()` e o mesmo check em `LembreteReceiver` evitam o crash caindo para `set()`
   (aproximado) quando a permissão não está concedida — mas a TELA de pedir a permissão
   (spec-ios.md 8.4) ainda não existe; sem ela, todo alarme desta v1 é aproximado, não exato, o que
   é uma degradação silenciosa que precisa virar UI antes do 8.4 ser considerado pronto.
4. **`AlarmManager` não sobrevive a reboot** (diferente do iOS, onde notificações locais
   persistem). Sem o `BootReceiver` reagendando tudo via `AgendadorLembretes.reconciliarTudo()`, os
   avisos somem silenciosamente depois que o idoso reinicia o aparelho. Ver spec.md §6.
5. **Cota de 64 notificações do iOS (11.4) NÃO se aplica aqui** — decisão documentada em spec.md
   §6: todo remédio ativo com `lembrete_ativo=1` agenda todos os seus horários, sempre. Não portar
   esse corte para o Android sem reabrir a decisão com o Alexandre.
6. **`intervalo_dias` existe no schema e na lógica de cálculo, mas não tem UI** — mesma decisão do
   iOS (spec.md §7). Não adicionar o bloco "A cada X dias" no cadastro sem confirmar com o
   Alexandre que isso mudou.
7. **Textos em `strings.xml` são a tradução letra-por-letra de `textos.ts`**, com 3 adaptações
   deliberadas de plataforma (`iPhone`→`celular`, comentadas no topo do arquivo). Qualquer novo
   texto de interface entra em `strings.xml`, nunca hardcoded numa tela.
8. **`StateFlow.stateIn` com `WhileSubscribed` só coleta o upstream enquanto alguém está
   observando via Compose (`collectAsState`)**. `HomeViewModel.remediosParaExportar()` lê uma
   StateFlow só por `.value`, nunca via `collectAsState` — com `WhileSubscribed` ela nunca seria
   coletada de verdade e `.value` ficava travado no valor inicial (`emptyList()`), fazendo a share
   sheet abrir sem nenhum remédio mesmo com a Home cheia (bug real, visto no emulador). Precisa de
   `SharingStarted.Eagerly` nesse caso específico — qualquer StateFlow acessada só por `.value` fora
   de composição tem que usar `Eagerly`, não `WhileSubscribed`.
9. **`NotificationChannel` não deixa trocar o som depois de criado** (diferente do iOS, onde o som
   é por notificação). `NotificationHelper` cria DOIS canais (com som / silencioso) e escolhe pelo
   valor ATUAL de `preferencias.som_lembrete` a cada disparo — nunca crie um 3º canal achando que
   vai "consertar" isso; o padrão certo é sempre 2 canais fixos + escolha em runtime.
10. **Botão com `.height(N.dp)` fixo corta o texto em 2 linhas na escala `gigante`** (visto ao vivo
    no emulador: "Apagar todos os meus dados" cortado, acceptance criterion violado). Todo botão que
    pode ter texto de 2+ palavras usa `.heightIn(min = N.dp)`, nunca `.height(N.dp)` fixo — deixa o
    botão crescer se o texto quebrar linha em vez de cortar.

## Convenções

- **Banco em português `snake_case`** (`@ColumnInfo(name = "...")` em toda entidade); **código
  Kotlin em inglês** (nomes de classe/função/variável) — mas comentários e nomes de função de
  domínio ficam em português quando espelham vocabulário do produto (ex.: `agruparParaHome`,
  `proximoDisparoMillis`), seguindo o padrão que o próprio spec.md pede.
- **Comentário de cabeçalho em português** em todo arquivo novo, citando a seção do spec/spec-ios
  que implementa.
- Manter `CLAUDE.md`, `DOCUMENTACAO-TECNICA.md` (ao fim) e `README.md` atualizados no mesmo commit
  da mudança, quando existirem.

## Estado atual (2026-08-24, sessão autônoma)

**Todos os épicos 8.0→8.7 implementados e verificados no emulador (Pixel_10), fluxo ponta a
ponta sem crash:** onboarding (2 telas) → Home vazia → cadastro (8 blocos, menos o 4b cortado) →
salvar → Home mostra os remédios agrupados por horário/faixa, com rodízio de cor → tocar no card
abre o Detalhe → Editar reabre o formulário preenchido → "Parar de tomar" com confirmação e nome
interpolado → arquiva → faixa "X foi arquivado" + **Desfazer confirmado funcionando** → Ajustes
(engrenagem no cabeçalho) → tamanho de letra Grande/Gigante aplica em TODO o app na hora (inclusive
o próprio texto do botão "Apagar todos os meus dados", que quebra em 2 linhas sem cortar) → Som do
aviso → Remédios que parei → Voltar a tomar traz o remédio de volta com horário/cor originais →
Compartilhar minha lista (Home e Ajustes) → Apagar todos os meus dados com dupla confirmação exata
do spec → volta ao onboarding com o banco limpo. 20 testes JVM passando (`gerarTexto` com
comparação literal 22.1.3, `agruparParaHome`, `proximoDisparoMillis`, `calcularCustoDeAgendamento`).

**Motor de lembretes (8.4) + fluxo de permissão completo e verificado no emulador** —
`AgendadorLembretes.agendarDoRemedio` é chamado de verdade pelo cadastro/edição/arquivamento/
"voltar a tomar", e `mudarSom` (Ajustes) reagenda tudo trocando de canal (com som/silencioso — ver
"Pegadinhas" #9). `telas/permissao/PermissaoAvisoScreen` (nova) reproduz o "Fluxo de permissão" da
8.4: `CadastroViewModel.salvar` PULA o agendamento quando falta permissão e avisa quem chama
(`precisaPermissao=true`); `RootView` desvia pra tela explicativa antes de agendar de verdade.
Testado no emulador ponta a ponta: tela explicativa → diálogo real do Android pra
`POST_NOTIFICATIONS` → tela do sistema "Alarms & reminders" (deep link certo pro app, via
`Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM`) → volta pro Home com o remédio já agendado, sem
`SecurityException`. Home também ganhou a faixa "Os avisos estão desligados" (`PermissoesLembrete`,
reverificada a cada `ON_RESUME` — a pessoa pode ter mudado a permissão nos Ajustes sem fechar o
app), igual à faixa do iOS. **Falta só**: observar o disparo real da notificação na hora certa em
device físico (o emulador não é confiável pra isso).

**Bugs reais encontrados e corrigidos nesta sessão, todos verificados no emulador**: trigger
recursivo do SQLite (crash), `SCHEDULE_EXACT_ALARM` sem checagem em runtime (crash), rótulos de
forma sem acento, mensagem de horário repetido mostrando a chave crua, `StateFlow.WhileSubscribed`
nunca coletada travando a exportação em lista vazia, cabeçalho da Home sob a status bar, botão de
"Apagar tudo" cortando texto em 2 linhas na escala gigante.

**Feito depois do fluxo de permissão (mesma sessão):** confirmação ao mudar frequência pra
"quando_precisar" durante a EDIÇÃO (spec-ios.md 8.3, "Regras de edição" — string exata "Esse
remédio vai sair dos horários e os avisos vão parar. Continuar?", verificada no emulador: Cancelar
mantém o formulário, Continuar colapsa horários/lembrete e salva sem crash); ícone próprio da
notificação (`ic_notificacao.xml`, sino monocromático, trocou o placeholder de sistema em
`NotificationHelper`); grep final por `.height()` fixo fora de Spacers (achou 1 em `CadastroScreen`
— campos de texto e a linha de frequência usavam `heightMinimo()` fixo, virou `heightIn(min=)`);
testes instrumentados do Room (`AppDatabaseConstraintsTest`, 9 testes, exercitam CHECK/FK/UNIQUE/
trigger que só o SQLite real valida — Room não olha isso na validação de schema);
`DOCUMENTACAO-TECNICA.md` e `README.md` escritos a partir do sistema real implementado (regra da
seção 19 do spec.md — atualizar os dois no mesmo commit de qualquer mudança de schema/trigger/lógica
de tela).

**Falta:**
1. Observar o disparo real da notificação em device físico (maior item pendente — só dá pra testar
   fora do emulador).
2. Telas de marketing da Play Store — tem uma referência visual pronta em
   `marketing/reference-appstore-ios/` (as 3 telas do iOS, mesmo conteúdo) esperando pra virar a
   versão Android (Material 3, tamanho que o Google exige — ver o README daquela pasta).
3. Play Console (assinatura release, ficha da loja, produto/testador) — fica pro "meio"; o app já
   está navegável, então este é o próximo marco natural quando o Alexandre decidir seguir.

**Pendências para o Alexandre decidir** (não são técnicas — feedback_decisoes_tecnicas):
nenhuma ainda. Tudo que foi decidido até aqui era técnico e ficou registrado no spec.md/neste
arquivo. Se aparecer algo de produto/negócio nos próximos épicos, registrar aqui antes de decidir
sozinho.
