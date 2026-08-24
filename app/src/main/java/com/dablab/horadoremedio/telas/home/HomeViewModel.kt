// HomeViewModel.kt — expõe os remédios ativos já agrupados (spec-ios.md 8.1) como StateFlow,
// reagindo ao Room via Flow (equivalente ao useLiveQuery do Drizzle no iOS, spec.md §5). Também
// expõe a lista "crua" (com todos os horários por remédio) para a exportação (8.5).

package com.dablab.horadoremedio.telas.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dablab.horadoremedio.dados.RemedioComHorarios
import com.dablab.horadoremedio.dados.Repositorio
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(repositorio: Repositorio) : ViewModel() {
    private val ativosComHorariosFlow = repositorio.remedios.observarAtivosComHorarios()

    // `null` = ainda não chegou a 1ª emissão do Room (distinto de lista vazia — HomeScreen usa
    // isso pra não piscar o estado "vazio" antes do banco responder).
    val grupos: StateFlow<List<GrupoHome>?> =
        ativosComHorariosFlow
            .map { agruparParaHome(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Eagerly (não WhileSubscribed): esta StateFlow nunca é coletada via Compose — só lida por
    // `.value` no clique de compartilhar — então precisa se manter viva e atualizada sozinha,
    // senão o upstream nunca é coletado e `.value` fica preso no valor inicial (bug visto no
    // emulador: a share sheet abria sem nenhum remédio, mesmo com a Home cheia).
    private val ultimaListaParaExportar: StateFlow<List<RemedioComHorarios>> =
        ativosComHorariosFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun remediosParaExportar(): List<RemedioComHorarios> = ultimaListaParaExportar.value
}
