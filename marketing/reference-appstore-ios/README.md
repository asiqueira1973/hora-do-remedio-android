# Referência — marketing App Store (iOS)

3 telas de marketing geradas em 2026-08-24 pro app iOS (`~/hora-do-remedio`), no visual azul
atual (pós-redesenho), tamanho 6.5" da App Store (1284×2778px): Home, Cadastro, Ajustes.

Guardadas aqui pra servirem de referência visual quando chegar a hora de gerar as telas
equivalentes da Play Store para este port Android — mesmo conteúdo/copy, mas recriando as telas
reais do app Android (Material 3, não a interface iOS) e no tamanho que o Google exige:

**Tamanho de screenshot de telefone da Play Store** (checar de novo na hora, política muda):
JPEG ou PNG 24-bit, sem canal alfa, dimensão mínima 320px, máxima 3840px, razão entre o lado maior
e o menor ≤ 2:1. Tamanho comum usado: 1080×1920 (9:16) ou 1080×2400 pra bater com telas modernas.
Mínimo de 2 screenshots, até 8.

Arquivos:
- `hora-do-remedio-appstore-{1,2,3}-*.jpg` — as imagens finais (App Store, referência visual).
- `Main.dc.html`, `Cadastro.dc.html`, `Ajustes.dc.html`, `canvas.json` — fonte do canvas de design
  (Claude Design), caso precise reabrir/editar antes de adaptar pro Android.
