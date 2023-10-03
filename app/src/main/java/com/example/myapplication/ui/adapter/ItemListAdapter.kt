package com.example.myapplication.ui.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.TimeAgo
import com.example.myapplication.model.Comment
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView


class ItemListAdapter(
    val context: Context,
    // TODO INTERFACELER BURAYA
    private val onLikeCountListener: OnLikeCountListener,
    private val onLikeClickListener: OnLikeClickListener,
    private val onCommentClickListener: OnCommentClickListener,
    private val onCommentCountClickListener: OnCommentCountClickListener,
    private val onNickNameClickListener : OnNickNameClickListener
) : RecyclerView.Adapter<ItemListAdapter.ItemListViewHolder>() {

    val postList: ArrayList<PostModel> = ArrayList()
    private lateinit var databaseReference: DatabaseReference
    val postId: String? = null
    private val userList: ArrayList<UserModel> = ArrayList()
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private val likeArrayList: ArrayList<String> = ArrayList()
    val commentsList: ArrayList<Comment> = ArrayList()

    private var postRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Posts")
    val likeArrayRef = postRef.child("likeArray")
    private val likeList: ArrayList<UserModel> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        return ItemListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.card_view,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        val postItem = postList[position]
//        if (postId != null) {
//            getCommentCount(holder, postId)
//        }
        Log.d("Ecco", "${postList.size}")
        if (postList.isNotEmpty() && !postList.isNullOrEmpty()) {
            checkLikeState(holder, postItem)
            var userItem: UserModel? = null
           // var commentItem: CommentData? = null

            for (user in userList) {
                if (user.userId == postItem.userId) {
                    userItem = user
                }
            }

            userItem?.let { holder.bindItems(postItem, it) }

            val likeCount = holder.itemView.findViewById<AppCompatTextView>(R.id.likeCount)

            if (postItem.likeArray != null) {
                likeCount.text =
                    context.getString(R.string.like_count)
                        .format(postItem.likeArray.size.toString())
                likeCount.visibility = View.VISIBLE
            } else {
                likeCount.visibility = View.GONE
            }
            val commentView = holder.itemView.findViewById<ImageView>(R.id.commentButton)
            commentView.setOnClickListener {
                onCommentClickListener.onClick(postItem)
            }
            val userNickname = holder.itemView.findViewById<TextView>(R.id.userNickname)
            userNickname.setOnClickListener {
                if (userItem != null) {
                    onNickNameClickListener.onClick(userItem)
                }
            }
            val timestamp = holder.itemView.findViewById<AppCompatTextView>(R.id.timestamp)
            timestamp.text = postItem.timestamp?.let { TimeAgo.getTimeAgo(it.toLong()) }
            val homeCommentCount = holder.itemView.findViewById<TextView>(R.id.commentCount)
//            commentItem?.comments = commentsList
            val comments = postItem?.comments
            val commentCount = comments?.size ?: 0
            Log.d("CommentCount", "$commentCount")

            if (commentCount > 0) {
                homeCommentCount.text = context.getString(R.string.comment_count).format(commentCount.toString())
                homeCommentCount.visibility = View.VISIBLE
            } else {
                homeCommentCount.visibility = View.GONE
            }
            homeCommentCount?.setOnClickListener {
                if (postItem != null) {
                    onCommentCountClickListener.onClick(postItem)
                }
            }

            val likeImageView = holder.itemView.findViewById<ImageView>(R.id.likeButton)
            likeCount.setOnClickListener {
                onLikeCountListener.onClick(postItem)
            }
            likeImageView.setOnClickListener {
                onLikeClickListener.onClick(postItem)
                likePost(postItem, holder)
            }
        }
    }
//    private fun getCommentCount(holder: ItemListViewHolder, postId: String) {
//
//        val commentsRef = databaseReference.child("Posts").child(postId).child("comments")
//        val homeCommentCount = holder.itemView.findViewById<TextView>(R.id.homeCommentText)
//
//        commentsRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val commentCount = snapshot.childrenCount
//                homeCommentCount?.text = "Comment Count: $commentCount"
//                homeCommentCount.visibility = View.VISIBLE
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // İşlem iptal edildiğinde yapılacak işlemler
//            }
//        })
//    }
    private fun checkLikeState(holder: ItemListViewHolder, postItem: PostModel) {
        val likeImageView = holder.itemView.findViewById<ImageView>(R.id.likeButton)

        if (postItem.likeArray?.contains(firebaseUser.uid) == true) {
            Log.d("ViewModel", "POST HAVE USER")
            Glide.with(holder.itemView)
                .load(R.drawable.ic_insta_red_like)
                .into(likeImageView)
        } else {
            Glide.with(holder.itemView)
                .load(R.drawable.ic_insta_post_like)
                .into(likeImageView)
        }
    }


    private fun likePost(postItem: PostModel, holder: ItemListViewHolder) {
        val likeButton = holder.itemView.findViewById<ImageView>(R.id.likeButton)

        // Kullanıcının UID'sini beğenme listesine ekleyin veya çıkarın
        val userLiked = postItem.likeArray?.contains(firebaseUser.uid) == true
        if (userLiked) {
            postItem.likeArray?.remove(firebaseUser.uid)
        } else {
            postItem.likeArray?.add(firebaseUser.uid)
        }

        // Beğeni durumunu Firebase veritabanında güncelleyin
        val postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postItem.postId.toString())
        postRef.child("likeArray").setValue(postItem.likeArray)

        // Beğeni simgesini güncelleyin
        if (userLiked) {
            Glide.with(holder.itemView)
                .load(R.drawable.ic_insta_post_like)
                .into(likeButton)
        } else {
            Glide.with(holder.itemView)
                .load(R.drawable.ic_insta_red_like)
                .into(likeButton)
        }
    }


    fun submitPostsList(posts: ArrayList<PostModel>) {
        postList.addAll(posts)
        notifyDataSetChanged()
    }

    fun submitUsersList(users: ArrayList<UserModel>) {
        userList.addAll(users)
        notifyDataSetChanged()
    }

    fun submitLikeList(like: ArrayList<String>) {
        likeArrayList.addAll(like)
        notifyDataSetChanged()
    }


    class OnCommentClickListener(val clickListener: (postModel: PostModel) -> Unit) {
        fun onClick(postModel: PostModel) = clickListener(postModel)
    }

    class OnLikeCountListener(val clickListener: (postModel: PostModel) -> Unit) {
        fun onClick(postModel: PostModel) = clickListener(postModel)
    }

    class OnLikeClickListener(val clickListener: (postModel: PostModel) -> Unit) {
        fun onClick(postModel: PostModel) = clickListener(postModel)
    }
    class OnCommentCountClickListener(val clickListener: (postModel: PostModel) -> Unit) {
        fun onClick(postModel: PostModel) = clickListener(postModel)
    }
    class OnNickNameClickListener(val clickListener: (userModel: UserModel) -> Unit) {
        fun onClick(userModel: UserModel) = clickListener(userModel)
    }

    inner class ItemListViewHolder(itemView: View) : ViewHolder(itemView) {

        fun bindItems(postModel: PostModel, userModel: UserModel) {
            if (postList.isNotEmpty() && !postList.isNullOrEmpty()) {
                val postImageView: AppCompatImageView = itemView.findViewById(R.id.postPhoto)
                val profilePhoto: CircleImageView = itemView.findViewById(R.id.profilePhoto)
                val nickName: AppCompatTextView = itemView.findViewById(R.id.userNickname)


                nickName.text = userModel.userName
                Glide.with(context).load(postModel.postPhoto?.first()).into(postImageView)
                Glide.with(context).load(userModel.profilePhoto).into(profilePhoto)
            }

        }

    }
}

