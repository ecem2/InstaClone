package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemLikeBinding
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.ItemListAdapter

class LikeAdapter(
    val context: Context,
    val likeList: ArrayList<UserModel>

) : ListAdapter<UserModel, LikeAdapter.LikeViewHolder>(MyDiffUtil) {



    companion object MyDiffUtil : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem.userNickName == newItem.userNickName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        return LikeViewHolder(
            ItemLikeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return likeList.size
    }

    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        val likeItem = likeList[position]
        holder.itemView.setOnClickListener {

        }
        holder.bind(likeItem)
    }


    fun submitLikeList(like: ArrayList<UserModel>) {
        likeList.addAll(like)
        notifyDataSetChanged()
    }

    inner class LikeViewHolder(private val binding: ItemLikeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userModel: UserModel?) {
            Glide.with(context)
                .load(userModel?.profilePhoto)
                .into(binding.likeHomeProfilePhoto)

            binding.likeNickname.text = userModel?.userNickName
            binding.likeName.text = userModel?.userName
        }
    }


}



