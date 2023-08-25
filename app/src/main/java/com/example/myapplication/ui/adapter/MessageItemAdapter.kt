package com.example.myapplication.ui.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.ChatFragment
import com.example.myapplication.R
import com.example.myapplication.model.Message
import com.example.myapplication.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView

class MessageItemAdapter(
    val context: Context
) : RecyclerView.Adapter<MessageItemAdapter.ViewHolder>() {
    private lateinit var databaseReference: DatabaseReference
    var senderUid: String? = null
    var receiverUid: String? = null
    val receiverUidList = ArrayList<String>()
    val updateUserList: ArrayList<UserModel> = ArrayList()
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return updateUserList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = updateUserList[position]

        if (user.profilePhoto!!.isEmpty()) {
            holder.profileImage.setImageResource(R.drawable.ic_fifth)
        } else {
            Glide.with(context).load(user.profilePhoto)
                .into(holder.profileImage)
        }
        var userItem: UserModel? = null
        // var commentItem: CommentData? = null

        for (user in updateUserList) {
            if (user.userId == user.userId) {
                userItem = user
            }
        }


        holder.userFullName.text = user.userName + " " + user.userSurname

        holder.messageAbs.text = user.userNickName

        user.userId?.let { getMessage(holder, it) }
        //getConversationParticipants(holder)
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable("clickedUserId", user)
            it.findNavController().navigate(R.id.chatFragment, bundle)
        }
    }

    fun submitMessageList(messages: ArrayList<UserModel>) {
        updateUserList.addAll(messages)
        notifyDataSetChanged()
//        notifyItemRangeChanged(0, userList.size)
    }


    fun getMessage(holder: ViewHolder, receiverUid: String) {
        databaseReference = FirebaseDatabase.getInstance().reference
        senderUid = FirebaseAuth.getInstance().currentUser?.uid

        val chatRef = databaseReference.child("Chats")
            .child(senderUid!!)
            .child(receiverUid)
            .child("messages")
            .orderByKey()
            .limitToLast(1)
        Log.d("adaad", "$chatRef")
        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val messageSnapshot = dataSnapshot.children.firstOrNull()
                    val lastMessage = messageSnapshot?.child("message")?.getValue(String::class.java)
                    if (messageSnapshot != null) {
                        Log.d("MessageSnapshot", "Key: ${messageSnapshot.key}, Value: ${messageSnapshot.value}")
                    }


                    Log.d("LastMessage", "Last message: $lastMessage")

                    if (lastMessage != null) {

                        holder.messageAbs.text = lastMessage
                    } else {
                        holder.messageAbs.text = "No messages yet"
                    }
                } else {
                    holder.messageAbs.text = "No messages yet"
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userFullName: TextView = itemView.findViewById(R.id.messageNickName)
        var messageAbs: TextView = itemView.findViewById(R.id.messageText)
        var profileImage: CircleImageView = itemView.findViewById(R.id.messageProfilePhoto)
    }
}