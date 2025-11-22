package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.jstahl.clarifile.backend.Storage
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Preview
fun App(storage: Storage) {

    MaterialTheme {
        val selectedTags = remember { mutableStateListOf<String>() }

        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Clarifile") })
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .safeContentPadding()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.Start,
            ) {
                Button(onClick = {
                    scope.launch {
                        storage.addFile("~/geheimedokumente.txb")
                    }
                }) {
                    Text("Add file")
                }
                Column(modifier =
                    Modifier.padding(16.dp)
                        .fillMaxWidth()) {
                    // Single text field tag selector with chips inside
                    TagSelector(
                        storage,
                        selectedTags = selectedTags,
                        onAddTag = { tag -> if (tag !in selectedTags) selectedTags.add(tag) },
                        onRemoveTag = { tag -> selectedTags.remove(tag) }
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}