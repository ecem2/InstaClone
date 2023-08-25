package com.example.myapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentChatBinding
import com.example.myapplication.extension.hideKeyboard
import com.example.myapplication.model.Comment
import com.example.myapplication.model.Message
import com.example.myapplication.model.UserModel
import com.example.myapplication.ui.adapter.CommentAdapter
import com.example.myapplication.ui.adapter.MessageAdapter
import com.example.myapplication.ui.adapter.SearchAdapter
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    var messageAdapter: MessageAdapter? = null
    var messages: ArrayList<Message> = ArrayList()
    var chatUserData: UserModel? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        dialog = ProgressDialog(requireContext())
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)


        binding.gallery.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        binding.sendMessage.setOnClickListener {
            val messageText: String = binding.messageBox.text.toString()
            val date = Date()
            val message = Message(
                message = messageText,
                senderId = senderUid,
                receiverId = receiverUid,
                timeStamp = date.time
            )
            val receiverMessage = Message(
                message = messageText,
                senderId = senderUid,
                receiverId = receiverUid,
                timeStamp = date.time
            )

            binding.messageBox.setText("")

            val messageId = databaseReference.child("Chats").child(senderUid!!)
                .child(receiverUid!!).child("messages").push().key

            databaseReference.child("Chats").child(senderUid!!).child(receiverUid!!)
                .child("messages").child(messageId!!)
                .setValue(message).addOnSuccessListener {

                    // Mesaj başarıyla gönderildi
                }.addOnFailureListener { e ->
                    Log.e("FirebaseError", "Message sending failed: ${e.message}")
                }
            val receiverMessageId = databaseReference.child("Chats").child(receiverUid!!)
                .child(senderUid!!).child("messages").push().key
            databaseReference.child("Chats").child(receiverUid!!).child(senderUid!!)
                .child("messages").child(receiverMessageId!!)
                .setValue(receiverMessage).addOnSuccessListener {
                    // Mesaj başarıyla gönderildi
                    Handler(Looper.getMainLooper()).postDelayed({
                        getMessage()
                    }, 100)
                }.addOnFailureListener { e ->
                    Log.e("FirebaseError", "Message sending failed: ${e.message}")
                }

            sendMessage()  // Mesaj gönderme işlemini çağırın
            hideKeyboard()
        }
        getUserMessage()

        return view
    }

    fun getUserMessage() {
        val handler = Handler()
        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                databaseReference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 100)
            }

            val userStoppedTyping = Runnable {
                databaseReference.child("Presence")
                    .child(senderUid!!)
                    .setValue("online")
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backIV.setOnClickListener {
            findNavController().popBackStack()
        }
        arguments?.let {
            chatUserData = it.getParcelable("clickedUserId")
            if (chatUserData != null) {
                Log.d("aaaaaa", "$chatUserData")
                receiverUid = chatUserData?.userId
                Log.d("bbbbb", "$receiverUid")
                getUsersData(chatUserData!!)

            }
        }
        setupRecyclerChatView()
        getMessage()


    }


    private fun sendMessage() {
        val messageText: String = binding.messageBox.text.toString()
        if (messageText.isNotEmpty()) {
            // Mesajı göndermek için gerekli işlemler
            // Mesajı temizle
            binding.messageBox.setText("")
        }
    }

    private fun setupRecyclerChatView() {
        messageAdapter = MessageAdapter(requireContext(), messages)
        binding.chatRV.apply {
            adapter = messageAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            (layoutManager as LinearLayoutManager).stackFromEnd =
                true // Yeni mesajlar eklenince en altta görüntülemek için
        }
    }

    private fun getMessage() {
        val userIdList: ArrayList<String> = ArrayList()
        var message: Message? = null
        databaseReference.child("Chats").child(senderUid!!).child(receiverUid!!).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        message = dataSnapshot.getValue(Message::class.java)
                        message?.let {
                            if (!messages.contains(it)) {
                                messages.add(it)
                                it.messageId?.let { messageId -> userIdList.add(messageId) }
                            }
                        }
                        Log.d("messagess", "$messages")
                    }

                    messageAdapter?.notifyDataSetChanged()  // Tüm veri değişikliklerini bildirin
                    binding.chatRV.scrollToPosition(messages.size - 1)
                }
                override fun onCancelled(error: DatabaseError) {
                    // Hata durumu
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                val selectedImage = data.data


            }
        }
    }

    private fun getUsersData(userModel: UserModel) {
        val userName = userModel.userName ?: ""
        val surname = userModel.userSurname ?: ""
        binding.chatNickname.text = "$userName $surname"
        Glide.with(requireContext()).load(userModel.profilePhoto).into(binding.chatPP)

    }


}