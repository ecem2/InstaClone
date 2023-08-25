package com.example.myapplication.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R

class ProfileUsersAdapter(val context: Context) :
    RecyclerView.Adapter<ProfileUsersAdapter.UserProfileViewHolder>() {

    private var postList: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserProfileViewHolder {
        return UserProfileViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_post_photo,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: UserProfileViewHolder, position: Int) {
        var postItem = postList[position]
        if (postItem.startsWith("[") && postItem.endsWith("]")) {
            postItem = postItem.replace("[", "")
            postItem = postItem.replace("]", "")
        }



        try {
            Glide.with(context)
                .load(postItem)
                .into(holder.postPhoto)

        } catch (ex: IllegalArgumentException) {
            Log.d("salimmm", java.lang.String.valueOf(holder.postPhoto.tag))
        }
        //Glide.with(context).load(postItem.first()).into(holder.postPhoto)
    }

    fun submitPostPhotoList(posts: ArrayList<String>) {
        postList.addAll(posts)
        notifyDataSetChanged()
    }

    class UserProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postPhoto: AppCompatImageView = itemView.findViewById(R.id.usersPostPhoto)
    }
}

