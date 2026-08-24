// DetalheScreen.kt — detalhe do remédio (spec-ios.md 8.3): todos os horários juntos, botões
// Editar / Parar de tomar / Voltar. Vocabulário obrigatório 15.4 — nunca "excluir"/"apagar".

package com.dablab.horadoremedio.telas.detalhe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dablab.horadoremedio.R
import com.dablab.horadoremedio.designsystem.Cores
import com.dablab.horadoremedio.designsystem.CorRemedio
import com.dablab.horadoremedio.designsystem.LocalEscalaFonte
import com.dablab.horadoremedio.designsystem.TokenTipografico
import com.dablab.horadoremedio.designsystem.estiloTipografico
import com.dablab.horadoremedio.telas.common.iconeDaForma

@Composable
fun DetalheScreen(
    viewModel: DetalheViewModel,
    aoEditar: (Long) -> Unit,
    aoVoltar: () -> Unit,
    aoArquivar: (nome: String) -> Unit,
) {
    val escala = LocalEscalaFonte.current
    val estado by viewModel.estado.collectAsState()
    var confirmando by remember { mutableStateOf(false) }
    val remedio = estado.remedio ?: return

    val cor = CorRemedio.deValor(remedio.cor)

    Scaffold(containerColor = Cores.neutroFundo) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            TextButton(onClick = aoVoltar) {
                Text(stringResource(R.string.detalhe_voltar), style = estiloTipografico(TokenTipografico.BOTAO, escala), color = Cores.acao)
            }

            Spacer(Modifier.height(24.dp))

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(72.dp).background(cor.fundo, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(iconeDaForma(remedio.forma), contentDescription = null, tint = cor.detalhe)
                }
                Spacer(Modifier.height(12.dp))
                Text(remedio.nome, style = estiloTipografico(TokenTipografico.DISPLAY, escala), color = Cores.neutroTexto, textAlign = TextAlign.Center)
                remedio.dose?.let {
                    Text(it, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                stringResource(R.string.detalhe_titulo_detalhes).uppercase(),
                style = estiloTipografico(TokenTipografico.APOIO, escala),
                color = Cores.neutroTextoSecundario,
            )
            Spacer(Modifier.height(8.dp))
            Column(
                Modifier.fillMaxWidth().background(Cores.neutroSuperficie, RoundedCornerShape(14.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (estado.horarios.isEmpty()) {
                    Text(stringResource(R.string.home_quando_precisar), style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTexto)
                } else {
                    estado.horarios.forEach { horario ->
                        Text(horario.horaMinuto, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTexto)
                    }
                }
                remedio.observacao?.let {
                    Text(it, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
                }
                if (remedio.lembreteAtivo && remedio.frequencia != "quando_precisar") {
                    Text(stringResource(R.string.detalhe_avisos_ligados), style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.acao)
                } else if (remedio.frequencia != "quando_precisar") {
                    Text(stringResource(R.string.home_sem_aviso), style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
                }
            }

            Spacer(Modifier.height(32.dp))
            Text(
                stringResource(R.string.detalhe_titulo_acoes).uppercase(),
                style = estiloTipografico(TokenTipografico.APOIO, escala),
                color = Cores.neutroTextoSecundario,
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { aoEditar(remedio.id) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.detalhe_editar), style = estiloTipografico(TokenTipografico.BOTAO, escala))
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { confirmando = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cores.neutroSuperficie, contentColor = Cores.perigo),
            ) {
                Text(stringResource(R.string.detalhe_parar), style = estiloTipografico(TokenTipografico.BOTAO, escala))
            }
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text(stringResource(R.string.detalhe_parar_confirmacao_titulo, remedio.nome)) },
            text = { Text(stringResource(R.string.detalhe_parar_confirmacao_texto)) },
            confirmButton = {
                TextButton(onClick = { viewModel.pararDeTomar(aoArquivar) }) {
                    Text(stringResource(R.string.detalhe_parar_confirmacao_sim), color = Cores.perigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text(stringResource(R.string.detalhe_parar_confirmacao_cancelar)) }
            },
        )
    }
}
