// Compartilhar.kt — abre a share sheet nativa do Android (spec.md §5: Share.share do iOS vira
// Intent.ACTION_SEND, chooser nativo). Assunto sugerido (14.1) via EXTRA_SUBJECT — usado só por
// apps que têm campo de assunto (e-mail); os demais (WhatsApp etc.) ignoram.

package com.dablab.horadoremedio.exportacao

import android.content.Context
import android.content.Intent

fun compartilharTexto(context: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
        putExtra(Intent.EXTRA_SUBJECT, assuntoCompartilhamento())
    }
    context.startActivity(Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
