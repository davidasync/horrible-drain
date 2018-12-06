package com.horibble.drain

import com.horibble.drain.dataClass.BatchEpisode
import com.horibble.drain.dataClass.Episode
import com.horibble.drain.dataClass.Movie
import com.horibble.drain.dataClass.Provider
import com.horibble.drain.util.get
import org.jsoup.nodes.Element
import java.util.*

private fun getMovieId(movieDetailDoc: Element): String? {
    var movieId: String? = null
    val elements = movieDetailDoc.getElementsByTag("script")

    run breaker@{
        elements.forEach { element ->
            element.dataNodes().forEach { node ->
                val text = node.wholeData

                if (text.indexOf("hs_showid") >= 0) {
                    movieId = text.substring(
                        text.indexOf('=') + 2,
                        text.indexOf(';')
                    )

                    return@breaker
                }
            }
        }
    }

    return movieId
}

private fun extractProviderData(doc: Element): List<Provider> {
    val providers = mutableListOf<Provider>()

    doc.select("span.dl-type").forEach { dlTypeDoc ->
        val linkDoc = dlTypeDoc.selectFirst("a")

        if (linkDoc != null) {
            val providerName = linkDoc.text()
            val downloadLink = linkDoc.attr("href")
            val provider = Provider(providerName, downloadLink)
            providers.add(provider)
        }
    }

    return providers
}

private fun extractResolutionData(doc: Element): Map<String, List<Provider>> {
    val resolution = mutableMapOf<String, List<Provider>>()

    doc.select("div.rls-link").forEach { rlsLinkDoc ->
        val resolutionNumber = rlsLinkDoc.attr("id").split("-").last()

        resolution[resolutionNumber] = extractProviderData(rlsLinkDoc)
    }

    return resolution
}

private fun extractEpisodeData(episodesDoc: Element): List<Episode> {
    return episodesDoc.select("div.rls-info-container").map {
        val episodeAt = it.attr("id")
        val resolution = extractResolutionData(episodesDoc)

        Episode(
            episode = episodeAt,
            resolution = resolution
        )
    }
}

private fun getEpisodes(movieId: String): List<Episode> {
    val episodes = mutableListOf<Episode>()
    val initialEpisodeUrl = initialEpisodeUrlTemplate.format(movieId)
    val initialEpisodesDoc = get(initialEpisodeUrl)!!
    val initialEpisodeData = extractEpisodeData(initialEpisodesDoc)

    episodes.addAll(initialEpisodeData)

    var isDone = false
    var counter = 1

    while (!isDone) {
        val currMilli = Calendar.getInstance().timeInMillis
        val episodeUrl = episodeUrlTemplate.format(movieId, counter, currMilli)
        val episodeDoc = get(episodeUrl)!!

        println("getEpisodes $episodeUrl")
        counter += 1

        if (episodeDoc.text().trim() != "DONE") {
            val episodesData = extractEpisodeData(episodeDoc)
            episodes.addAll(episodesData)
        } else {
            isDone = true
        }
    }

    return episodes
}

private fun getBatchEpisode(movieId: String): List<BatchEpisode> {
    val batchEpisodes = mutableListOf<BatchEpisode>()
    val batchEpisodesUrl = batchUrlTemplate.format(movieId)
    val batchEpisodeDoc = get(batchEpisodesUrl)!!

    batchEpisodeDoc.select("div.rls-info-container").forEach {
        val (startEpisode, endEpisode) = it.attr("id").split("-")

        val resolutions = extractResolutionData(it)
        val batchEpisode = BatchEpisode(
            startEpisode = startEpisode,
            endEpisode = endEpisode,
            resolution = resolutions
        )

        batchEpisodes.add(batchEpisode)
    }

    return batchEpisodes
}

fun getMovieData(movieUrl: String): Movie {
    println("getMovieData $movieUrl")

    val res = get(movieUrl)!!
    val movieId = getMovieId(res)!!
    val name = res.selectFirst(".entry-title").text()!!
    val slug = slg.slugify(name)!!
    val desc = res.selectFirst("div.series-desc").text()!!
    val coverImageUrl = res.selectFirst("div.series-image > img").attr("src")!!

    val episodes = getEpisodes(movieId)
    val batchEpisodes = getBatchEpisode(movieId)

    return Movie(
        movieId = movieId,
        name = name,
        slug = slug,
        description = desc,
        coverImageUrl = baseUrl + coverImageUrl,
        source = movieUrl,
        episodes = episodes,
        batchEpisodes = batchEpisodes,
        lastUpdated = null
    )
}