package com.mobility.mediastoreexam

import android.app.Application
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
        list += AudioType.Ringtone
        list += AudioType.Notification
        list += AudioType.Mp3
        list += AudioType.Music

        _audioTypes.value = list
    }
}