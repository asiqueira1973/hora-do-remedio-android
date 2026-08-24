// Tema.kt — MaterialTheme raiz do app (épico 8.0). Sempre claro (paridade com o
// userInterfaceStyle "light" do iOS, spec-ios.md 4.2) — Material Design 3, não clone do iOS
// (spec.md §2). `escala` vem de preferencias.escala_fonte e fica disponível via LocalEscalaFonte
// para qualquer tela pedir estiloTipografico(token, LocalEscalaFonte.current).

package com.dablab.horadoremedio.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.dablab.horadoremedio.dados.EscalaFonte

@Composable
fun HoraDoRemedioTheme(
    escala: EscalaFonte = EscalaFonte.GRANDE,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalEscalaFonte provides escala) {
        MaterialTheme(
            colorScheme = esquemaDeCoresHoraDoRemedio,
            content = content,
        )
    }
}
