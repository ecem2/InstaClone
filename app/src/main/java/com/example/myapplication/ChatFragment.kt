package com.example.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
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
import androidx.annotation.RequiresApi
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

import com.example.myapplication.ui.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.internal.notify
import org.json.JSONObject
import java.io.Serializable

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
    var receiverFCMToken: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        databaseReference = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
        senderUid = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            MyFirebaseMessagingService.token = it
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

//
//        dialog = ProgressDialog(requireContext())
//        dialog!!.setMessage("Uploading image...")
//        dialog!!.setCancelable(false)
        return view

    }

    object MyFirebaseMessagingUtil {

        // Your shared functionality here
        fun onMessageReceiver() {
            // Implement the shared functionality here
        }
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

        lifecycleScope.launchWhenStarted {
            try {
                fetchReceiverToken()
            } catch (e: Exception) {
                Log.d("fatoss","receiverFCMToken EXCEPTION ${e.localizedMessage}")
            }
        }

        binding.gallery.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        binding.sendMessage.setOnClickListener {
            try {
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


               // sendNotification(sendName,messageText)
                Log.d("FATOSSS", "receiverToken = $receiverFCMToken")
                if (sendName.isNotEmpty() && messageText.isNotEmpty()) {
                    PushNotification(
                        NotificationData(sendName, messageText),
                        receiverFCMToken
                    ).also {
                        sendNotification(sendName, messageText)
                    }
                }

                binding.messageBox.setText("")


                val messageId = UUID.randomUUID().toString().substring(0, 15)
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
            } catch (e: Exception){
                Log.d("fatoss", "$e")
            }
        }
        getUserMessage()

    }

    private fun setupFirebaseMessaging() {
        val firebaseMessaging = FirebaseMessaging.getInstance()
        firebaseMessaging.subscribeToTopic("Chats") // Bu, konuya abone olmayı sağlar

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
//        val firebaseMessaging = FirebaseMessaging.getInstance()
//        firebaseMessaging.token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                Log.d("tokenn", "Firebase token: $token")
//
//                // Token'ı saklayın, bu şekilde mesaj gönderirken kullanabilirsiniz
//                receiverFCMToken = token
//            } else {
//                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
//            }
//        }
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
    private fun sendNotification(title: String, message: String) {
        val notificationData = hashMapOf(
            "title" to title,
            "message" to message
        )

        val notification = hashMapOf(
            "to" to receiverFCMToken,
            "data" to notificationData
        )
        Log.d("Notification", "Title: $title, Message: $message")

        try {
            FirebaseMessaging.getInstance().send(notification)
            Log.d("Notification", "Notification sent successfully")
        } catch (e: Exception) {
            Log.e("Notification", "Error sending notification: ${e.message}", e)
        }
    }

    private fun getMessage() {
        var message: Message? = null
        if (senderUid != null && receiverUid != null) {
        databaseReference.child("Chats")
            .child(senderUid!!)
            .child(receiverUid!!)
            .child("messages").orderByChild("timeStamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        message = dataSnapshot.getValue(Message::class.java)
                        message?.let {
                            if (!messages.contains(it)) {
                                messages.add(it)
                                //it.messageId?.let { messageId -> userIdList.add(messageId) }
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

    private fun fetchReceiverToken() {
        val tokenReference = databaseReference.child("Token").child(receiverUid!!)

        tokenReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    try {
                        receiverFCMToken = dataSnapshot.child("token").getValue(String::class.java).toString()
                        Log.e("fatoss", "receiverFCMToken dsdsdsds $receiverFCMToken")
                    } catch (e: java.lang.Exception) {
                        Log.e("fatoss", "UAUAUASDUASD ${e.localizedMessage}")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Hata işleme
                Log.e("FirebaseToken", "Token alımı iptal edildi: ${databaseError.message}")
            }
        })
    }





    private fun getUsersData(userModel: UserModel) {
        val userName = userModel.userName ?: ""
        val surname = userModel.userSurname ?: ""
        binding.chatNickname.text = "$userName $surname"
        Glide.with(requireContext()).load(userModel.profilePhoto).into(binding.chatPP)

    }


}

private fun FirebaseMessaging.send(notification: HashMap<String, Serializable>) {

}
