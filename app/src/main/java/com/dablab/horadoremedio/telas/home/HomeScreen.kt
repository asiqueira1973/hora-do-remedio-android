// HomeScreen.kt — tela inicial (spec-ios.md 8.1). Lista agrupada por horário, estado vazio, FAB
// "+ Adicionar remédio". A navegação para cadastro/detalhe chega nos próximos épicos (spec.md §8).

package com.dablab.horadoremedio.telas.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.dablab.horadoremedio.exportacao.compartilharTexto
import com.dablab.horadoremedio.exportacao.gerarTexto
import com.dablab.horadoremedio.telas.common.iconeDaForma

private fun rotuloSaudacao(saudacao: Saudacao): Int = when (saudacao) {
    Saudacao.MANHA -> R.string.home_saudacao_manha
    Saudacao.TARDE -> R.string.home_saudacao_tarde
    Saudacao.NOITE -> R.string.home_saudacao_noite
}

private fun rotuloFrequencia(remedio: RemedioEntity): String? = when (remedio.frequencia) {
    "dias_semana" -> remedio.diasSemana // formatação amigável chega no épico 8.2/16.3
    "intervalo_dias" -> remedio.intervaloDias?.let { "A cada $it dias" }
    else -> null // "diario" e "quando_precisar" não escrevem nada aqui (11.7)
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    aoTocarAdicionar: () -> Unit,
    aoTocarRemedio: (Long) -> Unit,
    nomeRecemArquivado: String? = null,
    aoDesfazerArquivamento: () -> Unit = {},
) {
    val grupos by viewModel.grupos.collectAsState()
    val escala = LocalEscalaFonte.current
    val context = LocalContext.current
    val temAtivos = !grupos.isNullOrEmpty()

    Scaffold(
        containerColor = Cores.neutroFundo,
        floatingActionButton = {},
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Cores.neutroFundo)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(rotuloSaudacao(saudacaoPara())),
                    style = estiloTipografico(TokenTipografico.DISPLAY, escala),
                    color = Cores.neutroTexto,
                )
                IconButton(
                    onClick = {
                        compartilharTexto(context, gerarTexto(viewModel.remediosParaExportar()))
                    },
                    enabled = temAtivos,
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = stringResource(
                            if (temAtivos) R.string.home_botao_compartilhar else R.string.home_botao_compartilhar_desabilitado
                        ),
                        tint = if (temAtivos) Cores.acao else Cores.neutroBorda,
                    )
                }
            }
        },
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
                        item { Spacer(Modifier.height(4.dp)) }
                        items(grupos!!) { grupo -> GrupoSecao(grupo, aoTocarRemedio, escala) }
                        item { Spacer(Modifier.height(96.dp)) }
                    }
                }
            }

            nomeRecemArquivado?.let { nome ->
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 20.dp, end = 20.dp, bottom = 96.dp)
                        .fillMaxWidth()
                        .background(Cores.neutroTexto, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.detalhe_arquivado_faixa, nome),
                        style = estiloTipografico(TokenTipografico.CORPO, escala),
                        color = androidx.compose.ui.graphics.Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.material3.TextButton(onClick = aoDesfazerArquivamento) {
                        Text(
                            stringResource(R.string.detalhe_arquivado_desfazer),
                            style = estiloTipografico(TokenTipografico.BOTAO, escala),
                            color = Cores.acao,
                            maxLines = 1,
                        )
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
            .clickable { aoTocarRemedio(remedio.id) }
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
