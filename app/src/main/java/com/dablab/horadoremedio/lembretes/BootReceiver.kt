// BootReceiver.kt — mecanismo que NÃO PODE QUEBRAR (spec.md §6): alarmes exatos do AlarmManager
// não sobrevivem a reboot, diferente das notificações locais do iOS. Sem isto, os avisos somem
// silenciosamente depois que o idoso reinicia o aparelho.

package com.dablab.horadoremedio.lembretes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dablab.horadoremedio.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as App
                app.repositorio.prepararNaAbertura()
                AgendadorLembretes(context, app.repositorio).reconciliarTudo()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
