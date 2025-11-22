package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import eu.jstahl.clarifile.backend.File
import eu.jstahl.clarifile.backend.GeoLocation
import eu.jstahl.clarifile.backend.Storage
import kotlinx.coroutines.launch
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MapScreen(storage: Storage, onEditFile: (File) -> Unit) {
    var selectedLocation by remember { mutableStateOf<GeoLocation?>(null) }
    var radiusKm by remember { mutableStateOf(50f) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenHorizontalPadding, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Tap on the map to select a location",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Simple world map visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE3F2FD))
        ) {
            WorldMapCanvas(
                modifier = Modifier.fillMaxSize(),
                onLocationSelected = { location ->
                    selectedLocation = location
                }
            )
            
            // Show selected location marker
            selectedLocation?.let { location ->
                // Convert lat/lon to canvas coordinates
                val x = ((location.longitude + 180) / 360).toFloat()
                val y = ((90 - location.latitude) / 180).toFloat()
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = (x * 300.dp).coerceIn(0.dp, 300.dp),
                            top = (y * 300.dp).coerceIn(0.dp, 300.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color.Red, CircleShape)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Radius slider
        Text(
            "Search radius: ${radiusKm.toInt()} km",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Slider(
            value = radiusKm,
            onValueChange = { radiusKm = it },
            valueRange = 1f..500f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display files in selected area
        selectedLocation?.let { location ->
            val filesInArea by produceState<List<File>>(initialValue = emptyList(), location, radiusKm) {
                value = getFilesInRadius(storage, location, radiusKm.toDouble())
            }
            
            Text(
                "Files in selected area: ${filesInArea.size}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                for (file in filesInArea) {
                    FileListItem(file = file, onEdit = { onEditFile(file) })
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun WorldMapCanvas(
    modifier: Modifier = Modifier,
    onLocationSelected: (GeoLocation) -> Unit
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Convert canvas coordinates to lat/lon
                    val longitude = (offset.x / size.width) * 360 - 180
                    val latitude = 90 - (offset.y / size.height) * 180
                    onLocationSelected(GeoLocation(latitude, longitude))
                }
            }
    ) {
        // Draw simple world map background with grid
        val gridColor = Color.Gray.copy(alpha = 0.3f)
        
        // Draw vertical lines (longitude)
        for (i in 0..12) {
            val x = size.width * i / 12
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
        }
        
        // Draw horizontal lines (latitude)
        for (i in 0..6) {
            val y = size.height * i / 6
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }
        
        // Draw equator
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2f
        )
        
        // Draw prime meridian
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2f
        )
    }
}

@Composable
private fun FileListItem(file: File, onEdit: () -> Unit) {
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

/**
 * Retrieves all files from storage and filters them by distance from the center point
 */
private suspend fun getFilesInRadius(
    storage: Storage,
    center: GeoLocation,
    radiusKm: Double
): List<File> {
    // Get all files (we'll filter by location)
    val allFiles = mutableListOf<File>()
    storage.getFiles(eu.jstahl.clarifile.backend.FileRequest(emptyList(), eu.jstahl.clarifile.backend.LogicalOperator.And, ""))
        .collect { files ->
            allFiles.clear()
            allFiles.addAll(files)
        }
    
    // Filter files by distance
    return allFiles.filter { file ->
        val location = file.getGpsLocation()
        location != null && calculateDistance(center, location) <= radiusKm
    }
}

/**
 * Calculate distance between two points using Haversine formula
 * Returns distance in kilometers
 */
private fun calculateDistance(point1: GeoLocation, point2: GeoLocation): Double {
    val earthRadiusKm = 6371.0
    
    val lat1Rad = Math.toRadians(point1.latitude)
    val lat2Rad = Math.toRadians(point2.latitude)
    val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
    val deltaLonRad = Math.toRadians(point2.longitude - point1.longitude)
    
    val a = sin(deltaLatRad / 2).pow(2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(deltaLonRad / 2).pow(2)
    
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    
    return earthRadiusKm * c
}
