package dev.lelonio.square.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.SpotifyLogo
import com.adamglin.phosphoricons.regular.YoutubeLogo
import dev.lelonio.square.R
import dev.lelonio.square.backend.BackendId
import dev.lelonio.square.backend.backendIdOf
import dev.lelonio.square.ui.theme.InkDim

@Composable
fun SourceIcon(uri: String, modifier: Modifier = Modifier) {
    val source = backendIdOf(uri) ?: return
    Icon(
        if (source == BackendId.SPOTIFY) PhosphorIcons.Regular.SpotifyLogo else PhosphorIcons.Regular.YoutubeLogo,
        contentDescription = stringResource(if (source == BackendId.SPOTIFY) R.string.backend_spotify else R.string.backend_youtube),
        tint = InkDim,
        modifier = modifier.size(16.dp),
    )
}
