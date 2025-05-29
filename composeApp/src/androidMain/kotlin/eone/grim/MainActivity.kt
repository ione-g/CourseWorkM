package eone.grim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import eone.grim.firebase.AuthRepo
import eone.grim.firebase.FirebaseInit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        FirebaseInit.start(this)
        super.onCreate(savedInstanceState)
        val repo = AuthRepo()
        setContent {
            App(repo)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(AuthRepo())
}