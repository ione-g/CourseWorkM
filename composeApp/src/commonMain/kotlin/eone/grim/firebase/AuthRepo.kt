package eone.grim.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepo(
    private val auth: FirebaseAuth = Firebase.auth
) {
    suspend fun register(email: String, pass: String) =
        auth.createUserWithEmailAndPassword(email, pass).user!!

    suspend fun login(email: String, pass: String) =
        auth.signInWithEmailAndPassword(email, pass).user!!

    suspend fun logout() =
        auth.signOut()

    fun observeUser(): Flow<FirebaseUser?> = auth.authStateChanged

}
