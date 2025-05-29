package eone.grim

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.initialize
import eone.grim.firebase.AuthRepo
import eone.grim.firebase.FirebaseInit
fun initFirebaseDesktop() {
    Firebase.initialize(

        options = FirebaseOptions(
            apiKey          = "AIzaSyBXDBI8DeP9zGczNMkQkSln8jJqPMSFE10",
            applicationId   = "1:6210794631:android:f0ee812b272e40ae9bf422",
            projectId       = "courseworkeone"
        )
    )
}


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CourseWorkM",
    ) {
        initFirebaseDesktop()
        val firebaseAuth by lazy { Firebase.auth }
        App(AuthRepo(firebaseAuth))
    }
}