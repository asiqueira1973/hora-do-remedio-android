// RemedioComHorarios.kt — join Room (@Relation) de um remédio com seus horários, para a Home
// não precisar de N queries (spec-ios.md 11.7: agrupamento por horário entre remédios).

package com.dablab.horadoremedio.dados

import androidx.room.Embedded
import androidx.room.Relation

data class RemedioComHorarios(
    @Embedded val remedio: RemedioEntity,
    @Relation(parentColumn = "id", entityColumn = "remedio_id")
    val horarios: List<HorarioEntity>,
)
