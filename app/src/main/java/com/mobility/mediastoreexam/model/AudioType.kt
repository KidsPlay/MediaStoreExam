package com.mobility.mediastoreexam.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioType(
    val type: Type,
    val uri: Uri,
    val selection: String? = null
) : DisplayableItem, Parcelable {

    enum class Type(name: String) {
        Ringtone("Ringtones"),
        Notification("Notifications"),
        Mp3("mp3"),
        Music("music")
    }
}
