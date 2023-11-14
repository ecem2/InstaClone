package com.example.myapplication.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.model.Comment
import com.example.myapplication.model.UserModel
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//class CommentAdapter(
//    val context: Context,
//    val commentList: ArrayList<Comment>,
//    val userList: ArrayList<UserModel>
//) : ListAdapter<UserModel, CommentAdapter.CommentViewHolder>(MyDiffUtil) {
//
//    companion object MyDiffUtil : DiffUtil.ItemCallback<UserModel>() {
//        override fun areItemsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
//            return oldItem == newItem
//        }
//
//        override fun areContentsTheSame(oldItem: UserModel, newItem: UserModel): Boolean {
//            return oldItem.userNickName == newItem.userNickName
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
//        return CommentViewHolder(
//            ItemCommentBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun getItemCount(): Int {
//        return commentList.size
//    }
//
//    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
//        val commentItem = commentList[position]
//        val userItem = userList[position]
//
//        holder.bind(commentItem, userItem)
//    }
//
//
//    fun submitCommentList(comment: ArrayList<Comment>) {
//        commentList.addAll(comment)
//        notifyDataSetChanged()
//    }
//
//    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(comment: Comment,userModel: UserModel) {
//            Glide.with(context)
//                .load(userModel.profilePhoto)
//                .into(binding.commentHomeProfilePhoto)
//
//           binding.commentNickname.text = userModel.userNickName
//            binding.commentTime.text = comment.commentTime
//            binding.comment.text = comment.commentText
//        }
//    }
//
//
//}


private const val TAG = "CommentAdapter"
class CommentAdapter(
    private val mContext: Context,
    private val mComment: ArrayList<Comment> = ArrayList(),
    val userList: ArrayList<UserModel> = ArrayList(),
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    private lateinit var postId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.postId = pref.getString("postId", "none").toString()
        }
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_comment,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mComment.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < mComment.size && position < userList.size) {
            val comment = mComment[position]
            val user = userList[position]
            holder.bind(comment, user)
        } else {
            Log.e("eco567", "Index out of bounds: position=$position, mComment.size=${mComment.size}, userList.size=${userList.size}")
        }
    }

    fun submitCommentList(users: ArrayList<UserModel>) {
            userList.addAll(users)
        Log.d("eco567", "mComment size: ${mComment.size}, userList size: ${userList.size}")

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        var profileImage = itemView.findViewById<CircleImageView>(R.id.commentHomeProfilePhoto)
        var commentDescription = itemView.findViewById<AppCompatTextView>(R.id.comment)
        var nickname = itemView.findViewById<AppCompatTextView>(R.id.commentNickname)
        var commentTime = itemView.findViewById<AppCompatTextView>(R.id.commentTime)

        fun bind(comment: Comment, user: UserModel?) {
            commentDescription.text = comment.commentText
            nickname.text = user?.userNickName ?: " "

            if (user?.profilePhoto != null) {
                Log.d("eco333", "ProfilePhoto: ${user.profilePhoto}")

                Glide.with(mContext)
                    .load(user.profilePhoto)
                    .into(profileImage)
            } else {
                Log.d("eco333", "ProfilePhoto is null")

                Glide.with(mContext)
                    .load(R.drawable.ic_fifth)
                    .into(profileImage)
            }
            commentTime.text = comment.commentTime?.let { calculateTimeDifference(it) }

        }
    }
    private fun calculateTimeDifference(commentTime: String): String {
        val timestamp = commentTime.toLong()
        val currentDate = System.currentTimeMillis()

        val diffInMilli = currentDate - timestamp

        val seconds = diffInMilli / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "$seconds seconds ago"
        }
    }
}
