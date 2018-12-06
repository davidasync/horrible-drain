package com.horibble.drain.dataClass

private data class MovieEssence(
    val movieId: String,
    val name: String,
    val slug: String,
    val description: String,
    val coverImageUrl: String,
    val source: String,
    val episodes: List<Episode>?,
    val batchEpisodes: List<BatchEpisode>?
) {
    constructor(movie: Movie) : this(
        movieId = movie.movieId,
        name = movie.name,
        slug = movie.slug,
        description = movie.description,
        coverImageUrl = movie.coverImageUrl,
        source = movie.source,
        episodes = movie.episodes,
        batchEpisodes = movie.batchEpisodes
    )
}

data class Movie(
    val movieId: String,
    val name: String,
    val slug: String,
    val description: String,
    val coverImageUrl: String,
    val source: String,
    val episodes: List<Episode>?,
    val batchEpisodes: List<BatchEpisode>?,

    var lastUpdated: Long?
) {
    override fun equals(other: Any?) = MovieEssence(this) == MovieEssence(other as Movie)
    override fun hashCode() = MovieEssence(this).hashCode()
    override fun toString() = MovieEssence(this).toString().replaceFirst("EssentialData", "Person")
}


data class Episode(
    val episode: String,
    val resolution: Map<String, List<Provider>>
)

data class BatchEpisode(
    val startEpisode: String,
    val endEpisode: String,
    val resolution: Map<String, List<Provider>>
)

data class Provider(
    val name: String,
    val link: String
)
