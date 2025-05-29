package eone.grim.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eone.grim.ElectionPart.ElectionRow
import eone.grim.ElectionPart.ElectionsRepo
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    repo: ElectionsRepo,
    onOpenElection: (String) -> Unit,          // → ElectionEditorScreen
) {
    val scope = rememberCoroutineScope()
    val elections by repo.electionsFlow.collectAsState(emptyList())

    /* ────── UI state ────── */
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { pad ->
        if (elections.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), Alignment.Center) {
                Text("No elections yet. Tap + to create one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(elections, key = { it.id }) { e ->
                    ListItem(
                        headlineContent = { Text(e.title) },
                        supportingContent = {
                            Text(if (e.active) "Active" else "Closed")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenElection(e.id) }      // ← навігація
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    /* ────── Dialog “New election” ────── */
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { if (!busy) showDialog = false },
            confirmButton = {
                TextButton(
                    enabled = title.isNotBlank() && !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            try {
                                val id = repo.createElection(title.trim())
                                showDialog = false
                                title = ""
                                onOpenElection(id)                 // одразу в редактор
                            } catch (t: Throwable) {
                                error = t.message
                            } finally { busy = false }
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { if (!busy) showDialog = false }) {
                Text("Cancel")
            }},
            title = { Text("New election") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        )
    }
}