// PreferenciasEntity.kt — tabela `preferencias` (spec-ios.md 7.5). Linha única, id sempre 1
// (ck_preferencias_linha_unica recriada em AppDatabase).

package com.dablab.horadoremedio.dados

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferencias")
data class PreferenciasEntity(
    @PrimaryKey val id: Long = 1,
    @ColumnInfo(name = "escala_fonte") val escalaFonte: String = EscalaFonte.GRANDE.valor,
    @ColumnInfo(name = "som_lembrete") val somLembrete: Boolean = true,
    @ColumnInfo(name = "onboarding_concluido_em") val onboardingConcluidoEm: String? = null,
    @ColumnInfo(name = "atualizado_em") val atualizadoEm: String,
)
