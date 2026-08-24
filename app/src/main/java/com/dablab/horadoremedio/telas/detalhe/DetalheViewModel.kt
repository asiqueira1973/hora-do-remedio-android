// DetalheViewModel.kt — tela de detalhe/arquivamento (spec-ios.md 8.3). "Parar de tomar" grava
// arquivado_em e cancela as notificações; nunca apaga a linha (soft delete, 7.8).

package com.dablab.horadoremedio.telas.detalhe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dablab.horadoremedio.dados.HorarioEntity
import com.dablab.horadoremedio.dados.RemedioEntity
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.lembretes.AgendadorLembretes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

data class DetalheUiState(val remedio: RemedioEntity?, val horarios: List<HorarioEntity>)

class DetalheViewModel(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
    private val remedioId: Long,
) : ViewModel() {
    val estado: StateFlow<DetalheUiState> =
        combine(repositorio.remedios.observar(remedioId), repositorio.horarios.listarDoRemedio(remedioId)) { remedio, horarios ->
            DetalheUiState(remedio, horarios.sortedBy { it.horaMinuto })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DetalheUiState(null, emptyList()))

    /** Arquiva e cancela os lembretes. Devolve o nome do remédio para a faixa "X foi arquivado". */
    fun pararDeTomar(aoConcluir: (String) -> Unit) {
        viewModelScope.launch {
            val remedio = estado.value.remedio ?: return@launch
            repositorio.remedios.arquivar(remedioId, Instant.now().toString())
            agendador.cancelarDoRemedio(remedioId)
            aoConcluir(remedio.nome)
        }
    }
}
