package com.horibble.drain.util

import com.google.gson.GsonBuilder
import java.io.PrintWriter

fun jsonWriter(jsonObject: Any?, filename: String) {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val jsonArray = gson.toJson(jsonObject)
    val writer = PrintWriter("data/$filename.json", "UTF-8")

    writer.println(jsonArray)
    writer.close()
}