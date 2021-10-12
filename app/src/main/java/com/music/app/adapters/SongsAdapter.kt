package com.music.app.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.music.app.R
import com.music.app.models.SongsModel
import de.hdodenhof.circleimageview.CircleImageView
import java.io.FileNotFoundException

open class SongsAdapter(
    private var context: Context,
    private var list: MutableList<SongsModel.Audio>,
    private var listener: SongSelectionListener
) : RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

    private var previousSelection: Int = -1
    private var currentSelection: Int = 0
    companion object {
        private const val VIEW_PLAYING = 0
        private const val VIEW_IDLE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        var view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_list_idle, parent, false)

        if (viewType == VIEW_PLAYING) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_list_playing, parent, false)
        }

        return SongsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {

        holder.songName.text = list[position].title
        holder.albumName.text = list[position].album

        val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getBitmapFromUri(list[position].uri)
        } else {
            getBitmapFromUri(list[position].albumArt)
        }

        Glide.with(context).load(bitmap).into(holder.poster)

        holder.itemView.setOnClickListener {
            if(list[holder.absoluteAdapterPosition].isSelected) {
                return@setOnClickListener
            }
            updateNowPlaying(holder.absoluteAdapterPosition)
            listener.onSongsSelect(currentSelection)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateNowPlaying(position: Int) {
        previousSelection = currentSelection
        currentSelection = position
        list[currentSelection].isSelected = true
        list[previousSelection].isSelected = false
        notifyItemChanged(currentSelection)
        notifyItemChanged(previousSelection)
    }

    fun getBitmapFromUri(uri: Uri?): Bitmap? {

        if (uri == null) {
            return BitmapFactory.decodeResource(context.resources, R.drawable.frame_1)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                context.contentResolver.loadThumbnail(uri, Size(300, 300), null)
            } catch (e: FileNotFoundException) {
                BitmapFactory.decodeResource(context.resources, R.drawable.frame_1)
            }
        } else {
            val stream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream)
        }

    }

    override fun getItemViewType(position: Int): Int {

        val isSelected = list[position].isSelected

        return if (isSelected) {
            VIEW_PLAYING
        } else {
            VIEW_IDLE
        }

    }

    open class SongsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var songName: TextView = itemView.findViewById(R.id.song_name)
        var albumName: TextView = itemView.findViewById(R.id.album_name)
        var poster: CircleImageView = itemView.findViewById(R.id.song_poster)
    }

    interface SongSelectionListener {
        fun onSongsSelect(position: Int)
    }
}