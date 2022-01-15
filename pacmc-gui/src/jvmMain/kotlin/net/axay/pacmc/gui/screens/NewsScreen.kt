package net.axay.pacmc.gui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.ktor.client.request.*
import net.axay.pacmc.app.ktorClient
import net.axay.pacmc.gui.cache.producePainterCached
import net.axay.pacmc.server.model.MinecraftArticle

@OptIn(ExperimentalFoundationApi::class, ExperimentalUnitApi::class)
@Composable
fun NewsScreen() {
    val news by produceState<List<MinecraftArticle.SearchResult>?>(null) {
        value = ktorClient.get("http://localhost:8080/news/minecraft")
    }

    val currentNews = news
    if (currentNews != null) {
        LazyVerticalGrid(
            cells = GridCells.Adaptive(500.dp),
        ) {
            items(currentNews) { searchResult ->
                Box(contentAlignment = Alignment.TopCenter) {
                    ArticleResult(searchResult)
                }
            }
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
                    Modifier.fillMaxWidth().aspectRatio(headerImageRatio).shadow(4.dp),
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
