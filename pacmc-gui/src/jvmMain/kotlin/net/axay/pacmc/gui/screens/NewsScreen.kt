package net.axay.pacmc.gui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
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
                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier.clickable {
                            currentArticle = searchResult.id
                        }
                    ) {
                        ArticleResult(searchResult)
                    }
                }
            }
        }
    } else {
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            contentAlignment = Alignment.TopCenter
        ) {
            ArticleView(currentArticle!!)
        }
    }
}

private const val headerImageRatio = 1170f / 500f

@OptIn(ExperimentalUnitApi::class)
@Composable
private fun ArticleResult(result: MinecraftArticle.SearchResult) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp).width(600.dp),
    ) {
        val imageUrl = result.headerImage ?: result.previewImage
        val resolvedImageUrl = remember(imageUrl) {
            (result.headerImage ?: result.previewImage)
                ?.let { if (it.startsWith("/content")) "https://www.minecraft.net${it}" else it }
        }

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

        Spacer(Modifier.height(5.dp))
        Text(
            result.title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = TextUnit(22f, TextUnitType.Sp),
            textAlign = TextAlign.Center,
        )
        result.description?.let { Text(it, textAlign = TextAlign.Center) }
    }
}

@Composable
private fun ArticleView(articleId: String) {
    val articleState by produceState<MinecraftArticle?>(null, key1 = articleId) {
        value = ktorClient.get("http://localhost:8080/news/minecraft/$articleId").body()
    }
    val article = articleState

    if (article != null) {
        val rootNode = remember { Json.decodeFromString<JsonMarkup.RootNode>(article.contentJson) }
        Box(Modifier.width(800.dp)) {
            SelectionContainer {
                JsonMarkup(rootNode)
            }
        }
    }
}
