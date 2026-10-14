package dev.lelonio.square.ui.settings

internal enum class ExternalMusicService(val url: String, val packageName: String) {
    SPOTIFY("https://open.spotify.com/", "com.spotify.music"),
    YOUTUBE_MUSIC("https://music.youtube.com/", "com.google.android.apps.youtube.music"),
}

internal suspend fun launchExternalMusic(
    service: ExternalMusicService,
    packages: List<String>,
    ownPackage: String,
    pause: suspend () -> Unit,
    launch: (String, String) -> Boolean,
): Boolean {
    val destinations = packages.filter { it.isNotBlank() && it != ownPackage }.distinct()
    if (destinations.isEmpty()) return false
    pause()
    return destinations.any { launch(service.url, it) }
}
