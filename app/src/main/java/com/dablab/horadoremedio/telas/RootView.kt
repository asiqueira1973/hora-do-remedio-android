// RootView.kt — roteamento raiz (épico 8.0/8.7/8.2): prepara o banco, decide onboarding × Home
// pelas preferências, e navega para o cadastro (Navigation Compose — spec.md §5).

package com.dablab.horadoremedio.telas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dablab.horadoremedio.dados.EscalaFonte
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.designsystem.HoraDoRemedioTheme
import com.dablab.horadoremedio.lembretes.AgendadorLembretes
import com.dablab.horadoremedio.telas.cadastro.CadastroScreen
import com.dablab.horadoremedio.telas.cadastro.CadastroViewModel
import com.dablab.horadoremedio.telas.home.HomeScreen
import com.dablab.horadoremedio.telas.home.HomeViewModel
import com.dablab.horadoremedio.telas.onboarding.OnboardingScreen
import kotlinx.coroutines.launch

private class HomeViewModelFactory(private val repositorio: Repositorio) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(repositorio) as T
}

private class CadastroViewModelFactory(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        CadastroViewModel(repositorio, agendador) as T
}

@Composable
fun RootView(repositorio: Repositorio, aoFicarPronto: () -> Unit) {
    var carregado by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        repositorio.prepararNaAbertura()
        carregado = true
    }

    if (!carregado) return // splash continua cobrindo (MainActivity.setKeepOnScreenCondition)

    val preferencias by repositorio.preferencias.observar().collectAsState(initial = null)
    if (preferencias == null) return // ainda esperando a 1ª emissão do Flow — evita flicker de onboarding

    LaunchedEffect(Unit) { aoFicarPronto() }

    val escala = EscalaFonte.deValor(preferencias!!.escalaFonte)
    val onboardingConcluido = preferencias!!.onboardingConcluidoEm != null
    val escopo = rememberCoroutineScope()
    val navController = rememberNavController()

    HoraDoRemedioTheme(escala = escala) {
        NavHost(navController, startDestination = if (onboardingConcluido) "home" else "onboarding") {
            composable("onboarding") {
                OnboardingScreen(
                    aoConcluir = {
                        escopo.launch {
                            repositorio.preferencias.concluirOnboarding(java.time.Instant.now().toString())
                            navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                        }
                    }
                )
            }
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(repositorio))
                HomeScreen(
                    viewModel = homeViewModel,
                    aoTocarAdicionar = { navController.navigate("cadastro/novo") },
                    aoTocarRemedio = { /* detalhe chega no épico 8.3 */ },
                )
            }
            composable("cadastro/novo") {
                val agendador = remember { AgendadorLembretes(context, repositorio) }
                val cadastroViewModel: CadastroViewModel = viewModel(factory = CadastroViewModelFactory(repositorio, agendador))
                CadastroScreen(
                    viewModel = cadastroViewModel,
                    aoSalvar = { navController.popBackStack() },
                    aoCancelar = { navController.popBackStack() },
                )
            }
        }
    }
}
