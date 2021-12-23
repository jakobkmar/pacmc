package net.axay.pacmc.gui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import compose.icons.TablerIcons
import compose.icons.tablericons.Download
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import kotlinx.coroutines.runBlocking
import net.axay.pacmc.repoapi.modrinth.ModrinthApi
import net.axay.pacmc.repoapi.modrinth.model.ProjectResult

private val mods = runBlocking {
    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
            })
        }
    }
    ModrinthApi(client).searchProjects("")!!.hits
}

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "pacmc",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
    ) {
        Surface(
            Modifier.fillMaxSize(),
            color = Color(232, 232, 232)
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
}

@Composable
fun ModItem(mod: ProjectResult, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black).fillMaxHeight().aspectRatio(1f).align(Alignment.CenterVertically))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Row {
                        Text(mod.title!!, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("by ${mod.author}", Modifier.align(Alignment.CenterVertically))
                    }
                    Text(mod.description!!)
                }

                Box {
                    Row(Modifier.background(Color(232, 232, 232), RoundedCornerShape(5.dp)).padding(4.dp)) {
                        Icon(TablerIcons.Download, "Download", Modifier.align(Alignment.CenterVertically))
                        Text("Install", Modifier.padding(5.dp))
                    }
                }
            }
        }
    }
}
