package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.FragmentChatBinding
import com.example.myapplication.extension.hideKeyboard
import com.example.myapplication.model.Message
import com.example.myapplication.model.NotificationData
import com.example.myapplication.model.PushNotification
import com.example.myapplication.model.UserModel
import com.example.myapplication.service.MyFirebaseMessagingService
import com.example.myapplication.service.MyFirebaseMessagingService.Companion.token
import com.example.myapplication.ui.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.util.*
import kotlin.collections.ArrayList

const val TOPIC = "/topics/myTopic"
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
   // private lateinit var receiverFCMToken: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
        setupFirebaseMessaging()
//
//        dialog = ProgressDialog(requireContext())
//        dialog!!.setMessage("Uploading image...")
//        dialog!!.setCancelable(false)

        binding.gallery.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        binding.sendMessage.setOnClickListener {
            val messageText: String = binding.messageBox.text.toString()
            val sendName: String = binding.chatNickname.text.toString()
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
            if(sendName.isNotEmpty() && messageText.isNotEmpty()){
                PushNotification(
                    NotificationData(sendName, messageText),
                    TOPIC
                ).also {
                    sendNotification(it)
                }
            }

            binding.messageBox.setText("")


            val messageId = UUID.randomUUID().toString().substring(0,15)
                //databaseReference.child("Chats").child(senderUid!!)
               // .child(receiverUid!!).child("messages").push().key
            Log.e("FirebaseErrorrr", "Message sender uid: ${senderUid}")
            Log.e("FirebaseErrorrr", "Message receiver uid: ${receiverUid}")

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

            // sendMessage(messageText, receiverUid!!)  // Mesaj gönderme işlemini çağırın
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
            chatUserData = it.getParcelable("clickedUserIdModel")
            if (chatUserData != null) {
                Log.d("aaaaaa", "$chatUserData")
                receiverUid = chatUserData?.userId
                Log.d("bbbbb", "$receiverUid")
                getUsersData(chatUserData!!)
               // fcmToken()

            }
        }
        setupRecyclerChatView()
        getMessage()



    }
    private fun setupFirebaseMessaging() {
//        val firebaseMessaging = FirebaseMessaging.getInstance()
//        firebaseMessaging.subscribeToTopic("Chats") // Bu, konuya abone olmayı sağlar
//
//        val service = MyFirebaseMessagingService()
//
//        firebaseMessaging.token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                Log.d("tokenn", "Firebase token: $token")
//                service.onNewToken(token)
//            } else {
//                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
//            }
//        }
        val firebaseMessaging = FirebaseMessaging.getInstance()
        firebaseMessaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("tokenn", "Firebase token: $token")
                // Token'ı saklayın
              //  receiverFCMToken = token
            } else {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
            }
        }

    }


    //    private fun sendMessage(messageText: String, title: String) {
//        if (messageText.isNotEmpty()) {
//            val fcmData = HashMap<String, String>()
//            fcmData["title"] = title // Bildirim başlığı olarak kullanıcının takma adını ayarlayın
//            fcmData["body"] = messageText // Bildirim metni olarak gönderilen mesajı ayarlayın
//            val recipientUid = receiverUid ?: return
//
//            // Use a coroutine to wait for the FCM message to be sent
//            lifecycleScope.launch(Dispatchers.IO) {
//                try {
//                    val response = Firebase.messaging.send(
//                        remoteMessage(recipientUid) {
//                            data = fcmData
//                            setupFirebaseMessaging()
//                        }
//                    )
//                    Log.d("sendMessageaaa", "Response: $response")
//                    // Notification will be triggered from the service
//
//                    // Clear the message box
//                    withContext(Dispatchers.Main) {
//                        binding.messageBox.setText("")
//                    }
//                } catch (e: Exception) {
//                    Log.e("sendMessage", "Error sending FCM message: ${e.message}")
//                }
//            }
//        }
//    }
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
        if (senderUid != null && receiverUid != null) {
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                val selectedImage = data.data


            }
        }
    }
//    private fun fcmToken() {
//        databaseReference.child("Token").child(receiverUid!!).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // "Token" düğümü varsa, altındaki veriyi alın
//                    val tokenData = dataSnapshot.getValue() as? HashMap<String, Any>
//                    if (tokenData != null) {
//                        // "token" anahtarını kullanarak FCM token'ı alın
//                        receiverFCMToken = tokenData["token"] as? String ?: ""
//                    }
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Hata işleme
//            }
//        })
//    }






    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            // receiverFCMToken = notification.to
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d("adad", "Notification sent successfully")
            } else {
                Log.e("adad", "Notification sending failed with code: ${response.code()}")
                Log.e("adad", "Error response body: ${response.errorBody()?.string()}")
                // Burada bildirim gönderme hatasıyla ilgili işlemleri gerçekleştirebilirsiniz.
            }
        } catch (e: Exception) {
            Log.e("adad", "Notification sending failed with exception: ${e.toString()}")
            // Burada bildirim gönderme hatasıyla ilgili işlemleri gerçekleştirebilirsiniz.
        }
    }


    private fun getUsersData(userModel: UserModel) {
        val userName = userModel.userName ?: ""
        val surname = userModel.userSurname ?: ""
        binding.chatNickname.text = "$userName $surname"
        Glide.with(requireContext()).load(userModel.profilePhoto).into(binding.chatPP)

    }


}