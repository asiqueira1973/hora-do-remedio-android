// OnboardingScreen.kt — 2 telas (spec-ios.md 8.7): sem pedir permissão nem dado pessoal. Grava
// preferencias.onboarding_concluido_em ao tocar "Começar" (2ª tela).

package com.dablab.horadoremedio.telas.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dablab.horadoremedio.R
import com.dablab.horadoremedio.designsystem.Cores
import com.dablab.horadoremedio.designsystem.LocalEscalaFonte
import com.dablab.horadoremedio.designsystem.TokenTipografico
import com.dablab.horadoremedio.designsystem.estiloTipografico

@Composable
fun OnboardingScreen(aoConcluir: () -> Unit) {
    var tela by remember { mutableIntStateOf(1) }
    val escala = LocalEscalaFonte.current

    Scaffold(containerColor = Cores.neutroFundo) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(Cores.neutroFundo).padding(padding).padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (tela == 1) {
                Text(
                    stringResource(R.string.onboarding_tela1_titulo),
                    style = estiloTipografico(TokenTipografico.DISPLAY, escala),
                    color = Cores.neutroTexto,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.onboarding_tela1_texto),
                    style = estiloTipografico(TokenTipografico.CORPO, escala),
                    color = Cores.neutroTextoSecundario,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(40.dp))
                Button(onClick = { tela = 2 }, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    Text(stringResource(R.string.onboarding_tela1_botao), style = estiloTipografico(TokenTipografico.BOTAO, escala))
                }
            } else {
                Text(
                    stringResource(R.string.onboarding_tela2_titulo),
                    style = estiloTipografico(TokenTipografico.DISPLAY, escala),
                    color = Cores.neutroTexto,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.onboarding_tela2_texto),
                    style = estiloTipografico(TokenTipografico.CORPO, escala),
                    color = Cores.neutroTextoSecundario,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(40.dp))
                Button(onClick = aoConcluir, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                    Text(stringResource(R.string.onboarding_tela2_botao), style = estiloTipografico(TokenTipografico.BOTAO, escala))
                }
            }
        }
    }
}
