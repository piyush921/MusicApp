package com.music.app.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.music.app.R
import com.music.app.models.SongsModel
import de.hdodenhof.circleimageview.CircleImageView
import java.io.FileNotFoundException

open class SongsAdapter(
    private var context: Context,
    private var list: MutableList<SongsModel.Audio>,
    private var listener: SongSelectionListener
) : RecyclerView.Adapter<SongsAdapter.SongsViewHolder>() {

    private var previousSelection: Int = 0
    private var currentSelection: Int = 0
    private val VIEW_PLAYING = 0
    private val VIEW_IDLE = 1

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
        var bitmap: Bitmap? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                bitmap =
                    context.contentResolver.loadThumbnail(list[position].uri, Size(100, 100), null)
                Glide.with(context).load(bitmap).into(holder.poster)
            } catch (e: FileNotFoundException) {
                Glide.with(context).load(R.drawable.frame_1).into(holder.poster)
            }
        } else {
            Glide.with(context).load(list[position].albumArt).into(holder.poster)
        }

        holder.itemView.setOnClickListener {

            if(bitmap != null) {
                previousSelection = currentSelection
                currentSelection = holder.absoluteAdapterPosition
                list[currentSelection].isSelected = true
                list[previousSelection].isSelected = false
                notifyItemChanged(currentSelection)
                notifyItemChanged(previousSelection)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listener.onSongsSelect(list[currentSelection], bitmap)
            } else {
                val stream =
                    context.contentResolver.openInputStream(list[currentSelection].albumArt!!)
                bitmap = BitmapFactory.decodeStream(stream)
                listener.onSongsSelect(list[currentSelection], bitmap!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
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
        var adjust: ImageView = itemView.findViewById(R.id.adjust)
    }

    interface SongSelectionListener {
        fun onSongsSelect(model: SongsModel.Audio, bitmap: Bitmap?)
    }
}