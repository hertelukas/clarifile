package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import eu.jstahl.clarifile.backend.FileRequest
import eu.jstahl.clarifile.backend.LogicalOperator
import eu.jstahl.clarifile.backend.Storage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelector(
    storage: Storage,
    selectedTags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit = {},
    allowFreeText: Boolean = false,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var tagInput by remember { mutableStateOf("") }

    val allTags by remember { storage.getTags() }.collectAsState(initial = emptyList())
    val availableTags = remember(allTags, selectedTags) {
        allTags.filter { existing ->
            selectedTags.none { sel -> sel.equals(existing, ignoreCase = true) }
        }
    }

    fun tryAddFromInput() {
        val input = tagInput.trim()
        if (input.isEmpty()) return
        val exactExisting = availableTags.firstOrNull { it.equals(input, ignoreCase = true) }
        val toAdd = when {
            exactExisting != null -> exactExisting
            allowFreeText -> input
            else -> null
        }
        toAdd?.let {
            onAddTag(it)
            tagInput = ""
            dropdownExpanded = false
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Decide where to show the hint: inline (placeholder) when there are no chips
        // or when the user is typing; otherwise show it as supporting text underneath
        val showInlinePlaceholder = selectedTags.isEmpty() || tagInput.isNotEmpty()

        OutlinedTextField(
            label = { Text("Tags") },
            value = tagInput,
            onValueChange = { value ->
                tagInput = value
                dropdownExpanded = true
            },
            singleLine = true,
            placeholder = if (showInlinePlaceholder) ({ Text("Search tags") }) else null,
            supportingText = if (!showInlinePlaceholder) ({ Text("Search tags") }) else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                val filtered = if (tagInput.isBlank()) availableTags else availableTags.filter {
                    it.contains(tagInput, ignoreCase = true)
                }
                if (dropdownExpanded && filtered.isNotEmpty()) {
                    // keep menu open; choose explicitly or add current token if allowed
                    if (allowFreeText) {
                        tryAddFromInput()
                    }
                } else {
                    tryAddFromInput()
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
                    dropdownExpanded = if (state.hasFocus) true else dropdownExpanded
                },
            prefix = {
                // Render selected tags as removable chips inside the text field
                Row(
                    // keep chips in a single row to honor singleLine
                ) {
                    selectedTags.forEach { tag ->
                        AssistChip(
                            onClick = { onRemoveTag(tag) },
                            label = { Text(tag) },
                            trailingIcon = { Text("×") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        )

        val filtered = if (tagInput.isBlank()) availableTags else availableTags.filter {
            it.contains(tagInput, ignoreCase = true)
        }
        val hasSuggestions = filtered.isNotEmpty()

        DropdownMenu(
            expanded = dropdownExpanded && (hasSuggestions || (allowFreeText && tagInput.isNotBlank())),
            onDismissRequest = { dropdownExpanded = false },
            properties = PopupProperties(focusable = false),
        ) {
            if (!allowFreeText && availableTags.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No more tags") },
                    onClick = { dropdownExpanded = false }
                )
            } else if (filtered.isEmpty()) {
                if (allowFreeText) {
                    val toAdd = tagInput.trim()
                    if (toAdd.isNotEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Add '$toAdd'") },
                            onClick = {
                                onAddTag(toAdd)
                                tagInput = ""
                                dropdownExpanded = false
                            }
                        )
                    }
                } else {
                    DropdownMenuItem(
                        text = { Text("No matches") },
                        onClick = { }
                    )
                }
            } else {
                filtered.forEachIndexed { index, tag ->
                    DropdownMenuItem(
                        text = { Text(tag) },
                        onClick = {
                            onAddTag(tag)
                            tagInput = ""
                            dropdownExpanded = false
                        }
                    )
                }
                if (allowFreeText) {
                    val toAdd = tagInput.trim()
                    if (toAdd.isNotEmpty() && filtered.none { it.equals(toAdd, ignoreCase = true) }) {
                        DropdownMenuItem(
                            text = { Text("Add '$toAdd'") },
                            onClick = {
                                onAddTag(toAdd)
                                tagInput = ""
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
