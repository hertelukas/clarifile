package eu.jstahl.clarifile.frontend

import androidx.compose.ui.unit.Dp

// Platform-specific horizontal padding for the screen.
// Android uses a bit less to satisfy the requirement to reduce left/right padding.
expect val ScreenHorizontalPadding: Dp
