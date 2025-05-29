package eone.grim.ElectionPart

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import eone.grim.ElectionPart.model.Election
import eone.grim.ElectionPart.model.Question
import eone.grim.crypto.CryptoModule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class ElectionsRepo() {
    private val electionsCollection = Firebase.firestore.collection("elections")

    val electionsFlow: Flow<List<Election>> =
        electionsCollection.snapshots.map { snap ->
            snap.documents.map { doc ->
                doc.data<Election>().copy(id = doc.id)
            }
        }

    val activeElectionsFlow: Flow<List<Election>> =
        electionsCollection.snapshots.map { snap ->
            snap.documents.map { doc ->
                doc.data<Election>().copy(id = doc.id)
            }
        }

    fun electionById(eid: String): Flow<Election> {
        return electionsCollection
            .document(eid)
            .snapshots
            .mapNotNull { it.data<Election>().copy(id = it.id) }
    }

    suspend fun createElection(title: String) : String {
        val (publicKey, privateKey) = CryptoModule.generateKeyPair()
        val ref = electionsCollection.add(Election(title = title, publicKey = publicKey))
        ref.update(mapOf("id" to ref.id))
        return ref.id
    }

    fun questionsFlow(electionId: String): Flow<List<Question>> {
      return  electionsCollection.document(electionId)
            .collection("questions")
            .snapshots
            .map { snap ->
                snap.documents
                    .mapNotNull { doc -> doc.data<Question>() }
                    .sortedBy { it.order }
            }
    }


   suspend fun saveAll(electionId: String, updated: List<Question>) {
        val col = electionsCollection.document(electionId).collection("questions")

        val existingDocs = col.get().documents
        val keepIds = updated.map { it.id }.toSet()

        val batch = Firebase.firestore.batch()

        // Видаляємо зайві
        existingDocs
            .filter { it.id !in keepIds }
            .forEach { doc -> batch.delete(doc.reference) }

        // Додаємо / оновлюємо
        updated.forEachIndexed { index, q ->
            val id = if (q.id.isBlank()) col.document.id else q.id
            val withOrder = q.copy(id = id, order = index)
            batch.set(col.document(id), withOrder)
        }

        batch.commit()
    }






//    suspend fun saveAll(list: List<Question>) = Firebase.firestore.runBatch { batch ->
//        // 1. видаляємо зайві
//        val ids = list.map { it.id }.toSet()
//        col.get().await().documents
//            .filter { it.id !in ids }
//            .forEach { batch.delete(it.reference) }
//
//        // 2. ставимо / оновлюємо актуальні
//        list.forEach { q ->
//            batch.set(col.document(q.id), q.copy(order = list.indexOf(q)))
//        }
//    }
}



