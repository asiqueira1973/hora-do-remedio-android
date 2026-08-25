// PermissoesLembrete.kt — tradução do "Fluxo de permissão" (spec-ios.md 8.4) pro Android: duas
// permissões, não uma. `POST_NOTIFICATIONS` (Android 13+) é um diálogo do sistema, igual ao do
// iOS; `SCHEDULE_EXACT_ALARM` (Android 12+) NÃO tem diálogo — só a tela de Ajustes do sistema
// (spec.md, "Pegadinhas" #3). A explicação prévia (permissao-aviso) cobre as duas de uma vez.

package com.dablab.horadoremedio.lembretes

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object PermissoesLembrete {
    fun notificacoesPermitidas(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun alarmeExatoPermitido(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        return alarmManager.canScheduleExactAlarms()
    }

    /** Se alguma das duas falta, mostra a tela explicativa antes de pedir (spec-ios.md 8.4, passo 2). */
    fun precisaExplicarPermissao(context: Context): Boolean =
        !notificacoesPermitidas(context) || !alarmeExatoPermitido(context)
}
