package com.mobility.mediastoreexam.ui.list

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
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


class AudioListViewModel(application: Application) : AndroidViewModel(application) {

    @Suppress("PrivatePropertyName")
    private val TAG = javaClass.simpleName

    private val _audios = MutableLiveData<List<AudioItem>>()
    val audios: LiveData<List<AudioItem>> = _audios

    private var mediaPlayer: MediaPlayer? = null

    fun loadAudios(audioType: AudioType) {
        viewModelScope.launch {
            val audioList = when (audioType) {
                AudioType.Ringtone -> {
                    fetchSoundsFromRingtoneManager(getApplication(), RingtoneManager.TYPE_RINGTONE)
                }
                AudioType.Notification -> {
                    fetchSoundsFromRingtoneManager(
                        getApplication(),
                        RingtoneManager.TYPE_NOTIFICATION
                    )
                }
                else -> {
                    fetchSoundsFromContentResolver(audioType)
                }
            }
            _audios.postValue(audioList)
        }
    }

    /**
     * @param context [Context]
     * @param type    <br></br>
     * RingtoneManager.TYPE_RINGTONE <br></br>
     * RingtoneManager.TYPE_ALARM <br></br>
     * RingtoneManager.TYPE_NOTIFICATION <br></br>
     * RingtoneManager.TYPE_ALL
     * @return Obj_Sounds List
     */
    private suspend fun fetchSoundsFromRingtoneManager(
        context: Context,
        type: Int,
    ): List<AudioItem> {
        val sounds = mutableListOf<AudioItem>()

        withContext(Dispatchers.IO) {
            val manager = RingtoneManager(context).apply {
                setType(type)
            }

            manager.cursor.let { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(RingtoneManager.ID_COLUMN_INDEX);
                    val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                    val uri = manager.getRingtoneUri(cursor.position)
                    sounds.add(AudioItem(id, title, uri))
                }
            }
        }

        return sounds
    }

    private suspend fun fetchSoundsFromContentResolver(audioType: AudioType): List<AudioItem> {
        val sounds = mutableListOf<AudioItem>()

        withContext(Dispatchers.IO) {
            val uri = getUri(audioType)

            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
            )

            val selection = getSelection(audioType)
            val selectionArgs = getSelectionArgs(audioType)
            val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

            getApplication<Application>().contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

                Log.i(TAG, "Found ${cursor.count} audios")
                while (cursor.moveToNext()) {

                    // Here we'll use the column indexs that we found above.
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val audio = AudioItem(id, displayName, contentUri)
                    sounds += audio

                    // For debugging, we'll output the image objects we create to logcat.
                    Log.v(TAG, "Added audio: $audio")
                }
            }
        }

        return sounds
    }

    private fun getUri(type: AudioType): Uri {
        return when (type) {
            AudioType.Ringtone,
            AudioType.Notification,
            -> MediaStore.Audio.Media.INTERNAL_CONTENT_URI

            AudioType.Mp3,
            AudioType.Music,
            -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }
    }

    private fun getSelection(type: AudioType): String {
        return when (type) {
            AudioType.Ringtone -> "${MediaStore.Audio.Media.IS_RINGTONE} != ?"
            AudioType.Notification -> "${MediaStore.Audio.Media.IS_NOTIFICATION} != ?"
            AudioType.Mp3 -> "${MediaStore.Audio.Media.MIME_TYPE} = ?"
            AudioType.Music -> "${MediaStore.Audio.Media.IS_MUSIC} != ?"
        }
    }

    private fun getSelectionArgs(audioType: AudioType): Array<String>? {
        return when (audioType) {
            AudioType.Mp3 -> MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3")
                ?.let { arrayOf(it) }
            else -> arrayOf("0")
        }
    }

    fun playAudio(audioItem: AudioItem) {
        releaseAudio()

        mediaPlayer = MediaPlayer().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
                )
            } else {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            try {
                setDataSource(getApplication(), audioItem.contentUri)
            } catch (e: Exception) {
                setMediaPlayerDataSource(getApplication(), this, audioItem.toString())
            }

            setOnErrorListener { mp, what, extra ->
                Log.d(TAG, "what = $what")
                Log.d(TAG, "extra = $extra")
                release()
                true
            }
            setOnPreparedListener {
                it.start()
            }
            prepareAsync()
        }
    }

    fun releaseAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    @Throws(Exception::class)
    fun setMediaPlayerDataSource(
        context: Context,
        mp: MediaPlayer,
        fileInfo: String,
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
        mp: MediaPlayer,
        fileInfo: String,
    ) {
        mp.reset()
        mp.setDataSource(fileInfo)
    }

    @Throws(Exception::class)
    private fun setMediaPlayerDataSourcePostHoneyComb(
        context: Context,
        mp: MediaPlayer,
        fileInfo: String,
    ) {
        mp.reset()
        mp.setDataSource(context, Uri.parse(Uri.encode(fileInfo)))
    }

    @Throws(Exception::class)
    private fun setMediaPlayerDataSourceUsingFileDescriptor(
        context: Context, mp: MediaPlayer, fileInfo: String,
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
        contentUri: Uri,
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