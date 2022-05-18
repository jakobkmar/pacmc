package net.axay.pacmc.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.gui.cache.producePainterCached
import net.axay.pacmc.gui.util.JsonMarkup
import net.axay.pacmc.server.model.JsonMarkup
import net.axay.pacmc.server.model.MinecraftArticle

@Composable
fun NewsScreen() {
    val news by produceState<List<MinecraftArticle.SearchResult>?>(null) {
        value = ktorClient.get("http://localhost:8080/news/minecraft").body()
    }

    var currentArticle by remember { mutableStateOf<String?>(null) }

    if (currentArticle == null) {
        val currentNews = news
        if (currentNews != null) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(500.dp),
            ) {
                items(currentNews) { searchResult ->
                    ArticleResult(
                        searchResult,
                        onClick = {
                            currentArticle = searchResult.id
                        }
                    )
                }
            }
        }
    } else {
        val articleState by produceState<MinecraftArticle?>(null, key1 = currentArticle) {
            value = ktorClient.get("http://localhost:8080/news/minecraft/$currentArticle").body()
        }
        val article = articleState

        if (article != null) {
            Column {
                SmallTopAppBar(
                    title = {
                        Row {
                            Text(article.title, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(5.dp))
                            Text("by ${article.author}")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { currentArticle = null }) {
                            Icon(Icons.Filled.ArrowBack, "go back")
                        }
                    }
                )

                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ArticleView(article)
                }
            }
        }
    }
}

private const val headerImageRatio = 1170f / 500f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleResult(
    result: MinecraftArticle.SearchResult,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(10.dp),
        border = ButtonDefaults.outlinedButtonBorder,
    ) {
        val imageUrl = result.headerImage ?: result.previewImage
        val resolvedImageUrl = remember(imageUrl) {
            (result.headerImage ?: result.previewImage)
                ?.let { if (it.startsWith("/content")) "https://www.minecraft.net${it}" else it }
        }

        Box(modifier = Modifier.clip(RoundedCornerShape(12.0.dp))) {
            if (resolvedImageUrl != null) {
                val imagePainter = producePainterCached(
                    resolvedImageUrl,
                    "minecraft_net_content",
                    remember(resolvedImageUrl) { resolvedImageUrl.takeLastWhile { it != '/' } },
                    24 * 7 * 4
                )

                if (imagePainter != null) {
                    Image(
                        imagePainter,
                        result.title,
                        Modifier.fillMaxWidth().aspectRatio(headerImageRatio),
                        contentScale = ContentScale.FillWidth,
                    )
                } else {
                    Spacer(Modifier.fillMaxWidth().aspectRatio(headerImageRatio))
                }
            } else {
                Spacer(Modifier.fillMaxWidth().aspectRatio(headerImageRatio).background(Color.Black))
            }
        }

        Column(Modifier.padding(vertical = 15.dp, horizontal = 10.dp)) {
            Text(
                result.title,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                fontFamily = FontFamily.Default
            )
            Spacer(Modifier.height(8.dp))
            result.description?.let { Text(it) }
        }
    }
}

@Composable
private fun ArticleView(article: MinecraftArticle) {
    val rootNode = remember { Json.decodeFromString<JsonMarkup.RootNode>(article.contentJson) }
    Box(Modifier.width(800.dp)) {
        SelectionContainer {
            JsonMarkup(rootNode)
        }
    }
}
