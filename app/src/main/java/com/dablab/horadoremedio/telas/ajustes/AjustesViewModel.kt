// AjustesViewModel.kt — épico 8.6. Tamanho de letra e som gravam em preferencias (Flow reativo,
// aplicado na hora — RootView já observa preferencias.escala_fonte). Som também REAGENDA todas as
// notificações (o som faz parte do agendamento no Android, por causa do canal — ver
// NotificationHelper). "Apagar tudo" apaga o banco inteiro e cancela todos os alarmes.

package com.dablab.horadoremedio.telas.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dablab.horadoremedio.dados.EscalaFonte
import com.dablab.horadoremedio.dados.PreferenciasEntity
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.exportacao.gerarTexto
import com.dablab.horadoremedio.lembretes.AgendadorLembretes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AjustesViewModel(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
) : ViewModel() {
    val preferencias: StateFlow<PreferenciasEntity?> =
        repositorio.preferencias.observar().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun mudarEscala(escala: EscalaFonte) {
        viewModelScope.launch { repositorio.preferencias.atualizarEscalaFonte(escala.valor) }
    }

    fun mudarSom(ligado: Boolean) {
        viewModelScope.launch {
            repositorio.preferencias.atualizarSomLembrete(ligado)
            agendador.reconciliarTudo() // reagenda tudo — novos identificadores em notificacoes_agendadas
        }
    }

    fun textoParaCompartilhar(aoGerar: (String) -> Unit) {
        viewModelScope.launch {
            val ativos = repositorio.remedios.observarAtivosComHorarios().first()
            aoGerar(gerarTexto(ativos))
        }
    }

    fun apagarTudo(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            agendador.cancelarTudo()
            repositorio.apagarTudo()
            aoConcluir()
        }
    }
}
