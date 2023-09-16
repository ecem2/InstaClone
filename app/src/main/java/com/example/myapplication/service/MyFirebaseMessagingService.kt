package com.example.myapplication.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.myapplication.R
import com.example.myapplication.RetrofitInstance
import com.example.myapplication.model.NotificationData
import com.example.myapplication.model.PushNotification
import com.example.myapplication.ui.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MyFirebaseMessagingService : FirebaseMessagingService() {

    val CHANNEL_ID = "canberk-odev"
    companion object{
        var sharedPref: SharedPreferences? = null

        var token:String?
            get(){
                return sharedPref?.getString("token","")
            }
            set(value){
                sharedPref?.edit()?.putString("token",value)?.apply()
            }

    }



    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MyFirebaseMessagingService.token = token
    }

    @SuppressLint("MissingPermission")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FATOS SERVICE", "From: ${remoteMessage.from}")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this@MyFirebaseMessagingService, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["message"])
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)


        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        createNotificationChannel(notificationManager)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "canberk-odev"
            val channelName = "ChannelFirebaseChat"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(remoteMessage.data["title"], remoteMessage.data["message"], importance)
            notificationManager.createNotificationChannel(channel)
        }


        val pref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val userUid = pref?.getString("USER_ID", null).toString()
        sendNotification(
            PushNotification(
                NotificationData(
                    remoteMessage.data["title"].toString(),
                    remoteMessage.data["message"].toString()
                ),
                userUid
            )
        )

        notificationManager.notify(1, builder.build())


//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
//            .setContentTitle(remoteMessage.data["title"])
//            .setContentText(remoteMessage.data["message"])
//            .setPriority(NotificationCompat.PRIORITY_MAX)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentIntent(pendingIntent)
//            .build()

//        notificationManager.notify(notificationId,notification)

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FATOSS SERVICE", "Message Notification Body: ${it.body}")
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d("fatoss", "Notification sent successfully")
            } else {
                Log.e("fatoss", "Notification sending failed with code: ${response.code()}")
                Log.e("fatoss", "Error response body: ${response.errorBody()?.string()}")
                // Burada bildirim gönderme hatasıyla ilgili işlemleri gerçekleştirebilirsiniz.
            }
        } catch (e: Exception) {
            Log.e("fatoss", "Notification sending failed with exception: ${e.toString()}")
            // Burada bildirim gönderme hatasıyla ilgili işlemleri gerçekleştirebilirsiniz.
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){



//        val notificationChannel = NotificationChannel(channelId,channelName,IMPORTANCE_HIGH)
//            notificationChannel.enableLights(true)
//            notificationChannel.lightColor = Color.RED
//            notificationChannel.enableVibration(true)
//            notificationManager.createNotificationChannel(notificationChannel)
//

    }
}