package com.mobility.mediastoreexam

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.mobility.mediastoreexam.dsl.audioTypeAdapterDelegates
import com.mobility.mediastoreexam.model.AudioType
import com.mobility.mediastoreexam.model.DisplayableItem

class MainListAdapter(itemClickListener: (AudioType) -> Unit) :
    ListDelegationAdapter<List<DisplayableItem>>(
        audioTypeAdapterDelegates(itemClickListener)
    )
