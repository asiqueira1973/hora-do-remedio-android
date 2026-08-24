# SCHEMA.md

Dump do schema real do SQLite (`horadoremedio.db`), gerado com `sqlite3 <banco> ".schema --indent"`
a partir do banco do app rodando no simulador — não escrito à mão (18.4). Atualizar este arquivo a
cada migration nova.

## PRAGMAs aplicados pelo app na abertura da conexão (`src/db/client.ts`)

Não ficam persistidos no arquivo `.db` — são configurados a cada conexão nova:

```sql
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;
```

## Tabelas, constraints e índices

```sql
CREATE TABLE `remedios`(
  `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
  `nome` text NOT NULL,
  `dose` text,
  `forma` text DEFAULT 'comprimido' NOT NULL,
  `frequencia` text DEFAULT 'diario' NOT NULL,
  `intervalo_dias` integer,
  `dias_semana` text,
  `observacao` text,
  `lembrete_ativo` integer DEFAULT 1 NOT NULL,
  `cor` text NOT NULL,
  `arquivado_em` text,
  `criado_em` text DEFAULT(datetime('now')) NOT NULL,
  `atualizado_em` text DEFAULT(datetime('now')) NOT NULL,
  CONSTRAINT "ck_remedios_nome_nao_vazio" CHECK(length(trim("remedios"."nome")) BETWEEN 1 AND 60),
  CONSTRAINT "ck_remedios_forma" CHECK("remedios"."forma" IN('comprimido', 'capsula', 'gotas', 'xarope', 'injecao', 'pomada', 'spray', 'outro')),
  CONSTRAINT "ck_remedios_frequencia" CHECK("remedios"."frequencia" IN('diario', 'dias_semana', 'intervalo_dias', 'quando_precisar')),
  CONSTRAINT "ck_remedios_cor" CHECK("remedios"."cor" IN('coral', 'azul', 'verde', 'amarelo', 'lilas', 'pessego')),
  CONSTRAINT "ck_remedios_intervalo_coerente" CHECK(("remedios"."frequencia" = 'intervalo_dias' AND "remedios"."intervalo_dias" BETWEEN 2 AND 30) OR("remedios"."frequencia" <> 'intervalo_dias' AND "remedios"."intervalo_dias" IS NULL)),
  CONSTRAINT "ck_remedios_dias_semana_coerente" CHECK(("remedios"."frequencia" = 'dias_semana' AND "remedios"."dias_semana" GLOB '[1-7]*' AND length("remedios"."dias_semana") BETWEEN 1 AND 7) OR("remedios"."frequencia" <> 'dias_semana' AND "remedios"."dias_semana" IS NULL)),
  CONSTRAINT "ck_remedios_lembrete_ativo" CHECK("remedios"."lembrete_ativo" IN(0, 1))
);
CREATE INDEX `idx_remedios_ativos` ON `remedios`(`arquivado_em`);

CREATE TABLE `horarios`(
  `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
  `remedio_id` integer NOT NULL,
  `hora_minuto` text NOT NULL,
  `ordem` integer DEFAULT 0 NOT NULL,
  `criado_em` text DEFAULT(datetime('now')) NOT NULL,
  FOREIGN KEY(`remedio_id`) REFERENCES `remedios`(`id`) ON UPDATE no action ON DELETE cascade,
  CONSTRAINT "ck_horarios_formato" CHECK("horarios"."hora_minuto" GLOB '[0-2][0-9]:[0-5][0-9]')
);
CREATE INDEX `idx_horarios_remedio` ON `horarios`(`remedio_id`);
CREATE INDEX `idx_horarios_hora` ON `horarios`(`hora_minuto`);
CREATE UNIQUE INDEX `uq_horarios_remedio_hora` ON `horarios`(`remedio_id`, `hora_minuto`);

CREATE TABLE `notificacoes_agendadas`(
  `id` integer PRIMARY KEY AUTOINCREMENT NOT NULL,
  `horario_id` integer NOT NULL,
  `identificador_ios` text NOT NULL,
  `disparo_em` text,
  `criado_em` text DEFAULT(datetime('now')) NOT NULL,
  FOREIGN KEY(`horario_id`) REFERENCES `horarios`(`id`) ON UPDATE no action ON DELETE cascade
);
CREATE INDEX `idx_notificacoes_horario` ON `notificacoes_agendadas`(`horario_id`);
CREATE INDEX `idx_notificacoes_disparo` ON `notificacoes_agendadas`(`disparo_em`);
CREATE UNIQUE INDEX `uq_notificacoes_identificador` ON `notificacoes_agendadas`(`identificador_ios`);

CREATE TABLE `preferencias`(
  `id` integer PRIMARY KEY DEFAULT 1 NOT NULL,
  `escala_fonte` text DEFAULT 'grande' NOT NULL,
  `som_lembrete` integer DEFAULT 1 NOT NULL,
  `onboarding_concluido_em` text,
  `atualizado_em` text DEFAULT(datetime('now')) NOT NULL,
  CONSTRAINT "ck_preferencias_linha_unica" CHECK("preferencias"."id" = 1),
  CONSTRAINT "ck_preferencias_escala" CHECK("preferencias"."escala_fonte" IN('grande', 'gigante')),
  CONSTRAINT "ck_preferencias_som" CHECK("preferencias"."som_lembrete" IN(0, 1))
);
```

## Triggers

```sql
CREATE TRIGGER trg_remedios_atualizado_em
AFTER UPDATE ON remedios
FOR EACH ROW
BEGIN
  UPDATE remedios SET atualizado_em = datetime('now') WHERE id = NEW.id;
END;

CREATE TRIGGER trg_preferencias_atualizado_em
AFTER UPDATE ON preferencias
FOR EACH ROW
BEGIN
  UPDATE preferencias SET atualizado_em = datetime('now') WHERE id = NEW.id;
END;
```

Ver `DOCUMENTACAO-TECNICA.md` §19.1–19.3 para a explicação em português de cada tabela, coluna,
relação e regra.
