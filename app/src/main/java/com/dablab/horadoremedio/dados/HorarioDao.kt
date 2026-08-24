// HorarioDao.kt — acesso a `horarios`.

package com.dablab.horadoremedio.dados

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HorarioDao {
    @Query("SELECT * FROM horarios WHERE remedio_id = :remedioId ORDER BY hora_minuto")
    fun listarDoRemedio(remedioId: Long): Flow<List<HorarioEntity>>

    @Query("SELECT * FROM horarios WHERE remedio_id = :remedioId ORDER BY hora_minuto")
    suspend fun listarDoRemedioUmaVez(remedioId: Long): List<HorarioEntity>

    // Home agrupa por horário entre remédios diferentes (spec-ios.md 11.7) — join simples.
    @Query(
        """
        SELECT h.* FROM horarios h
        INNER JOIN remedios r ON r.id = h.remedio_id
        WHERE r.arquivado_em IS NULL
        ORDER BY h.hora_minuto
        """
    )
    fun listarDeAtivos(): Flow<List<HorarioEntity>>

    @Insert
    suspend fun inserir(horario: HorarioEntity): Long

    @Insert
    suspend fun inserirTodos(horarios: List<HorarioEntity>)

    @Query("DELETE FROM horarios WHERE remedio_id = :remedioId")
    suspend fun removerDoRemedio(remedioId: Long)
}
