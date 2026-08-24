// CalculoAgendamentoTest.kt — cobre spec.md §5/§6/§7: próximo disparo (diário, dias da semana,
// intervalo_dias mantido por completude) e o custo de agendamento sem o corte de 64 do iOS.

package com.dablab.horadoremedio.lembretes

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

private val ZONA = ZoneId.of("America/Sao_Paulo")

class CalculoAgendamentoTest {
    @Test
    fun `diario ainda nao passou hoje agenda para hoje`() {
        val agora = ZonedDateTime.of(2026, 7, 26, 7, 0, 0, 0, ZONA)
        val proximo = proximoDisparoMillis("08:00", "diario", diasSemana = null, agora = agora)
        val esperado = ZonedDateTime.of(2026, 7, 26, 8, 0, 0, 0, ZONA).toInstant().toEpochMilli()
        assertEquals(esperado, proximo)
    }

    @Test
    fun `diario ja passou hoje agenda para amanha`() {
        val agora = ZonedDateTime.of(2026, 7, 26, 9, 0, 0, 0, ZONA)
        val proximo = proximoDisparoMillis("08:00", "diario", diasSemana = null, agora = agora)
        val esperado = ZonedDateTime.of(2026, 7, 27, 8, 0, 0, 0, ZONA).toInstant().toEpochMilli()
        assertEquals(esperado, proximo)
    }

    @Test
    fun `dias_semana pula para o proximo dia da lista`() {
        // 26-07-2026 é domingo (dígito 1). dias_semana = "246" = seg, qua, sex.
        val agora = ZonedDateTime.of(2026, 7, 26, 7, 0, 0, 0, ZONA)
        val proximo = proximoDisparoMillis("08:00", "dias_semana", diasSemana = "246", agora = agora)
        val esperado = ZonedDateTime.of(2026, 7, 27, 8, 0, 0, 0, ZONA).toInstant().toEpochMilli() // segunda
        assertEquals(esperado, proximo)
    }

    @Test
    fun `intervalo_dias mantido para paridade de testes, nao alimentado pela UI`() {
        val base = ZonedDateTime.of(2026, 7, 26, 8, 0, 0, 0, ZONA)
        val proximo = proximoDisparoMillis(
            "08:00", "intervalo_dias", diasSemana = null, ultimoDisparo = base, intervaloDias = 3, agora = base.plusHours(1),
        )
        val esperado = base.plusDays(3).toInstant().toEpochMilli()
        assertEquals(esperado, proximo)
    }

    @Test
    fun `custo diario e numero de horarios`() {
        assertEquals(2, calcularCustoDeAgendamento("diario", numeroHorarios = 2))
    }

    @Test
    fun `custo dias_semana e dias vezes horarios`() {
        assertEquals(6, calcularCustoDeAgendamento("dias_semana", numeroHorarios = 2, diasSemana = "246"))
    }

    @Test
    fun `custo quando_precisar e zero`() {
        assertEquals(0, calcularCustoDeAgendamento("quando_precisar", numeroHorarios = 0))
    }
}
