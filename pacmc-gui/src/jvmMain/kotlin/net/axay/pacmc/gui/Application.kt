package net.axay.pacmc.gui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import compose.icons.TablerIcons
import compose.icons.tablericons.Download
import kotlinx.coroutines.launch
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.repoapi.RepositoryApi
import net.axay.pacmc.app.repoapi.model.CommonProjectInfo
import net.axay.pacmc.gui.cache.producePainterCached

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "pacmc",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
    ) {
        Column(
            Modifier.fillMaxSize().background(Color(232, 232, 232)),
        ) {
            val searchScope = rememberCoroutineScope()
            var searchTerm by remember { mutableStateOf("") }
            val searchResults = remember { mutableStateListOf<CommonProjectInfo>() }

            OutlinedTextField(
                searchTerm,
                onValueChange = {
                    searchTerm = it
                    searchScope.launch {
                        println("sending request")
                        val projects = RepositoryApi.search(it, Repository.MODRINTH)
                        if (searchTerm == it) {
                            searchResults.clear()
                            searchResults.addAll(projects)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )

            LazyVerticalGrid(
                cells = GridCells.Adaptive(500.dp),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
            ) {
                items(searchResults) { ProjectItem(it) }
            }
        }
    }
}

@Composable
fun ProjectItem(project: CommonProjectInfo, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(125.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(Modifier.fillMaxHeight(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProjectIconImage(
                project,
                Modifier.clip(RoundedCornerShape(8.dp)).fillMaxHeight().aspectRatio(1f).align(Alignment.CenterVertically)
            )
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Row {
                        Text(project.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("by ${project.author}", Modifier.align(Alignment.CenterVertically))
                    }
                    Text(project.description, maxLines = 2, overflow = TextOverflow.Ellipsis)
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

@Composable
fun ProjectIconImage(
    project: CommonProjectInfo,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val painter = producePainterCached(
        project.iconUrl?.ifEmpty { null } ?: "https://cdn.modrinth.com/placeholder.svg",
        project.id
    )

    if (painter != null) {
        Image(
            painter = painter!!,
            contentDescription = "Icon of ${project.name}",
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(modifier)
    }
}
