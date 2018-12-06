package com.horibble.drain.dataClass

data class Schedule(
    val slug: String,
    val weekDay: String,
    val hour: Int,
    val minute: Int,

    var onScheduled: Boolean
)