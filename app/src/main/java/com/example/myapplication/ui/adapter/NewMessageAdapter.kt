package com.example.myapplication.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.SearchCardViewBinding
import com.example.myapplication.model.UserModel

class NewMessageAdapter(
    val context: Context,
    val searchList: ArrayList<UserModel>,
    private val onClickListener: OnClickListener
) : ListAdapter<UserModel, NewMessageAdapter.SearchViewHolder>(MyDiffUtil) {

    companion object MyDiffUtil : DiffUtil.ItemCallback<UserModel>() {
        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
            return oldItem.userNickName == newItem.userNickName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            SearchCardViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val searchItem = searchList[position]

        holder.itemView.setOnClickListener {
            onClickListener.onClick(searchItem)
        }
        holder.bind(searchItem)
    }

    fun submitMessageList(search: ArrayList<UserModel>) {
        searchList.addAll(search)
        notifyDataSetChanged()
    }

    inner class SearchViewHolder(private val binding: SearchCardViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userModel: UserModel?) {
            Glide.with(context)
                .load(userModel?.profilePhoto)
                .into(binding.searchPhoto)

            binding.searchNickname.text = userModel?.userNickName
        }
    }

    class OnClickListener(val clickListener: (userModel: UserModel) -> Unit) {
        fun onClick(userModel: UserModel) = clickListener(userModel)
    }
}


