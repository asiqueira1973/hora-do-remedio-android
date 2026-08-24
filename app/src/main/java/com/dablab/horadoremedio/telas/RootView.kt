// RootView.kt — roteamento raiz (épico 8.0/8.7): prepara o banco, decide onboarding × Home pelas
// preferências, e libera a splash screen assim que souber qual tela mostrar.

package com.dablab.horadoremedio.telas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dablab.horadoremedio.dados.EscalaFonte
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.designsystem.HoraDoRemedioTheme
import com.dablab.horadoremedio.telas.home.HomeScreen
import com.dablab.horadoremedio.telas.home.HomeViewModel
import com.dablab.horadoremedio.telas.onboarding.OnboardingScreen
import kotlinx.coroutines.launch

private class HomeViewModelFactory(private val repositorio: Repositorio) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(repositorio) as T
}

@Composable
fun RootView(repositorio: Repositorio, aoFicarPronto: () -> Unit) {
    var pronto by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repositorio.prepararNaAbertura()
        pronto = true
        aoFicarPronto()
    }

    if (!pronto) return // splash continua cobrindo (MainActivity.setKeepOnScreenCondition)

    val preferencias by repositorio.preferencias.observar().collectAsState(initial = null)
    val escala = preferencias?.escalaFonte?.let { EscalaFonte.deValor(it) } ?: EscalaFonte.GRANDE
    val onboardingConcluido = preferencias?.onboardingConcluidoEm != null
    val escopo = androidx.compose.runtime.rememberCoroutineScope()

    HoraDoRemedioTheme(escala = escala) {
        if (!onboardingConcluido) {
            OnboardingScreen(
                aoConcluir = {
                    escopo.launch {
                        repositorio.preferencias.concluirOnboarding(java.time.Instant.now().toString())
                    }
                }
            )
        } else {
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repositorio))
            HomeScreen(
                viewModel = homeViewModel,
                aoTocarAdicionar = { /* cadastro chega no épico 8.2 */ },
                aoTocarRemedio = { /* detalhe chega no épico 8.3 */ },
            )
        }
    }
}
