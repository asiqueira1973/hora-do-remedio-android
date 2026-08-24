// ArquivadosViewModel.kt — "Remédios que parei" (spec-ios.md 8.3/8.6). "Voltar a tomar" limpa
// arquivado_em e reagenda os lembretes com os horários originais intactos (eles nunca foram
// apagados — arquivar é soft delete, 7.8).

package com.dablab.horadoremedio.telas.arquivados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dablab.horadoremedio.dados.RemedioEntity
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.lembretes.AgendadorLembretes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArquivadosViewModel(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
) : ViewModel() {
    val arquivados: StateFlow<List<RemedioEntity>> =
        repositorio.remedios.listarArquivados().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun voltarATomar(remedio: RemedioEntity) {
        viewModelScope.launch {
            repositorio.remedios.reativar(remedio.id)
            agendador.agendarDoRemedio(remedio.copy(arquivadoEm = null))
        }
    }
}
