package dev.lelonio.square.ui.settings

import org.junit.Assert.*
import org.junit.Test
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.yield

class ExternalMusicTest {
    @Test fun cancelledPauseNeverOpensExternalApp() = runBlocking {
        val acknowledged = CompletableDeferred<Unit>()
        var opened = false
        val task = async {
            launchExternalMusic(
                ExternalMusicService.SPOTIFY, listOf("com.spotify.music"), "dev.lelonio.square",
                pause = { acknowledged.await() },
                launch = { _, _ -> opened = true; true },
            )
        }
        yield()
        task.cancel()
        acknowledged.complete(Unit)
        task.join()
        assertFalse(opened)
    }

    @Test fun waitsForPauseAcknowledgementBeforeLeaving() = runBlocking {
        val acknowledged = CompletableDeferred<Unit>()
        var opened = false
        val task = async {
            launchExternalMusic(
                ExternalMusicService.SPOTIFY, listOf("com.spotify.music"), "dev.lelonio.square",
                pause = { acknowledged.await() },
                launch = { _, _ -> opened = true; true },
            )
        }
        yield()
        assertFalse(opened)
        acknowledged.complete(Unit)
        assertTrue(task.await())
        assertTrue(opened)
    }

    @Test fun pausesBeforeOpeningTheOfficialApp() = runBlocking {
        val events = mutableListOf<String>()
        val opened = launchExternalMusic(
            ExternalMusicService.SPOTIFY, listOf("com.spotify.music"), "dev.lelonio.square",
            pause = { events += "pause" },
            launch = { url, target -> events += "$target:$url"; true },
        )
        assertTrue(opened)
        assertEquals(listOf("pause", "com.spotify.music:https://open.spotify.com/"), events)
    }

    @Test fun youtubeOpensItsOwnAppAndUrl() = runBlocking {
        var destination = ""
        assertTrue(launchExternalMusic(
            ExternalMusicService.YOUTUBE_MUSIC,
            listOf(ExternalMusicService.YOUTUBE_MUSIC.packageName), "dev.lelonio.square", {},
        ) { url, target -> destination = "$target:$url"; true })
        assertEquals("com.google.android.apps.youtube.music:https://music.youtube.com/", destination)
    }

    @Test fun neverRoutesBackToSquareAndRetriesUnavailableBrowsers() = runBlocking {
        val targets = mutableListOf<String>()
        assertTrue(launchExternalMusic(
            ExternalMusicService.SPOTIFY,
            listOf("dev.lelonio.square", "browser.unavailable", "browser.available", "browser.unused"),
            "dev.lelonio.square", {},
        ) { _, target -> targets += target; target == "browser.available" })
        assertEquals(listOf("browser.unavailable", "browser.available"), targets)
    }

    @Test fun unavailableAppReturnsFalseWithoutImplicitFallback() = runBlocking {
        var attempts = 0
        assertFalse(launchExternalMusic(
            ExternalMusicService.SPOTIFY, listOf("com.spotify.music"), "dev.lelonio.square", {},
        ) { _, _ -> attempts++; false })
        assertEquals(1, attempts)
    }

    @Test fun noExternalDestinationDoesNotPauseOrLaunch() = runBlocking {
        assertFalse(launchExternalMusic(
            ExternalMusicService.SPOTIFY, listOf("dev.lelonio.square", ""), "dev.lelonio.square",
            pause = { fail("No destination: playback should stay unchanged") },
            launch = { _, _ -> fail("Must not open Square"); true },
        ))
    }

    @Test fun failedPausePreventsExternalLaunch() = runBlocking {
        try {
            launchExternalMusic(
                ExternalMusicService.SPOTIFY, listOf("com.spotify.music"), "dev.lelonio.square",
                pause = { throw IllegalStateException("Disconnected player") },
                launch = { _, _ -> fail("Must pause before leaving"); true },
            )
            fail("Expected pause failure")
        } catch (_: IllegalStateException) { }
    }
}
