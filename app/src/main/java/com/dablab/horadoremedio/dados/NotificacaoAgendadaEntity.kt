// NotificacaoAgendadaEntity.kt — tabela `notificacoes_agendadas` (spec-ios.md 7.4). Nome da coluna
// `identificador_ios` mantido de propósito (spec.md §4): no Android guarda o requestCode do
// PendingIntent do AlarmManager — é só um identificador opaco de agendamento, renomear quebraria a
// paridade de schema entre plataformas sem ganho nenhum.

package com.dablab.horadoremedio.dados

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notificacoes_agendadas")
data class NotificacaoAgendadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "horario_id") val horarioId: Long,
    @ColumnInfo(name = "identificador_ios") val identificadorIos: String,
    // Nulo quando o agendamento é repetitivo (sem uma data única de disparo) — spec-ios.md 7.4.
    @ColumnInfo(name = "disparo_em") val disparoEm: String? = null,
    @ColumnInfo(name = "criado_em") val criadoEm: String,
)
