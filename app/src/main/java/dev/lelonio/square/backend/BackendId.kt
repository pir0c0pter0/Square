package dev.lelonio.square.backend

/** Which service owns catalogue items and their playback. */
enum class BackendId { SPOTIFY, YOUTUBE_MUSIC }

/** The item's identity decides its source; local files have no remote source. */
fun backendIdOf(uri: String?): BackendId? = when {
    uri?.startsWith("spotify:") == true && !uri.startsWith("spotify:local:") -> BackendId.SPOTIFY
    uri?.startsWith("ytmusic:") == true -> BackendId.YOUTUBE_MUSIC
    else -> null
}
