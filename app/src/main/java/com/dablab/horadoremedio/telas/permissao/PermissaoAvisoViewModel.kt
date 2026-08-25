// PermissaoAvisoViewModel.kt — finaliza o agendamento que o CadastroViewModel pulou (épico 8.4,
// "Fluxo de permissão"): busca o remédio já salvo e agenda de verdade, depois de a pessoa passar
// pela explicação (e, se quiser, conceder as permissões do sistema).

package com.dablab.horadoremedio.telas.permissao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.lembretes.AgendadorLembretes
import kotlinx.coroutines.launch

class PermissaoAvisoViewModel(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
    private val remedioId: Long,
) : ViewModel() {
    fun finalizarAgendamento(aoConcluir: () -> Unit) {
        viewModelScope.launch {
            repositorio.remedios.buscar(remedioId)?.let { agendador.agendarDoRemedio(it) }
            aoConcluir()
        }
    }
}
