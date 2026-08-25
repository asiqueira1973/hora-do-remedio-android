# Google Play Console — Hora do Remédio (pacote de preenchimento)

Pronto para colar no Play Console. App: **Hora do Remédio** — lembrete de remédios para idosos,
Android nativo, local-first, 100% offline, **grátis, sem compra dentro do app**. Package
`com.dablab.horadoremedio`. Conta **Dab Lab Developer**.

> Reaproveita os ativos da Dab Lab (mesmo do iOS e do I See Life Android): Support URL no Carrd,
> contato `alexandre@dablab.com.br`. Publica **direto em produção** (conta estabelecida, isenta do
> teste fechado de 20 pessoas).

**Build assinado:** `app/build/outputs/bundle/release/app-release.aab` (gerado nesta sessão,
`versionCode 1`, `versionName 1.0.0`, assinado com a upload key `~/keys/horadoremedio-upload.jks`
— senha em `keystore.properties`, gitignored, não repetida aqui por segurança).

---

## A. Ficha da loja (Main store listing)

- **App name (≤30):** `Hora do Remédio`
- **Short description (≤80 chars):**
  ```
  Cadastre o remédio, a dose e o horário. O app avisa na hora certa — sem internet.
  ```
- **Full description (≤4000):**
  ```
  Um jeito simples de lembrar de tomar seus remédios no horário certo. Sem conta, sem login — tudo fica guardado só no seu celular, nada é enviado para a internet.

  O QUE O APP FAZ
  Você cadastra o remédio, a dose e o horário
  Ele avisa na hora certa, mesmo com o app fechado
  A tela mostra letra grande, pensada para quem tem dificuldade de enxergar
  Dá para compartilhar sua lista de remédios em texto, pronta para mandar por WhatsApp a um familiar ou cuidador
  Parar de tomar um remédio é simples, e dá para voltar depois

  FEITO PARA QUEM TEM DIFICULDADE COM TELA PEQUENA
  Letras grandes em todo o app, com uma opção "gigante" para quem precisar de ainda mais. Botões grandes, fáceis de tocar. Nada de menu escondido ou gesto complicado.

  SEM CONTA, SEM INTERNET, SEM ANÚNCIO
  O Hora do Remédio funciona inteiro sem sinal. Não pede e-mail, não pede senha, não mostra propaganda. Suas informações nunca saem do aparelho.

  Cadastre o primeiro remédio em menos de um minuto e deixe o app cuidar de avisar na hora certa.
  ```
- **App icon (512×512):** `play-assets/play_store_icon_512.png` (gerado nesta sessão a partir do
  ícone real do app — sino com coração, fundo coral `#E15E44`)
- **Feature graphic (1024×500):** `play-assets/feature_graphic_1024x500.png` (gerado nesta sessão)
- **Phone screenshots (3, 1080×1920, capturadas do app real rodando no emulador):**
  - `play-assets/screenshots/horadoremedio-play-1-home.jpg` — Home com remédios cadastrados,
    agrupados por horário
  - `play-assets/screenshots/horadoremedio-play-2-detalhe.jpg` — Detalhe de um remédio
  - `play-assets/screenshots/horadoremedio-play-3-ajustes.jpg` — Ajustes (tamanho de letra, som,
    privacidade)

---

## B. Categoria e detalhes de contato (App category / contact details)

- **App category:** `Health & Fitness` ✅ (decidido pelo Alexandre)
- **Email:** `alexandre@dablab.com.br`
- **Website:** `https://suportedablab.carrd.co/`
- **Telefone:** opcional (pode deixar em branco)

---

## C. Política de privacidade (Privacy policy)

- **URL:** reaproveitar a página do Notion já usada dentro do próprio app iOS/Android ("Sobre o
  app" em Ajustes):
  `https://app.notion.com/p/Pol-tica-de-Privacidade-Hora-do-Rem-dio-3ab4305f24e180ec88dedebfcade9f95?source=copy_link`
- **Adicionar (ou revisar) no documento, se ainda estiver escrito só pensando em iPhone** — trocar
  qualquer menção a "iPhone"/"iOS" por linguagem neutra de plataforma, ou adicionar este parágrafo
  específico do Android:

> ### Hora do Remédio — versão Android
>
> **1. Coleta de Dados.** O Hora do Remédio não coleta, transmite nem armazena dados pessoais em
> servidores. O app não tem conta, cadastro, login nem backend, e não usa Firebase, analytics ou
> ferramentas de crash reporting. As únicas informações guardadas são os remédios, doses e horários
> que você cadastra, e suas preferências de uso (tamanho de letra, som do aviso) — tudo fica
> exclusivamente no seu aparelho Android.
>
> **2. Uso dos Dados.** As informações que você cadastra são usadas apenas para mostrar seus
> remédios e avisar na hora certa, no seu próprio aparelho.
>
> **3. Armazenamento e Segurança.** Todos os dados ficam localmente no dispositivo e são removidos
> quando o app é desinstalado (ou ao usar "Apagar todos os meus dados" dentro do app). O app
> funciona 100% offline.
>
> **4. Permissões do sistema.** O app pede permissão de notificação e de alarme exato do Android
> só para poder avisar na hora do remédio — essas permissões não dão acesso a nenhum dado seu, só
> permitem que o aparelho toque o aviso no horário certo.
>
> **5. Compartilhamento com Terceiros.** Nenhum dado é compartilhado com terceiros. O app é
> gratuito, sem compra dentro do app.
>
> **6. Contato.** Dúvidas sobre privacidade: alexandre@dablab.com.br.

---

## D. Declarações do app ("Set up your app" — tarefas do painel)

- **App access (Sign in details):** **"All functionality is available without any special
  access"** (sem login/conta). Nada a fornecer.
- **Ads:** **"No, my app does not contain ads."**
- **Content rating (questionário IARC):**
  - Email: `alexandre@dablab.com.br`
  - Categoria do app no questionário: **Utility/Reference/Other** (compatível com `Health &
    Fitness`) — NÃO é jogo.
  - Todas as perguntas de conteúdo: **Não** (sem violência, sexo, linguagem imprópria, drogas,
    jogos de azar, conteúdo assustador, UGC compartilhado, etc.). Resultado esperado: **Livre /
    Everyone**.
- **Target audience and content (público-alvo):**
  - Faixas etárias: marque **18 e acima** (o público-alvo real é idoso; não há razão pra marcar
    faixas infantis). **NÃO** marque faixas abaixo de 13 (evita a política de Famílias).
  - "Appeals to children?" **No**.
- **Data safety:** **Nenhum dado coletado e nenhum dado compartilhado.**
  - "Does your app collect or share any of the required user data types?" → **No**.
  - Justificativa (caso pergunte): os remédios/doses/horários cadastrados e as preferências ficam
    só no aparelho (SQLite local via Room), nunca são transmitidos. As permissões de notificação e
    alarme exato são acesso a funcionalidade do sistema, não coleta de dado. O app é 100% offline,
    sem SDK de terceiro.
  - "Is all of the user data encrypted in transit?" → não se aplica (não há transmissão) — se
    exigir resposta, **Yes** (não há tráfego de dados do usuário).
  - "Do you provide a way for users to request that their data is deleted?" → sim: o próprio app
    tem "Apagar todos os meus dados" em Ajustes, além de desinstalar o app remover tudo.
- **Government apps:** **No.**
- **Financial features:** **No.**
- **Health apps:** o Play pode perguntar sobre app de saúde mesmo em `Health & Fitness` —
  responder que é um **lembrete pessoal**, não um app que dá diagnóstico, tratamento ou
  aconselhamento médico (mesmo disclaimer recomendado no "Sobre o app" da App Store, seção 20.6 do
  spec.md do iOS: *"Este app é um lembrete pessoal. Ele não substitui a orientação do seu
  médico."* — vale considerar adicionar essa frase também na tela "Sobre o app" do Android, hoje
  ela não está lá).

---

## E. Produto in-app

**Não se aplica.** O Hora do Remédio é gratuito, sem nenhuma compra dentro do app — diferente do I
See Life. Pular a seção "Monetize with Play" inteira.

## F. Testador de licença

**Não se aplica** (sem produto in-app pra testar).

---

## G. Release de produção (Production → Create new release)

- **App bundle:** subir `app/build/outputs/bundle/release/app-release.aab` (versionCode 1,
  versionName 1.0.0, assinado com a upload key).
- **Play App Signing:** aceitar (o Google gera/gerencia a chave de assinatura final; você sobe com
  a upload key). Recomendado.
- **Release name:** `1.0.0 (1)`
- **Release notes (pt-BR):**
  ```
  Primeira versão do Hora do Remédio para Android: cadastre o remédio, a dose e o horário, e o app avisa na hora certa. Letra grande em todo o app (com opção ainda maior), compartilhamento da lista em texto para mandar a um familiar ou cuidador, e tudo funcionando 100% offline, sem conta e sem anúncio.
  ```

---

## Pendências suas

1. **Revisar a política de privacidade no Notion** — adicionar o parágrafo Android da seção C, ou
   confirmar que a versão atual já serve sem menções a "iPhone"/"iOS".
2. **Countries/regions:** disponibilidade — sugiro todos os países (ou só Brasil, se preferir).
3. Considerar adicionar o disclaimer médico ("Este app é um lembrete pessoal...") na tela "Sobre o
   app" do Android (hoje só tem o texto de privacidade) — reduz risco de questionamento na seção
   "Health apps" das declarações. Aviso, não bloqueante: não fiz essa mudança de código sem sua
   confirmação, por ser texto que aparece pro usuário final.
