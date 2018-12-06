package com.horibble.drain

import com.github.slugify.Slugify
import com.google.gson.GsonBuilder

val slg = Slugify()
val gson = GsonBuilder().setPrettyPrinting().create()

const val baseUrl = "https://horriblesubs.info/"
const val listDir = "shows/"
const val movieListDIr = baseUrl + listDir
const val initialEpisodeUrlTemplate = baseUrl + "api.php?method=getshows&type=show&showid=%s"
const val episodeUrlTemplate = baseUrl + "api.php?method=getshows&type=show&showid=%s&nextid=%s&_=%s"
const val batchUrlTemplate = baseUrl + "api.php?method=getshows&type=batch&showid=%s"
const val scheduleUrl = baseUrl + "release-schedule/"