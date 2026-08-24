// CadastroScreen.kt — formulário de cadastro (spec-ios.md 8.2), tela única com rolagem, blocos
// 1→8 na ordem do spec (bloco 4b "A cada X dias" cortado da UI — spec.md §7). Validação só ao
// tocar "Salvar remédio" (nunca durante a digitação).

package com.dablab.horadoremedio.telas.cadastro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.dablab.horadoremedio.R
import com.dablab.horadoremedio.dados.Forma
import com.dablab.horadoremedio.designsystem.Cores
import com.dablab.horadoremedio.designsystem.CorRemedio
import com.dablab.horadoremedio.designsystem.LocalEscalaFonte
import com.dablab.horadoremedio.designsystem.TokenTipografico
import com.dablab.horadoremedio.designsystem.estiloTipografico
import com.dablab.horadoremedio.telas.common.iconeDaForma

private val DIAS_SEMANA = listOf('1' to "D", '2' to "S", '3' to "T", '4' to "Q", '5' to "Q", '6' to "S", '7' to "S")
private val CORES = CorRemedio.entries.map { it.name.lowercase() }

// Rótulos exatos da tabela 7.6 do spec-ios.md (o valor bruto do enum não carrega acento).
private val ROTULO_FORMA = mapOf(
    Forma.COMPRIMIDO.valor to "Comprimido",
    Forma.CAPSULA.valor to "Cápsula",
    Forma.GOTAS.valor to "Gotas",
    Forma.XAROPE.valor to "Xarope",
    Forma.INJECAO.valor to "Injeção",
    Forma.POMADA.valor to "Pomada",
    Forma.SPRAY.valor to "Spray",
    Forma.OUTRO.valor to "Outro",
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CadastroScreen(viewModel: CadastroViewModel, aoSalvar: (Long) -> Unit, aoCancelar: () -> Unit) {
    val escala = LocalEscalaFonte.current
    if (!viewModel.carregado) return // edição: espera carregar o remédio antes de desenhar o formulário
    var confirmandoSaida by remember { mutableStateOf(false) }
    var mostrandoTimePicker by remember { mutableStateOf(false) }
    var mensagemHorario by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = Cores.neutroFundo) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    if (viewModel.formularioPreenchido()) confirmandoSaida = true else aoCancelar()
                }) {
                    Text(stringResource(R.string.cadastro_cancelar), style = estiloTipografico(TokenTipografico.BOTAO, escala))
                }
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                // Bloco 1 — nome
                Bloco(stringResource(R.string.cadastro_nome_pergunta), escala) {
                    OutlinedTextField(
                        value = viewModel.nome,
                        onValueChange = { viewModel.nome = it },
                        modifier = Modifier.fillMaxWidth().heightMinimo(),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        isError = viewModel.erroNome != null,
                        textStyle = estiloTipografico(TokenTipografico.CORPO, escala),
                    )
                    viewModel.erroNome?.let { ErroTexto(stringResource(R.string.cadastro_erro_nome_vazio), escala) }
                }

                // Bloco 2 — dose
                Bloco(stringResource(R.string.cadastro_dose_pergunta), escala) {
                    OutlinedTextField(
                        value = viewModel.dose,
                        onValueChange = { viewModel.dose = it },
                        modifier = Modifier.fillMaxWidth().heightMinimo(),
                        placeholder = { Text(stringResource(R.string.cadastro_dose_exemplo)) },
                        textStyle = estiloTipografico(TokenTipografico.CORPO, escala),
                    )
                }

                // Bloco 3 — forma
                Bloco(stringResource(R.string.cadastro_forma_pergunta), escala) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ROTULO_FORMA.forEach { (valor, rotulo) ->
                            OpcaoComIcone(
                                selecionado = viewModel.forma == valor,
                                icone = iconeDaForma(valor),
                                rotulo = rotulo,
                                escala = escala,
                                aoTocar = { viewModel.forma = valor },
                            )
                        }
                    }
                }

                // Bloco 4 — frequência (3 opções — "A cada X dias" cortado da UI, spec.md §7)
                Bloco(stringResource(R.string.cadastro_frequencia_pergunta), escala) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OpcaoLinha(viewModel.frequencia == "diario", "Todo dia", escala) { viewModel.selecionarFrequencia("diario") }
                        OpcaoLinha(viewModel.frequencia == "dias_semana", "Alguns dias da semana", escala) { viewModel.selecionarFrequencia("dias_semana") }
                        OpcaoLinha(viewModel.frequencia == "quando_precisar", stringResource(R.string.home_quando_precisar), escala) { viewModel.selecionarFrequencia("quando_precisar") }
                    }
                }

                // Bloco 4a — dias da semana
                if (viewModel.frequencia == "dias_semana") {
                    Bloco(null, escala) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DIAS_SEMANA.forEach { (digito, letra) ->
                                DiaSemanaCirculo(
                                    letra = letra,
                                    selecionado = digito in viewModel.diasSemana,
                                    aoTocar = { viewModel.alternarDiaSemana(digito) },
                                )
                            }
                        }
                        viewModel.erroDiaSemana?.let { ErroTexto(stringResource(R.string.cadastro_erro_sem_dia_semana), escala) }
                    }
                }

                // Bloco 5 — horários (some inteiro em "quando_precisar", 11.1)
                if (viewModel.frequencia != "quando_precisar") {
                    Bloco(stringResource(R.string.cadastro_horarios_pergunta), escala) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            viewModel.horarios.sorted().forEach { hhmm ->
                                Row(
                                    Modifier.fillMaxWidth().background(Cores.neutroSuperficie, RoundedCornerShape(12.dp)).padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(hhmm, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTexto)
                                    IconButton(onClick = { viewModel.removerHorario(hhmm) }) {
                                        Icon(Icons.Filled.Close, contentDescription = null, tint = Cores.perigo)
                                    }
                                }
                            }
                            TextButton(
                                onClick = { mostrandoTimePicker = true },
                                enabled = viewModel.horarios.size < 6,
                            ) {
                                Text(stringResource(R.string.cadastro_horarios_adicionar), style = estiloTipografico(TokenTipografico.BOTAO, escala), color = Cores.acao)
                            }
                            if (mensagemHorario == "repetido") {
                                ErroTexto(stringResource(R.string.cadastro_erro_horario_repetido), escala)
                            }
                            viewModel.erroHorario?.let { ErroTexto(stringResource(R.string.cadastro_erro_sem_horario), escala) }
                        }
                    }

                    // Bloco 6 — lembrete (some junto com o 5 em "quando_precisar", 11.1)
                    Bloco(stringResource(R.string.cadastro_lembrete_pergunta), escala) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                if (viewModel.lembreteAtivo) stringResource(R.string.cadastro_lembrete_sim) else stringResource(R.string.cadastro_lembrete_nao),
                                style = estiloTipografico(TokenTipografico.CORPO, escala),
                                color = Cores.neutroTexto,
                            )
                            Switch(checked = viewModel.lembreteAtivo, onCheckedChange = { viewModel.lembreteAtivo = it })
                        }
                    }
                }

                // Bloco 7 — observação
                Bloco(stringResource(R.string.cadastro_observacao_pergunta), escala) {
                    OutlinedTextField(
                        value = viewModel.observacao,
                        onValueChange = { viewModel.observacao = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.cadastro_observacao_exemplo)) },
                        minLines = 2,
                        textStyle = estiloTipografico(TokenTipografico.CORPO, escala),
                    )
                }

                // Bloco 8 — cor
                Bloco(stringResource(R.string.cadastro_cor_pergunta), escala) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CORES.forEach { valor ->
                            val corInfo = CorRemedio.deValor(valor)
                            Box(
                                Modifier
                                    .size(48.dp)
                                    .background(corInfo.detalhe, CircleShape)
                                    .border(if (viewModel.cor == valor) 3.dp else 0.dp, Cores.neutroTexto, CircleShape)
                                    .clickable { viewModel.cor = valor },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (viewModel.cor == valor) Icon(Icons.Filled.Check, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.salvar(aoConcluir = aoSalvar) },
                modifier = Modifier.fillMaxWidth().padding(20.dp).heightIn(min = 64.dp),
                enabled = !viewModel.salvando,
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    stringResource(if (viewModel.ehEdicao) R.string.cadastro_salvar_edicao else R.string.cadastro_salvar),
                    style = estiloTipografico(TokenTipografico.BOTAO, escala),
                )
            }
        }
    }

    if (mostrandoTimePicker) {
        val estado = rememberTimePickerState(is24Hour = true)
        AlertDialog(
            onDismissRequest = { mostrandoTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hhmm = "%02d:%02d".format(estado.hour, estado.minute)
                    val erro = viewModel.adicionarHorario(hhmm)
                    mensagemHorario = null
                    if (erro == "repetido") mensagemHorario = "repetido"
                    mostrandoTimePicker = false
                }) { Text(stringResource(R.string.cadastro_horario_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrandoTimePicker = false }) { Text(stringResource(R.string.cadastro_horario_cancelar_selecao)) }
            },
            text = { TimePicker(state = estado) },
        )
    }

    if (confirmandoSaida) {
        AlertDialog(
            onDismissRequest = { confirmandoSaida = false },
            title = { Text(stringResource(R.string.cadastro_sair_sem_salvar_titulo)) },
            text = { Text(stringResource(R.string.cadastro_sair_sem_salvar_texto)) },
            confirmButton = {
                TextButton(onClick = aoCancelar) { Text(stringResource(R.string.cadastro_sair_sem_salvar_confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoSaida = false }) { Text(stringResource(R.string.cadastro_sair_sem_salvar_continuar)) }
            },
        )
    }
}

@Composable
private fun Bloco(titulo: String?, escala: com.dablab.horadoremedio.dados.EscalaFonte, conteudo: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        titulo?.let { Text(it, style = estiloTipografico(TokenTipografico.APOIO, escala), color = Cores.neutroTexto) }
        conteudo()
    }
}

@Composable
private fun ErroTexto(texto: String, escala: com.dablab.horadoremedio.dados.EscalaFonte) {
    Text(texto, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.perigo)
}

@Composable
private fun OpcaoLinha(selecionado: Boolean, rotulo: String, escala: com.dablab.horadoremedio.dados.EscalaFonte, aoTocar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightMinimo()
            .background(if (selecionado) Cores.acao else Cores.neutroSuperficie, RoundedCornerShape(14.dp))
            .clickable(onClick = aoTocar)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            rotulo,
            style = estiloTipografico(TokenTipografico.CORPO, escala),
            color = if (selecionado) androidx.compose.ui.graphics.Color.White else Cores.neutroTexto,
        )
    }
}

@Composable
private fun OpcaoComIcone(
    selecionado: Boolean,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    rotulo: String,
    escala: com.dablab.horadoremedio.dados.EscalaFonte,
    aoTocar: () -> Unit,
) {
    Column(
        Modifier
            .background(if (selecionado) Cores.acao else Cores.neutroSuperficie, RoundedCornerShape(14.dp))
            .clickable(onClick = aoTocar)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icone, contentDescription = null, tint = if (selecionado) androidx.compose.ui.graphics.Color.White else Cores.neutroTextoSecundario)
        Spacer(Modifier.height(4.dp))
        Text(rotulo, style = estiloTipografico(TokenTipografico.CORPO, escala), color = if (selecionado) androidx.compose.ui.graphics.Color.White else Cores.neutroTexto)
    }
}

@Composable
private fun DiaSemanaCirculo(letra: String, selecionado: Boolean, aoTocar: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .background(if (selecionado) Cores.acao else Cores.neutroSuperficie, CircleShape)
            .clickable(onClick = aoTocar),
        contentAlignment = Alignment.Center,
    ) {
        Text(letra, color = if (selecionado) androidx.compose.ui.graphics.Color.White else Cores.neutroTexto)
    }
}

// Toque mínimo de 60pt (spec-ios.md 8.2, critério de aceite).
private fun Modifier.heightMinimo(): Modifier = this.height(60.dp)
