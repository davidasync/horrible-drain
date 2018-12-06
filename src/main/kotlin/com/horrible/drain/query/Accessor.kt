package com.horrible.drain.query

import com.horibble.drain.dataClass.Movie
import com.horibble.drain.dataClass.Schedule
import com.mongodb.client.model.ReplaceOptions
import org.litote.kmongo.*
import java.util.*

val client = KMongo.createClient()
val downloadDirrectoryDb = client.getDatabase("download-directory")
val movieCol = downloadDirrectoryDb.getCollection<Movie>()
val scheduleCol = downloadDirrectoryDb.getCollection<Schedule>()

fun initMongo() {
    movieCol.ensureIndex(
        """
        {
            "slug": 1,
            "createdAt": 1,
        }
    """.trimIndent()
    )

    // Reset the onScheduled state
    val schedules = scheduleCol.find().toMutableList()
    schedules.forEach {
        it.onScheduled = false

        scheduleCol.updateOne(Schedule::slug eq it.slug, it)
    }
}

fun insertMovie(movie: Movie) {
    val insertedMovie = movieCol.findOne(Movie::slug eq movie.slug)
    val currMilli = Calendar.getInstance().timeInMillis

    // New movie
    if (insertedMovie == null) {
        movie.lastUpdated = currMilli
        movieCol.insertOne(movie)
    } else {
        // No updated data
        if (insertedMovie != movie) {
            movie.lastUpdated = currMilli

            movieCol.updateOne(Movie::slug eq movie.slug, movie)
        }

        // Else do nothing
    }
}

fun insertSchedule(schedule: Schedule) {
    val option = ReplaceOptions().upsert(true)

    scheduleCol.replaceOne(Movie::slug eq schedule.slug, schedule, option)
}