// CalculoAgendamento.kt — funções puras de agendamento (spec.md §5/§6, tradução de
// servicoLembretes/13-A.2 do iOS). O Android não tem um trigger nativo "toda semana às terças";
// em vez de UNCalendarNotificationTrigger, calculamos o PRÓXIMO disparo e o alarme se reagenda
// sozinho ao disparar (ver LembreteReceiver). Sem estado, sem I/O — 100% testável em JVM puro.

package com.dablab.horadoremedio.lembretes

import com.dablab.horadoremedio.dados.Frequencia
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

// dias_semana: dígitos 1-7, 1=domingo..7=sábado (spec-ios.md 7.2/11.2).
private val DIA_PARA_DAY_OF_WEEK = mapOf(
    '1' to DayOfWeek.SUNDAY,
    '2' to DayOfWeek.MONDAY,
    '3' to DayOfWeek.TUESDAY,
    '4' to DayOfWeek.WEDNESDAY,
    '5' to DayOfWeek.THURSDAY,
    '6' to DayOfWeek.FRIDAY,
    '7' to DayOfWeek.SATURDAY,
)

/**
 * Próximo instante (epoch millis) em que um horário deve disparar, a partir de `agora`.
 * `frequencia` = "diario" | "dias_semana" | "intervalo_dias" (mantido por completude — ver
 * spec.md §7, não é alimentado pela UI) | "quando_precisar" (nunca chamado — sem lembrete).
 */
fun proximoDisparoMillis(
    horaMinuto: String,
    frequencia: String,
    diasSemana: String?,
    ultimoDisparo: ZonedDateTime? = null,
    intervaloDias: Int? = null,
    agora: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
): Long {
    val (hora, minuto) = horaMinuto.split(":").map { it.toInt() }
    val horaAlvo = LocalTime.of(hora, minuto)

    return when (Frequencia.deValor(frequencia)) {
        Frequencia.DIARIO -> {
            var candidato = agora.toLocalDate().atTime(horaAlvo).atZone(agora.zone)
            if (!candidato.isAfter(agora)) candidato = candidato.plusDays(1)
            candidato.toInstant().toEpochMilli()
        }

        Frequencia.DIAS_SEMANA -> {
            val diasAlvo = diasSemana.orEmpty().mapNotNull { DIA_PARA_DAY_OF_WEEK[it] }.toSet()
            require(diasAlvo.isNotEmpty()) { "dias_semana vazio para frequencia dias_semana" }
            var candidato = agora.toLocalDate().atTime(horaAlvo).atZone(agora.zone)
            while (candidato.dayOfWeek !in diasAlvo || !candidato.isAfter(agora)) {
                candidato = candidato.plusDays(1)
            }
            candidato.toInstant().toEpochMilli()
        }

        Frequencia.INTERVALO_DIAS -> {
            val intervalo = requireNotNull(intervaloDias) { "intervalo_dias nulo para frequencia intervalo_dias" }
            val base = ultimoDisparo ?: agora
            var candidato = base.toLocalDate().atTime(horaAlvo).atZone(base.zone)
            while (!candidato.isAfter(agora)) candidato = candidato.plusDays(intervalo.toLong())
            candidato.toInstant().toEpochMilli()
        }

        Frequencia.QUANDO_PRECISAR ->
            error("quando_precisar não agenda lembrete (spec-ios.md 11.1)")
    }
}

/**
 * Custo de agendamento por remédio — equivalente a calcularCustoDeAgendamento do iOS (11.4).
 * Mantido como sinal útil mesmo sem o corte rígido de 64 no Android (spec.md §6).
 */
fun calcularCustoDeAgendamento(
    frequencia: String,
    numeroHorarios: Int,
    diasSemana: String? = null,
    intervaloDias: Int? = null,
): Int = when (Frequencia.deValor(frequencia)) {
    Frequencia.DIARIO -> numeroHorarios
    Frequencia.DIAS_SEMANA -> diasSemana.orEmpty().length * numeroHorarios
    Frequencia.INTERVALO_DIAS -> {
        val intervalo = intervaloDias ?: 1
        (30 + intervalo - 1) / intervalo * numeroHorarios // ⌈30/N⌉ × horários
    }
    Frequencia.QUANDO_PRECISAR -> 0
}
