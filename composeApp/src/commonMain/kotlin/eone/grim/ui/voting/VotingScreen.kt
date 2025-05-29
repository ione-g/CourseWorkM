package eone.grim.ui.voting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eone.grim.ElectionPart.VotingRepo
import eone.grim.ElectionPart.model.Question
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun VotingScreen(
    eid: String,
    repo: VotingRepo,
    onFinish: () -> Unit
) {
    val qs by repo.questionsFlow(eid).collectAsState(emptyList())
    val answers = remember { mutableStateMapOf<String, Any>() }
    var sending by remember { mutableStateOf(false) }
    val scope     = rememberCoroutineScope()
    if (qs.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    // Скролимо запитання
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(qs, key = { it.id }) { q ->
            when (q.type) {
                Question.Type.YES_NO -> YesNoBlock(q, answers)
                Question.Type.SINGLE -> SingleChoiceBlock(q, answers)
                Question.Type.MULTI -> MultiChoiceBlock(q, answers)
                Question.Type.RANKED -> RankedBlock(q, answers)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        item {
            Button(
                onClick = {
                    scope.launch {                       // ← запускаємо корутину
                        sending = true
                        repo.encryptAndSubmit(eid, qs, answers)
                        sending = false
                        onFinish()
                    }
                },
                enabled = !sending && answers.size == qs.size,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) { Text("Надіслати") }
        }
    }
}

@Composable fun YesNoBlock(q: Question, ans: MutableMap<String, Any>) {
    Column { Text(q.title); Row {
        RadioButton(ans[q.id]==true, { ans[q.id]=true }); Text("Так")
        RadioButton(ans[q.id]==false, { ans[q.id]=false }); Text("Ні")
    }}
}

@Composable fun SingleChoiceBlock(q: Question, ans: MutableMap<String, Any>) {
    Column { Text(q.title)
        q.options.forEach { opt ->
            Row(Modifier.clickable { ans[q.id]=opt }) {
                RadioButton(ans[q.id]==opt, { ans[q.id]=opt })
                Text(opt)
            }
        }
    }
}

/* MultiChoice – чекбокси з обмеженням maxSelect */
@Composable fun MultiChoiceBlock(q: Question, ans: MutableMap<String, Any>) {
    val sel = (ans[q.id] as? MutableSet<String>) ?: mutableSetOf()
    Column { Text(q.title)
        q.options.forEach { opt ->
            val checked = sel.contains(opt)
            Row(Modifier.clickable {
                if (checked) sel.remove(opt)
                else if (sel.size < q.maxSelect) sel += opt
                ans[q.id] = sel
            }) {
                Checkbox(checked, null); Text(opt)
            }
        }
        Text("${sel.size}/${q.maxSelect}")
    }
}

/* Ranked – перетягування (спрощено) */
@Composable fun RankedBlock(q: Question, ans: MutableMap<String, Any>) {
    val list = remember { ans[q.id] as? SnapshotStateList<String>
        ?: q.options.toMutableStateList().also { ans[q.id]=it } }
    Column { Text(q.title)
        list.forEachIndexed { idx,opt ->
            Row { Text("${idx+1}. $opt") /* drag handles TODO */ }
        }
    }
}

