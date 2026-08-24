// ExportadorListaTest.kt — teste obrigatório 22.1.3 (spec-ios.md): gerarTexto comparado
// LITERALMENTE com o exemplo da seção 14.1 — porte 1:1 de exportadorLista.test.ts (iOS).

package com.dablab.horadoremedio.exportacao

import com.dablab.horadoremedio.dados.HorarioEntity
import com.dablab.horadoremedio.dados.RemedioComHorarios
import com.dablab.horadoremedio.dados.RemedioEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

private var proximoIdHorario = 1L

private fun horario(horaMinuto: String) =
    HorarioEntity(id = proximoIdHorario++, remedioId = 1, horaMinuto = horaMinuto, ordem = 0, criadoEm = "2026-07-01T00:00:00Z")

private fun remedioComHorarios(
    nome: String = "Remédio Teste",
    dose: String? = null,
    frequencia: String = "diario",
    diasSemana: String? = null,
    observacao: String? = null,
    horarios: List<HorarioEntity> = listOf(horario("08:00")),
): RemedioComHorarios {
    val remedio = RemedioEntity(
        id = 1,
        nome = nome,
        dose = dose,
        forma = "comprimido",
        frequencia = frequencia,
        diasSemana = diasSemana,
        observacao = observacao,
        cor = "coral",
        criadoEm = "2026-07-01T00:00:00Z",
        atualizadoEm = "2026-07-01T00:00:00Z",
    )
    return RemedioComHorarios(remedio, horarios)
}

private val HOJE = LocalDate.of(2026, 7, 26)

class ExportadorListaTest {
    @Test
    fun `reproduz o exemplo da 14 1 caractere por caractere`() {
        val losartana = remedioComHorarios(nome = "Losartana", dose = "50mg", horarios = listOf(horario("08:00")))
        val metformina = remedioComHorarios(
            nome = "Metformina", dose = "850mg", observacao = "Tomar junto com a comida", horarios = listOf(horario("08:00")),
        )
        val omeprazol = remedioComHorarios(nome = "Omeprazol", dose = "20mg", horarios = listOf(horario("14:00")))
        val sinvastatina = remedioComHorarios(
            nome = "Sinvastatina", dose = "20mg", frequencia = "dias_semana", diasSemana = "246", horarios = listOf(horario("20:00")),
        )
        val dipirona = remedioComHorarios(
            nome = "Dipirona", dose = "1 comprimido", frequencia = "quando_precisar", observacao = "Só se tiver dor", horarios = emptyList(),
        )

        val esperado = listOf(
            "💊 MEUS REMÉDIOS",
            "Atualizado em 26/07/2026",
            "🌅 MANHÃ — 08:00",
            "• Losartana — 50mg",
            "• Metformina — 850mg",
            "  ⚠️ Tomar junto com a comida",
            "🌇 TARDE — 14:00",
            "• Omeprazol — 20mg",
            "🌙 NOITE — 20:00",
            "• Sinvastatina — 20mg",
            "  📅 Seg, Qua, Sex",
            "💊 QUANDO PRECISAR",
            "• Dipirona — 1 comprimido",
            "  ⚠️ Só se tiver dor",
            "—",
            "Lista gerada pelo app Hora do Remédio",
        ).joinToString("\n\n")

        val texto = gerarTexto(listOf(losartana, metformina, omeprazol, sinvastatina, dipirona), HOJE)
        assertEquals(esperado, texto)
    }

    @Test
    fun `remedio sem dose nao escreve travessao`() {
        val texto = gerarTexto(listOf(remedioComHorarios(nome = "Vitamina D", dose = null)), HOJE)
        assertTrue(texto.contains("• Vitamina D"))
        assertFalse(texto.contains("Vitamina D —"))
    }

    @Test
    fun `remedio sem observacao nao escreve linha de aviso`() {
        val texto = gerarTexto(listOf(remedioComHorarios(nome = "Vitamina D", observacao = null)), HOJE)
        assertFalse(texto.contains("⚠️"))
    }

    @Test
    fun `so quando precisar gera cabecalho, bloco e rodape, sem grupo de horario`() {
        val r = remedioComHorarios(nome = "Dipirona", dose = "1 comprimido", frequencia = "quando_precisar", horarios = emptyList())
        val esperado = listOf(
            "💊 MEUS REMÉDIOS",
            "Atualizado em 26/07/2026",
            "💊 QUANDO PRECISAR",
            "• Dipirona — 1 comprimido",
            "—",
            "Lista gerada pelo app Hora do Remédio",
        ).joinToString("\n\n")

        assertEquals(esperado, gerarTexto(listOf(r), HOJE))
    }

    @Test
    fun `lista vazia gera so cabecalho e rodape`() {
        val esperado = listOf(
            "💊 MEUS REMÉDIOS",
            "Atualizado em 26/07/2026",
            "—",
            "Lista gerada pelo app Hora do Remédio",
        ).joinToString("\n\n")

        assertEquals(esperado, gerarTexto(emptyList(), HOJE))
    }
}
