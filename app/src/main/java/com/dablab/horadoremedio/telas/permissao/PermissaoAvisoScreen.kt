// PermissaoAvisoScreen.kt — tela explicativa ANTES do diálogo do sistema (spec-ios.md 8.4, passo
// 2): "Continuar" pede POST_NOTIFICATIONS (Android 13+, diálogo real) e, se faltar, manda pra tela
// de Ajustes do sistema pro alarme exato (SCHEDULE_EXACT_ALARM não tem diálogo — spec.md
// "Pegadinhas" #3). "Agora não" pula tudo: o remédio já foi salvo com lembrete_ativo=1 mesmo assim
// (spec-ios.md 8.4, passo 3), e a faixa da Home cobre o resto.

package com.dablab.horadoremedio.telas.permissao

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dablab.horadoremedio.R
import com.dablab.horadoremedio.designsystem.Cores
import com.dablab.horadoremedio.designsystem.LocalEscalaFonte
import com.dablab.horadoremedio.designsystem.TokenTipografico
import com.dablab.horadoremedio.designsystem.estiloTipografico
import com.dablab.horadoremedio.lembretes.PermissoesLembrete

@Composable
fun PermissaoAvisoScreen(viewModel: PermissaoAvisoViewModel, aoConcluir: () -> Unit) {
    val escala = LocalEscalaFonte.current
    val context = LocalContext.current

    fun continuarComAlarmeExato() {
        if (!PermissoesLembrete.alarmeExatoPermitido(context)) {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
            )
        }
        viewModel.finalizarAgendamento(aoConcluir)
    }

    val pedirNotificacao = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        continuarComAlarmeExato()
    }

    Scaffold(containerColor = Cores.neutroFundo) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.permissao_titulo),
                style = estiloTipografico(TokenTipografico.DISPLAY, escala),
                color = Cores.neutroTexto,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.permissao_texto),
                style = estiloTipografico(TokenTipografico.CORPO, escala),
                color = Cores.neutroTextoSecundario,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PermissoesLembrete.notificacoesPermitidas(context)) {
                        pedirNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        continuarComAlarmeExato()
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) {
                Text(stringResource(R.string.permissao_continuar), style = estiloTipografico(TokenTipografico.BOTAO, escala))
            }
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = { viewModel.finalizarAgendamento(aoConcluir) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
            ) {
                Text(stringResource(R.string.permissao_agora_nao), style = estiloTipografico(TokenTipografico.BOTAO, escala), color = Cores.neutroTextoSecundario)
            }
        }
    }
}
