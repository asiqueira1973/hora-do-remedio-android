// RootView.kt — roteamento raiz (épico 8.0/8.7/8.2/8.3): prepara o banco, decide onboarding ×
// Home pelas preferências, e navega entre Home, cadastro, edição e detalhe (Navigation Compose —
// spec.md §5). A faixa "X foi arquivado" + Desfazer (spec-ios.md 8.3) é hoisted aqui porque
// precisa sobreviver à navegação de volta do detalhe para a Home.

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dablab.horadoremedio.dados.EscalaFonte
import com.dablab.horadoremedio.dados.Repositorio
import com.dablab.horadoremedio.designsystem.HoraDoRemedioTheme
import com.dablab.horadoremedio.lembretes.AgendadorLembretes
import com.dablab.horadoremedio.telas.ajustes.AjustesScreen
import com.dablab.horadoremedio.telas.ajustes.AjustesViewModel
import com.dablab.horadoremedio.telas.arquivados.ArquivadosScreen
import com.dablab.horadoremedio.telas.arquivados.ArquivadosViewModel
import com.dablab.horadoremedio.telas.cadastro.CadastroScreen
import com.dablab.horadoremedio.telas.cadastro.CadastroViewModel
import com.dablab.horadoremedio.telas.detalhe.DetalheScreen
import com.dablab.horadoremedio.telas.detalhe.DetalheViewModel
import com.dablab.horadoremedio.telas.home.HomeScreen
import com.dablab.horadoremedio.telas.home.HomeViewModel
import com.dablab.horadoremedio.telas.onboarding.OnboardingScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class HomeViewModelFactory(private val repositorio: Repositorio) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        HomeViewModel(repositorio) as T
}

private class CadastroViewModelFactory(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
    private val remedioIdParaEditar: Long? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        CadastroViewModel(repositorio, agendador, remedioIdParaEditar) as T
}

private class DetalheViewModelFactory(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
    private val remedioId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        DetalheViewModel(repositorio, agendador, remedioId) as T
}

private class AjustesViewModelFactory(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        AjustesViewModel(repositorio, agendador) as T
}

private class ArquivadosViewModelFactory(
    private val repositorio: Repositorio,
    private val agendador: AgendadorLembretes,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
        ArquivadosViewModel(repositorio, agendador) as T
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
    val agendador = remember { AgendadorLembretes(context, repositorio) }

    var arquivadoRecente by remember { mutableStateOf<Pair<Long, String>?>(null) }
    LaunchedEffect(arquivadoRecente) {
        if (arquivadoRecente != null) {
            delay(5000)
            arquivadoRecente = null
        }
    }

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
                    aoTocarRemedio = { id -> navController.navigate("detalhe/$id") },
                    aoAbrirAjustes = { navController.navigate("ajustes") },
                    nomeRecemArquivado = arquivadoRecente?.second,
                    aoDesfazerArquivamento = {
                        val (id, _) = arquivadoRecente ?: return@HomeScreen
                        escopo.launch {
                            repositorio.remedios.reativar(id)
                            repositorio.remedios.buscar(id)?.let { agendador.agendarDoRemedio(it) }
                        }
                        arquivadoRecente = null
                    },
                )
            }
            composable("cadastro/novo") {
                val cadastroViewModel: CadastroViewModel = viewModel(factory = CadastroViewModelFactory(repositorio, agendador))
                CadastroScreen(
                    viewModel = cadastroViewModel,
                    aoSalvar = { navController.popBackStack() },
                    aoCancelar = { navController.popBackStack() },
                )
            }
            composable(
                "cadastro/editar/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entrada ->
                val id = entrada.arguments!!.getLong("id")
                val cadastroViewModel: CadastroViewModel = viewModel(
                    key = "editar-$id",
                    factory = CadastroViewModelFactory(repositorio, agendador, id),
                )
                CadastroScreen(
                    viewModel = cadastroViewModel,
                    aoSalvar = { navController.popBackStack("home", inclusive = false) },
                    aoCancelar = { navController.popBackStack() },
                )
            }
            composable(
                "detalhe/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entrada ->
                val id = entrada.arguments!!.getLong("id")
                val detalheViewModel: DetalheViewModel = viewModel(
                    key = "detalhe-$id",
                    factory = DetalheViewModelFactory(repositorio, agendador, id),
                )
                DetalheScreen(
                    viewModel = detalheViewModel,
                    aoEditar = { navController.navigate("cadastro/editar/$id") },
                    aoVoltar = { navController.popBackStack() },
                    aoArquivar = { nome ->
                        arquivadoRecente = id to nome
                        navController.popBackStack("home", inclusive = false)
                    },
                )
            }
            composable("ajustes") {
                val ajustesViewModel: AjustesViewModel = viewModel(factory = AjustesViewModelFactory(repositorio, agendador))
                AjustesScreen(
                    viewModel = ajustesViewModel,
                    aoVoltar = { navController.popBackStack() },
                    aoAbrirArquivados = { navController.navigate("ajustes/arquivados") },
                    aoApagarTudo = {
                        navController.navigate("onboarding") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                )
            }
            composable("ajustes/arquivados") {
                val arquivadosViewModel: ArquivadosViewModel = viewModel(factory = ArquivadosViewModelFactory(repositorio, agendador))
                ArquivadosScreen(viewModel = arquivadosViewModel, aoVoltar = { navController.popBackStack() })
            }
        }
    }
}
