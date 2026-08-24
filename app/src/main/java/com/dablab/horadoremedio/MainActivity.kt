// MainActivity.kt — Activity única (épico 8.0). Prepara o banco (bootstrap idempotente) antes de
// exibir a UI, com a Splash Screen segurando (equivalente a `estadoBanco === 'carregando'` no
// _layout.tsx do iOS) — RootView decide onboarding × app pelas preferências.

package com.dablab.horadoremedio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dablab.horadoremedio.telas.RootView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        var pronto = false
        splash.setKeepOnScreenCondition { !pronto }

        val repositorio = (application as App).repositorio
        setContent {
            RootView(
                repositorio = repositorio,
                aoFicarPronto = { pronto = true },
            )
        }
    }
}
