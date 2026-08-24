// Enums.kt — valores das colunas-enum de `remedios`/`preferencias` (spec-ios.md 7.6), guardados
// como TEXT no banco (mesmo valor literal dos dois lados, para a paridade de schema do spec.md §4).

package com.dablab.horadoremedio.dados

enum class Forma(val valor: String) {
    COMPRIMIDO("comprimido"),
    CAPSULA("capsula"),
    GOTAS("gotas"),
    XAROPE("xarope"),
    INJECAO("injecao"),
    POMADA("pomada"),
    SPRAY("spray"),
    OUTRO("outro");

    companion object {
        fun deValor(valor: String): Forma = entries.first { it.valor == valor }
    }
}

enum class Frequencia(val valor: String) {
    DIARIO("diario"),
    DIAS_SEMANA("dias_semana"),
    INTERVALO_DIAS("intervalo_dias"),
    QUANDO_PRECISAR("quando_precisar");

    companion object {
        fun deValor(valor: String): Frequencia = entries.first { it.valor == valor }
    }
}

enum class Cor(val valor: String) {
    CORAL("coral"),
    AZUL("azul"),
    VERDE("verde"),
    AMARELO("amarelo"),
    LILAS("lilas"),
    PESSEGO("pessego");

    companion object {
        fun deValor(valor: String): Cor = entries.first { it.valor == valor }
    }
}

enum class EscalaFonte(val valor: String) {
    GRANDE("grande"),
    GIGANTE("gigante");

    companion object {
        fun deValor(valor: String): EscalaFonte = entries.first { it.valor == valor }
    }
}
