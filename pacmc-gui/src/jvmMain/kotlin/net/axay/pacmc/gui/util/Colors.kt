package net.axay.pacmc.gui.util

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse

@Composable
fun currentBaseTextColor(): Color {
    return LocalTextStyle.current.color.takeOrElse { LocalContentColor.current }
}
