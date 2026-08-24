// HomeViewModel.kt — expõe os remédios ativos já agrupados (spec-ios.md 8.1) como StateFlow,
// reagindo ao Room via Flow (equivalente ao useLiveQuery do Drizzle no iOS, spec.md §5).

package com.dablab.horadoremedio.telas.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dablab.horadoremedio.dados.Repositorio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repositorio: Repositorio) : ViewModel() {
    val grupos: StateFlow<List<GrupoHome>?> =
        repositorio.remedios.observarAtivosComHorarios()
            .map { agruparParaHome(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
