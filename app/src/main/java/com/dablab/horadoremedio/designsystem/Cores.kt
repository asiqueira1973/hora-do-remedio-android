// Cores.kt — ColorScheme Material 3 fixo (não Material You / dynamic color) — spec.md §2: a
// identidade visual da família Dab Lab não pode variar por papel de parede do usuário. Valores
// idênticos aos tokens do redesenho iOS (DOCUMENTACAO-TECNICA-ios.md §19.6) e a colors.xml.

package com.dablab.horadoremedio.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Cores {
    val acao = Color(0xFF007AFF)
    val perigo = Color(0xFFFF3B30)
    val atencao = Color(0xFFFF9500)
    val neutroTexto = Color(0xFF1C1C1E)
    val neutroTextoSecundario = Color(0xFF6E6E73)
    val neutroFundo = Color(0xFFF2F2F7)
    val neutroSuperficie = Color(0xFFFFFFFF)
    val neutroBorda = Color(0xFFE5E5EA)
    val chevron = Color(0xFFC7C7CC)
}

// Paleta de identificação dos 6 remédios (spec-ios.md 16.3) — cor de marca, não semântica
// (16.4): nunca reaproveitada para ação/perigo, para não coincidir com `acao` (azul) ou
// `perigo` (vermelho) por acaso.
enum class CorRemedio(val fundo: Color, val texto: Color, val detalhe: Color) {
    CORAL(Color(0xFFFFD9D0), Color(0xFF3A2520), Color(0xFFE8654A)),
    AZUL(Color(0xFFCFE4FF), Color(0xFF1C2B3D), Color(0xFF2F7FE0)),
    VERDE(Color(0xFFCFF0D8), Color(0xFF1D3325), Color(0xFF2FA35C)),
    AMARELO(Color(0xFFFFEBB8), Color(0xFF3A3018), Color(0xFFE5A400)),
    LILAS(Color(0xFFE4D9FF), Color(0xFF2C2340), Color(0xFF7C5AE0)),
    PESSEGO(Color(0xFFFFE0C2), Color(0xFF3A2A18), Color(0xFFE07B2F));

    companion object {
        fun deValor(valor: String): CorRemedio = valueOf(valor.uppercase())
    }
}

val esquemaDeCoresHoraDoRemedio: ColorScheme = lightColorScheme(
    primary = Cores.acao,
    onPrimary = Color.White,
    error = Cores.perigo,
    onError = Color.White,
    background = Cores.neutroFundo,
    onBackground = Cores.neutroTexto,
    surface = Cores.neutroSuperficie,
    onSurface = Cores.neutroTexto,
    surfaceVariant = Cores.neutroFundo,
    onSurfaceVariant = Cores.neutroTextoSecundario,
    outline = Cores.neutroBorda,
)
