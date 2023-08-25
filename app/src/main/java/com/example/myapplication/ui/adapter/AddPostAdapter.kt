package com.example.myapplication.ui.users

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.example.myapplication.R

class AddPostAdapter(
    val context: Context,
    val postAddList: ArrayList<String> = ArrayList(),
    private val onClickListener: OnClickListener,

) :
    RecyclerView.Adapter<AddPostAdapter.AddPostViewHolder>() {
    private var selectedImage: String? = null
    fun getSelectedImage(): String? {
        return selectedImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddPostViewHolder {
        return AddPostViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_post_add_photo,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return postAddList.size
    }

    override fun onBindViewHolder(holder: AddPostViewHolder, position: Int) {
        val postItem = postAddList[position]


        holder.postPhoto.setOnClickListener {
            onClickListener.onClick(postItem)
            selectedImage = postAddList[position]
            notifyDataSetChanged()
        }
        try {
            Glide.with(context)
                .load(postItem)
                .into(holder.postPhoto)

        } catch (ex: IllegalArgumentException) {
            Log.d("salimmm", java.lang.String.valueOf(holder.postPhoto.tag))
        }
    }

    fun submitPostPhotoList(posts: ArrayList<String>) {
        postAddList.addAll(posts)
        notifyDataSetChanged()
    }

    class AddPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postPhoto: AppCompatImageView = itemView.findViewById(R.id.addPostPhoto)
    }

    class OnClickListener(val clickListener: (string: String) -> Unit) {
        fun onClick(string: String) = clickListener(string)
    }
}

