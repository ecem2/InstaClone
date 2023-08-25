package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R

class StoryClickAdapter(val context: Context) :
    RecyclerView.Adapter<StoryClickAdapter.StoryClickViewHolder>() {

    private var storyList: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryClickViewHolder {
        return StoryClickViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.fragment_story_click,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return storyList.size
    }
    override fun onBindViewHolder(holder: StoryClickViewHolder, position: Int) {
        val storyItem = storyList[position]

        Glide.with(context)
            .load(storyItem)
            .into(holder.userStory)
    }
    fun submitStoryClickList(story: ArrayList<String>) {
        storyList.addAll(story)
        notifyDataSetChanged()
    }
    class StoryClickViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userStory: AppCompatImageView = itemView.findViewById(R.id.storyPhoto)

    }
}