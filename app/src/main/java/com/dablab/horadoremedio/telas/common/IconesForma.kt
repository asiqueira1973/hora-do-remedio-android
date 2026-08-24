// IconesForma.kt — ícone por forma farmacêutica (spec-ios.md 7.6), compartilhado entre a Home
// (8.1) e o Cadastro (8.2) para não duplicar o mapeamento.

package com.dablab.horadoremedio.telas.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.ui.graphics.vector.ImageVector

fun iconeDaForma(forma: String): ImageVector = when (forma) {
    "gotas" -> Icons.Filled.Opacity
    "xarope" -> Icons.Filled.LocalDrink
    "injecao" -> Icons.Filled.Vaccines
    "pomada" -> Icons.Filled.Sanitizer
    "spray" -> Icons.Filled.Air
    "outro" -> Icons.Filled.MedicalServices
    else -> Icons.Filled.Medication // comprimido, capsula
}
