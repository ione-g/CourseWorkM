package eone.grim

import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import eone.grim.firebase.AuthRepo
import eone.grim.routes.AppNav

@Composable
@Preview
fun App(repo: AuthRepo) {
    AppNav(repo)
}




