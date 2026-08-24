// LembreteReceiver.kt — dispara quando um alarme exato do AlarmManager chega (spec.md §5/§6):
// mostra a notificação e, como o Android não tem repetição nativa por dia-da-semana, se
// REAGENDA para a próxima ocorrência (o alarme carrega tudo que precisa nos extras — não depende
// do banco estar acessível no instante exato do disparo).

package com.dablab.horadoremedio.lembretes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LembreteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val horarioId = intent.getLongExtra(EXTRA_HORARIO_ID, -1)
        val remedioId = intent.getLongExtra(EXTRA_REMEDIO_ID, -1)
        val nome = intent.getStringExtra(EXTRA_NOME) ?: return
        val dose = intent.getStringExtra(EXTRA_DOSE)
        val horaMinuto = intent.getStringExtra(EXTRA_HORA_MINUTO) ?: return
        val frequencia = intent.getStringExtra(EXTRA_FREQUENCIA) ?: return
        val diasSemana = intent.getStringExtra(EXTRA_DIAS_SEMANA)
        val intervaloDias = if (intent.hasExtra(EXTRA_INTERVALO_DIAS)) {
            intent.getIntExtra(EXTRA_INTERVALO_DIAS, 0)
        } else null

        NotificationHelper.mostrar(context, remedioId, nome, dose)

        if (frequencia == "quando_precisar" || horarioId < 0) return

        // Reagenda a próxima ocorrência do MESMO horário — goAsync porque grava em
        // notificacoes_agendadas (Room) e o receiver não pode bloquear a thread principal.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val proximo = proximoDisparoMillis(
                    horaMinuto = horaMinuto,
                    frequencia = frequencia,
                    diasSemana = diasSemana,
                    intervaloDias = intervaloDias,
                )
                val alarmManager = context.getSystemService(AlarmManager::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    horarioId.toInt(),
                    Intent(context, LembreteReceiver::class.java).apply {
                        putExtras(intent)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                // Mesma degradação de AgendadorLembretes.podeAgendarExato: sem SCHEDULE_EXACT_ALARM
                // concedido, cai para set() em vez de crashar com SecurityException.
                val podeExato = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
                if (podeExato) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, proximo, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, proximo, pendingIntent)
                }
                // O espelho em notificacoes_agendadas fica com o disparo_em antigo até a próxima
                // reconciliação (abertura do app) — tabela derivada, reconstruível (spec-ios.md 7.4).
            } finally {
                pendingResult.finish()
            }
        }
    }
}
