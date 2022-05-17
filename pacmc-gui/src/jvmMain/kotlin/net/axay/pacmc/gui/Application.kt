package net.axay.pacmc.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.axay.pacmc.app.CommonApplication
import net.axay.pacmc.app.database.realm
import net.axay.pacmc.gui.screens.ArchiveScreen
import net.axay.pacmc.gui.screens.NewsScreen
import net.axay.pacmc.gui.screens.SearchScreen

private enum class Screen(
    val displayName: String,
    val icon: ImageVector,
) {
    SEARCH("Search", TablerIcons.Package),
    INSTANCE("Instances", TablerIcons.Stack),
    ARCHIVE("Archives", TablerIcons.Archive),
    SERVER("Server", TablerIcons.Server),
    NEWS("News", TablerIcons.News);
}

fun main() {
    CommonApplication.init()

    CoroutineScope(Dispatchers.IO).launch { realm }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "pacmc",
            state = rememberWindowState(width = 1200.dp, height = 800.dp),
        ) {
            Row {
                var currentScreen by remember { mutableStateOf(Screen.SEARCH) }

                NavigationRail(
                    containerColor = Color.Unspecified,
                ) {
                    Screen.values().forEach {
                        NavigationRailItem(
                            currentScreen == it,
                            onClick = { currentScreen = it },
                            icon = { Icon(it.icon, it.displayName) },
                            label = { Text(it.displayName) }
                        )
                    }
                }

                Box {
                    when (currentScreen) {
                        Screen.SEARCH -> SearchScreen()
                        Screen.ARCHIVE -> ArchiveScreen()
                        Screen.NEWS -> NewsScreen()
                    }
                }
            }
        }
    }
}
