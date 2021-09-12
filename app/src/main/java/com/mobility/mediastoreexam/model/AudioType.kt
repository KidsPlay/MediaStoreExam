package com.mobility.mediastoreexam.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioType(
    val name: String,
    val uri: Uri,
    val selection: String? = null
) : DisplayableItem, Parcelable
