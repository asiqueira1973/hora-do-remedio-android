// Tipografia.kt — tokens de texto (spec-ios.md 16.2, valores pós-redesenho de 2026-08-23 —
// mesma tabela do src/theme/typography.ts do iOS). `apoio` em `grande` (20sp) é o piso absoluto
// do app (princípio 0.1) — nenhum token deste arquivo usa valor menor. Fonte do sistema (Roboto),
// igual à decisão do iOS de usar a fonte nativa em vez de empacotar uma família própria.

package com.dablab.horadoremedio.designsystem

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dablab.horadoremedio.dados.EscalaFonte

enum class TokenTipografico { DISPLAY, TITULO, CORPO, BOTAO, APOIO }

private val TAMANHOS: Map<TokenTipografico, Map<EscalaFonte, Int>> = mapOf(
    TokenTipografico.DISPLAY to mapOf(EscalaFonte.GRANDE to 34, EscalaFonte.GIGANTE to 40),
    TokenTipografico.TITULO to mapOf(EscalaFonte.GRANDE to 28, EscalaFonte.GIGANTE to 34),
    TokenTipografico.CORPO to mapOf(EscalaFonte.GRANDE to 22, EscalaFonte.GIGANTE to 26),
    TokenTipografico.BOTAO to mapOf(EscalaFonte.GRANDE to 22, EscalaFonte.GIGANTE to 26),
    TokenTipografico.APOIO to mapOf(EscalaFonte.GRANDE to 20, EscalaFonte.GIGANTE to 22),
)

private val PESOS: Map<TokenTipografico, FontWeight> = mapOf(
    TokenTipografico.DISPLAY to FontWeight.W800,
    TokenTipografico.TITULO to FontWeight.W700,
    TokenTipografico.CORPO to FontWeight.W600,
    TokenTipografico.BOTAO to FontWeight.W700,
    TokenTipografico.APOIO to FontWeight.W600,
)

/** Altura de linha sempre 1.35× o tamanho da fonte, em todos os tokens (spec-ios.md 16.2). */
fun estiloTipografico(token: TokenTipografico, escala: EscalaFonte): TextStyle {
    val tamanho = requireNotNull(TAMANHOS[token]?.get(escala))
    return TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = PESOS.getValue(token),
        fontSize = tamanho.sp,
        lineHeight = (tamanho * 1.35).sp,
    )
}

/** Escala de fonte ativa (de `preferencias.escala_fonte`) disponível a qualquer tela via CompositionLocal. */
val LocalEscalaFonte = compositionLocalOf { EscalaFonte.GRANDE }
