// HorarioEntity.kt — tabela `horarios` (spec-ios.md 7.3). FK ON DELETE CASCADE recriada em
// AppDatabase via SQL bruto (Room's @ForeignKey não gera o CASCADE com o nome de constraint certo).

package com.dablab.horadoremedio.dados

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "horarios")
data class HorarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "remedio_id") val remedioId: Long,
    // "HH:MM", 24h, zero à esquerda — comparável/ordenável como texto (spec-ios.md 7.3).
    @ColumnInfo(name = "hora_minuto") val horaMinuto: String,
    val ordem: Int = 0,
    @ColumnInfo(name = "criado_em") val criadoEm: String,
)
