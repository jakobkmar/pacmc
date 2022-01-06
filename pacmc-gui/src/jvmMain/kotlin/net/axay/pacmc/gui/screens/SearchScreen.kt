package net.axay.pacmc.gui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import net.axay.pacmc.app.data.Repository
import net.axay.pacmc.app.repoapi.RepositoryApi
import net.axay.pacmc.app.repoapi.model.CommonProjectInfo
import net.axay.pacmc.gui.cache.producePainterCached

private sealed interface SearchResponse

private class SearchResponseSuccess(
    val results: List<CommonProjectInfo>,
) : SearchResponse

private class SearchResponseFailure(
    val reason: String,
) : SearchResponse

@OptIn(ExperimentalFoundationApi::class, ExperimentalUnitApi::class)
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
                RepositoryApi.search(fetchTerm, Repository.MODRINTH)
            }
            if (searchTerm == fetchTerm) {
                loading = false
                result.onFailure { exc ->
                    Logger.w("Failed to fetch search results for '${fetchTerm}' (${exc.message})")
                    searchResponse = SearchResponseFailure(exc.message ?: "unknown reason")
                }.onSuccess {
                    searchResponse = SearchResponseSuccess(it)
                }
            }
        }
    }

    if (loading) {
        LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp), color = Color.DarkGray, )
    }

    Column {
        Row(
            Modifier
                .fillMaxWidth().padding(20.dp).height(40.dp)
                .background(Color.White, RoundedCornerShape(10.dp)),
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
                    textStyle = TextStyle(fontSize = TextUnit(18f, TextUnitType.Sp)),
                    maxLines = 1,
                )
                if (searchTerm.isEmpty()) {
                    Text("Search...", Modifier.padding(start = 4.dp))
                }
            }
        }

        val listState = rememberLazyListState()

        when (val currentResponse = searchResponse) {
            is SearchResponseSuccess -> {
                LazyVerticalGrid(
                    state = listState,
                    cells = GridCells.Adaptive(500.dp),
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
                            appendLine("Did not receive a valid response for '${searchTerm}'")
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
            painter = painter,
            contentDescription = "Icon of ${project.name}",
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(modifier)
    }
}
