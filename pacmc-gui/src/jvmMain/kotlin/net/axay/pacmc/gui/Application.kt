package net.axay.pacmc.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        var darkTheme by remember { mutableStateOf(false) }

        MaterialTheme(
            if (darkTheme) darkColorScheme() else lightColorScheme()
        ) {
            Window(
                onCloseRequest = ::exitApplication,
                title = "pacmc",
                state = rememberWindowState(width = 1200.dp, height = 800.dp),
            ) {
                Row {
                    var currentScreen by remember { mutableStateOf(Screen.SEARCH) }

                    NavigationRail {
                        Spacer(Modifier.height(5.dp))
                        remember { Screen.values() }.forEach {
                            NavigationRailItem(
                                currentScreen == it,
                                onClick = { currentScreen = it },
                                icon = { Icon(it.icon, it.displayName) },
                                label = { Text(it.displayName) }
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = { darkTheme = !darkTheme },
                        ) {
                            Icon(if (darkTheme) TablerIcons.Sun else TablerIcons.Moon, "toggle theme")
                        }
                    }

                    Surface(Modifier.fillMaxSize()) {
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
    }
}
