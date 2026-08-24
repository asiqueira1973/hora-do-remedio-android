// AgrupamentoHomeTest.kt — cobre 11.7 (spec-ios.md): agrupamento por horário, remédio repetido
// em cada horário, ordenação alfabética sem diferenciar maiúscula/acento, bloco "quando_precisar"
// separado no fim.

package com.dablab.horadoremedio.telas.home

import com.dablab.horadoremedio.dados.HorarioEntity
import com.dablab.horadoremedio.dados.RemedioComHorarios
import com.dablab.horadoremedio.dados.RemedioEntity
import org.junit.Assert.assertEquals
import org.junit.Test

private var proximoId = 1L

private fun remedio(nome: String, frequencia: String = "diario", vararg horarios: String): RemedioComHorarios {
    val remedioId = proximoId++
    val entity = RemedioEntity(
        id = remedioId,
        nome = nome,
        forma = "comprimido",
        frequencia = frequencia,
        cor = "coral",
        criadoEm = "2026-07-01T00:00:00Z",
        atualizadoEm = "2026-07-01T00:00:00Z",
    )
    val horarioEntities = horarios.map {
        HorarioEntity(id = proximoId++, remedioId = remedioId, horaMinuto = it, ordem = 0, criadoEm = "2026-07-01T00:00:00Z")
    }
    return RemedioComHorarios(entity, horarioEntities)
}

class AgrupamentoHomeTest {
    @Test
    fun `agrupa por horario com faixa certa e ordem crescente`() {
        val grupos = agruparParaHome(listOf(remedio("Losartana", horarios = arrayOf("08:00")), remedio("Sinvastatina", horarios = arrayOf("20:00"))))
        assertEquals(listOf("Manhã — 08:00", "Noite — 20:00"), grupos.map { it.rotulo })
    }

    @Test
    fun `remedio com N horarios aparece N vezes`() {
        val grupos = agruparParaHome(listOf(remedio("Losartana", horarios = arrayOf("08:00", "20:00"))))
        assertEquals(2, grupos.size)
        assertEquals("Losartana", grupos[0].cards.single().remedio.nome)
        assertEquals("Losartana", grupos[1].cards.single().remedio.nome)
    }

    @Test
    fun `ordena por nome sem diferenciar maiuscula ou acento`() {
        val grupos = agruparParaHome(
            listOf(
                remedio("écora", horarios = arrayOf("08:00")),
                remedio("Ácido fólico", horarios = arrayOf("08:00")),
                remedio("Bicarbonato", horarios = arrayOf("08:00")),
            )
        )
        assertEquals(listOf("Ácido fólico", "Bicarbonato", "écora"), grupos.single().cards.map { it.remedio.nome })
    }

    @Test
    fun `quando_precisar fica em bloco separado no fim`() {
        val grupos = agruparParaHome(
            listOf(
                remedio("Losartana", horarios = arrayOf("08:00")),
                remedio("Dipirona", frequencia = "quando_precisar"),
            )
        )
        assertEquals(listOf("Manhã — 08:00", "Quando precisar"), grupos.map { it.rotulo })
        assertEquals("Dipirona", grupos.last().cards.single().remedio.nome)
    }
}
