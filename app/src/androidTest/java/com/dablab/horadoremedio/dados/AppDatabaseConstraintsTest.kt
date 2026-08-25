// AppDatabaseConstraintsTest.kt — testa a DDL bruta de AppDatabase.recriarComConstraints (spec-
// ios.md 7.2-7.7): CHECK, FOREIGN KEY, UNIQUE e trigger que o Room não valida sozinho e só o
// SQLite real aplica de verdade — por isso é um teste instrumentado (androidTest), não JVM puro
// (roteiro de implementação, Fase 9, teste "constraints do banco").

package com.dablab.horadoremedio.dados

import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseConstraintsTest {
    private lateinit var db: AppDatabase

    @Before
    fun criar() {
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = AppDatabase.construirEmMemoriaParaTeste(contexto)
    }

    @After
    fun fechar() {
        db.close()
    }

    private fun exec(sql: String) = db.openHelper.writableDatabase.execSQL(sql)

    private fun contar(tabela: String): Int =
        db.openHelper.writableDatabase.query("SELECT COUNT(*) FROM $tabela").use {
            it.moveToFirst()
            it.getInt(0)
        }

    private fun inserirRemedioValido(id: Int = 1) {
        exec(
            """
            INSERT INTO remedios (id, nome, forma, frequencia, lembrete_ativo, cor, criado_em, atualizado_em)
            VALUES ($id, 'Losartana', 'comprimido', 'diario', 1, 'coral', '2020-01-01T00:00:00Z', '2020-01-01T00:00:00Z')
            """.trimIndent()
        )
    }

    @Test
    fun nomeVazioEhRejeitado() {
        assertThrows(SQLiteConstraintException::class.java) {
            exec(
                "INSERT INTO remedios (id, nome, forma, frequencia, lembrete_ativo, cor, criado_em, atualizado_em) " +
                    "VALUES (1, '', 'comprimido', 'diario', 1, 'coral', 'x', 'x')"
            )
        }
    }

    @Test
    fun formaForaDaListaPermitidaEhRejeitada() {
        assertThrows(SQLiteConstraintException::class.java) {
            exec(
                "INSERT INTO remedios (id, nome, forma, frequencia, lembrete_ativo, cor, criado_em, atualizado_em) " +
                    "VALUES (1, 'X', 'invalida', 'diario', 1, 'coral', 'x', 'x')"
            )
        }
    }

    @Test
    fun frequenciaDiasSemanaComDigitoForaDoIntervalo1a7EhRejeitada() {
        // '89' não passa no GLOB '[1-7]*' (8 e 9 não são dias válidos).
        assertThrows(SQLiteConstraintException::class.java) {
            exec(
                "INSERT INTO remedios (id, nome, forma, frequencia, dias_semana, lembrete_ativo, cor, criado_em, atualizado_em) " +
                    "VALUES (1, 'X', 'comprimido', 'dias_semana', '89', 1, 'coral', 'x', 'x')"
            )
        }
    }

    @Test
    fun intervaloDiasForaDoIntervaloPermitidoEhRejeitado() {
        assertThrows(SQLiteConstraintException::class.java) {
            exec(
                "INSERT INTO remedios (id, nome, forma, frequencia, intervalo_dias, lembrete_ativo, cor, criado_em, atualizado_em) " +
                    "VALUES (1, 'X', 'comprimido', 'intervalo_dias', 1, 1, 'coral', 'x', 'x')"
            )
        }
    }

    @Test
    fun horarioComFormatoInvalidoEhRejeitado() {
        inserirRemedioValido()
        assertThrows(SQLiteConstraintException::class.java) {
            exec("INSERT INTO horarios (remedio_id, hora_minuto, ordem, criado_em) VALUES (1, '25:99', 0, 'x')")
        }
    }

    @Test
    fun horarioDuplicadoNoMesmoRemedioEhRejeitado() {
        inserirRemedioValido()
        exec("INSERT INTO horarios (remedio_id, hora_minuto, ordem, criado_em) VALUES (1, '08:00', 0, 'x')")
        assertThrows(SQLiteConstraintException::class.java) {
            exec("INSERT INTO horarios (remedio_id, hora_minuto, ordem, criado_em) VALUES (1, '08:00', 1, 'x')")
        }
    }

    @Test
    fun horarioSemRemedioCorrespondenteEhRejeitadoPelaForeignKey() {
        assertThrows(SQLiteConstraintException::class.java) {
            exec("INSERT INTO horarios (remedio_id, hora_minuto, ordem, criado_em) VALUES (999, '08:00', 0, 'x')")
        }
    }

    @Test
    fun apagarRemedioApagaHorariosEmCascata() {
        inserirRemedioValido()
        exec("INSERT INTO horarios (remedio_id, hora_minuto, ordem, criado_em) VALUES (1, '08:00', 0, 'x')")
        assertEquals(1, contar("horarios"))

        exec("DELETE FROM remedios WHERE id = 1")

        assertEquals(0, contar("horarios"))
    }

    @Test
    fun triggerAtualizaAtualizadoEmAoEditarRemedio() {
        inserirRemedioValido() // atualizado_em fixo em '2020-01-01T00:00:00Z'

        exec("UPDATE remedios SET nome = 'Losartana potássica' WHERE id = 1")

        val atualizadoEm = db.openHelper.writableDatabase.query("SELECT atualizado_em FROM remedios WHERE id = 1").use {
            it.moveToFirst()
            it.getString(0)
        }
        assertNotEquals("2020-01-01T00:00:00Z", atualizadoEm)
    }
}
