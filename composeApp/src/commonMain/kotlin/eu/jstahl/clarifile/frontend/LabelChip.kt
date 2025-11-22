package eu.jstahl.clarifile.frontend

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@Composable
fun LabelChip(
    text: String,
    onClick: (() -> Unit)? = null,
    removable: Boolean = false,
    onRemove: (() -> Unit)? = null,
) {
    val bg = colorForLabel(text)
    val fg = contentColorFor(bg)

    AssistChip(
        onClick = {
            if (removable) {
                // Prefer explicit onRemove when chip is marked removable
                onRemove?.invoke() ?: onClick?.invoke()
            } else {
                onClick?.invoke()
            }
        },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        trailingIcon = if (removable) ({ Text("×") }) else null,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bg,
            labelColor = fg
        )
    )
}

// Stable color generation for a given label
private fun colorForLabel(name: String): Color {
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
