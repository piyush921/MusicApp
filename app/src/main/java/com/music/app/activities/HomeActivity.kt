package com.music.app.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.music.app.R
import com.music.app.adapters.SongsAdapter
import com.music.app.base.BaseActivity
import com.music.app.databinding.ActivityHomeBinding
import com.music.app.models.SongsModel
import com.music.app.songsRepository.SongsRepository

class HomeActivity : BaseActivity(), View.OnClickListener
    , SongsAdapter.SongSelectionListener
    , SongsRepository.GetSongsListener{

    private lateinit var binding: ActivityHomeBinding
    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SongsAdapter
    private lateinit var songsList: MutableList<SongsModel.Audio>
    var songsRepository: SongsRepository = SongsRepository(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
        setupBottomSheer()
        setupRecyclerview()
        songsRepository.getAllSongs()

        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun init() {
        behavior = BottomSheetBehavior.from(binding.musicPlayer.bottomSheet)
        layoutManager = LinearLayoutManager(this)
        songsList = ArrayList()
        adapter = SongsAdapter(this, songsList, this)
    }

    private fun setListeners() {
        binding.musicPlayer.closeHideButton.setOnClickListener(this)
    }

    private fun setupBottomSheer() {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Log.d("thisisdata", "hidden")
                        binding.musicPlayer.closeHideButton.setImageResource(R.drawable.ic_arrow_up)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("thisisdata", "expanded")
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.musicPlayer.closeHideButton.setImageResource(R.drawable.ic_arrow_up)
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Log.d("thisisdata", "dragging")
                        binding.musicPlayer.closeHideButton.setImageResource(R.drawable.ic_arrow_down)
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Log.d("thisisdata", "settling")
                        binding.musicPlayer.closeHideButton.setImageResource(R.drawable.ic_arrow_down)
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.d("thisisdata", "half expanded")
                        binding.musicPlayer.closeHideButton.setImageResource(R.drawable.ic_arrow_down)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
    }

    private fun setupRecyclerview() {
        binding.songsRecyclerview.layoutManager = layoutManager
        binding.songsRecyclerview.adapter = adapter
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.close_hide_button -> {
                if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
                } else {
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                }
            }
        }
    }

    override fun onSongsGet(list: MutableList<SongsModel.Audio>) {
        with(songsList) {
            clear()
            addAll(list)
        }
        adapter.notifyDataSetChanged()
        binding.shimmerLayout.hideShimmer()
    }

    override fun onSongsSelect(model: SongsModel.Audio) {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}