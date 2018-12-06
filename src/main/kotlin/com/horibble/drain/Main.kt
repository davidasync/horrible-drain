package com.horibble.drain

import com.horibble.drain.dataClass.Schedule
import com.horibble.drain.util.get
import com.horibble.drain.util.jsonWriter
import com.horrible.drain.query.initMongo
import com.horrible.drain.query.insertMovie
import com.horrible.drain.query.insertSchedule
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

fun getMovieListUrl(): Set<String> {
    val res = get(movieListDIr)
    val movieListSet = mutableSetOf<String>()

    res!!.select("div.ind-show > a").forEach {
        movieListSet.add(it.attr("href"))
    }

    return movieListSet
}

suspend fun insertMovieData() = coroutineScope {
    val movieLinks = getMovieListUrl()
    val availableProcessor = Runtime.getRuntime().availableProcessors()
    val problematicMovieUrls = mutableListOf<String>()

    movieLinks.chunked(availableProcessor).forEach { chunkedMovieLinks ->
        chunkedMovieLinks.map {
            async {
                try {
                    getMovieData(baseUrl + it)
                } catch (e: Exception) {
                    problematicMovieUrls.add(it)

                    null
                }
            }
        }.forEach {
            val movie = it.await()

            if (movie != null) {
                insertMovie(movie)
            }
        }
    }

    jsonWriter(problematicMovieUrls, "problematicMovieUrls")
}

suspend fun insertScheduleData() = coroutineScope {
    val scheduleDoc = get(scheduleUrl)!!

    scheduleDoc.select(".weekday").forEach { weekdayDoc ->
        val weekday = weekdayDoc.text()
        val table = weekdayDoc.nextElementSibling()

        table.select(".schedule-page-item").map { schdulePageItemDoc ->
            async {
                val name = schdulePageItemDoc.selectFirst("a").text()
                val (hour, minute) = schdulePageItemDoc.selectFirst(".schedule-time").text().split(":")
                val slug = slg.slugify(name)

                Schedule(
                    slug = slug,
                    weekDay = weekday,
                    hour = hour.toInt(),
                    minute = minute.toInt(),
                    onScheduled = true
                )
            }
        }.forEach {
            insertSchedule(it.await())
        }
    }
}

suspend fun main() = coroutineScope {
    initMongo()
    insertMovieData()
    insertScheduleData()
}
