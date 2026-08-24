// RemedioEntity.kt — tabela `remedios` (spec-ios.md 7.2, traduzida em spec.md §4). Os enums
// (forma/frequencia/cor) ficam como String pura na entidade — a validação de valores permitidos
// é a CHECK constraint recriada em AppDatabase (SQL bruto), não o tipo Kotlin.

package com.dablab.horadoremedio.dados

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remedios")
data class RemedioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val dose: String? = null,
    val forma: String = Forma.COMPRIMIDO.valor,
    val frequencia: String = Frequencia.DIARIO.valor,
    // Só preenchido quando frequencia = "intervalo_dias" — cortado da UI (spec.md §7), mantido no
    // schema por paridade com o iOS.
    @ColumnInfo(name = "intervalo_dias") val intervaloDias: Int? = null,
    // Dígitos 1-7 ordenados sem separador (1=domingo..7=sábado), só quando frequencia = "dias_semana".
    @ColumnInfo(name = "dias_semana") val diasSemana: String? = null,
    val observacao: String? = null,
    @ColumnInfo(name = "lembrete_ativo") val lembreteAtivo: Boolean = true,
    val cor: String,
    // Nulo = ativo; preenchido (ISO 8601) = arquivado (soft delete, spec-ios.md 7.8).
    @ColumnInfo(name = "arquivado_em") val arquivadoEm: String? = null,
    @ColumnInfo(name = "criado_em") val criadoEm: String,
    // Mantida pela trigger trg_remedios_atualizado_em — nunca escrita pela aplicação.
    @ColumnInfo(name = "atualizado_em") val atualizadoEm: String,
)
