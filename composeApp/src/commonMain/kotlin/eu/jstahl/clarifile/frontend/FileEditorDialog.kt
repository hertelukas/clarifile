package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.jstahl.clarifile.backend.File
import eu.jstahl.clarifile.backend.Storage

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileEditorDialog(
    storage: Storage,
    file: File,
    onConfirm: (String, List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    val fileName by produceState(initialValue = "Loading...", file) {
        value = file.getName()
    }
    val fileTags by remember { file.getTags() }
        .collectAsState(initial = emptyList())

    var name by remember(fileName) { mutableStateOf(fileName) }
    val selectedTags = remember(fileTags) { mutableStateListOf<String>().apply { addAll(fileTags) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(name.trim(), selectedTags.toList())
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Edit file") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("File name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Use TagSelector to manage tags for this file. In this context we allow adding new tags.
                TagSelector(
                    storage = storage,
                    selectedTags = selectedTags,
                    onAddTag = { tag -> if (selectedTags.none { it.equals(tag, ignoreCase = true) }) selectedTags.add(tag) },
                    onRemoveTag = { tag -> selectedTags.remove(tag) },
                    allowFreeText = true,
                )
            }
        }
    )
}
