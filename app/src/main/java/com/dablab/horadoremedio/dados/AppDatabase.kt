// AppDatabase.kt — banco Room (spec.md §4). Room só gera CREATE TABLE simples a partir das
// entidades (sem CHECK/FK/trigger); no onCreate do Callback essas tabelas são DROPadas e
// recriadas com a DDL completa em SQL bruto — mesmos nomes de constraint/índice/trigger do iOS
// (spec-ios.md 7.2-7.7), porque SQLite é o mesmo motor dos dois lados (spec.md §4).
//
// Isso é seguro porque a validação de schema do Room (RoomOpenHelper) só compara nome/tipo/
// notNull/PK de coluna via `PRAGMA table_info` — CHECK, UNIQUE extra e trigger não fazem parte
// dessa comparação. Por isso as colunas abaixo não usam `DEFAULT` em SQL: a entidade Kotlin já
// preenche todo valor no insert, e um DEFAULT que a entidade não declarou quebraria a validação.

package com.dablab.horadoremedio.dados

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        RemedioEntity::class,
        HorarioEntity::class,
        NotificacaoAgendadaEntity::class,
        PreferenciasEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun remedioDao(): RemedioDao
    abstract fun horarioDao(): HorarioDao
    abstract fun notificacaoAgendadaDao(): NotificacaoAgendadaDao
    abstract fun preferenciasDao(): PreferenciasDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obter(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hora_do_remedio.db",
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING) // spec-ios.md 7.7
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            recriarComConstraints(db)
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            db.execSQL("PRAGMA foreign_keys = ON") // spec-ios.md 7.7, por conexão
                            // O SQLite empacotado no Android (diferente do iOS) liga
                            // recursive_triggers por padrão — sem isto, o UPDATE que o próprio
                            // trigger de atualizado_em faz dispara o trigger de novo, e de novo,
                            // até "too many levels of trigger recursion" (crash real, visto no
                            // emulador ao concluir o onboarding).
                            db.execSQL("PRAGMA recursive_triggers = OFF")
                        }
                    })
                    .build()
                    .also { instancia = it }
            }

        private fun recriarComConstraints(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS remedios")
            db.execSQL(
                """
                CREATE TABLE remedios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    nome TEXT NOT NULL,
                    dose TEXT,
                    forma TEXT NOT NULL,
                    frequencia TEXT NOT NULL,
                    intervalo_dias INTEGER,
                    dias_semana TEXT,
                    observacao TEXT,
                    lembrete_ativo INTEGER NOT NULL,
                    cor TEXT NOT NULL,
                    arquivado_em TEXT,
                    criado_em TEXT NOT NULL,
                    atualizado_em TEXT NOT NULL,
                    CONSTRAINT ck_remedios_nome_nao_vazio CHECK (length(trim(nome)) BETWEEN 1 AND 60),
                    CONSTRAINT ck_remedios_forma CHECK (forma IN
                        ('comprimido','capsula','gotas','xarope','injecao','pomada','spray','outro')),
                    CONSTRAINT ck_remedios_frequencia CHECK (frequencia IN
                        ('diario','dias_semana','intervalo_dias','quando_precisar')),
                    CONSTRAINT ck_remedios_cor CHECK (cor IN
                        ('coral','azul','verde','amarelo','lilas','pessego')),
                    CONSTRAINT ck_remedios_intervalo_coerente CHECK (
                        (frequencia = 'intervalo_dias' AND intervalo_dias BETWEEN 2 AND 30)
                        OR (frequencia <> 'intervalo_dias' AND intervalo_dias IS NULL)
                    ),
                    CONSTRAINT ck_remedios_dias_semana_coerente CHECK (
                        (frequencia = 'dias_semana' AND dias_semana GLOB '[1-7]*' AND length(dias_semana) BETWEEN 1 AND 7)
                        OR (frequencia <> 'dias_semana' AND dias_semana IS NULL)
                    ),
                    CONSTRAINT ck_remedios_lembrete_ativo CHECK (lembrete_ativo IN (0, 1))
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX idx_remedios_ativos ON remedios (arquivado_em)")

            db.execSQL("DROP TABLE IF EXISTS horarios")
            db.execSQL(
                """
                CREATE TABLE horarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    remedio_id INTEGER NOT NULL,
                    hora_minuto TEXT NOT NULL,
                    ordem INTEGER NOT NULL,
                    criado_em TEXT NOT NULL,
                    CONSTRAINT ck_horarios_formato CHECK (hora_minuto GLOB '[0-2][0-9]:[0-5][0-9]'),
                    FOREIGN KEY (remedio_id) REFERENCES remedios(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX uq_horarios_remedio_hora ON horarios (remedio_id, hora_minuto)")
            db.execSQL("CREATE INDEX idx_horarios_remedio ON horarios (remedio_id)")
            db.execSQL("CREATE INDEX idx_horarios_hora ON horarios (hora_minuto)")

            db.execSQL("DROP TABLE IF EXISTS notificacoes_agendadas")
            db.execSQL(
                """
                CREATE TABLE notificacoes_agendadas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    horario_id INTEGER NOT NULL,
                    identificador_ios TEXT NOT NULL,
                    disparo_em TEXT,
                    criado_em TEXT NOT NULL,
                    FOREIGN KEY (horario_id) REFERENCES horarios(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX uq_notificacoes_identificador ON notificacoes_agendadas (identificador_ios)")
            db.execSQL("CREATE INDEX idx_notificacoes_horario ON notificacoes_agendadas (horario_id)")
            db.execSQL("CREATE INDEX idx_notificacoes_disparo ON notificacoes_agendadas (disparo_em)")

            db.execSQL("DROP TABLE IF EXISTS preferencias")
            db.execSQL(
                """
                CREATE TABLE preferencias (
                    id INTEGER PRIMARY KEY NOT NULL,
                    escala_fonte TEXT NOT NULL,
                    som_lembrete INTEGER NOT NULL,
                    onboarding_concluido_em TEXT,
                    atualizado_em TEXT NOT NULL,
                    CONSTRAINT ck_preferencias_linha_unica CHECK (id = 1),
                    CONSTRAINT ck_preferencias_escala CHECK (escala_fonte IN ('grande','gigante')),
                    CONSTRAINT ck_preferencias_som CHECK (som_lembrete IN (0, 1))
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TRIGGER trg_remedios_atualizado_em AFTER UPDATE ON remedios
                BEGIN
                    UPDATE remedios SET atualizado_em = datetime('now') WHERE id = NEW.id;
                END
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TRIGGER trg_preferencias_atualizado_em AFTER UPDATE ON preferencias
                BEGIN
                    UPDATE preferencias SET atualizado_em = datetime('now') WHERE id = NEW.id;
                END
                """.trimIndent()
            )
        }
    }
}
