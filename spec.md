# Hora do Remédio — Android (spec de port)

**Fonte de verdade do comportamento:** [`spec-ios.md`](spec-ios.md) (produto) e
[`DOCUMENTACAO-TECNICA-ios.md`](DOCUMENTACAO-TECNICA-ios.md) (sistema como ficou implementado de
fato, incluindo o redesenho visual de 2026-08-23 — **a doc técnica manda mais que o spec original
onde os dois divergem**, porque ela captura o que o app realmente faz hoje). [`SCHEMA-ios.md`](SCHEMA-ios.md)
é o dump real do banco SQLite do iOS.

Este documento não repete o produto — ele traduz. Onde não disser o contrário, o comportamento é
**idêntico** ao iOS: mesmas regras de negócio, mesmos textos (em português), mesma UX pensada para
idosos (letra grande, toque grande, sem jargão). O que muda é só tecnologia.

**Regra de ouro (do roteiro):** o app iOS é intocável. Este é um repositório separado e
independente. Paridade é de **comportamento**, não de código compartilhado.

---

## 1. Decisões desta sessão

| Decisão | Resposta |
| :---- | :---- |
| Stack | Nativo: **Kotlin + Jetpack Compose** (não cross-platform) |
| Escopo da v1 | **Paridade total** com o iOS atual — os 8 épicos (8.0–8.7), incluindo o redesenho visual de 2026-08-23 |
| Conta de publicação | Conta de desenvolvedor Google Play já estabelecida (mesma usada no I See Life) — **isenta** da exigência de teste fechado de 20 pessoas |
| `applicationId` | `com.dablab.horadoremedio` — mesmo namespace de marca do bundle id do iOS |

## 2. Visual: Material Design 3, não um clone do iOS

O iOS foi redesenhado nesta sessão para o padrão nativo da Apple (HIG) — azul de sistema, tipografia
San Francisco, listas agrupadas nativas (ver `DOCUMENTACAO-TECNICA-ios.md` §19.6). A mesma lógica
de "parecer nativo da plataforma" se aplica aqui: o Android usa **Material Design 3** nativo
(Compose Material 3), não uma cópia pixel-a-pixel do visual do iOS. Paridade é de comportamento e
hierarquia de informação, não de pixel.

Tradução de tokens de design:

| iOS (pós-redesenho) | Android (Material 3) |
| :---- | :---- |
| Fonte do sistema (San Francisco) | Fonte do sistema (Roboto, `Typography` padrão do M3) |
| Azul de sistema `#007AFF` (`acao`) | `primary` do `ColorScheme` — usar um azul próximo, ex. seed color `#0B57D0`-ish via `dynamicColorScheme`/`lightColorScheme` fixo (decisão: **cor fixa, não Material You dinâmica** — consistência de marca entre os dois apps, mesmo raciocínio do iOS de reservar a cor de ação) |
| `perigo` `#FF3B30` | `error` do `ColorScheme` |
| `atencao` `#FF9500` | cor customizada `atencao` no `ColorScheme` (M3 não tem slot "warning" padrão) |
| Listas agrupadas (grouped list, cabeçalho maiúsculo cinza + grupo branco) | `Card`/`Surface` com `ListItem` do M3, `Column` com `HorizontalDivider` entre itens — visual nativamente Android, mesma estrutura de informação |
| Switch nativo iOS | `Switch` do M3 |
| Botão "+ Adicionar remédio" (barra larga ancorada embaixo) | `Button` (ou `FloatingActionButton` extendido — **decisão: manter barra larga ancorada embaixo**, igual ao iOS, porque FAB circular no rodapé é justamente o padrão Material que o iOS abandonou; a barra larga com texto é mais acessível para o público idoso e funciona igual de bem nas duas plataformas) |
| Ícone de forma do remédio em círculo colorido | Mesma estrutura: `Icon` do Material Symbols dentro de um `Box` circular tingido na cor do remédio |
| Paleta de 6 cores dos remédios (`coral`, `azul`, `verde`, `amarelo`, `lilas`, `pessego`) | **Idêntica** — são cores de identificação de produto, não de sistema operacional; os mesmos hex do iOS (`DOCUMENTACAO-TECNICA-ios.md` §19.6) |

**Piso de acessibilidade mantido, igual ao iOS:** nenhum texto menor que o equivalente a 20pt
(≈20sp em Compose, considerando que sp já escala com a preferência do sistema), nenhuma área
tocável menor que 60dp — mesmos princípios 0.1/0.2 do spec original, só a unidade muda (pt→dp/sp).

## 3. Arquitetura

- **UI:** Jetpack Compose, um `Activity` único (`ComponentActivity`) com Navigation Compose para as
  11 telas (mesmo inventário de telas do iOS, seção 10 do `spec-ios.md`).
- **Estado:** `ViewModel` + `StateFlow`/`collectAsStateWithLifecycle()` por tela — equivalente ao
  `useLiveQuery` reativo do iOS: Room com `Flow<T>` nas queries já é reativo por natureza (não
  precisa de nada extra para o card atualizar sozinho ao cadastrar um remédio).
- **Banco:** Room (SQLite por baixo — o mesmo motor do iOS, `expo-sqlite`), o que permite portar as
  regras do banco (constraints, triggers) quase literalmente via SQL bruto num
  `RoomDatabase.Callback`, em vez de reinventá-las em Kotlin.
- **Lembretes:** `AlarmManager` (`setExactAndAllowWhileIdle` para os alarmes) +
  `NotificationManager` para exibir o aviso — equivalente ao par
  `expo-notifications`/`UNCalendarNotificationTrigger` do iOS.
- **Preferências:** ficam na própria tabela `preferencias` do Room (não em `DataStore`) — mesma
  decisão do iOS de manter tudo no SQLite para ganhar reatividade de graça via `Flow`, sem duas
  fontes de verdade (ver divergência do iOS sobre `AsyncStorage`, §19.6 da doc técnica).
- **Sem rede, sem conta:** idêntico ao iOS — zero permissão de internet no `AndroidManifest.xml`
  além do que o próprio Android exige por padrão (nenhuma, na prática — o app não pede
  `INTERNET`).

## 4. Modelo de dados — tradução para Room

Mesmas 4 tabelas, mesmos nomes de tabela/coluna em português `snake_case` (convenção do produto,
seção 18.1 do `spec-ios.md`, mantida também no Android). Ver `hora-do-remedio.dbml` neste
repositório para o diagrama completo.

### `remedios`

| Coluna iOS (SQLite) | Tipo Room/Kotlin | Observação |
| :---- | :---- | :---- |
| `id` INTEGER PK | `Long` `@PrimaryKey(autoGenerate = true)` | |
| `nome` TEXT NOT NULL | `String` | |
| `dose` TEXT | `String?` | |
| `forma` TEXT NOT NULL DEFAULT 'comprimido' | `String` (enum `Forma` serializado como texto) | |
| `frequencia` TEXT NOT NULL DEFAULT 'diario' | `String` (enum `Frequencia`) | |
| `intervalo_dias` INTEGER | `Int?` | Mantido no schema por completude, igual ao iOS — **cortado da UI** (ver §7) |
| `dias_semana` TEXT | `String?` | |
| `observacao` TEXT | `String?` | |
| `lembrete_ativo` INTEGER NOT NULL DEFAULT 1 | `Boolean` (`TypeConverter` Int↔Boolean) | |
| `cor` TEXT NOT NULL | `String` (enum `Cor`) | |
| `arquivado_em` TEXT | `String?` (ISO 8601) | |
| `criado_em` TEXT NOT NULL | `String` | |
| `atualizado_em` TEXT NOT NULL | `String` | Mantida por trigger, igual ao iOS |

Constraints (CHECK) e o índice `idx_remedios_ativos` são recriados **literalmente iguais** via SQL
bruto num `RoomDatabase.Callback.onCreate` — os mesmos nomes de constraint do iOS
(`ck_remedios_nome_nao_vazio`, `ck_remedios_forma`, `ck_remedios_frequencia`, `ck_remedios_cor`,
`ck_remedios_intervalo_coerente`, `ck_remedios_dias_semana_coerente`,
`ck_remedios_lembrete_ativo`), porque SQLite é o mesmo motor dos dois lados.

### `horarios`

Igual ao iOS: `id`, `remedio_id` (FK `ON DELETE CASCADE`), `hora_minuto` (`ck_horarios_formato`),
`ordem`, `criado_em`. Índices `idx_horarios_remedio`, `idx_horarios_hora`, unique
`uq_horarios_remedio_hora`.

### `notificacoes_agendadas`

Igual ao iOS, com uma diferença de significado: `identificador_ios` (nome da coluna) vira o
identificador do **`AlarmManager`** (um `requestCode` inteiro, ou o `PendingIntent` associado) —
**mantém o nome da coluna** por consistência de schema entre plataformas (é só um identificador
opaco de agendamento, o nome não precisa dizer "iOS" para o Android funcionar; renomear quebraria a
paridade de schema sem ganho nenhum). FK `horario_id` `ON DELETE CASCADE`, unique
`uq_notificacoes_identificador`.

### `preferencias`

Igual ao iOS: linha única (`id = 1`, `ck_preferencias_linha_unica`), `escala_fonte`
(`ck_preferencias_escala`), `som_lembrete` (`ck_preferencias_som`), `onboarding_concluido_em`,
`atualizado_em` (trigger).

### Triggers

`trg_remedios_atualizado_em` e `trg_preferencias_atualizado_em`, **literalmente iguais** ao SQL do
iOS (`AFTER UPDATE ... BEGIN UPDATE ... SET atualizado_em = datetime('now') ... END`).

## 5. Mapa de tradução iOS → Android (específico deste app)

| iOS | Android |
| :---- | :---- |
| Expo Router (file-based) | Navigation Compose (`NavHost` + rotas) |
| `expo-sqlite` + Drizzle ORM | **Room** (DAO com `Flow` para as queries reativas) |
| `useLiveQuery` | `Flow` do Room + `collectAsStateWithLifecycle()` |
| `expo-notifications` (`UNCalendarNotificationTrigger`, `DAILY`/`WEEKLY`/`DATE`) | `AlarmManager.setExactAndAllowWhileIdle` (repetição manual: o Android não tem um trigger nativo "toda semana às terças" — o alarme reagenda a si mesmo ao disparar, ou a reconciliação recalcula o próximo horário a cada abertura, igual à estratégia de `intervalo_dias` do iOS) + `NotificationChannel`/`NotificationManager` para exibir |
| Cota de 64 notificações pendentes (regra 11.4 do iOS, restrição dura da plataforma) | **Não existe** limite equivalente do `AlarmManager` — divergência documentada: a lógica de `calcularCustoDeAgendamento`/priorização por proximidade é mantida (não custa nada e é boa prática), mas o corte rígido em 64 e a faixa "cota estourada" da Home **não se aplicam** no Android; ver §7 |
| `DateTimePicker` (spinner nativo) | `TimePicker` do Material 3 (`rememberTimePickerState`) |
| `Switch` (RN) | `Switch` (M3) |
| `Share.share` (share sheet) | `Intent.ACTION_SEND` (chooser nativo do Android) |
| `Linking.openURL` (Política de Privacidade) | `Intent.ACTION_VIEW` |
| Ícone do app (`AppIcon`) | Ícone adaptativo (`mipmap-anydpi-v26`: background + foreground na zona segura), reaproveitando o mesmo ícone 1024×1024 do iOS |
| `expo-localization` (fuso do aparelho) | `java.time.ZoneId.systemDefault()` |
| SafeAreaView / insets | `Scaffold` + `WindowInsets` (`systemBarsPadding()`) |
| Splash screen (`expo-splash-screen`) | `SplashScreen` API (`androidx.core.splashscreen`), mesma lógica de segurar até banco+preferências carregarem |

## 6. Regra da cota — divergência documentada desde já

A regra 11.4 do iOS (cota de 64 notificações pendentes) é uma restrição **dura e específica do
iOS** — não existe um teto equivalente documentado para o `AlarmManager` do Android (o limite
prático real é a bateria/Doze mode, não uma contagem fixa). Decisão para a v1 Android:

- Mantém a função pura de cálculo de custo (equivalente a `calcularCustoDeAgendamento`) porque é
  barata e útil como sinal de "este remédio tem muitos horários", mas **não** trava em 64 nem
  mostra a faixa "cota estourada" — em vez disso, cada remédio ativo com `lembrete_ativo = 1`
  agenda todos os seus alarmes, sempre.
- Cada alarme individual usa `setExactAndAllowWhileIdle` (preciso mesmo em Doze), e a
  reconciliação (equivalente à 13-A.3 do iOS) roda na abertura do app e depois de reiniciar o
  aparelho (`BOOT_COMPLETED`) — **alarmes exatos do Android não sobrevivem a reboot sozinhos**,
  diferente das notificações locais do iOS, que persistem. Isso é uma peça que **não pode
  quebrar**: sem o `BroadcastReceiver` de `BOOT_COMPLETED` reagendando tudo, os lembretes somem
  silenciosamente depois que o idoso reinicia o telefone.

## 7. `intervalo_dias` — mesma decisão do iOS

Igual ao iOS (pergunta aberta 25.8 do `spec-ios.md`, já respondida): a coluna existe no schema, a
lógica de cálculo existe e é testada, mas o bloco 4 do cadastro não oferece "A cada X dias" como
opção — só `diario`, `dias_semana`, `quando_precisar`.

## 8. Épicos — mesma ordem do iOS

Implementação segue a ordem do `spec-ios.md` seção 8, validando os critérios de aceite de cada
épico antes de avançar, adaptados para Android onde a seção 5 ou 6 deste documento define a
tradução:

1. **8.0 Fundação** — projeto Gradle, Room + schema + triggers, tema Material 3, splash.
2. **8.1 Home** — lista de remédios ativos agrupada por horário (Compose, `Flow` reativo).
3. **8.2 Cadastro** — formulário de 8 blocos, `TimePicker` nativo, validação só ao salvar.
4. **8.3 Detalhe, edição e arquivamento** — mesmas 3 telas/rotas do iOS.
5. **8.4 Lembretes** — `AlarmManager` + `NotificationManager` + `BroadcastReceiver` de disparo e de
   `BOOT_COMPLETED`; fluxo de permissão (`POST_NOTIFICATIONS`, Android 13+, `SCHEDULE_EXACT_ALARM`
   quando aplicável).
6. **8.5 Exportação** — `gerarTexto` (função pura, portada 1:1, o teste de comparação literal com
   o exemplo da 14.1 é o mesmo) + `Intent.ACTION_SEND`.
7. **8.6 Ajustes** — mesmos itens, mesma ordem; "Apagar todos os meus dados" limpa o Room e
   cancela todos os `PendingIntent` do `AlarmManager`.
8. **8.7 Onboarding** — 2 telas, mesmo texto.

Cada épico verificado com `./gradlew assembleDebug`, testes JVM para lógica pura (equivalente aos
testes Jest do iOS: `calcularCustoDeAgendamento`, `gerarTexto`, `agruparPorHorario`,
codificação de `dias_semana`), testes instrumentados para o que precisa de Android real (Room,
`AlarmManager`), e capturas de tela do emulador a cada tela nova.

## 9. Convenções (idênticas ao iOS)

- Banco em português `snake_case` (já é assim por natureza, mesmo SQL).
- Código Kotlin em inglês (nomes de classe/função/variável).
- Comentários em português citando a seção do spec/doc técnica que implementam — mesmo padrão do
  exemplo da 18.2 do iOS.
- Textos de interface centralizados (equivalente a `textos.ts`): `strings.xml` com os textos
  exatos da seção 15.5 do `spec-ios.md`, letra por letra.
- Sem `.env`, sem segredo, sem conta — herda tudo do iOS.

## 10. O que fica fora (igual ao iOS)

Nada da seção 28 (roadmap v2/v3) nem da 11.9. Sem Play Billing (não há compra no app). Sem
Google Sign-In nem qualquer login.

---

**Próximos passos deste documento:** gerar `hora-do-remedio.dbml`, montar o esqueleto Gradle
(`libs.versions.toml`, `app/build.gradle.kts`), e então implementar épico por épico.
