package net.axay.pacmc.gui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Place
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

class Mod(
    val name: String,
    val author: String,
    val description: String,
    val installed: Boolean,
)

private val mods = listOf(
    Mod("Test Mod 1", "Peter", "Eine Mod die sehr coole Sachen zum Spiel hinzufügt", false),
    Mod("Test Mod 2", "Maria", "Eine Mod die sehr coole Sachen zum Spiel hinzufügt", false),
    Mod("Test Mod 3", "Julius", "Eine Mod die sehr coole Sachen zum Spiel hinzufügt", true),
    Mod("Test Mod 4", "Julius", "Eine Mod die sehr coole Sachen zum Spiel hinzufügt", false),
    Mod("Test Mod 5", "Golem", "Eine Mod die sehr coole Sachen zum Spiel hinzufügt", true),
)

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "pacmc",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        LazyVerticalGrid(
            cells = GridCells.Adaptive(350.dp),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(mods) { ModItem(it) }
        }
    }
}

@Composable
fun ModItem(mod: Mod, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Gray)
            .padding(10.dp)
    ) {
        Row(Modifier.fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black).size(74.dp).align(Alignment.CenterVertically))
            Column {
                Row {
                    Text(mod.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("by ${mod.author}", Modifier.align(Alignment.CenterVertically))
                }
                Text(mod.description)

                if (mod.installed) {
                    Box(
                        Modifier.background(Color.Green, RoundedCornerShape(5.dp))
                    ) {
                        Row {
                            Icon(Icons.Default.Check, "Installed", Modifier.align(Alignment.CenterVertically))
                            Text("Installed", Modifier.padding(5.dp))
                        }
                    }
                } else {
                    Box(
                        Modifier.background(Color.White, RoundedCornerShape(5.dp))
                    ) {
                        Row {
                            Icon(Icons.Default.Place, "Download", Modifier.align(Alignment.CenterVertically))
                            Text("Install", Modifier.padding(5.dp))
                        }
                    }
                }
            }
        }
    }
}
