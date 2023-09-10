package com.example.myapplication

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentMessageBinding
import com.example.myapplication.model.Message
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.MyFirebaseMessagingService
import com.example.myapplication.ui.adapter.MessageAdapter
import com.example.myapplication.ui.adapter.MessageItemAdapter
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseReference: DatabaseReference
    var senderUid: String? = null
    private lateinit var messageItemAdapter: MessageItemAdapter
    var userList: ArrayList<UserModel> = ArrayList()

    //private var storage: Storage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        databaseReference = FirebaseDatabase.getInstance().reference
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        MyFirebaseMessagingService.sharedPref = activity?.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            MyFirebaseMessagingService.token = it
        }
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.messageButton.setOnClickListener {
            val newMessageFragment = NewMessageFragment()
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, newMessageFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
        setupRecyclerMessageView()
        getUsersChatData()
        //  fetchChatMessages()
    }

    private fun setupRecyclerMessageView() {
        messageItemAdapter = MessageItemAdapter(requireContext())
        binding.messageRV.apply {
            adapter = messageItemAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

//    fun fetchChatMessages() {
//        val chatRef = databaseReference.child("Chats").child(senderUid!!)
//
//        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val messageList = mutableListOf<Message>()
//
//                for (messageSnapshot in dataSnapshot.children) {
//                    val message = messageSnapshot.getValue(Message::class.java)
//                    message?.let {
//                        messageList.add(it)
//                    }
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Hata durumunda yapılacak işlemler
//            }
//        })
//    }

    private fun getUsersChatData() {
        var userChatList: ArrayList<String> = ArrayList()
        databaseReference.child("Chats").child(senderUid!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshotUser: DataSnapshot) {
                    if (snapshotUser.exists()) {
                        for (interactionSnapshot in snapshotUser.children) {
                            interactionSnapshot.key?.let { userChatList.add(it) }
                        }
                        getUserData(userChatList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun getUserData(receiverList: ArrayList<String>) {
        val updatedUserList = mutableListOf<UserModel>()

        for (receiverUid in receiverList) {
            val existingUser = updatedUserList.find { it.userId == receiverUid }

            if (existingUser == null) {
                databaseReference.child("Users").child(receiverUid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(profileSnapshot: DataSnapshot) {
                            try {
                                val profileData = profileSnapshot.getValue(UserModel::class.java)
                                profileData?.let {
                                    updatedUserList.add(it)
                                    Log.d("ECEMMM", "updatedUserList == $updatedUserList")
                                    Log.d("ECEMMM", "profileData ${profileData}")

                                    if (updatedUserList.size == receiverList.size) {
                                        messageItemAdapter.submitMessageList(updatedUserList as ArrayList<UserModel>)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ECEMMM", "Exception ${e.localizedMessage}")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }
        }
    }
}