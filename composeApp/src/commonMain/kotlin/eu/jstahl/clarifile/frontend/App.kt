package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.backend.File
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(storage: Storage) {
    val scope = rememberCoroutineScope()
    var editingFile by remember { mutableStateOf<File?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf("Files", "Map")

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTabIndex) {
                0 -> FilesScreen(
                    storage = storage,
                    onEditFile = { file -> editingFile = file }
                )
                1 -> MapScreen(
                    storage = storage,
                    onEditFile = { file -> editingFile = file }
                )
            }
            
            if (editingFile != null) {
                FileEditorDialog(
                    storage = storage,
                    file = editingFile!!,
                    onConfirm = { newName, tags ->
                        scope.launch {
                            editingFile?.setName(newName)
                            editingFile?.setTags(tags)
                            editingFile = null
                        }
                    },
                    onDismiss = { editingFile = null }
                )
            }
        }
    }
}