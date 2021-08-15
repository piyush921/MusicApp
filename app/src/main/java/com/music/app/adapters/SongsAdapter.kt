package com.music.app.adapters

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_list, parent, false)
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
                Glide.with(context).load(R.drawable.album).into(holder.poster)
            }
        }

        holder.itemView.setOnClickListener {
            listener.onSongsSelect(list[position], bitmap!!)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    open class SongsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var songName: TextView = itemView.findViewById(R.id.song_name)
        var albumName: TextView = itemView.findViewById(R.id.album_name)
        var poster: CircleImageView = itemView.findViewById(R.id.song_poster)
        var adjust: ImageView = itemView.findViewById(R.id.adjust)
    }

    interface SongSelectionListener {
        fun onSongsSelect(model: SongsModel.Audio, bitmap: Bitmap)
    }


}