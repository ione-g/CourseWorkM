package eone.grim.ElectionPart

import androidx.compose.runtime.snapshots.SnapshotStateMap
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.*
import dev.gitlive.firebase.firestore.firestore
import eone.grim.ElectionPart.model.Ballot
import eone.grim.ElectionPart.model.Election
import eone.grim.ElectionPart.model.Question
import eone.grim.crypto.CryptoModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import eone.grim.ElectionPart.model.Question.Type.*
import eone.grim.crypto.ByteString

class VotingRepo() {
    private val electionsCollection = Firebase.firestore.collection("elections")
    private val tab = Firebase.firestore


    fun questionsFlow(electionId: String): Flow<List<Question>> {
        return electionsCollection.document(electionId)
            .collection("questions")
            .snapshots
            .map { snap ->
                snap.documents
                    .mapNotNull { doc -> doc.data<Question>() }
                    .sortedBy { it.order }
            }
    }

    suspend fun encryptAndSubmit(
        eid: String,
        questions: List<Question>,
        answers: SnapshotStateMap<String, Any>
    ) {
        val pubDoc = Firebase.firestore.document("elections/$eid").get().data<Election>()
        val pbKey = pubDoc.publicKey

        val plainBytes: Map<String, ByteString> =
            encodeAnswersToBytes(questions, answers)

        val ctMap: Map<String, ByteString> = plainBytes.mapValues { (_, bs) ->
            CryptoModule.encrypt(bs, pbKey)
        }
        val uid = Firebase.auth.currentUser?.uid
        Firebase.firestore
            .document("elections/$eid/ballots/${uid}")
            .set(mapOf("uid" to uid, "vote" to ctMap))


    }
    fun encodeAnswersToBytes(
        questions: List<Question>,
        answers: Map<String, Any>
    ): Map<String, ByteString> = buildMap {
        for (q in questions) {
            val any = answers[q.id]
                ?: error("Answer missing for ${q.id}")
            val bytes: ByteArray = when (q.type) {
                Question.Type.YES_NO -> byteArrayOf(if (any as Boolean) 0x01 else 0x00)

                Question.Type.SINGLE -> byteArrayOf(
                    q.options.indexOf(any as String).toByte()
                )

                Question.Type.MULTI  -> {
                    val set = any as Set<*>
                    var mask = 0
                    set.forEach { opt ->
                        val idx = q.options.indexOf(opt as String)
                        mask = mask or (1 shl idx)
                    }
                    byteArrayOf(
                        (mask and 0xFF).toByte(),
                        (mask shr 8 and 0xFF).toByte(),
                        (mask shr 16 and 0xFF).toByte(),
                        (mask shr 24 and 0xFF).toByte()
                    )                }

                Question.Type.RANKED -> {
                    val list = any as List<*>
                    list.map { idx ->
                        q.options.indexOf(idx as String).toByte()
                    }.toByteArray()
                }
            }
            put(q.id, bytes.toByteString())
        }

    }
    fun ByteArray.toByteString(): ByteString = ByteString(this.copyOf())


}