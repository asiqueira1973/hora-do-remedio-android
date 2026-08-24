// Saudacao.kt — saudação por período do dia (spec-ios.md 11.6): Bom dia 05:00–11:59, Boa tarde
// 12:00–17:59, Boa noite 18:00–04:59. Função pura — a tela só resolve a string via strings.xml.

package com.dablab.horadoremedio.telas.home

import java.time.LocalTime

enum class Saudacao { MANHA, TARDE, NOITE }

fun saudacaoPara(hora: LocalTime = LocalTime.now()): Saudacao = when (hora.hour) {
    in 5..11 -> Saudacao.MANHA
    in 12..17 -> Saudacao.TARDE
    else -> Saudacao.NOITE
}
