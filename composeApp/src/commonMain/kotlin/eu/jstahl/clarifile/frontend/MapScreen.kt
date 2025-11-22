package eu.jstahl.clarifile.frontend

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(storage: Storage, onEditFile: (File) -> Unit) {
    var selectedLocation by remember { mutableStateOf<GeoLocation?>(null) }
    var radiusKm by remember { mutableStateOf(50f) }
    
    // Collect all files with GPS locations once
    val allFilesWithLocations by produceState<List<Pair<File, GeoLocation>>>(initialValue = emptyList()) {
        val files = mutableListOf<File>()
        storage.getFiles(eu.jstahl.clarifile.backend.FileRequest(emptyList(), eu.jstahl.clarifile.backend.LogicalOperator.And, ""))
            .collect { fileList ->
                files.clear()
                files.addAll(fileList)
            }
        value = files.mapNotNull { file ->
            file.getGpsLocation()?.let { location -> file to location }
        }
    }
    
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
                selectedLocation = selectedLocation,
                fileLocations = allFilesWithLocations.map { it.second },
                radiusKm = if (selectedLocation != null) radiusKm.toDouble() else null,
                onLocationSelected = { location ->
                    selectedLocation = location
                }
            )
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
            val filesInArea = remember(location, radiusKm, allFilesWithLocations) {
                allFilesWithLocations.filter { (_, fileLocation) ->
                    calculateDistance(location, fileLocation) <= radiusKm.toDouble()
                }.map { it.first }
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
    selectedLocation: GeoLocation?,
    fileLocations: List<GeoLocation>,
    radiusKm: Double?,
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
        
        // Draw file locations as small dots
        fileLocations.forEach { location ->
            val x = ((location.longitude + 180) / 360) * size.width
            val y = ((90 - location.latitude) / 180) * size.height
            
            drawCircle(
                color = Color.Blue,
                radius = 4f,
                center = Offset(x.toFloat(), y.toFloat())
            )
        }
        
        // Draw radius circle if location is selected
        if (selectedLocation != null && radiusKm != null) {
            val centerX = ((selectedLocation.longitude + 180) / 360) * size.width
            val centerY = ((90 - selectedLocation.latitude) / 180) * size.height
            
            // NOTE: This is a simplified approximation for visualization purposes.
            // The circle appears more distorted at higher latitudes due to the Mercator-like
            // projection. The actual distance calculation (Haversine) remains accurate.
            // At equator: 1 degree longitude ≈ 111 km
            val radiusInDegrees = radiusKm / 111.0
            val radiusInPixels = (radiusInDegrees / 360.0 * size.width).toFloat()
            
            // Draw radius circle
            drawCircle(
                color = Color.Red.copy(alpha = 0.2f),
                radius = radiusInPixels,
                center = Offset(centerX.toFloat(), centerY.toFloat())
            )
            
            // Draw center marker
            drawCircle(
                color = Color.Red,
                radius = 8f,
                center = Offset(centerX.toFloat(), centerY.toFloat())
            )
        }
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
