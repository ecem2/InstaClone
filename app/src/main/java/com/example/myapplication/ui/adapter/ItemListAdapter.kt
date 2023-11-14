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
import androidx.appcompat.widget.LinearLayoutCompat
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
import com.google.firebase.database.ktx.getValue
import de.hdodenhof.circleimageview.CircleImageView

class ItemListAdapter(
    val context: Context,
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
    val commentList: ArrayList<String> = ArrayList()

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
        val postId = postItem.postId

        if (postList.isNotEmpty() && !postList.isNullOrEmpty()) {
            checkLikeState(holder, postItem)
            getCommentCount(postId!!, holder, postItem)
            checkLikeCount(holder, postItem)

            var userItem: UserModel? = null

            for (user in userList) {
                if (user.userId == postItem.userId) {
                    userItem = user
                }
            }

            val likeCount = holder.itemView.findViewById<AppCompatTextView>(R.id.likeCount)
            val commentNicknameLL = holder.itemView.findViewById<LinearLayoutCompat>(R.id.commentNicknameLL)
            val commentCount = holder.itemView.findViewById<AppCompatTextView>(R.id.commentCount)
            val timestampTV = holder.itemView.findViewById<AppCompatTextView>(R.id.timestamp)
            val llBottomSection = holder.itemView.findViewById<LinearLayoutCompat>(R.id.ll_bottom_section)

            // Kontrolleri ekleyin
            if (postItem.likeArray != null && postItem.likeArray.isNotEmpty()) {
                likeCount.text = context.getString(R.string.like_count).format(postItem.likeArray.size.toString())
                commentCount.visibility = View.VISIBLE
            } else {
                likeCount.visibility = View.GONE
                commentCount.visibility = View.GONE
                timestampTV.visibility = View.VISIBLE
                llBottomSection.bringToFront()
            }

            userItem?.let { holder.bindItems(postItem, userItem) }

            val nicknameLL = holder.itemView.findViewById<LinearLayoutCompat>(R.id.nicknamePP)
            nicknameLL.setOnClickListener {
                if (userItem != null) {
                    onNickNameClickListener.onClick(userItem)
                }
            }

            val timestamp = holder.itemView.findViewById<AppCompatTextView>(R.id.timestamp)
            timestamp.text = postItem.timestamp?.let { TimeAgo.getTimeAgo(it.toLong()) }

            val commentView = holder.itemView.findViewById<ImageView>(R.id.commentButton)
            commentView.setOnClickListener {
                onCommentClickListener.onClick(postItem)
            }
        }
    }
    private fun getCommentCount(postId: String, holder: ItemListViewHolder, postItem: PostModel) {
        val commentCount = holder.itemView.findViewById<AppCompatTextView>(R.id.commentCount)

        postRef.child(postId).child("commentList").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0){
                    commentCount.visibility = View.VISIBLE
                    commentCount.text = context.getString(R.string.comment_count).format(snapshot.childrenCount.toString())
                }
                else {
                    commentCount.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        commentCount.setOnClickListener {
            onCommentCountClickListener.onClick(postItem)
        }
    }


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

        likeImageView.setOnClickListener {
            onLikeClickListener.onClick(postItem)
            likePost(postItem, holder)
        }
    }

    private fun checkLikeCount(holder: ItemListViewHolder, postItem: PostModel) {
        val likeCount = holder.itemView.findViewById<TextView>(R.id.likeCount)

        if (postItem.likeArray != null) {
            val likeCountText = context.getString(R.string.like_count).format(postItem.likeArray.size.toString())
            likeCount.text = likeCountText
            likeCount.visibility = View.VISIBLE
        } else {
            val likeCountText = context.getString(R.string.like_count).format("1")
            likeCount.text = likeCountText
            likeCount.visibility = View.VISIBLE
        }

        likeCount.setOnClickListener {
            onLikeCountListener.onClick(postItem)
        }
    }


    private fun likePost(postItem: PostModel, holder: ItemListViewHolder) {
        val likeButton = holder.itemView.findViewById<ImageView>(R.id.likeButton)
        val likeCount = holder.itemView.findViewById<TextView>(R.id.likeCount)
        val postId = postItem.postId.toString()

        val userLiked = postItem.likeArray?.contains(firebaseUser.uid) == true
        val postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId)

        postRef.child("likeArray").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likeArray = snapshot.getValue<List<String>>()
                if (likeArray == null) {
                    // Eğer likeArray daha önce oluşturulmamışsa, yeni bir likeArray oluştur
                    val newLikes = mutableListOf<String>()
                    newLikes.add(firebaseUser.uid)
                    postRef.child("likeArray").setValue(newLikes)
                } else {
                    // Eğer likeArray zaten varsa, mevcut duruma göre güncelle
                    if (userLiked) {
                        // Kullanıcı beğenisini geri çekti
                        postItem.likeArray?.remove(firebaseUser.uid)
                    } else {
                        // Kullanıcı beğeni ekledi
                        postItem.likeArray?.add(firebaseUser.uid)
                    }
                    postRef.child("likeArray").setValue(postItem.likeArray)
                }

                // UI'ı güncelle
                updateLikeUI(holder, likeArray)
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunu yönetmek için gerekirse burada işlem yapabilirsiniz
            }
        })
    }

    private fun updateLikeUI(holder: ItemListViewHolder, likeList: List<String>?) {
        val likeButton = holder.itemView.findViewById<ImageView>(R.id.likeButton)
        val likeCount = holder.itemView.findViewById<TextView>(R.id.likeCount)

        if (likeList != null) {
            // LikeArray varsa ve içerisinde beğeniler bulunuyorsa
            val userLiked = likeList.contains(firebaseUser.uid)
            if (userLiked) {
                // Kullanıcı beğenmişse
                Glide.with(holder.itemView)
                    .load(R.drawable.ic_insta_red_like)
                    .into(likeButton)
            } else {
                // Kullanıcı beğenmemişse
                Glide.with(holder.itemView)
                    .load(R.drawable.ic_insta_post_like)
                    .into(likeButton)
            }

            // Like sayısını güncelle
            val likeCountText = context.getString(R.string.like_count).format(likeList.size.toString())
            likeCount.text = likeCountText
            likeCount.visibility = View.VISIBLE
        } else {
            // LikeArray boşsa veya null ise
            Glide.with(holder.itemView)
                .load(R.drawable.ic_insta_post_like)
                .into(likeButton)
            likeCount.visibility = View.GONE
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
                val nicknameLL: LinearLayoutCompat = itemView.findViewById(R.id.nicknamePP)
                val commentNicknameLL: LinearLayoutCompat = itemView.findViewById(R.id.commentNicknameLL)
                val photoInfo: AppCompatTextView = itemView.findViewById(R.id.userNicknameComment)
                val commentNickname: AppCompatTextView = itemView.findViewById(R.id.userNicknameExplanation)

                commentNickname.text = userModel.userNickName
                nickName.text = userModel.userName
                Glide.with(context).load(postModel.postPhoto?.first()).into(postImageView)
                Glide.with(context).load(userModel.profilePhoto).into(profilePhoto)
                if (postModel.photoInfo != null && !postModel.photoInfo.isEmpty()) {
                    photoInfo.text = postModel.photoInfo
                    commentNicknameLL.visibility = View.VISIBLE
                } else {
                    photoInfo.text = ""
                    commentNicknameLL.visibility = View.GONE
                }
            }

        }

    }
}

