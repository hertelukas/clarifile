package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import eu.jstahl.clarifile.backend.FileRequest
import eu.jstahl.clarifile.backend.LogicalOperator
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.backend.File
import eu.jstahl.clarifile.utils.getStoragePath
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
@Preview
fun App(storage: Storage) {
    val scope = rememberCoroutineScope()
    var editingFile by remember { mutableStateOf<File?>(null) }
    val filePicker = rememberFilePickerLauncher(
        type = PickerType.File()
    ) { file ->
        file?.let {
            scope.launch {
                editingFile = storage.addFile(it.getStoragePath())
            }
        }
    }

    MaterialTheme {
        var searchName by remember { mutableStateOf("") }
        val selectedTags = remember { mutableStateListOf<String>() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Button(
                modifier = Modifier,
                onClick = {
                    filePicker.launch()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Add file")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name")},
                        placeholder = { Text("Search for name") },
                        value = searchName,
                        singleLine = true,
                        onValueChange = { value ->
                            searchName = value
                        }
                    )
                    TagSelector(
                        storage,
                        selectedTags = selectedTags,
                        onAddTag = { tag -> if (tag !in selectedTags) selectedTags.add(tag) },
                        onRemoveTag = { tag -> selectedTags.remove(tag) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                val files by remember(selectedTags.toList(), searchName) {
                    storage.getFiles(FileRequest(
                        selectedTags,
                        LogicalOperator.And,
                        searchName
                    )) }
                    .collectAsState(initial = emptyList())

                for (file in files) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = Color.LightGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                file.open()
                            },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            val fileName by produceState(initialValue = "Loading...", file) {
                                value = file.getName()
                            }
                            val fileTags by remember(file.getTags()) { file.getTags() }
                                .collectAsState(initial = emptyList())
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fileName, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    editingFile = file
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                            }
                            if (fileTags.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    fileTags.forEach { tag ->
                                        LabelChip(text = tag)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
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
}