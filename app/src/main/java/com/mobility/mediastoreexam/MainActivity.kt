package com.mobility.mediastoreexam

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import com.mobility.mediastoreexam.databinding.ActivityMainBinding
import com.mobility.mediastoreexam.ui.list.AudioListActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainListAdapter: MainListAdapter

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupRecyclerView()
        observeEvents()
    }

    private fun setupRecyclerView() {
        mainListAdapter = MainListAdapter { audioType ->
            startActivity(AudioListActivity.newIntent(this, audioType))
        }

        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
        binding.recyclerView.adapter = mainListAdapter
    }

    private fun observeEvents() {
        viewModel.audioTypes.observe(this) {
            mainListAdapter.items = it
            mainListAdapter.notifyDataSetChanged()
        }
    }
}