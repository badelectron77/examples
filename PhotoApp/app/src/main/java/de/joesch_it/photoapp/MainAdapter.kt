package de.joesch_it.photoapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import de.joesch_it.photoapp.models.Photo


class MainAdapter(var photos: List<Photo>, var clickListener: View.OnClickListener) :
        RecyclerView.Adapter<MainAdapter.PhotoViewHolder>() {

    fun getPhoto(adapterPosition : Int) : Photo {
        return photos[adapterPosition]
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(LayoutInflater
                .from(parent?.context)
                .inflate(R.layout.photo_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder : PhotoViewHolder?, position : Int) {
        val photo = photos[position]
        holder?.tags?.text = photo.tags
        holder?.likes?.text = photo.likes.toString()
        holder?.favorites?.text = photo.favorites.toString()
        if (photo.previewURL.isNotEmpty()) {
            Glide.with(holder?.tags?.context)
                    .load(photo.previewURL)
                    .into(holder?.photo_item)
        }
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    fun getStringLength(obj: Any): Int? {
        if (obj is String) {
            // `obj` is automatically cast to `String` in this branch
            return obj.length
        }

        // `obj` is still of type `Any` outside of the type-checked branch
        return null
    }

    inner class PhotoViewHolder(itemView : View) : RecyclerView.ViewHolder
    (itemView) {
        var tags: TextView
        var likes: TextView
        var favorites: TextView
        var photo_item: ImageView

        init {
            itemView.setOnClickListener(clickListener)
            itemView.tag = this
            tags = itemView.findViewById(R.id.tags) as TextView
            likes = itemView.findViewById(R.id.likes) as TextView
            favorites = itemView.findViewById(R.id.favorites) as TextView
            photo_item = itemView.findViewById(R.id.photo_item) as ImageView

        }
    }
}