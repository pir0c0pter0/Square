package dev.lelonio.square.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

internal suspend fun Context.openExternalMusic(
    service: ExternalMusicService,
    packages: List<String>,
    pause: suspend () -> Unit,
): Boolean = launchExternalMusic(service, packages, packageName, pause) { url, target ->
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setPackage(target))
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

/** Only general web browsers, never Square's own Spotify link handler. */
internal fun Context.musicBrowsers(): List<String> {
    // Discover independently of music domains: a default link handler can hide browsers.
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.invalid/"))
        .addCategory(Intent.CATEGORY_BROWSABLE)
    val browsers = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        .filter { it.handleAllWebDataURI && it.activityInfo.packageName != packageName }
        .map { it.activityInfo.packageName }
        .distinct()
    val preferred = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        ?.activityInfo?.packageName
    return browsers.sortedBy { it != preferred }
}
