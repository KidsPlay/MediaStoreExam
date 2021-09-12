package com.mobility.mediastoreexam.dsl

import android.annotation.SuppressLint
import android.util.Log
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.mobility.mediastoreexam.databinding.ListItemAudioBinding
import com.mobility.mediastoreexam.databinding.ListItemAudioTypeBinding
import com.mobility.mediastoreexam.model.AudioItem
import com.mobility.mediastoreexam.model.AudioType
import com.mobility.mediastoreexam.model.DisplayableItem
import com.mobility.mediastoreexam.model.MediaStoreImage

fun audioTypeAdapterDelegates(itemClickListener: (AudioType) -> Unit) =
    adapterDelegateViewBinding<AudioType, DisplayableItem, ListItemAudioTypeBinding>(
        { layoutInflater, parent ->
            ListItemAudioTypeBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        }
    ) {
        binding.tvAudioType.setOnClickListener {
            itemClickListener(this.item)
        }
        bind {
            binding.tvAudioType.text = item.type.name
        }
    }

typealias OnClickAudioListener = (item: AudioItem) -> Unit

@SuppressLint("SetTextI18n")
fun audioAdapterDelegates(onClickAudioListener: OnClickAudioListener) =
    adapterDelegateViewBinding<AudioItem, DisplayableItem, ListItemAudioBinding>(
        { layoutInflater, root -> ListItemAudioBinding.inflate(layoutInflater, root, false) }
    ) {
        binding.tvAudio.setOnClickListener {
            Log.d("Click", "Click ${item.displayName}")
            onClickAudioListener(item)
        }

        bind {
            binding.tvAudio.text = item.displayName
//                "${item.id} / ${item.displayName} / ${item.dateAdded} / ${item.contentUri}"
        }
    }

@SuppressLint("SetTextI18n")
fun imageAdapterDelegates() =
    adapterDelegateViewBinding<MediaStoreImage, DisplayableItem, ListItemAudioBinding>(
        { layoutInflater, root -> ListItemAudioBinding.inflate(layoutInflater, root, false) }
    ) {
        binding.tvAudio.setOnClickListener { Log.d("Click", "Click ${item.displayName}") }

        bind {
            binding.tvAudio.text =
                "${item.id} / ${item.displayName} / ${item.dateAdded} / ${item.contentUri}"
        }
    }