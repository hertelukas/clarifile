package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.jstahl.clarifile.backend.Storage
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Preview
fun App(storage: Storage) {
    val scope = rememberCoroutineScope()
    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File()
    ) { file ->
        file?.let {
            scope.launch {
                storage.addFile(it.path!!)
            }
        }
    }

    MaterialTheme {
        val selectedTags = remember { mutableStateListOf<String>() }


        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Clarifile") })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .safeContentPadding(),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Button(onClick = {
                            filePicker.launch()
                        }) {
                            Text("Add file")
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Single text field tag selector with chips inside
                            TagSelector(
                                storage,
                                selectedTags = selectedTags,
                                onAddTag = { tag -> if (tag !in selectedTags) selectedTags.add(tag) },
                                onRemoveTag = { tag -> selectedTags.remove(tag) }
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .safeContentPadding(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Selected tags: ${selectedTags.joinToString(", ")}")
                }
            }
        }
    }
}