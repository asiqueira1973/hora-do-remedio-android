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
./gradlew test                      # testes unitários JVM (ainda não escritos — ver "Falta")
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
  `calcularCustoDeAgendamento` — testáveis em JVM puro, ainda sem teste escrito),
  `AgendadorLembretes` (`AlarmManager.setExactAndAllowWhileIdle` por horário + espelho em
  `notificacoes_agendadas`), `NotificationHelper` (canal + notificação, texto exato da tabela 15.3
  do spec-ios.md), `LembreteReceiver` (dispara + se reagenda sozinho), `BootReceiver`
  (`RECEIVE_BOOT_COMPLETED` — mecanismo crítico, ver "Pegadinhas"). **Motor pronto (épico 8.4), mas
  sem UI de cadastro ainda não há como testar ponta a ponta** — falta verificar num device real
  (o emulador não é confiável para `AlarmManager`/Doze).
- `designsystem/` — `Cores.kt` (ColorScheme M3 fixo + `CorRemedio`, tradução 1:1 do redesenho iOS
  de 2026-08-23), `Tipografia.kt` (tokens display/titulo/corpo/botao/apoio, mesmos valores do
  `typography.ts` do iOS pós-redesenho), `Tema.kt` (`HoraDoRemedioTheme`). **Feito.**
- `telas/home/` — `AgrupamentoHome.kt` (função pura de agrupamento por horário/faixa do dia,
  11.7 do spec-ios.md — testável, ainda sem teste escrito), `HomeViewModel`, `HomeScreen`
  (lista agrupada + estado vazio + CTA "+ Adicionar remédio"). **Feito e verificado no emulador**
  (estado vazio). Falta verificar com remédios cadastrados de verdade — só é possível depois do 8.2.
- `telas/onboarding/OnboardingScreen.kt` — 2 telas, grava `onboarding_concluido_em`. **Feito e
  verificado no emulador.**
- `telas/RootView.kt` — decide onboarding × Home pelas preferências, libera a splash.
- `App.kt`, `MainActivity.kt` — bootstrap do banco, splash screen, cria o canal de notificação.

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
4. **Cota de 64 notificações do iOS (11.4) NÃO se aplica aqui** — decisão documentada em spec.md
   §6: todo remédio ativo com `lembrete_ativo=1` agenda todos os seus horários, sempre. Não portar
   esse corte para o Android sem reabrir a decisão com o Alexandre.
5. **`intervalo_dias` existe no schema e na lógica de cálculo, mas não tem UI** — mesma decisão do
   iOS (spec.md §7). Não adicionar o bloco "A cada X dias" no cadastro sem confirmar com o
   Alexandre que isso mudou.
6. **Textos em `strings.xml` são a tradução letra-por-letra de `textos.ts`**, com 3 adaptações
   deliberadas de plataforma (`iPhone`→`celular`, comentadas no topo do arquivo). Qualquer novo
   texto de interface entra em `strings.xml`, nunca hardcoded numa tela.

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

**Feito e verificado no emulador (Pixel_10), fluxo ponta a ponta sem crash:** onboarding (2 telas)
→ Home vazia → cadastro (todos os 8 blocos do spec, menos o 4b cortado) → salvar → Home mostra os
remédios agrupados por horário/faixa do dia, ordenados, com rodízio de cor certo (coral → azul no
2º remédio criado). Épicos **8.0** (fundação) e **8.7** (onboarding) completos; **8.1** (Home) e
**8.2** (Cadastro) funcionando de ponta a ponta, mas sem edição de forma completa ainda faltam
alguns critérios de aceite finos (ver abaixo). Dois crashes reais encontrados e corrigidos durante
a verificação no emulador — ver "Pegadinhas" #1 (trigger recursivo) e #3 (`SCHEDULE_EXACT_ALARM`).

**Motor de lembretes (8.4)** — `AgendadorLembretes.agendarDoRemedio` já é chamado de verdade pelo
`CadastroViewModel.salvar`; o alarme é agendado (aproximado, já que a permissão de alarme exato
ainda não tem tela de pedido — ver "Pegadinhas" #3) mas **o disparo em si (a notificação
aparecendo na hora certa) ainda não foi observado** — só a chamada ao `AlarmManager` sem exceção.
Precisa validar disparo real em device (o emulador não é confiável para isso).

**Falta (nesta ordem, spec.md §8):**
1. Fechar as pontas do 8.2: horário duplicado/máximo de 6 (lógica já existe em
   `CadastroViewModel.adicionarHorario`, mas a mensagem "repetido" na tela ainda não usa a string
   certa — só mostra a chave crua), teste em escala `gigante`, e revalidar bloco "Alguns dias da
   semana"/"Quando precisar" (implementados, não testados no emulador ainda).
2. **Épico 8.3 — Detalhe, edição, arquivamento** (3 rotas) — hoje tocar um card na Home não faz
   nada (`aoTocarRemedio` é um no-op em `RootView`).
3. Tela de pedir `SCHEDULE_EXACT_ALARM`/`POST_NOTIFICATIONS` (spec-ios.md 8.4) e validar o disparo
   da notificação de verdade em device real.
4. **Épico 8.5 — Exportação** (`gerarTexto` portado + `Intent.ACTION_SEND`).
5. **Épico 8.6 — Ajustes** (tamanho de letra, som, arquivados, sobre, apagar tudo).
6. Testes: JVM puro para `agruparParaHome`, `proximoDisparoMillis`, `calcularCustoDeAgendamento`
   (funções já são puras e testáveis, só falta escrever os testes); instrumentados para Room.
8. Ícone de notificação próprio (hoje usa um ícone de sistema placeholder em `NotificationHelper`
   — falta um ícone monocromático adequado para a barra de status).
9. `DOCUMENTACAO-TECNICA.md` e `README.md` (entregáveis de fim, só depois do app estar navegável).
10. Play Console (assinatura release, ficha da loja, produto/testador) — fica pro "meio", quando o
    app estiver navegável (mesma sequência usada no I See Life Android).

**Pendências para o Alexandre decidir** (não são técnicas — feedback_decisoes_tecnicas):
nenhuma ainda. Tudo que foi decidido até aqui era técnico e ficou registrado no spec.md/neste
arquivo. Se aparecer algo de produto/negócio nos próximos épicos, registrar aqui antes de decidir
sozinho.
