package com.mobility.mediastoreexam.ui.list

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobility.mediastoreexam.model.AudioItem
import com.mobility.mediastoreexam.model.AudioType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit


class AudioListViewModel(application: Application) : AndroidViewModel(application) {

    @Suppress("PrivatePropertyName")
    private val TAG = javaClass.simpleName

    private val _audios = MutableLiveData<List<AudioItem>>()
    val audios: LiveData<List<AudioItem>> = _audios

    private var mediaPlayer: MediaPlayer? = null

    fun loadAudios(audioType: AudioType) {
        viewModelScope.launch {
            val audioList = queryAudios(audioType)
            _audios.postValue(audioList)
        }
    }

    private suspend fun queryAudios(audioType: AudioType): List<AudioItem> {
        val audios = mutableListOf<AudioItem>()

        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DISPLAY_NAME,
            )

            val selection = if (audioType.selection.isNullOrEmpty()) {
                null
            } else {
                "${audioType.selection} != 0"
            }

            val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} asc"

            getApplication<Application>().contentResolver.query(
                audioType.uri,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                Log.i(TAG, "Found ${cursor.count} audios")
                while (cursor.moveToNext()) {

                    // Here we'll use the column indexs that we found above.
                    val id = cursor.getLong(idColumn)
                    val dateModified =
                        Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)))
                    val displayName = cursor.getString(displayNameColumn)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val audio = AudioItem(
                        id,
                        displayName,
                        dateModified,
                        contentUri
                    )
                    audios += audio

                    // For debugging, we'll output the image objects we create to logcat.
                    Log.v(TAG, "Added audio: $audio")
                }
            }
        }

        return audios
    }

    fun playAudio(audioItem: AudioItem) {
        releaseAudio()

        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
//            setDataSource(getApplication(), audioItem.contentUri)
            setMediaPlayerDataSource(getApplication(), this, audioItem.toString())
            prepare() // might take long! (for buffering, etc)
            start()
        }
    }

    fun releaseAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private var ringtone: Ringtone? = null

    fun playRingtone(audioItem: AudioItem) {
        releaseRingtone()

        ringtone = RingtoneManager.getRingtone(getApplication(), audioItem.contentUri).apply {
            play()
        }
    }

    fun releaseRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    @Throws(Exception::class)
    fun setMediaPlayerDataSource(
        context: Context,
        mp: MediaPlayer, fileInfo: String
    ) {
        var fileInfo = fileInfo
        if (fileInfo.startsWith("content://")) {
            try {
                val uri: Uri = Uri.parse(fileInfo)
                fileInfo = getRingtonePathFromContentUri(context, uri)
            } catch (e: Exception) {
            }
        }
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) try {
                setMediaPlayerDataSourcePreHoneyComb(context, mp, fileInfo)
            } catch (e: Exception) {
                setMediaPlayerDataSourcePostHoneyComb(context, mp, fileInfo)
            } else setMediaPlayerDataSourcePostHoneyComb(context, mp, fileInfo)
        } catch (e: Exception) {
            try {
                setMediaPlayerDataSourceUsingFileDescriptor(
                    context, mp,
                    fileInfo
                )
            } catch (ee: Exception) {
                val uri = getRingtoneUriFromPath(context, fileInfo)
                mp.reset()
                mp.setDataSource(uri)
            }
        }
    }

    @Throws(Exception::class)
    private fun setMediaPlayerDataSourcePreHoneyComb(
        context: Context,
        mp: MediaPlayer, fileInfo: String
    ) {
        mp.reset()
        mp.setDataSource(fileInfo)
    }

    @Throws(Exception::class)
    private fun setMediaPlayerDataSourcePostHoneyComb(
        context: Context,
        mp: MediaPlayer, fileInfo: String
    ) {
        mp.reset()
        mp.setDataSource(context, Uri.parse(Uri.encode(fileInfo)))
    }

    @Throws(Exception::class)
    private fun setMediaPlayerDataSourceUsingFileDescriptor(
        context: Context, mp: MediaPlayer, fileInfo: String
    ) {
        val file = File(fileInfo)
        val inputStream = FileInputStream(file)
        mp.reset()
        mp.setDataSource(inputStream.fd)
        inputStream.close()
    }

    private fun getRingtoneUriFromPath(context: Context, path: String): String {
        val ringtonesUri: Uri = MediaStore.Audio.Media.getContentUriForPath(path)!!
        val ringtoneCursor: Cursor = context.contentResolver.query(
            ringtonesUri, null,
            MediaStore.Audio.Media.DATA + "='" + path + "'", null, null
        )!!

        if (ringtoneCursor.moveToFirst()) {
            val id: Long = ringtoneCursor.getLong(
                ringtoneCursor
                    .getColumnIndex(MediaStore.Audio.Media._ID)
            )
            ringtoneCursor.close()
            return if (!ringtonesUri.toString().endsWith(id.toString())) {
                "$ringtonesUri/$id"
            } else {
                ringtonesUri.toString()
            }
        }

        return path
    }

    fun getRingtonePathFromContentUri(
        context: Context,
        contentUri: Uri
    ): String {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val ringtoneCursor: Cursor = context.contentResolver.query(
            contentUri,
            proj, null, null, null
        )!!
        ringtoneCursor.moveToFirst()
        val path: String = ringtoneCursor.getString(
            ringtoneCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        )
        ringtoneCursor.close()
        return path
    }
}