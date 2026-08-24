// RemedioDao.kt — acesso a `remedios`. listarAtivos()/listarArquivados() em Flow: equivalente ao
// useLiveQuery do Drizzle (iOS) — a Home recompõe sozinha quando o banco muda (spec.md §5).

package com.dablab.horadoremedio.dados

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RemedioDao {
    @Query("SELECT * FROM remedios WHERE arquivado_em IS NULL ORDER BY nome COLLATE NOCASE")
    fun listarAtivos(): Flow<List<RemedioEntity>>

    @Transaction
    @Query("SELECT * FROM remedios WHERE arquivado_em IS NULL")
    fun observarAtivosComHorarios(): Flow<List<RemedioComHorarios>>

    @Query("SELECT * FROM remedios WHERE arquivado_em IS NOT NULL ORDER BY nome COLLATE NOCASE")
    fun listarArquivados(): Flow<List<RemedioEntity>>

    @Query("SELECT * FROM remedios WHERE id = :id")
    fun observar(id: Long): Flow<RemedioEntity?>

    @Query("SELECT * FROM remedios WHERE id = :id")
    suspend fun buscar(id: Long): RemedioEntity?

    // Rodízio de cor (spec-ios.md 11.8): próxima cor após a do último remédio criado, incluindo arquivados.
    @Query("SELECT cor FROM remedios ORDER BY id DESC LIMIT 1")
    suspend fun buscarUltimaCorCriada(): String?

    @Insert
    suspend fun inserir(remedio: RemedioEntity): Long

    @Update
    suspend fun atualizar(remedio: RemedioEntity)

    @Delete
    suspend fun remover(remedio: RemedioEntity)

    @Query("UPDATE remedios SET arquivado_em = :arquivadoEm WHERE id = :id")
    suspend fun arquivar(id: Long, arquivadoEm: String)

    @Query("UPDATE remedios SET arquivado_em = NULL WHERE id = :id")
    suspend fun reativar(id: Long)

    @Query("DELETE FROM remedios")
    suspend fun apagarTudo()
}
