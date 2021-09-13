package com.mobility.mediastoreexam.model

import android.net.Uri

data class AudioItem(
    val id: Long,
    val title: String,
    val contentUri: Uri
) : DisplayableItem
