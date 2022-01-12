package net.axay.pacmc.server.feeds

import co.touchlab.kermit.Logger
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.axay.pacmc.server.database.db
import net.axay.pacmc.server.httpClient
import net.axay.pacmc.server.requestText
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MinecraftFeedHandler {
    companion object {
        const val FEED_URL = "https://www.minecraft.net/en-us/feeds/community-content/rss"
    }

    private val monitorScope = CoroutineScope(Dispatchers.Default)

    private val xmlMapper = XmlMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private val minecraftArticleRegex = Regex("""(http(s)?://(www.)?minecraft.net/)(.*)(/article/)""")

    private val log = Logger.withTag("Minecraft Feed")

    fun monitor() = monitorScope.launch {
        while (isActive) {
            httpClient.requestText(FEED_URL).onSuccess { response ->
                kotlin.runCatching {
                    xmlMapper.readValue<RssFeed>(response.body())
                }.onSuccess { rssFeed ->
                    for (item in rssFeed.channel.item) {
                        if (!minecraftArticleRegex.containsMatchIn(item.link)) {
                            log.w("Won't fetch ${item.link} because it does not match the article regex")
                            continue
                        }

                        if (db.minecraftFeed.countDocuments(MinecraftArticle::url eq item.link) > 0) {

                        } else {
                            log.i("Parsing ${item.link}")
                            val parseResult = parseHtmlArticle(item.link) ?: continue

                            db.minecraftFeed.insertOne(
                                MinecraftArticle(
                                    item.link.trim(),
                                    ZonedDateTime.parse(item.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                                        .toLocalDateTime().toKotlinLocalDateTime().toInstant(TimeZone.UTC),
                                    item.primaryTag.trim(),
                                    item.imageURL.trim(),
                                    parseResult.headerImage,
                                    item.title.trim(),
                                    item.description.trim(),
                                    parseResult.author,
                                    parseResult.content.html(),
                                    Json.encodeToString(HtmlMarkupParser.parse(parseResult.content))
                                )
                            )
                        }
                    }
                }.onFailure {
                    log.e("Failed to deserialize rss feed", it)
                }
            }.onFailure {
                log.e("Failed to request rss feed from $FEED_URL", it)
            }

            delay(1000 * 60 * 30)
        }
    }

    private suspend fun parseHtmlArticle(url: String): HtmlArticleResult? {
        val response = httpClient.requestText(url)
            .onFailure { log.e("Failed to request article $url", it) }
            .getOrElse { return null }

//        articleContent.select("img[src]").forEach {
//            val src = it.attr("src")
//            if (src.startsWith("/")) {
//                it.attr("src", "https://www.minecraft.net${src}")
//            }
//        }


        // possible for later:
        // category:        html.select(".article-category__text").firstOrNull()?.text()
        // title:           articleBody.selectFirst("h1")!!.text()
        // description:     articleBody.selectFirst(".lead")!!.text()

        return kotlin.runCatching {
            val html = Jsoup.parse(response.body())
            val articleBody = html.selectFirst(".article-body")!!
            val articleContent = articleBody.selectFirst(".aem-Grid")!!

            HtmlArticleResult(
                html.select(".article-head__image").firstOrNull()?.attr("src"),
                articleBody.select(".attribution__details").single().selectFirst("dd")!!.text(),
                articleContent
            )
        }.onFailure {
            log.e("Failed to parse $url (${it.message})", it)
        }.getOrNull()
    }

    fun updateContentJson() = monitorScope.launch {
        var count = 0
        db.minecraftFeed.find().toFlow().collect {
            db.minecraftFeed.updateOne(
                MinecraftArticle::url eq it.url,
                setValue(MinecraftArticle::contentJson, Json.encodeToString(HtmlMarkupParser.parse(Jsoup.parse(it.contentHtml))))
            )
            count++
        }
        log.i("Updated json of $count articles")
    }

    private data class RssFeed(val channel: Channel) {
        data class Channel(@JacksonXmlElementWrapper(useWrapping = false) val item: List<Item>) {
            data class Item(
                val title: String,
                val link: String,
                val description: String,
                val imageURL: String,
                val primaryTag: String,
                val pubDate: String,
            )
        }
    }

    private data class HtmlArticleResult(
        val headerImage: String?,
        val author: String,
        val content: Element,
    )
}
