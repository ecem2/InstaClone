package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemCommentBinding
import com.example.myapplication.databinding.ItemLikeBinding
import com.example.myapplication.model.Comment
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.ItemListAdapter

class CommentAdapter(
    val context: Context,
    val commentList: ArrayList<Comment>
    ) : ListAdapter<UserModel, CommentAdapter.CommentViewHolder>(MyDiffUtil) {



    companion object MyDiffUtil : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem.userNickName == newItem.userNickName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val commentItem = commentList[position]

        holder.bind(commentItem)
    }


    fun submitCommentList(comment: ArrayList<Comment>) {
        commentList.addAll(comment)
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
//            Glide.with(context)
//                .load(comment.profilePhoto)
//                .into(binding.commentHomeProfilePhoto)
//
//            binding.commentNickname.text = comment.userNickName
            binding.commentTime.text = comment.commentTime
            binding.comment.text = comment.commentText
        }
    }


}



