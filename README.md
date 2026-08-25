# Hora do Remédio (Android)

Lembrete de remédios para idosos: cadastra o remédio, a dose e o horário, e o app avisa na hora
certa — sem conta, sem login, sem internet. Tudo fica guardado só no aparelho.

Port nativo Android (Kotlin + Jetpack Compose) do app iOS [`Hora do Remédio`](../hora-do-remedio),
mesma proposta de produto, primitivas nativas de cada plataforma. Detalhes de arquitetura e as
divergências entre as duas plataformas estão em [`DOCUMENTACAO-TECNICA.md`](DOCUMENTACAO-TECNICA.md).

## Stack

- Kotlin + Jetpack Compose (Material Design 3, sem dynamic color)
- Room (SQLite) — schema com CHECK/FK/trigger nomeados, ver [`SCHEMA-ios.md`](SCHEMA-ios.md) para a
  estrutura (idêntica dos dois lados) e [`DOCUMENTACAO-TECNICA.md` §19.3](DOCUMENTACAO-TECNICA.md)
  para como ela chega ao SQLite no Android
- `AlarmManager` + `BroadcastReceiver` para os lembretes (sem servidor, sem push — 100% local)
- Navigation Compose (single-activity)
- `minSdk 26`, `targetSdk 35`, `compileSdk 35`, phone-only, retrato apenas

## Pré-requisitos

- JDK 21 — **não** o JDK mais novo do sistema; o Android Gradle Plugin não builda com JDK 25+.
  Qualquer JDK 21 serve (Homebrew `openjdk@21`, o JBR que vem com o Android Studio, etc.).
- Android SDK (command-line tools bastam — não precisa do Android Studio para compilar/rodar via
  linha de comando). `local.properties` aponta `sdk.dir` para a instalação local — não versionado.
- Um emulador ou device físico Android 8.0+ (API 26+) para rodar.

## Setup

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21     # ajuste pro seu JDK 21
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools   # ajuste pro seu SDK

./gradlew :app:assembleDebug
```

Se `local.properties` não existir, crie com uma linha `sdk.dir=<caminho do Android SDK>`.

## Rodar

Num emulador já criado (AVD), sem precisar abrir o Android Studio:

```bash
export ANDROID_SDK_ROOT=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"

emulator -avd <nome-do-avd> -no-snapshot -no-boot-anim &
adb wait-for-device
./gradlew installDebug
adb shell am start -n com.dablab.horadoremedio.debug/com.dablab.horadoremedio.MainActivity
```

Criar um AVD novo, se precisar: `avdmanager create avd -n Pixel_10 -k "system-image;android-35;google_apis;arm64-v8a"`
(ajuste a arquitetura da imagem pro seu Mac — `arm64-v8a` em Apple Silicon).

Num device físico com depuração USB ligada, troque o passo do emulador por `adb devices` para
confirmar que o aparelho aparece, e siga o resto igual.

## Testes

```bash
./gradlew test                        # testes unitários JVM — funções puras (agendamento, agrupamento da Home, exportação)
./gradlew connectedDebugAndroidTest    # testes instrumentados — CHECK/FK/UNIQUE/trigger do banco real, precisa de emulador/device rodando
./gradlew lint                        # lint Android
```

Os testes instrumentados (`app/src/androidTest/`) existem porque o Room só valida
nome/tipo/`NOT NULL`/PK de coluna — CHECK, FK e trigger só o SQLite real aplica, então só rodam num
dispositivo/emulador de verdade, nunca em JVM pura.

## Build de release

Assinatura lê `keystore.properties` na raiz do repo (não versionado, mesmo formato do app irmão
I See Life Android):

```properties
storeFile=/caminho/para/horadoremedio-upload.jks
storePassword=...
keyAlias=horadoremedio
keyPassword=...
```

Sem esse arquivo, `./gradlew assembleRelease`/`bundleRelease` builda sem assinatura (útil em dev,
não instalável/publicável como está).

## Estrutura do projeto

```
app/src/main/java/com/dablab/horadoremedio/
├── dados/          # Room: entidades, DAOs, AppDatabase (schema em SQL bruto), Repositorio
├── lembretes/       # AlarmManager: cálculo de agendamento (puro), AgendadorLembretes,
│                     # NotificationHelper, LembreteReceiver, BootReceiver
├── designsystem/     # Cores, tipografia, tema Material 3
├── exportacao/       # Geração e compartilhamento da lista de remédios em texto
└── telas/            # Uma pasta por tela (home, cadastro, detalhe, ajustes, arquivados,
                       # permissao, onboarding), RootView.kt como roteador raiz
```

## Documentos deste repositório

- [`CLAUDE.md`](CLAUDE.md) — guia operacional para quem (ou o agente que) for dar manutenção:
  comandos, decisões técnicas, bugs já encontrados e corrigidos, o que falta.
- [`DOCUMENTACAO-TECNICA.md`](DOCUMENTACAO-TECNICA.md) — o sistema como foi implementado: modelo de
  dados, lógica de cada tela, e as divergências em relação ao spec e ao port iOS.
- [`spec.md`](spec.md) — especificação de produto, comum às duas plataformas.
- `spec-ios.md`, `DOCUMENTACAO-TECNICA-ios.md`, `SCHEMA-ios.md` — cópias de referência do projeto
  iOS irmão, só para consulta (nunca editadas aqui).

## Estado atual

App navegável de ponta a ponta (onboarding → cadastro → Home → detalhe → edição → arquivamento →
ajustes), com o fluxo de permissão de notificação/alarme exato implementado e verificado no
emulador. O que falta antes de publicar está listado em [`CLAUDE.md`](CLAUDE.md#falta) — o item mais
importante é observar o disparo real da notificação num device físico (o emulador não é confiável
para isso).
