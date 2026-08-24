// ArquivadosScreen.kt — lista os remédios com arquivado_em preenchido, cada um com "Voltar a
// tomar" (spec-ios.md 8.3/8.6).

package com.dablab.horadoremedio.telas.arquivados

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dablab.horadoremedio.R
import com.dablab.horadoremedio.designsystem.Cores
import com.dablab.horadoremedio.designsystem.LocalEscalaFonte
import com.dablab.horadoremedio.designsystem.TokenTipografico
import com.dablab.horadoremedio.designsystem.estiloTipografico

@Composable
fun ArquivadosScreen(viewModel: ArquivadosViewModel, aoVoltar: () -> Unit) {
    val escala = LocalEscalaFonte.current
    val arquivados by viewModel.arquivados.collectAsState()

    Scaffold(containerColor = Cores.neutroFundo) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            TextButton(onClick = aoVoltar) {
                Text(stringResource(R.string.detalhe_voltar), style = estiloTipografico(TokenTipografico.BOTAO, escala), color = Cores.acao)
            }
            Text(stringResource(R.string.ajustes_arquivados), style = estiloTipografico(TokenTipografico.DISPLAY, escala), color = Cores.neutroTexto)

            if (arquivados.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.arquivados_vazio), style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(arquivados) { remedio ->
                        Row(
                            Modifier.fillMaxWidth().background(Cores.neutroSuperficie, RoundedCornerShape(14.dp)).padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(remedio.nome, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTexto)
                            TextButton(onClick = { viewModel.voltarATomar(remedio) }) {
                                Text(stringResource(R.string.arquivados_voltar_a_tomar), style = estiloTipografico(TokenTipografico.BOTAO, escala), color = Cores.acao)
                            }
                        }
                    }
                }
            }
        }
    }
}
