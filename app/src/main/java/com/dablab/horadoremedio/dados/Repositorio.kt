// Repositorio.kt — bootstrap na abertura (equivalente ao seed.ts do iOS, spec-ios.md 7.9): garante
// a linha única de `preferencias` se ainda não existir. Nenhum remédio de exemplo é criado.

package com.dablab.horadoremedio.dados

import java.time.Instant

class Repositorio(private val database: AppDatabase) {
    val remedios get() = database.remedioDao()
    val horarios get() = database.horarioDao()
    val notificacoes get() = database.notificacaoAgendadaDao()
    val preferencias get() = database.preferenciasDao()

    suspend fun prepararNaAbertura() {
        preferencias.inserirSeAusente(
            PreferenciasEntity(atualizadoEm = Instant.now().toString())
        )
    }
}
