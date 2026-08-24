// CadastroViewModel.kt — estado e validação do formulário (spec-ios.md 8.2). Validação só ao
// tocar Salvar (nunca durante a digitação — 8.2). intervalo_dias fica fora da UI (spec.md §7).

package com.dablab.horadoremedio.telas.cadastro

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dablab.horadoremedio.dados.Forma
import com.dablab.horadoremedio.dados.RemedioEntity
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.designsystem.CorRemedio
import com.dablab.horadoremedio.lembretes.AgendadorLembretes
import kotlinx.coroutines.launch
import java.time.Instant

class CadastroViewModel(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
) : ViewModel() {
    var nome by mutableStateOf("")
    var dose by mutableStateOf("")
    var forma by mutableStateOf(Forma.COMPRIMIDO.valor)
    var frequencia by mutableStateOf("diario")
    var diasSemana by mutableStateOf(setOf<Char>())
    val horarios = mutableStateListOf("08:00")
    var lembreteAtivo by mutableStateOf(true)
    var observacao by mutableStateOf("")
    var cor by mutableStateOf(CorRemedio.CORAL.name.lowercase())

    var erroNome by mutableStateOf<String?>(null)
        private set
    var erroHorario by mutableStateOf<String?>(null)
        private set
    var erroDiaSemana by mutableStateOf<String?>(null)
        private set
    var salvando by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            cor = CorRemedio.proxima(repositorio.remedios.buscarUltimaCorCriada()).name.lowercase()
        }
    }

    fun selecionarFrequencia(novaFrequencia: String) {
        frequencia = novaFrequencia
        if (novaFrequencia == "diario" && horarios.isEmpty()) horarios.add("08:00")
    }

    fun alternarDiaSemana(digito: Char) {
        diasSemana = if (digito in diasSemana) diasSemana - digito else diasSemana + digito
    }

    /** Devolve null se adicionou; devolve a chave de erro (não traduzida) se bloqueou. */
    fun adicionarHorario(hhmm: String): String? = when {
        horarios.contains(hhmm) -> "repetido"
        horarios.size >= 6 -> "maximo"
        else -> { horarios.add(hhmm); null }
    }

    fun removerHorario(hhmm: String) {
        horarios.remove(hhmm)
    }

    /** Valida e salva. Devolve true se salvou. */
    fun salvar(aoConcluir: (Long) -> Unit) {
        erroNome = if (nome.isBlank()) "vazio" else null
        erroDiaSemana = if (frequencia == "dias_semana" && diasSemana.isEmpty()) "vazio" else null
        erroHorario = if (frequencia != "quando_precisar" && horarios.isEmpty()) "vazio" else null

        if (erroNome != null || erroDiaSemana != null || erroHorario != null) return

        salvando = true
        viewModelScope.launch {
            val agora = Instant.now().toString()
            val ehQuandoPrecisar = frequencia == "quando_precisar"
            val remedio = RemedioEntity(
                nome = nome.trim(),
                dose = dose.trim().ifBlank { null },
                forma = forma,
                frequencia = frequencia,
                diasSemana = if (frequencia == "dias_semana") diasSemana.sorted().joinToString("") else null,
                observacao = observacao.trim().ifBlank { null },
                lembreteAtivo = if (ehQuandoPrecisar) false else lembreteAtivo,
                cor = cor,
                criadoEm = agora,
                atualizadoEm = agora,
            )
            val horariosParaSalvar = if (ehQuandoPrecisar) emptyList() else horarios.sorted()
            val id = repositorio.criarRemedio(remedio, horariosParaSalvar)
            if (remedio.lembreteAtivo) {
                agendador.agendarDoRemedio(remedio.copy(id = id))
            }
            salvando = false
            aoConcluir(id)
        }
    }

    fun formularioPreenchido(): Boolean =
        nome.isNotBlank() || dose.isNotBlank() || observacao.isNotBlank() || frequencia != "diario"
}
