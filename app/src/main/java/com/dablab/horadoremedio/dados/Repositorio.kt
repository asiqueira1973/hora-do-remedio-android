// Repositorio.kt — bootstrap na abertura (equivalente ao seed.ts do iOS, spec-ios.md 7.9): garante
// a linha única de `preferencias` se ainda não existir. Nenhum remédio de exemplo é criado.

package com.dablab.horadoremedio.dados

import androidx.room.withTransaction
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

    /** Cria remédio + horários numa transação só (spec-ios.md 13-A.1). Devolve o id do remédio. */
    suspend fun criarRemedio(remedio: RemedioEntity, horariosHhMm: List<String>): Long =
        database.withTransaction {
            val id = remedios.inserir(remedio)
            val agora = Instant.now().toString()
            if (horariosHhMm.isNotEmpty()) {
                horarios.inserirTodos(
                    horariosHhMm.mapIndexed { indice, hhmm ->
                        HorarioEntity(remedioId = id, horaMinuto = hhmm, ordem = indice, criadoEm = agora)
                    }
                )
            }
            id
        }

    /** Atualiza um remédio e REESCREVE seus horários (spec-ios.md 8.3: nunca edita horário
     *  existente — apaga e recria, para o reagendamento de notificações ser cancelar+recriar). */
    suspend fun atualizarRemedioComHorarios(remedio: RemedioEntity, horariosHhMm: List<String>) =
        database.withTransaction {
            remedios.atualizar(remedio)
            horarios.removerDoRemedio(remedio.id)
            val agora = Instant.now().toString()
            if (horariosHhMm.isNotEmpty()) {
                horarios.inserirTodos(
                    horariosHhMm.mapIndexed { indice, hhmm ->
                        HorarioEntity(remedioId = remedio.id, horaMinuto = hhmm, ordem = indice, criadoEm = agora)
                    }
                )
            }
        }
}
