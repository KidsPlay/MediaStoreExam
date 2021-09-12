package com.mobility.mediastoreexam.ui.list

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.mobility.mediastoreexam.dsl.OnClickAudioListener
import com.mobility.mediastoreexam.dsl.audioAdapterDelegates
import com.mobility.mediastoreexam.model.DisplayableItem

class AudioListAdapter(onClickAudioListener: OnClickAudioListener) :
    ListDelegationAdapter<List<DisplayableItem>>(
        audioAdapterDelegates(onClickAudioListener)
    )