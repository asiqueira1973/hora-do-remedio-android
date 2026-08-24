// AgendadorLembretes.kt — equivalente a servicoLembretes (13-A.2 do iOS): agenda/cancela alarmes
// exatos por horário via AlarmManager e mantém `notificacoes_agendadas` como espelho local
// (spec-ios.md 7.4). Cada alarme carrega os dados do próprio disparo no PendingIntent (extras),
// para o LembreteReceiver não depender do banco estar acessível no instante do disparo.

package com.dablab.horadoremedio.lembretes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dablab.horadoremedio.dados.HorarioEntity
import com.dablab.horadoremedio.dados.RemedioEntity
import com.dablab.horadoremedio.dados.Repositorio
import kotlinx.coroutines.flow.first
import java.time.Instant

const val EXTRA_HORARIO_ID = "horario_id"
const val EXTRA_REMEDIO_ID = "remedio_id"
const val EXTRA_NOME = "nome"
const val EXTRA_DOSE = "dose"
const val EXTRA_HORA_MINUTO = "hora_minuto"
const val EXTRA_FREQUENCIA = "frequencia"
const val EXTRA_DIAS_SEMANA = "dias_semana"
const val EXTRA_INTERVALO_DIAS = "intervalo_dias"

class AgendadorLembretes(
    private val context: Context,
    private val repositorio: Repositorio,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** Agenda todos os horários de um remédio ativo com lembrete_ativo=1 (spec.md §6: sem cota). */
    suspend fun agendarDoRemedio(remedio: RemedioEntity) {
        if (!remedio.lembreteAtivo || remedio.arquivadoEm != null) return
        if (remedio.frequencia == "quando_precisar") return

        cancelarDoRemedio(remedio.id)
        val horarios = repositorio.horarios.listarDoRemedioUmaVez(remedio.id)
        for (horario in horarios) {
            agendarHorario(remedio, horario)
        }
    }

    private suspend fun agendarHorario(remedio: RemedioEntity, horario: HorarioEntity) {
        val disparoEm = proximoDisparoMillis(
            horaMinuto = horario.horaMinuto,
            frequencia = remedio.frequencia,
            diasSemana = remedio.diasSemana,
            intervaloDias = remedio.intervaloDias,
        )
        val pendingIntent = pendingIntentDoHorario(remedio, horario)
        agendarExatoOuAproximado(disparoEm, pendingIntent)

        repositorio.notificacoes.inserir(
            com.dablab.horadoremedio.dados.NotificacaoAgendadaEntity(
                horarioId = horario.id,
                identificadorIos = horario.id.toString(),
                disparoEm = Instant.ofEpochMilli(disparoEm).toString(),
                criadoEm = Instant.now().toString(),
            )
        )
    }

    /** Cancela todos os alarmes de um remédio (edição/arquivamento — spec-ios.md 8.3). */
    suspend fun cancelarDoRemedio(remedioId: Long) {
        val horarios = repositorio.horarios.listarDoRemedioUmaVez(remedioId)
        for (horario in horarios) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                horario.id.toInt(),
                Intent(context, LembreteReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE,
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
        repositorio.notificacoes.removerDoRemedio(remedioId)
    }

    /** Reconciliação (equivalente a 13-A.3): reagenda tudo a partir de remedios+horarios — chamada
     *  na abertura do app e depois de BOOT_COMPLETED (spec.md §6, mecanismo que não pode quebrar). */
    suspend fun reconciliarTudo() {
        // listarAtivos() é Flow; a reconciliação lê o valor atual uma vez.
        val remedios = repositorio.remedios.listarAtivos().first()
        for (remedio in remedios) {
            agendarDoRemedio(remedio)
        }
    }

    /**
     * `SCHEDULE_EXACT_ALARM` (Android 12+) é uma permissão "especial": tê-la no manifesto não
     * basta, o usuário precisa concedê-la em Ajustes (ou o app precisa checar antes de chamar
     * `setExactAndAllowWhileIdle`, senão o sistema derruba o app com `SecurityException` — já
     * visto no emulador). Sem a permissão, cai para `set()` (aproximado) em vez de crashar; a
     * tela própria de pedir a permissão (spec-ios.md 8.4) é um próximo épico.
     */
    fun podeAgendarExato(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun agendarExatoOuAproximado(disparoEm: Long, pendingIntent: PendingIntent) {
        if (podeAgendarExato()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, disparoEm, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, disparoEm, pendingIntent)
        }
    }

    private fun pendingIntentDoHorario(remedio: RemedioEntity, horario: HorarioEntity): PendingIntent {
        val intent = Intent(context, LembreteReceiver::class.java).apply {
            putExtra(EXTRA_HORARIO_ID, horario.id)
            putExtra(EXTRA_REMEDIO_ID, remedio.id)
            putExtra(EXTRA_NOME, remedio.nome)
            putExtra(EXTRA_DOSE, remedio.dose)
            putExtra(EXTRA_HORA_MINUTO, horario.horaMinuto)
            putExtra(EXTRA_FREQUENCIA, remedio.frequencia)
            putExtra(EXTRA_DIAS_SEMANA, remedio.diasSemana)
            remedio.intervaloDias?.let { putExtra(EXTRA_INTERVALO_DIAS, it) }
        }
        return PendingIntent.getBroadcast(
            context,
            horario.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
