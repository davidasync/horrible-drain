package com.horibble.drain.util

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun get(url: String): Document? {
    var error = true
    var doc: Document? = null
    var counter = 0

    while (error && counter <= 10) {
        try {
            val connection = Jsoup
                .connect(url)
                .userAgent(getRandomUserAgent())
                .timeout(30 * 1000)
                .method(Connection.Method.GET)

            doc = connection.get()
            error = false
        } catch (e: Exception) {
            counter++
            println("Connection Error try again to scrape $url! $counter")
            error = true
        }
    }

    return doc
}
