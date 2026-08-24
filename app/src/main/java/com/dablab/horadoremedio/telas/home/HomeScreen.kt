// HomeScreen.kt — tela inicial (spec-ios.md 8.1). Lista agrupada por horário, estado vazio, FAB
// "+ Adicionar remédio". A navegação para cadastro/detalhe chega nos próximos épicos (spec.md §8).

package com.dablab.horadoremedio.telas.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dablab.horadoremedio.R
import com.dablab.horadoremedio.dados.RemedioEntity
import com.dablab.horadoremedio.designsystem.Cores
import com.dablab.horadoremedio.designsystem.CorRemedio
import com.dablab.horadoremedio.designsystem.LocalEscalaFonte
import com.dablab.horadoremedio.designsystem.TokenTipografico
import com.dablab.horadoremedio.designsystem.estiloTipografico

private fun iconeDaForma(forma: String): ImageVector = when (forma) {
    "gotas" -> Icons.Filled.Opacity
    "xarope" -> Icons.Filled.LocalDrink
    "injecao" -> Icons.Filled.Vaccines
    "pomada" -> Icons.Filled.Sanitizer
    "spray" -> Icons.Filled.Air
    "outro" -> Icons.Filled.MedicalServices
    else -> Icons.Filled.Medication // comprimido, capsula
}

private fun rotuloFrequencia(remedio: RemedioEntity): String? = when (remedio.frequencia) {
    "dias_semana" -> remedio.diasSemana // formatação amigável chega no épico 8.2/16.3
    "intervalo_dias" -> remedio.intervaloDias?.let { "A cada $it dias" }
    else -> null // "diario" e "quando_precisar" não escrevem nada aqui (11.7)
}

@Composable
fun HomeScreen(viewModel: HomeViewModel, aoTocarAdicionar: () -> Unit, aoTocarRemedio: (Long) -> Unit) {
    val grupos by viewModel.grupos.collectAsState()
    val escala = LocalEscalaFonte.current

    Scaffold(
        containerColor = Cores.neutroFundo,
        floatingActionButton = {},
    ) { paddingInterno ->
        Box(Modifier.fillMaxSize().padding(paddingInterno)) {
            when {
                grupos == null -> Unit // splash já cobriu a carga inicial (8.0) — sem spinner (8.1)
                grupos!!.isEmpty() -> EstadoVazio(Modifier.align(Alignment.Center))
                else -> Column(Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item { Spacer(Modifier.height(20.dp)) }
                        items(grupos!!) { grupo -> GrupoSecao(grupo, aoTocarRemedio, escala) }
                        item { Spacer(Modifier.height(96.dp)) }
                    }
                }
            }

            Button(
                onClick = aoTocarAdicionar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(
                    stringResource(R.string.home_botao_adicionar),
                    style = estiloTipografico(TokenTipografico.BOTAO, escala),
                )
            }
        }
    }
}

@Composable
private fun GrupoSecao(
    grupo: GrupoHome,
    aoTocarRemedio: (Long) -> Unit,
    escala: com.dablab.horadoremedio.dados.EscalaFonte,
) {
    Column {
        Text(
            grupo.rotulo.uppercase(),
            style = estiloTipografico(TokenTipografico.APOIO, escala),
            color = Cores.neutroTextoSecundario,
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            grupo.cards.forEach { card -> CardRemedio(card, aoTocarRemedio, escala) }
        }
    }
}

@Composable
private fun CardRemedio(
    card: CardHome,
    aoTocarRemedio: (Long) -> Unit,
    escala: com.dablab.horadoremedio.dados.EscalaFonte,
) {
    val remedio = card.remedio
    val cor = CorRemedio.deValor(remedio.cor)
    val frequenciaTexto = rotuloFrequencia(remedio)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cores.neutroSuperficie, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).background(cor.fundo, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconeDaForma(remedio.forma), contentDescription = null, tint = cor.detalhe)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(remedio.nome, style = estiloTipografico(TokenTipografico.TITULO, escala), color = Cores.neutroTexto)
            remedio.dose?.let {
                Text(it, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
            }
            remedio.observacao?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = Cores.atencao, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(it, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
                }
            }
            frequenciaTexto?.let {
                Text(it, style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
            }
            if (!remedio.lembreteAtivo && remedio.frequencia != "quando_precisar") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.NotificationsOff, contentDescription = null, tint = Cores.neutroTextoSecundario, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.home_sem_aviso), style = estiloTipografico(TokenTipografico.CORPO, escala), color = Cores.neutroTextoSecundario)
                }
            }
        }
    }
}

@Composable
private fun EstadoVazio(modifier: Modifier = Modifier) {
    val escala = LocalEscalaFonte.current
    Column(modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(R.string.home_vazio_titulo),
            style = estiloTipografico(TokenTipografico.TITULO, escala),
            color = Cores.neutroTexto,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.home_vazio_texto),
            style = estiloTipografico(TokenTipografico.CORPO, escala),
            color = Cores.neutroTextoSecundario,
            textAlign = TextAlign.Center,
        )
    }
}
