package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import eu.jstahl.clarifile.backend.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    storage: Storage,
    selectedTags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit = {},
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }
    var highlightedIndex by remember { mutableStateOf(0) }

    val availableTags = storage.getTags().filter { existing ->
        selectedTags.none { it.equals(existing, ignoreCase = true) }
    }

    fun tryAddExactMatch() {
        val input = tagInput.trim()
        if (input.isEmpty()) return
        val exactExisting = availableTags.firstOrNull { it.equals(input, ignoreCase = true) }
        if (exactExisting != null) {
            onAddTag(exactExisting)
            tagInput = ""
            dropdownExpanded = false
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            label = { Text("Tags") },
            value = tagInput,
            onValueChange = { value ->
                tagInput = value
                dropdownExpanded = true
                highlightedIndex = 0
            },
            singleLine = false,
            placeholder = { Text("Search tags") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                // If dropdown is open and we have suggestions, prefer selecting the highlighted one
                val filtered = if (tagInput.isBlank()) availableTags else availableTags.filter {
                    it.contains(tagInput, ignoreCase = true)
                }
                if (dropdownExpanded && filtered.isNotEmpty()) {
                    val idx = highlightedIndex.coerceIn(0, filtered.lastIndex)
                    val toAdd = filtered[idx]
                    onAddTag(toAdd)
                    tagInput = ""
                    dropdownExpanded = false
                } else {
                    tryAddExactMatch()
                }
            }),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    dropdownExpanded = true
                }
                .onFocusChanged { state ->
                    if (state.isFocused) dropdownExpanded = true
                }
                .onPreviewKeyEvent { event: androidx.compose.ui.input.key.KeyEvent ->
                    // Handle keyboard navigation for the non-focusable dropdown
                    if (!dropdownExpanded) return@onPreviewKeyEvent false
                    val filtered = if (tagInput.isBlank()) availableTags else availableTags.filter {
                        it.contains(tagInput, ignoreCase = true)
                    }
                    if (filtered.isEmpty()) return@onPreviewKeyEvent false

                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.DirectionDown -> {
                                highlightedIndex = (highlightedIndex + 1) % filtered.size
                                true
                            }
                            Key.DirectionUp -> {
                                highlightedIndex = (highlightedIndex - 1 + filtered.size) % filtered.size
                                true
                            }
                            Key.Enter, Key.Tab -> {
                                val idx = highlightedIndex.coerceIn(0, filtered.lastIndex)
                                val toAdd = filtered[idx]
                                onAddTag(toAdd)
                                tagInput = ""
                                dropdownExpanded = false
                                true
                            }
                            Key.Escape -> {
                                dropdownExpanded = false
                                true
                            }
                            else -> false
                        }
                    } else false
                },
            prefix = {
                // Render selected tags as removable chips inside the text field
                androidx.compose.foundation.layout.Row(
                    // keep chips in a single row to honor singleLine
                ) {
                    selectedTags.forEach { tag ->
                        androidx.compose.material3.AssistChip(
                            onClick = { onRemoveTag(tag) },
                            label = { Text(tag) },
                            trailingIcon = { Text("×") }
                        )
                        // small spacer between chips
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        )

        val filtered = if (tagInput.isBlank()) availableTags else availableTags.filter {
            it.contains(tagInput, ignoreCase = true)
        }
        val hasSuggestions = filtered.isNotEmpty()
        // keep highlightedIndex within bounds when data changes
        if (hasSuggestions) {
            if (highlightedIndex !in filtered.indices) highlightedIndex = 0
        } else if (dropdownExpanded) {
            highlightedIndex = 0
        }

        DropdownMenu(
            expanded = dropdownExpanded && hasSuggestions,
            onDismissRequest = { dropdownExpanded = false },
            // Keep the text field focused while suggestions are shown so typing always works
            properties = PopupProperties(focusable = true),
        ) {
            if (availableTags.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No more tags") },
                    onClick = { dropdownExpanded = false }
                )
            } else if (filtered.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No matches") },
                    onClick = { }
                )
            } else {
                filtered.forEachIndexed { index, tag ->
                    val isHighlighted = index == highlightedIndex
                    DropdownMenuItem(
                        text = { Text(if (isHighlighted) "➤ $tag" else tag) },
                        modifier = if (isHighlighted) Modifier
                            .defaultMinSize(minHeight = 0.dp) // ensure modifier presence across platforms
                        else Modifier,
                        onClick = {
                            onAddTag(tag)
                            tagInput = ""
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}
