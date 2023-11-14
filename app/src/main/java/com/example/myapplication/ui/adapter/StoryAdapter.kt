package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.ItemStoriesBinding
import com.example.myapplication.model.UserModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class StoryAdapter(
    val context: Context,
    val storyList: ArrayList<UserModel>,
    private val onClickListener: StoryAdapter.OnClickListener,
) : ListAdapter<UserModel, StoryAdapter.StoryViewHolder>(StoryDiffUtil) {
    val user = Firebase.auth.currentUser!!.uid
    private lateinit var database: DatabaseReference
    companion object StoryDiffUtil : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem.userNickName == newItem.userNickName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryAdapter.StoryViewHolder {
        return StoryViewHolder(
            ItemStoriesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }


    override fun getItemCount(): Int {
        return storyList.size
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val storyItem = storyList[position]

        holder.itemView.setOnClickListener {
            onClickListener.onClick(storyItem)
        }

        holder.bind(storyItem)


        /*   var postItem = storyList[position]

       holder.bind(postItem)*/

/*
        try {

                Glide.with(context)
                    .load(postItem)
                    .into(holder.profilePhoto)
                holder.userNickName.text = storyList.toString()


        } catch (ex: IllegalArgumentException) {
            Log.d("salimmm", java.lang.String.valueOf(holder.profilePhoto.tag))
        }*/

    }
    fun submitStoryList(story: ArrayList<UserModel>) {
        storyList.addAll(story)
        notifyDataSetChanged()
    }
    inner class StoryViewHolder(private val binding: ItemStoriesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userModel: UserModel?) {
            Glide.with(context)
                .load(userModel?.profilePhoto)
                .into(binding.storyView)


            if (userModel?.userStory?.isNotEmpty() == true) {
                binding.storyColor.visibility = View.VISIBLE
                binding.storyNickname.text = userModel?.userNickName
            } else {
                binding.storyColor.visibility = View.INVISIBLE // Görünmez, ancak yer kaplar
                binding.storyNickname.text = userModel?.userNickName
            }
        }
    }
    class OnClickListener(val clickListener: (userModel: UserModel) -> Unit) {
        fun onClick(userModel: UserModel) = clickListener(userModel)
    }
    /*class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePhoto: AppCompatImageView = itemView.findViewById(R.id.storyView)
        val userNickName : AppCompatTextView = itemView.findViewById(R.id.storyNickname)
    }*/
}

