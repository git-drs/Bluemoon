package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Song
import com.example.ui.theme.EditorialDark
import com.example.ui.theme.EditorialIceBlue
import com.example.ui.theme.EditorialSurface

@Composable
fun SongArtwork(
    song: Song?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (song == null) {
        Box(
            modifier = modifier.background(EditorialSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MusicNote,
                contentDescription = null,
                tint = EditorialIceBlue.copy(alpha = 0.5f)
            )
        }
        return
    }

    val embeddedBitmap = remember(song.id, song.artworkBytes) {
        song.artworkBytes?.let { bytes ->
            try {
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)?.asImageBitmap()
            } catch (_: Throwable) {
                null
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when {
            embeddedBitmap != null -> {
                Image(
                    bitmap = embeddedBitmap,
                    contentDescription = song.title,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            song.artworkUri != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(song.artworkUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = song.title,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = song.artworkResId),
                    placeholder = painterResource(id = song.artworkResId)
                )
            }
            song.artworkResId != 0 -> {
                Image(
                    painter = painterResource(id = song.artworkResId),
                    contentDescription = song.title,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(song.primaryColor, song.secondaryColor)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = song.title,
                        tint = EditorialDark,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
