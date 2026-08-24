// NotificacaoAgendadaDao.kt — acesso a `notificacoes_agendadas` (tabela derivada, spec-ios.md 7.4:
// reconstruível a qualquer momento a partir de remedios+horarios).

package com.dablab.horadoremedio.dados

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificacaoAgendadaDao {
    @Query(
        """
        SELECT n.* FROM notificacoes_agendadas n
        INNER JOIN horarios h ON h.id = n.horario_id
        WHERE h.remedio_id = :remedioId
        """
    )
    suspend fun listarDoRemedio(remedioId: Long): List<NotificacaoAgendadaEntity>

    @Insert
    suspend fun inserir(notificacao: NotificacaoAgendadaEntity): Long

    @Query(
        """
        DELETE FROM notificacoes_agendadas
        WHERE horario_id IN (SELECT id FROM horarios WHERE remedio_id = :remedioId)
        """
    )
    suspend fun removerDoRemedio(remedioId: Long)

    @Query("SELECT * FROM notificacoes_agendadas")
    suspend fun listarTodas(): List<NotificacaoAgendadaEntity>

    @Query("DELETE FROM notificacoes_agendadas")
    suspend fun apagarTudo()
}
