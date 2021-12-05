package com.music.app.dialogs

import android.R.attr
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.music.app.R
import com.music.app.databinding.DialogSongInfoBinding

import android.R.attr.data
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import com.bumptech.glide.Glide
import com.music.app.models.Audio
import com.music.app.utils.ImageUtils
import com.music.app.utils.NumberUtils
import java.io.File


open class SongInfoDialog(
    private val listener: SongInfoListener, private val model: Audio
) : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: DialogSongInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogSongInfoBinding.inflate(inflater, container, false)

        initUi()
        setListeners()

        return binding.root
    }

    private fun initUi() {
        //path, duration, size, date

        val duration = NumberUtils.convertSecondsToTime(model.duration / 1000)
        val size: String? = NumberUtils.convertSizeToFormat(model.size.toLong())
        val date: String = NumberUtils.convertMillisecondsToDate("yyyy-MM-dd", model.dateAdded.toLong())

        binding.title.text =
            Html.fromHtml(context?.getString(R.string.song_info_title, model.title))
        binding.displayName.text =
            Html.fromHtml(context?.getString(R.string.song_info_display_name, model.displayName))
        binding.path.text =
            Html.fromHtml(context?.getString(R.string.song_info_path, Uri.parse(model.uri).path))
        binding.duration.text =
            Html.fromHtml(context?.getString(R.string.song_info_duration, duration))
        binding.size.text =
            Html.fromHtml(context?.getString(R.string.song_info_size, size.toString()))
        binding.album.text =
            Html.fromHtml(context?.getString(R.string.song_info_album, model.album))
        binding.artist.text =
            Html.fromHtml(context?.getString(R.string.song_info_artist, model.artist))
        binding.dateAdded.text =
            Html.fromHtml(context?.getString(R.string.song_info_date_added, date))

        val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ImageUtils.getBitmapFromUri(requireContext(), Uri.parse(model.uri))
        } else {
            ImageUtils.getBitmapFromUri(requireContext(), Uri.parse(model.albumArt))
        }
        Glide.with(requireContext()).load(bitmap).into(binding.image)
    }

    private fun setListeners() {
        binding.close.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close -> {
                dismiss()
                listener.onSongInfoClose()
            }
        }
    }

    interface SongInfoListener {
        fun onSongInfoClose()
    }

}