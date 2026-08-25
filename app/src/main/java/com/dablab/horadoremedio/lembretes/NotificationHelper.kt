// NotificationHelper.kt — canal e exibição da notificação (spec.md §5, tradução de
// expo-notifications). Título/corpo exatos da tabela 15.3 do spec-ios.md: uma notificação por
// remédio, nunca agrupada (2.4 — a pessoa precisa saber O QUÊ tomar sem abrir o app).
//
// Dois canais (com som / silencioso) em vez de um: o Android não deixa mudar o som de um
// NotificationChannel depois de criado (diferente do iOS, onde o som é por notificação — 8.6).
// `mostrar` escolhe o canal pelo valor ATUAL de preferencias.som_lembrete a cada disparo.

package com.dablab.horadoremedio.lembretes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.dablab.horadoremedio.MainActivity
import com.dablab.horadoremedio.R

private const val CANAL_COM_SOM = "lembretes_remedio_com_som"
private const val CANAL_SILENCIOSO = "lembretes_remedio_silencioso"

object NotificationHelper {
    fun criarCanal(context: Context) {
        val gerenciador = context.getSystemService(NotificationManager::class.java)
        val titulo = context.getString(R.string.notificacao_titulo)

        gerenciador.createNotificationChannel(
            NotificationChannel(CANAL_COM_SOM, titulo, NotificationManager.IMPORTANCE_HIGH)
        )
        gerenciador.createNotificationChannel(
            NotificationChannel(CANAL_SILENCIOSO, titulo, NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
            }
        )
    }

    fun mostrar(context: Context, remedioId: Long, nome: String, dose: String?, comSom: Boolean) {
        val corpo = if (dose.isNullOrBlank()) nome else "$nome — $dose"
        val canalId = if (comSom) CANAL_COM_SOM else CANAL_SILENCIOSO

        val abrirApp = PendingIntent.getActivity(
            context,
            remedioId.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacao = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(R.drawable.ic_notificacao)
            .setContentTitle(context.getString(R.string.notificacao_titulo))
            .setContentText(corpo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(abrirApp)
            .build()

        val gerenciador = context.getSystemService(NotificationManager::class.java)
        // Um id por remédio: uma segunda notificação do MESMO remédio substitui a anterior em vez
        // de empilhar; remédios diferentes usam ids diferentes (nunca agrupa, 15.3).
        gerenciador.notify(remedioId.toInt(), notificacao)
    }
}
