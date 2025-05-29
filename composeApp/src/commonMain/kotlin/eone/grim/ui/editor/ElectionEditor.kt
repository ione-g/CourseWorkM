package eone.grim.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import eone.grim.ElectionPart.ElectionsRepo
import eone.grim.ElectionPart.model.Question
import eone.grim.ElectionPart.model.Question.Type
import kotlinx.coroutines.launch
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectionEditor(
    eid: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val repo = remember { ElectionsRepo() }
    val questions by repo.questionsFlow(eid).collectAsState(initial = emptyList())
    val election by repo.electionById(eid).collectAsState(null)
    var showDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editingIndex = null; showDialog = true }) {
                Icon(Icons.Default.Add, null)
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title= { election?.let{Text("Editing ${it.title}")} },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            repo.saveAll(eid, questions.withIndex().map { (i, q) -> q.copy(order = i) })
                        }
                    }) { Icon(Icons.Default.Save, "Save") }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                itemsIndexed(questions, key = { _, q -> q.id }) { idx, q ->
                    QuestionRow(
                        question = q,
                        onEdit = { editingIndex = idx; showDialog = true },
                        onDelete = {
                            scope.launch {
                                val updated = questions.toMutableList().apply { removeAt(idx) }
                                repo.saveAll(eid, updated)
                            }
                        },
                        onMoveUp = {
                            if (idx > 0) scope.launch {
                                val updated = questions.toMutableList().apply { swap(idx, idx - 1) }
                                repo.saveAll(eid, updated)
                            }
                        },
                        onMoveDown = {
                            if (idx < questions.lastIndex) scope.launch {
                                val updated = questions.toMutableList().apply { swap(idx, idx + 1) }
                                repo.saveAll(eid, updated)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        val edit = editingIndex?.let { questions[it] }
        QuestionDialog(
            original = edit,
            onDismiss = { showDialog = false },
            onConfirm = { q ->
                scope.launch {
                    val updated = questions.toMutableList().apply {
                        if (edit == null) add(q.copy(id = q.id.ifBlank { randomId() }))
                        else set(editingIndex!!, q)
                    }
                    repo.saveAll(eid, updated)
                    showDialog = false
                }
            }
        )
    }
}
@Composable
private fun QuestionRow(
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DragHandle, null)
            Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(question.titleOrFallback(), style = MaterialTheme.typography.titleMedium)
                Text(question.type.name, style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onMoveUp) { Text("↑") }
            IconButton(onClick = onMoveDown) { Text("↓") }
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionDialog(
    original: Question?,
    onDismiss: () -> Unit,
    onConfirm: (Question) -> Unit,
) {
    var title by remember { mutableStateOf(original?.titleOrFallback() ?: "") }
    var type by remember { mutableStateOf(original?.type ?: Type.SINGLE) }
    var options by remember { mutableStateOf(original?.options?.joinToString("\n") ?: "") }
    var maxSelect by remember { mutableStateOf(original?.maxSelect?.toString() ?: "1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val list = if (type == Type.YES_NO) listOf("Yes", "No")
                else options.lines().filter { it.isNotBlank() }
                onConfirm(
                    Question(
                        id = original?.id ?: randomId(),
                        order = original?.order ?: 0,
                        title = title,
                        type = type,
                        options = list,
                        maxSelect = maxSelect.toIntOrNull() ?: 1
                    )
                )
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(if (original == null) "New question" else "Edit question") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Prompt") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                // ---------- Type picker (Material3 exposed) ----------
                ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        readOnly = true,
                        value = type.name,
                        onValueChange = {},
                        label = { Text("Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth().clickable { expanded = true }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        Type.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.name) },
                                onClick = {
                                    type = t
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (type != Type.YES_NO) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = options,
                        onValueChange = { options = it },
                        label = { Text("Options (one per line)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                }
                if (type == Type.MULTI) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = maxSelect,
                        onValueChange = { maxSelect = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Max selections") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    )
}

private fun MutableList<Question>.swap(i: Int, j: Int) { val tmp = this[i]; this[i] = this[j]; this[j] = tmp }
private fun randomId(): String = Random.nextBytes(6).joinToString("") { "%02x".format(it) }
private fun Question.titleOrFallback() = title.ifBlank {
    when (type) {
        Type.YES_NO -> "Yes / No"
        Type.SINGLE -> "Single choice"
        Type.MULTI -> "Multiple choice"
        Type.RANKED -> "Ranked choice"
    }
}
