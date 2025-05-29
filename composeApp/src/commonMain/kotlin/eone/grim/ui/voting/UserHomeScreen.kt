package eone.grim.ui.voting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import eone.grim.ElectionPart.ElectionsRepo

@Composable
fun UserHomeScreen(
    repo: ElectionsRepo,
    onVote: (String) -> Unit
) {
    val elections by repo.activeElectionsFlow.collectAsState(emptyList())

    Scaffold { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad)) {
            items(elections, key = { it.id }) { e ->
                ListItem(
                    headlineContent = { Text(e.title) },
                    supportingContent = {
                        Text(if (e.active) "Active" else "Closed")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVote(e.id) }      // ← навігація
                )
                HorizontalDivider()
            }
        }
    }
}