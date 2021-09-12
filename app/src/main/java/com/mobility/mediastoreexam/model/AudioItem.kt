package com.mobility.mediastoreexam.model

import android.net.Uri
import java.util.*

data class AudioItem(
    val id: Long,
    val displayName: String,
    val dateAdded: Date,
    val contentUri: Uri
) : DisplayableItem
