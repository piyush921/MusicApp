package com.music.app.activities

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.music.app.Constants
import com.music.app.R
import com.music.app.SongSearch
import com.music.app.adapters.SongsAdapter
import com.music.app.base.BaseActivity
import com.music.app.database.SongsDao
import com.music.app.database.SongsDatabase
import com.music.app.databinding.ActivityHomeBinding
import com.music.app.dialogs.SongInfoDialog
import com.music.app.models.Audio
import com.music.app.services.PlayerService
import com.music.app.repository.SongsRepository
import com.music.app.storage.PrefsHelper
import com.music.app.utils.*

class HomeActivity : BaseActivity(), View.OnClickListener, SongsAdapter.SongSelectionListener,
    SongsRepository.GetSongsListener, TextWatcher, SongSearch.SongSearchListener,
    SongInfoDialog.SongInfoListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SongsAdapter
    private lateinit var songsList: MutableList<Audio>
    private var isExpanded: Boolean = false
    private var isPlaying: Boolean = false
    private lateinit var context: Context
    private lateinit var prefsHelper: PrefsHelper
    private lateinit var handler: Handler
    private lateinit var songSearch: SongSearch
    private lateinit var songsDao: SongsDao
    private var position = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
        setupBottomSheet()
        setupRecyclerview()

        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        binding.musicPlayer.seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, isSeeking: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress: Int? = seekBar?.progress
                val intent = Intent(context, PlayerService::class.java)
                intent.action = Constants.SERVICE_ACTION_SEEK
                intent.putExtra(Constants.KEY_PROGRESS, progress)
                Util.startForegroundService(context, intent)
            }
        })
    }

    private fun init() {
        context = this
        prefsHelper = PrefsHelper(context)
        behavior = BottomSheetBehavior.from(binding.musicPlayer.bottomSheet)
        layoutManager = LinearLayoutManager(context)
        songsList = ArrayList()
        adapter = SongsAdapter(context, songsList, this)
        handler = Handler(Looper.getMainLooper())
        songSearch = SongSearch(songsList, this)

        songsDatabase = Room.databaseBuilder(
            applicationContext,
            SongsDatabase::class.java,
            "SongsDb"
        )
            .allowMainThreadQueries()
            .build()
        songsDao = songsDatabase.songsDatabase()
        songsRepository = SongsRepository(context, this, songsDatabase, songsDao)
    }

    private fun setListeners() {
        binding.musicPlayer.closeHideButton.setOnClickListener(this)
        binding.musicPlayer.playPause.setOnClickListener(this)
        binding.clearSearch.setOnClickListener(this)
        binding.musicPlayer.next.setOnClickListener(this)
        binding.musicPlayer.previous.setOnClickListener(this)
        binding.musicPlayer.more.setOnClickListener(this)
        binding.musicPlayer.exoShuffle.setOnClickListener(this)
        binding.searchField.addTextChangedListener(this)
        binding.moreOptions.setOnClickListener(this)
    }

    private fun checkLocalDatabase() {
        val tableSize = songsDao.getSize()
        if (tableSize > 0) {
            val list: MutableList<Audio> = songsDao.getAll()
            onGetSongs(list)
        } else {
            songsRepository.getAllSongs()
        }
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
            R.id.next -> {
                nextPrevious(Constants.SERVICE_ACTION_NEXT)
            }
            R.id.previous -> {
                nextPrevious(Constants.SERVICE_ACTION_PREVIOUS)
            }
            R.id.more -> {
                showMorePopup(v, songsList[position])
            }
            R.id.more_options -> {
                showMoreOptions(v)
            }
            R.id.exo_shuffle -> {
                shuffle()
            }
        }
    }

    private fun nextPrevious(action: String) {
        val intent = Intent(context, PlayerService::class.java)
        intent.action = action
        Util.startForegroundService(context, intent)
    }

    private fun showMorePopup(v: View, model: Audio) {
        val popupMenu = PopupMenu(this, v)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.music_player_more_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.song_info -> {
                    DialogUtils.showSongInfoDialog(this, supportFragmentManager, model)
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun showMoreOptions(v: View) {
        val popupMenu = PopupMenu(this, v)
        val inflater = popupMenu.menuInflater
        inflater.inflate(R.menu.more_options, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.sync -> {
                    songsRepository.getAllSongs()
                }
                R.id.go_online -> {
                    showToast("Coming soon")
                }
            }
            true
        }
        popupMenu.show()
    }

    override fun onSongInfoClose() {

    }

    private fun shuffle() {
        val intent = Intent(context, PlayerService::class.java)
        val shuffleState = prefsHelper.getPref(PrefsHelper.SHUFFLE_STATE)
        if (shuffleState == Constants.SHUFFLE_STATE_DISABLED) {
            prefsHelper.savePref(PrefsHelper.SHUFFLE_STATE, Constants.SHUFFLE_STATE_ENABLED)
            intent.action = Constants.SERVICE_ACTION_SHUFFLE_ON
            binding.musicPlayer.exoShuffle.setImageResource(com.google.android.exoplayer2.R.drawable.exo_controls_shuffle_on)
        } else {
            prefsHelper.savePref(PrefsHelper.SHUFFLE_STATE, Constants.SHUFFLE_STATE_DISABLED)
            intent.action = Constants.SERVICE_ACTION_SHUFFLE_OFF
            binding.musicPlayer.exoShuffle.setImageResource(com.google.android.exoplayer2.R.drawable.exo_controls_shuffle_off)
        }
        Util.startForegroundService(context, intent)
    }

    override fun onResume() {
        super.onResume()

        if (!PermissionUtils.checkReadWritePermission(context)) {
            PermissionUtils.askReadWritePermission(context)
        } else {
            if (songsList.isEmpty()) {
                checkLocalDatabase()
            }
        }

        setupShuffle()
        updatePlayerPosition()
        updatePlayer(prefsHelper.getPref(PrefsHelper.PLAYER_STATE).toString())

        val intentFilter = IntentFilter(PlayerService.PLAYER_ACTION)
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun setupShuffle() {
        val shuffleState = prefsHelper.getPref(PrefsHelper.SHUFFLE_STATE)
        if (shuffleState == null || shuffleState.isEmpty() || shuffleState == "null") {
            prefsHelper.savePref(PrefsHelper.SHUFFLE_STATE, Constants.SHUFFLE_STATE_DISABLED)
            binding.musicPlayer.exoShuffle.setImageResource(com.google.android.exoplayer2.R.drawable.exo_controls_shuffle_off)
        } else {
            if (shuffleState == Constants.SHUFFLE_STATE_DISABLED) {
                binding.musicPlayer.exoShuffle.setImageResource(com.google.android.exoplayer2.R.drawable.exo_controls_shuffle_off)
            } else {
                binding.musicPlayer.exoShuffle.setImageResource(com.google.android.exoplayer2.R.drawable.exo_controls_shuffle_on)
            }
        }
    }

    private fun updatePlayerPosition() {
        if (IntentUtils.isServiceRunning(context, PlayerService::class.java)) {
            if (PlayerService.position != -1) {
                position = PlayerService.position
                if (songsList.isNotEmpty()) {
                    adapter.updateNowPlaying(position)
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    updateMusicPlayerInfo(songsList[position], position)
                }
            }
        }
    }

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val state: String = intent?.getStringExtra(Constants.KEY_PLAYER_STATE).toString()
            val progress: Int = intent?.getIntExtra(Constants.KEY_PROGRESS, 0)!!
            val mediaId = intent.getStringExtra(Constants.KEY_MEDIA_ID).toString()
            var position: Int = intent.getIntExtra(Constants.KEY_POSITION, -1)

            when {
                state.isNotEmpty() && state != "null" -> {
                    updatePlayer(state)
                }
                progress > 0 -> {
                    updateSeekbar(progress)
                }
                mediaId.isNotEmpty() && mediaId != "null" -> {
                    songsList.forEachIndexed { index, audio ->
                        if (audio.id == mediaId.toLong()) {
                            position = index
                        }
                    }
                    adapter.updateNowPlaying(position)
                    updateMusicPlayerInfo(songsList[position], position)
                }
            }
        }
    }

    private fun updateSeekbar(progress: Int) {
        binding.musicPlayer.seekbar.progress = progress
        binding.musicPlayer.currentTime.text = NumberUtils.convertSecondsToTime(progress)
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

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
    }

    override fun onBackPressed() {
        if (isExpanded) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onGetSongs(list: MutableList<Audio>) {
        if (list.isEmpty()) {
            binding.noSongs.visibility = View.VISIBLE
        }

        notifyAdapter(list)

        if (position != -1) {
            adapter.updateNowPlaying(position)
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            updateMusicPlayerInfo(songsList[position], position)
        }
    }

    override fun onSongSearch(songList: MutableList<Audio>) {
        notifyAdapter(songList)
        binding.searchProgressbar.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyAdapter(list: MutableList<Audio>) {
        with(songsList) {
            clear()
            addAll(list)
        }
        songsList.sortBy { it.displayName }
        adapter.notifyDataSetChanged()
        binding.shimmerLayout.hideShimmer()
    }

    override fun onSongsSelect(position: Int) {
        this.position = position
        val model = songsList[position]
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        updateMusicPlayerInfo(model, position)
        startSong(model)
    }

    private fun updateMusicPlayerInfo(model: Audio, position: Int) {

        this.position = position

        val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ImageUtils.getBitmapFromUri(context, Uri.parse(model.uri))
        } else {
            ImageUtils.getBitmapFromUri(context, Uri.parse(model.albumArt))
        }

        binding.musicPlayer.songName1.text = model.title
        binding.musicPlayer.songName2.text = model.title
        binding.musicPlayer.songName3.text = model.title
        binding.musicPlayer.albumName.text = model.album
        Glide.with(context).load(bitmap).into(binding.musicPlayer.songImage1)
        Glide.with(context).load(bitmap).into(binding.musicPlayer.songImage2)

        binding.musicPlayer.songTime.text = NumberUtils.convertSecondsToTime(model.duration / 1000)
        binding.musicPlayer.finishTime.text =
            NumberUtils.convertSecondsToTime(model.duration / 1000)
        Glide.with(context).load(R.drawable.ic_play).into(binding.musicPlayer.playPause)

        binding.musicPlayer.seekbar.max = model.duration / 1000
    }

    private fun startSong(model: Audio) {

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
            checkLocalDatabase()
            binding.searchProgressbar.visibility = View.GONE
        } else {
            binding.clearSearch.visibility = View.VISIBLE
            binding.searchProgressbar.visibility = View.VISIBLE
            handler.postDelayed({
                songSearch.searchSong(editable.toString())
            }, 1000)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}