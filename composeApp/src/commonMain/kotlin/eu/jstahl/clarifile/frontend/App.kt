package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.database.FileDao
import eu.jstahl.clarifile.database.FileEntity
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(storage: Storage) {

    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }

        val tags = storage.getTags()
        val tagStates = HashMap<String, MutableState<Boolean>>()
        tags.forEach { tag -> tagStates[tag] = remember { mutableStateOf(true) } }

        Scaffold(floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Text("Add")
            }
        }) { paddingValues ->
            FilterChip(
                onClick = { showContent = !showContent },
                label = { Text(if (showContent) "a" else "b") },
                selected = showContent
            )
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .safeContentPadding()
                    .fillMaxSize(),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Tags:")
                    for (tag in tags) {
                        var state by tagStates[tag]!!
                        FilterChip(
                            onClick = { state = !state },
                            label = { Text(tag) },
                            selected = state
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}