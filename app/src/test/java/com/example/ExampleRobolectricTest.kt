package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.SongRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sonora", appName)
    }

    @Test
    fun `repository loads curated songs and playlists`() {
        val repo = SongRepository()
        val songs = repo.songs.value
        val playlists = repo.playlists.value
        val moods = repo.moods.value

        assertTrue(songs.isNotEmpty())
        assertTrue(playlists.isNotEmpty())
        assertTrue(moods.isNotEmpty())

        val firstSong = songs.first()
        assertNotNull(firstSong.title)
        assertTrue(firstSong.lyrics.isNotEmpty())
    }
}
