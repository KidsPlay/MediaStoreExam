package com.mobility.mediastoreexam

import android.app.Application
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mobility.mediastoreexam.model.AudioType

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _audioTypes = MutableLiveData<List<AudioType>>()
    val audioTypes: LiveData<List<AudioType>> = _audioTypes

    init {
        initAudioTypes()
    }

    private fun initAudioTypes() {
        val list = mutableListOf<AudioType>()
        list += AudioType(
            AudioType.Type.Ringtone,
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            "${MediaStore.Audio.Media.IS_RINGTONE} != 0"
        )

        list += AudioType(
            AudioType.Type.Notification,
            MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
            "${MediaStore.Audio.Media.IS_NOTIFICATION} != 0"
        )

        list += AudioType(
            AudioType.Type.Mp3,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Audio.Media.DISPLAY_NAME} like '%.mp3'"
        )

        list += AudioType(
            AudioType.Type.Music,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        )

//        list += AudioType(
//            "Alarms",
//            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//            "${MediaStore.Audio.Media.IS_ALARM} != 0"
//        )

        _audioTypes.value = list
    }
}