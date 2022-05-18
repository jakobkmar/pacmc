package net.axay.pacmc.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import co.touchlab.kermit.Logger
import compose.icons.TablerIcons
import compose.icons.tablericons.Download
import compose.icons.tablericons.MoodCry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.axay.pacmc.app.repoapi.model.CommonProjectResult
import net.axay.pacmc.app.repoapi.repoApiContext
import net.axay.pacmc.common.data.Repository
import net.axay.pacmc.gui.cache.producePainterCached
import net.axay.pacmc.gui.util.currentBaseTextColor
import net.axay.pacmc.repoapi.CachePolicy

private sealed interface SearchResponse

private class SearchResponseSuccess(
    val results: List<CommonProjectResult>,
) : SearchResponse

private class SearchResponseFailure(
    val reason: String,
    val fetchedTerm: String,
) : SearchResponse

@OptIn(ExperimentalUnitApi::class)
@Composable
fun SearchScreen() {
    val searchScope = rememberCoroutineScope()
    var searchTerm by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var searchResponse by remember { mutableStateOf<SearchResponse>(SearchResponseSuccess(emptyList())) }

    fun fetchResults(typing: Boolean) {
        val fetchTerm = searchTerm

        loading = true
        searchScope.launch {
            if (typing) {
                delay(200)
                if (searchTerm != fetchTerm) return@launch
            }

            val result = kotlin.runCatching {
                repoApiContext(CachePolicy.ONLY_FRESH) { it.search(fetchTerm, Repository.MODRINTH) }
            }
            if (searchTerm == fetchTerm) {
                loading = false
                result.onFailure { exc ->
                    Logger.w("Failed to fetch search results for '${fetchTerm}' (${exc.message})")
                    searchResponse = SearchResponseFailure((exc.message ?: "unknown reason") + " (${exc::class.simpleName})", fetchTerm)
                }.onSuccess {
                    searchResponse = SearchResponseSuccess(it)
                }
            }
        }
    }

    if (loading) {
        LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp))
    }

    Column {
        Row(
            Modifier
                .fillMaxWidth().padding(20.dp).height(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, "Search", Modifier.padding(start = 8.dp).padding(vertical = 4.dp))
            Box(contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    searchTerm,
                    onValueChange = {
                        searchTerm = it
                        fetchResults(true)
                    },
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp).padding(vertical = 4.dp),
                    textStyle = TextStyle(
                        fontSize = TextUnit(18f, TextUnitType.Sp),
                        color = currentBaseTextColor()
                    ),
                    maxLines = 1,
                )
                if (searchTerm.isEmpty()) {
                    Text("Search...", Modifier.padding(start = 4.dp))
                }
            }
        }

        val gridState = rememberLazyGridState()

        when (val currentResponse = searchResponse) {
            is SearchResponseSuccess -> {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(500.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp),
                ) {
                    items(currentResponse.results) {
                        ProjectItem(it)
                    }
                }
            }
            is SearchResponseFailure -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(TablerIcons.MoodCry, "Sad Face", Modifier.size(40.dp))
                    Spacer(Modifier.height(5.dp))
                    Text(
                        buildString {
                            appendLine("Did not receive a valid response for '${currentResponse.fetchedTerm}'")
                            appendLine("Reason: ${currentResponse.reason}")
                        },
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { fetchResults(false) },
                    ) {
                        Text("Try again")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectItem(project: CommonProjectResult, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.height(135.dp),
    ) {
        Row(Modifier.fillMaxHeight().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Text(project.description, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 0.1.sp)
                }

                ElevatedButton(
                    onClick = {},
                    modifier = Modifier.height(30.dp),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 3.dp, hoveredElevation = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 15.dp)
                    ) {
                        Icon(TablerIcons.Download, "Download", Modifier.size(20.dp).padding(end = 5.dp))
                        Text("Install")
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectIconImage(
    project: CommonProjectResult,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val painter = producePainterCached(
        project.iconUrl?.ifEmpty { null } ?: "https://cdn.modrinth.com/placeholder.svg",
        "project_icons",
        "${project.id.repository.shortForm}_${project.id.id}",
    )

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "Icon of ${project.name}",
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(modifier)
    }
}
