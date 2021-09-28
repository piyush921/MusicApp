package com.music.app.activities

import android.content.*
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.music.app.Constants
import com.music.app.R
import com.music.app.SongSearch
import com.music.app.adapters.SongsAdapter
import com.music.app.base.BaseActivity
import com.music.app.databinding.ActivityHomeBinding
import com.music.app.models.SongsModel
import com.music.app.services.PlayerService
import com.music.app.repository.SongsRepository
import com.music.app.utils.NumberUtils
import java.util.concurrent.TimeUnit
import com.music.app.storage.PrefsHelper
import com.music.app.utils.KeyboardUtils
import com.music.app.utils.PermissionUtils
import kotlin.collections.ArrayList

class HomeActivity : BaseActivity(), View.OnClickListener, SongsAdapter.SongSelectionListener,
    SongsRepository.GetSongsListener, TextWatcher, SongSearch.SongSearchListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SongsAdapter
    private lateinit var songsList: MutableList<SongsModel.Audio>
    private var isExpanded: Boolean = false
    private var isPlaying: Boolean = false
    private lateinit var context: Context
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var handler: Handler
    private lateinit var songSearch: SongSearch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
        setupBottomSheet()
        setupRecyclerview()
        songsRepository.getAllSongs()

        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun init() {
        context = this
        songsRepository = SongsRepository(context, this)
        prefsHelper = PrefsHelper(context)
        behavior = BottomSheetBehavior.from(binding.musicPlayer.bottomSheet)
        layoutManager = LinearLayoutManager(context)
        songsList = ArrayList()
        adapter = SongsAdapter(context, songsList, this)
        handler = Handler(Looper.getMainLooper())
        songSearch = SongSearch(songsList, this)
    }

    private fun setListeners() {
        binding.musicPlayer.closeHideButton.setOnClickListener(this)
        binding.musicPlayer.playPause.setOnClickListener(this)
        binding.clearSearch.setOnClickListener(this)
        binding.searchField.addTextChangedListener(this)
    }

    private fun setupBottomSheet() {
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Glide.with(context).load(R.drawable.ic_arrow_down)
                            .into(binding.musicPlayer.closeHideButton)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        isExpanded = true
                        expanded()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Glide.with(context).load(R.drawable.ic_arrow_up)
                            .into(binding.musicPlayer.closeHideButton)
                        isExpanded = false
                        collapsed()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Glide.with(context).load(R.drawable.ic_arrow_down)
                            .into(binding.musicPlayer.closeHideButton)
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Glide.with(context).load(R.drawable.ic_arrow_down)
                            .into(binding.musicPlayer.closeHideButton)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
    }

    private fun collapsed() {
        binding.musicPlayer.collapsedLayout.visibility = View.VISIBLE
        binding.musicPlayer.songName2.visibility = View.GONE
        binding.musicPlayer.more.visibility = View.GONE

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.musicPlayer.bottomSheet)
        constraintSet.clear(R.id.close_hide_button, ConstraintSet.START)
        constraintSet.connect(
            R.id.close_hide_button,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        constraintSet.applyTo(binding.musicPlayer.bottomSheet)
    }

    private fun expanded() {
        binding.musicPlayer.collapsedLayout.visibility = View.GONE
        binding.musicPlayer.songName2.visibility = View.VISIBLE
        binding.musicPlayer.more.visibility = View.VISIBLE

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.musicPlayer.bottomSheet)
        constraintSet.clear(R.id.close_hide_button, ConstraintSet.END)
        constraintSet.connect(
            R.id.close_hide_button,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.applyTo(binding.musicPlayer.bottomSheet)
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
            R.id.play_pause -> {

                isPlaying = !isPlaying

                val intent = Intent(context, PlayerService::class.java)
                if (!isPlaying) {
                    intent.action = Constants.SERVICE_ACTION_PLAY
                    Glide.with(context).load(R.drawable.ic_pause)
                        .into(binding.musicPlayer.playPause)
                } else {
                    intent.action = Constants.SERVICE_ACTION_PAUSE
                    Glide.with(context).load(R.drawable.ic_play).into(binding.musicPlayer.playPause)
                }
                Util.startForegroundService(context, intent)
            }
            R.id.clear_search -> {
                binding.searchField.setText("")
                binding.searchField.clearFocus()
                KeyboardUtils.hideKeyboard(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!PermissionUtils.checkReadWritePermission(context)) {
            val intent = Intent(context, PermissionDeniedActivity::class.java)
            intent.putExtra(
                Constants.KEY_TEXT,
                getString(R.string.permission_denied_read_write_text)
            )
            intent.putExtra(Constants.KEY_IMAGE, R.drawable.permission_read_write)
            startActivity(intent)
        } else {
            if (songsList.isEmpty()) {
                songsRepository.getAllSongs()
            }
        }

        updatePlayer(prefsHelper.getPref(PrefsHelper.PLAYER_STATE).toString())

        val intentFilter = IntentFilter(PlayerService.ACTION_PLAYER)
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state: String = intent?.getStringExtra(Constants.KEY_PLAYER_STATE).toString()
            updatePlayer(state)
        }
    }

    private fun updatePlayer(state: String) {
        isPlaying = if (state == PrefsHelper.PLAYER_STATE_PLAYING) {
            Glide.with(context).load(R.drawable.ic_play).into(binding.musicPlayer.playPause)
            false
        } else {
            Glide.with(context).load(R.drawable.ic_pause).into(binding.musicPlayer.playPause)
            true
        }
    }

    override fun onStop() {
        super.onStop()

        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    }

    override fun onBackPressed() {
        if (isExpanded) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onGetSongs(list: MutableList<SongsModel.Audio>) {
        notifyAdapter(list)
    }

    override fun onSongSearch(songList: MutableList<SongsModel.Audio>) {
        notifyAdapter(songList)
        binding.searchProgressbar.visibility = View.GONE
    }

    private fun notifyAdapter(list: MutableList<SongsModel.Audio>) {
        with(songsList) {
            clear()
            addAll(list)
        }
        adapter.notifyDataSetChanged()
        binding.shimmerLayout.hideShimmer()
    }

    override fun onSongsSelect(model: SongsModel.Audio, bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(context, "cannot play the song", Toast.LENGTH_SHORT).show()
            return
        }
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        updateMusicPlayerInfo(model, bitmap)
        startSong(model)
    }

    private fun updateMusicPlayerInfo(model: SongsModel.Audio, bitmap: Bitmap) {

        binding.musicPlayer.songName1.text = model.title
        binding.musicPlayer.songName2.text = model.title
        binding.musicPlayer.songName3.text = model.title
        binding.musicPlayer.albumName.text = model.album
        Glide.with(context).load(bitmap).into(binding.musicPlayer.songImage1)
        Glide.with(context).load(bitmap).into(binding.musicPlayer.songImage2)

        val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(model.duration.toLong())
        val seconds: Long = TimeUnit.MILLISECONDS.toSeconds(model.duration.toLong())
        val duration =
            "${NumberUtils.addZeroBeforeNumber(minutes.toString())} " +
                    ": ${NumberUtils.addZeroBeforeNumber((seconds % 60).toString())}"
        binding.musicPlayer.songTime.text = duration
        binding.musicPlayer.finishTime.text = duration
        Glide.with(context).load(R.drawable.ic_play).into(binding.musicPlayer.playPause)
    }

    private fun startSong(model: SongsModel.Audio) {

        val intent = Intent(context, PlayerService::class.java)
        if (!isPlaying) {
            intent.action = Constants.SERVICE_ACTION_NOT_PLAYING
        } else {
            intent.action = Constants.SERVICE_ACTION_ALREADY_PLAYING
        }
        intent.putExtra(Constants.KEY_POSITION, songsList.indexOf(model))
        intent.putParcelableArrayListExtra(Constants.KEY_LIST, ArrayList(songsList))
        Util.startForegroundService(context, intent)

        isPlaying = true
    }

    override fun beforeTextChanged(string: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(string: CharSequence?, p1: Int, p2: Int, p3: Int) {
        handler.removeCallbacksAndMessages(null)
    }

    override fun afterTextChanged(editable: Editable?) {
        if (editable?.isEmpty() == true) {
            binding.clearSearch.visibility = View.GONE
            songsRepository.getAllSongs()
            binding.searchProgressbar.visibility = View.GONE
        } else {
            binding.clearSearch.visibility = View.VISIBLE
            binding.searchProgressbar.visibility = View.VISIBLE
            handler.postDelayed({
                songSearch.searchSong(editable.toString())
            }, 1000)
        }
    }

}