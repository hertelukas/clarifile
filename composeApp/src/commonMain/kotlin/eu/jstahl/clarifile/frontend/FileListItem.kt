package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.jstahl.clarifile.backend.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileListItem(file: File, onEdit: () -> Unit) {
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
            val fileExtension by produceState(initialValue = "Loading...", file) {
                value = file.getExtension()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(fileName, modifier = Modifier.weight(1f))
                Text(".$fileExtension")
                IconButton(onClick = onEdit) {
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
}
