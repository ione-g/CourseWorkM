package eone.grim.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import eone.grim.ElectionPart.ElectionsRepo
import eone.grim.ElectionPart.VotingRepo
import eone.grim.firebase.AuthRepo
import eone.grim.ui.auth.AuthScreen
import eone.grim.ui.editor.ElectionEditor
import eone.grim.ui.home.HomeScreen
import eone.grim.ui.voting.UserHomeScreen
import eone.grim.ui.voting.VotingScreen
import kotlinx.coroutines.launch


@Composable
fun AppNav(repo: AuthRepo) {
    val nav = rememberNavController()
    val user by repo.observeUser().collectAsState(null)

    val start = if (user == null) Route.Auth.path else Route.Home.path

    NavHost(navController = nav, startDestination = start) {

        composable(Route.Auth.path) {
            AuthScreen(
                authAction = { email, pass, isLogin ->
                    if (isLogin) repo.login(email, pass) else repo.register(email, pass)
                },
                onSuccess = { nav.navigate(Route.Home.path) {
                    popUpTo(Route.Auth.path) { inclusive = true }
                } }
            )
        }

        composable(Route.Home.path) {
            val electionsRepo = ElectionsRepo()
//            HomeScreen(repo = electionsRepo, onOpenElection = { id -> nav.navigate("editor/$id") })
            UserHomeScreen(electionsRepo, onVote = {id -> nav.navigate("editor/$id")} )
        }

        composable(
            route = "editor/{eid}",
            arguments = listOf(navArgument("eid") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("eid") ?: return@composable
//            ElectionEditorScreen(electionId = id, onBack = { nav.popBackStack() })
            VotingScreen(id, VotingRepo(),{})
        }
    }
}

@Composable
fun ElectionEditorScreen(
    electionId: String,
    onBack: () -> Unit
) {
    val repo = remember { ElectionsRepo() }
    val scope = rememberCoroutineScope()
    val questions by repo.questionsFlow(electionId).collectAsState(initial = emptyList())

    ElectionEditor(
        eid = electionId,

    )

}
