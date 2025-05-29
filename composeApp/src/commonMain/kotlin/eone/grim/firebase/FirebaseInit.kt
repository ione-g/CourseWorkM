package eone.grim.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.initialize

object FirebaseInit {
    private var started = false
    fun start(context: Any? = null) {
        if (started) return
        Firebase.initialize(context)
        started = true
    }
}