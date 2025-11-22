package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import eu.jstahl.clarifile.backend.FileRequest
import eu.jstahl.clarifile.backend.LogicalOperator
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.backend.File
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
                editingFile = storage.addFile(it.path!!)
            }
        }
    }

    MaterialTheme {
        var searchName by remember { mutableStateOf("") }
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
                Button(
                    modifier = Modifier
                            .padding(16.dp),
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
                Box(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .safeContentPadding()
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
                        Column(
                            modifier = Modifier.fillMaxWidth()
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
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .safeContentPadding()
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
                                .safeContentPadding()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    color = Color.LightGray,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .safeContentPadding(),
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
                                            val bg = colorForTag(tag)
                                            val fg = contentColorFor(bg)
                                            AssistChip(
                                                onClick = {},
                                                label = { Text(tag) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = bg,
                                                    labelColor = fg
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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
}

// Let's ignore weird AI slob as it's just for generating colors :)
private fun colorForTag(name: String): Color {
    fun fnv1a32(input: String): UInt {
        var hash = 2166136261u // FNV offset basis
        val prime = 16777619u  // FNV prime
        for (ch in input) {
            hash = hash xor ch.code.toUInt()
            hash *= prime
        }
        return hash
    }

    val normalized = name.trim().lowercase()
    val h = fnv1a32(normalized)

    val hue = (h and 0xFFFFu).toInt() % 360
    val satByte = ((h shr 16) and 0xFFu).toInt()
    val valByte = ((h shr 24) and 0xFFu).toInt()
    val saturation = 0.45f + (satByte / 255f) * 0.4f
    val value = 0.65f + (valByte / 255f) * 0.3f

    return Color.hsv(hue.toFloat(), saturation, value)
}

private fun contentColorFor(background: Color): Color {
    return if (background.luminance() < 0.5f) Color.White else Color.Black
}