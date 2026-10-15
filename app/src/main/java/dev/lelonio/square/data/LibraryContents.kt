package dev.lelonio.square.data

/** Independent service results combined without changing their identities. */
data class LibraryContents(
    val playlists: List<CatalogPlaylist> = emptyList(),
    val albums: List<CatalogPlaylist> = emptyList(),
    val artists: List<SearchItem> = emptyList(),
    val tracks: List<CatalogTrack> = emptyList(),
) {
    operator fun plus(other: LibraryContents) = LibraryContents(
        playlists = (playlists + other.playlists).distinctBy { it.uri },
        albums = (albums + other.albums).distinctBy { it.uri },
        artists = (artists + other.artists).distinctBy { it.uri },
        tracks = (tracks + other.tracks).distinctBy { it.uri },
    )
}
