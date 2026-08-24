// PreferenciasDao.kt — acesso à linha única de `preferencias` (id sempre 1, spec-ios.md 7.5).

package com.dablab.horadoremedio.dados

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenciasDao {
    @Query("SELECT * FROM preferencias WHERE id = 1")
    fun observar(): Flow<PreferenciasEntity?>

    @Query("SELECT * FROM preferencias WHERE id = 1")
    suspend fun buscar(): PreferenciasEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserirSeAusente(preferencias: PreferenciasEntity)

    @Query("UPDATE preferencias SET escala_fonte = :escalaFonte WHERE id = 1")
    suspend fun atualizarEscalaFonte(escalaFonte: String)

    @Query("UPDATE preferencias SET som_lembrete = :somLembrete WHERE id = 1")
    suspend fun atualizarSomLembrete(somLembrete: Boolean)

    @Query("UPDATE preferencias SET onboarding_concluido_em = :dataHora WHERE id = 1")
    suspend fun concluirOnboarding(dataHora: String)
}
