// AjustesScreen.kt — épico 8.6, itens na ordem exata do spec-ios.md 8.6: tamanho das letras, som
// do aviso, remédios que parei, compartilhar minha lista, sobre o app, apagar todos os meus dados.

package com.dablab.horadoremedio.telas.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dablab.horadoremedio.R
import com.dablab.horadoremedio.dados.EscalaFonte
import com.dablab.horadoremedio.designsystem.Cores
import com.dablab.horadoremedio.designsystem.LocalEscalaFonte
import com.dablab.horadoremedio.designsystem.TokenTipografico
import com.dablab.horadoremedio.designsystem.estiloTipografico
import com.dablab.horadoremedio.exportacao.compartilharTexto

@Composable
fun AjustesScreen(
    viewModel: AjustesViewModel,
    aoVoltar: () -> Unit,
    aoAbrirArquivados: () -> Unit,
    aoApagarTudo: () -> Unit,
) {
    val escala = LocalEscalaFonte.current
    val preferencias by viewModel.preferencias.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var confirmacao1 by remember { mutableStateOf(false) }
    var confirmacao2 by remember { mutableStateOf(false) }

    Scaffold(containerColor = Cores.neutroFundo) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            TextButton(onClick = aoVoltar) {
                Text(stringResource(R.string.detalhe_voltar), style = estiloTipografico(TokenTipografico.BOTAO, escala), color = Cores.acao)
            }
            Text(stringResource(R.string.ajustes_titulo), style = estiloTipografico(TokenTipografico.DISPLAY, escala), color = Cores.neutroTexto)

            // Tamanho das letras
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.ajustes_tamanho_letras), style = estiloTipografico(TokenTipografico.APOIO, escala), color = Cores.neutroTextoSecundario)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(EscalaFonte.GRANDE to R.string.ajustes_tamanho_grande, EscalaFonte.GIGANTE to R.string.ajustes_tamanho_gigante).forEach { (valor, rotuloRes) ->
                        val selecionado = preferencias?.escalaFonte == valor.valor
                        Column(
                            Modifier
                                .weight(1f)
                                .background(if (selecionado) Cores.acao else Cores.neutroSuperficie, RoundedCornerShape(14.dp))
                                .clickable { viewModel.mudarEscala(valor) }
                                .padding(16.dp),
                        ) {
                            Text(
                                stringResource(rotuloRes),
                                style = estiloTipografico(TokenTipografico.BOTAO, escala),
                                color = if (selecionado) androidx.compose.ui.graphics.Color.White else Cores.neutroTexto,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            Text(
                                stringResource(R.string.ajustes_tamanho_exemplo),
                                style = estiloTipografico(TokenTipografico.CORPO, valor),
                                color = if (selecionado) androidx.compose.ui.graphics.Color.White else Cores.neutroTextoSecundario,
                            )
                        }
                    }
                }
            }

            // Som do aviso
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.ajustes_som_aviso), style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTexto)
                Switch(checked = preferencias?.somLembrete ?: true, onCheckedChange = { viewModel.mudarSom(it) })
            }

            // Remédios / Sobre
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                LinhaAjuste(stringResource(R.string.ajustes_arquivados), escala, aoAbrirArquivados)
                LinhaAjuste(stringResource(R.string.home_botao_compartilhar), escala) {
                    viewModel.textoParaCompartilhar { texto -> compartilharTexto(context, texto) }
                }
                LinhaAjuste(stringResource(R.string.ajustes_sobre), escala) {}
            }

            // Sobre o app (conteúdo direto — sem sub-tela, spec-ios.md 8.6)
            Column(
                Modifier.fillMaxWidth().background(Cores.neutroSuperficie, RoundedCornerShape(14.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.ajustes_privacidade), style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
                Text(
                    stringResource(R.string.sobre_politica_privacidade),
                    style = estiloTipografico(TokenTipografico.CORPO, escala),
                    color = Cores.acao,
                    modifier = Modifier.clickable { uriHandler.openUri(context.getString(R.string.sobre_url_politica_privacidade)) },
                )
            }

            // Apagar todos os meus dados
            Button(
                onClick = { confirmacao1 = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cores.neutroSuperficie, contentColor = Cores.perigo),
            ) {
                Text(stringResource(R.string.ajustes_apagar_tudo), style = estiloTipografico(TokenTipografico.BOTAO, escala))
            }
        }
    }

    if (confirmacao1) {
        AlertDialog(
            onDismissRequest = { confirmacao1 = false },
            title = { Text(stringResource(R.string.ajustes_apagar_confirmacao1_titulo)) },
            text = { Text(stringResource(R.string.ajustes_apagar_confirmacao1_texto)) },
            confirmButton = {
                TextButton(onClick = { confirmacao1 = false; confirmacao2 = true }) {
                    Text(stringResource(R.string.ajustes_apagar_confirmacao1_continuar), color = Cores.perigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmacao1 = false }) { Text(stringResource(R.string.ajustes_apagar_confirmacao1_cancelar)) }
            },
        )
    }

    if (confirmacao2) {
        AlertDialog(
            onDismissRequest = { confirmacao2 = false },
            title = { Text(stringResource(R.string.ajustes_apagar_confirmacao2_titulo)) },
            text = { Text(stringResource(R.string.ajustes_apagar_confirmacao2_texto)) },
            confirmButton = {
                TextButton(onClick = { confirmacao2 = false; viewModel.apagarTudo(aoApagarTudo) }) {
                    Text(stringResource(R.string.ajustes_apagar_confirmacao2_confirmar), color = Cores.perigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmacao2 = false }) { Text(stringResource(R.string.ajustes_apagar_confirmacao2_cancelar)) }
            },
        )
    }
}

@Composable
private fun LinhaAjuste(rotulo: String, escala: EscalaFonte, aoTocar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Cores.neutroSuperficie, RoundedCornerShape(0.dp))
            .clickable(onClick = aoTocar)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(rotulo, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTexto)
    }
}
