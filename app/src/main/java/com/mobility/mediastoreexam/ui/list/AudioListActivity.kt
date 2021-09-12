package com.mobility.mediastoreexam.ui.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.mobility.mediastoreexam.R
import com.mobility.mediastoreexam.databinding.ActivityAudioListBinding
import com.mobility.mediastoreexam.model.AudioType

class AudioListActivity : AppCompatActivity() {

    private val viewModel: AudioListViewModel by viewModels()
    private lateinit var binding: ActivityAudioListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_list)

        val audioType =
            if (savedInstanceState == null) {
                intent.getParcelableExtra<AudioType>(EXTRA_AUDIO_TYPE)
            } else {
                savedInstanceState.getParcelable(EXTRA_AUDIO_TYPE)
            }

        viewModel.loadAudios(audioType!!)

        initRecyclerView()
        observeLiveDatas()
    }

    private lateinit var adapter: AudioListAdapter

    private fun initRecyclerView() {
        adapter = AudioListAdapter {
            viewModel.playAudio(it)
//            viewModel.playRingtone(it)
        }

        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.recyclerView.adapter = adapter
    }

    private fun observeLiveDatas() {
        viewModel.audios.observe(this) {
            adapter.items = it
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        viewModel.releaseAudio()
        viewModel.releaseRingtone()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_AUDIO_TYPE = "extraAudioType"

        fun newIntent(context: Context, audioType: AudioType): Intent {
            return Intent(context, AudioListActivity::class.java).apply {
                putExtra(EXTRA_AUDIO_TYPE, audioType)
            }
        }
    }
}