package dev.lelonio.square.data

import dev.lelonio.square.backend.BackendId
import dev.lelonio.square.backend.backendIdOf
import org.junit.Assert.*
import org.junit.Test

class LibraryContentsTest {
    @Test fun keepsDifferentServicesEvenWithTheSameTitle() {
        val spotify = CatalogPlaylist("spotify:playlist:1", "Favourites")
        val youtube = CatalogPlaylist("ytmusic:playlist:1", "Favourites")
        val merged = LibraryContents(playlists = listOf(spotify)) + LibraryContents(playlists = listOf(youtube))
        assertEquals(listOf(spotify, youtube), merged.playlists)
    }

    @Test fun removesRepeatedIdsInEveryShelfAndPreservesOrder() {
        val playlist = CatalogPlaylist("spotify:playlist:1", "Playlist")
        val album = CatalogPlaylist("spotify:album:1", "Album")
        val artist = SearchItem("spotify:artist:1", "Artist", "", null)
        val track = CatalogTrack("spotify:track:1", "Song", "Artist")
        val source = LibraryContents(listOf(playlist, playlist), listOf(album), listOf(artist), listOf(track))
        val merged = source + source
        assertEquals(listOf(playlist), merged.playlists)
        assertEquals(listOf(album), merged.albums)
        assertEquals(listOf(artist), merged.artists)
        assertEquals(listOf(track), merged.tracks)
    }

    @Test fun missingSourceDoesNotEraseAvailableContent() {
        val track = CatalogTrack("ytmusic:track:1", "Song", "Artist")
        assertEquals(listOf(track), (LibraryContents(tracks = listOf(track)) + LibraryContents()).tracks)
    }

    @Test fun identifiesSourceForPlaylistsAndTracksWithoutGuessingLocalFiles() {
        assertEquals(BackendId.SPOTIFY, backendIdOf("spotify:playlist:1"))
        assertEquals(BackendId.SPOTIFY, backendIdOf("spotify:track:1"))
        assertEquals(BackendId.YOUTUBE_MUSIC, backendIdOf("ytmusic:playlist:1"))
        assertEquals(BackendId.YOUTUBE_MUSIC, backendIdOf("ytmusic:track:1"))
        assertNull(backendIdOf("spotify:local:artist:album:song:123"))
        assertNull(backendIdOf("local:file:1"))
        assertNull(backendIdOf(null))
    }
}
