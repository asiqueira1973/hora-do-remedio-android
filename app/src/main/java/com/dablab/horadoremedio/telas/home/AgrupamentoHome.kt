// AgrupamentoHome.kt — função pura de agrupamento/ordenação da Home (spec-ios.md 11.7): agrupa por
// hora_minuto crescente, um card por (remédio, horário), ordem alfabética dentro do grupo
// (sem diferenciar maiúscula/acento), "quando_precisar" num bloco à parte no fim. Sem I/O — testável
// em JVM puro (equivalente ao teste obrigatório 22.1 do iOS para esta regra).

package com.dablab.horadoremedio.telas.home

import com.dablab.horadoremedio.dados.RemedioComHorarios
import com.dablab.horadoremedio.dados.RemedioEntity
import java.text.Normalizer

data class CardHome(val remedio: RemedioEntity, val horaMinuto: String?)
data class GrupoHome(val rotulo: String, val cards: List<CardHome>)

private fun chaveAlfabetica(nome: String): String =
    Normalizer.normalize(nome, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase()

private fun faixaDoHorario(horaMinuto: String): String {
    val hora = horaMinuto.substringBefore(":").toInt()
    return when (hora) {
        in 0..4 -> "Madrugada"
        in 5..11 -> "Manhã"
        in 12..17 -> "Tarde"
        else -> "Noite"
    }
}

/** Grupos de horário (11.7.1-11.7.3/11.7.5) + bloco "Quando precisar" (11.7.4) por último, se houver. */
fun agruparParaHome(remediosAtivos: List<RemedioComHorarios>): List<GrupoHome> {
    val porHorario = linkedMapOf<String, MutableList<CardHome>>()
    val quandoPrecisar = mutableListOf<CardHome>()

    for (item in remediosAtivos) {
        if (item.remedio.frequencia == "quando_precisar") {
            quandoPrecisar += CardHome(item.remedio, horaMinuto = null)
        } else {
            for (horario in item.horarios) {
                porHorario.getOrPut(horario.horaMinuto) { mutableListOf() } += CardHome(item.remedio, horario.horaMinuto)
            }
        }
    }

    val gruposDeHorario = porHorario.keys.sorted().map { horaMinuto ->
        val cards = porHorario.getValue(horaMinuto).sortedBy { chaveAlfabetica(it.remedio.nome) }
        GrupoHome("${faixaDoHorario(horaMinuto)} — $horaMinuto", cards)
    }

    val grupos = gruposDeHorario.toMutableList()
    if (quandoPrecisar.isNotEmpty()) {
        grupos += GrupoHome("Quando precisar", quandoPrecisar.sortedBy { chaveAlfabetica(it.remedio.nome) })
    }
    return grupos
}
