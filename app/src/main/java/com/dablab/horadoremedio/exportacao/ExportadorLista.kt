// ExportadorLista.kt — gerarTexto (spec-ios.md 14.1), portado caractere por caractere de
// exportadorLista.ts (iOS) — inclusive a junção `\n\n` entre TODAS as linhas (14.1.2), confirmada
// pelo teste literal 22.1.3 do iOS (exportadorLista.test.ts). Função pura, sem I/O.

package com.dablab.horadoremedio.exportacao

import com.dablab.horadoremedio.dados.RemedioComHorarios
import com.dablab.horadoremedio.dados.RemedioEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val ROTULO_DIA_SEMANA = mapOf(
    '1' to "Dom", '2' to "Seg", '3' to "Ter", '4' to "Qua", '5' to "Qui", '6' to "Sex", '7' to "Sáb",
)

private fun faixaEEmoji(horaMinuto: String): Pair<String, String> {
    val hora = horaMinuto.substringBefore(":").toInt()
    return when (hora) {
        in 0..4 -> "MADRUGADA" to "🌃"
        in 5..11 -> "MANHÃ" to "🌅"
        in 12..17 -> "TARDE" to "🌇"
        else -> "NOITE" to "🌙"
    }
}

/** formatarFrequencia (iOS) — null para "diario"/"quando_precisar" (14.1.7). */
private fun formatarFrequencia(remedio: RemedioEntity): String? = when (remedio.frequencia) {
    "dias_semana" -> remedio.diasSemana?.map { ROTULO_DIA_SEMANA[it].orEmpty() }?.joinToString(", ")
    "intervalo_dias" -> remedio.intervaloDias?.let { "A cada $it dias" }
    else -> null
}

/** linhasDoRemedio (iOS) — nome/dose, depois ⚠️, depois 📅 (14.1.5, 14.1.6, 14.1.8). */
private fun linhasDoRemedio(remedio: RemedioEntity): List<String> {
    val linhas = mutableListOf(
        if (remedio.dose.isNullOrBlank()) "• ${remedio.nome}" else "• ${remedio.nome} — ${remedio.dose}"
    )
    remedio.observacao?.let { linhas += "  ⚠️ $it" }
    formatarFrequencia(remedio)?.let { linhas += "  📅 $it" }
    return linhas
}

/**
 * gerarTexto (13-A.4 do iOS) — texto de exportação, exatamente como a 14.1 especifica.
 * Recebe os remédios ATIVOS (quem chama já filtrou arquivado_em IS NULL, 14.1.11).
 */
fun gerarTexto(remediosAtivos: List<RemedioComHorarios>, hoje: LocalDate = LocalDate.now()): String {
    val porHorario = linkedMapOf<String, MutableList<RemedioEntity>>()
    val quandoPrecisar = mutableListOf<RemedioEntity>()

    for (item in remediosAtivos) {
        if (item.remedio.frequencia == "quando_precisar") {
            quandoPrecisar += item.remedio
        } else {
            for (horario in item.horarios) {
                porHorario.getOrPut(horario.horaMinuto) { mutableListOf() } += item.remedio
            }
        }
    }

    val linhas = mutableListOf(
        "💊 MEUS REMÉDIOS",
        "Atualizado em ${hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
    )

    for (horaMinuto in porHorario.keys.sorted()) {
        val (faixa, emoji) = faixaEEmoji(horaMinuto)
        linhas += "$emoji $faixa — $horaMinuto"
        for (remedio in porHorario.getValue(horaMinuto)) {
            linhas += linhasDoRemedio(remedio)
        }
    }

    if (quandoPrecisar.isNotEmpty()) {
        linhas += "💊 QUANDO PRECISAR"
        for (remedio in quandoPrecisar) {
            linhas += linhasDoRemedio(remedio)
        }
    }

    linhas += "—"
    linhas += "Lista gerada pelo app Hora do Remédio"

    return linhas.joinToString("\n\n")
}

/** Assunto sugerido no compartilhamento (usado por e-mail) — 14.1 final. */
fun assuntoCompartilhamento(hoje: LocalDate = LocalDate.now()): String =
    "Meus remédios — ${hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}"
