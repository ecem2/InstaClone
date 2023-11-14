package com.example.myapplication.ui.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.service.MyFirebaseMessagingService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import java.util.concurrent.atomic.AtomicInteger

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var isRunningValue: Boolean = true
    var senderUid: String? = null
    companion object {
        private const val TAG = "MainActivity"
        private const val NOTIFICATION_REQUEST_CODE = 1234
        const val NOTIFICATION_ID = 101
        const val KEY_EVENT_ACTION = "key_event_action"
        const val KEY_EVENT_EXTRA = "key_event_extra"
        private const val IMMERSIVE_FLAG_TIMEOUT = 500L
        const val CHANNEL_ID = "Canberk-odev"
    }
     @RequiresApi(Build.VERSION_CODES.O)
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = ActivityMainBinding.inflate(layoutInflater)
         setContentView(binding.root)
         FirebaseApp.initializeApp(this)
         onSendMessage()

         askNotificationPermission()
         subscribeTopics()
         senderUid = FirebaseAuth.getInstance().currentUser?.uid


         val navHostFragment =
             supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
         navController = navHostFragment.navController
         setupWithNavController(binding.bottomNavigationView, navController)
        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
        // [END handle_data_extras]
    }
     fun onSendMessage() {
        val intent = Intent(this, MyFirebaseMessagingService::class.java)
        startService(intent)
    }

    private val listener =
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.storyClickFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                R.id.userProfileFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                R.id.cameraFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                R.id.chatFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                R.id.messageFragment -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.GONE
                }
                else -> {
                    val navBar: BottomNavigationView = findViewById(R.id.bottomNavigationView)
                    navBar.visibility = View.VISIBLE
                }

            }

        }



    fun runtimeEnableAutoInit() {
        // [START fcm_runtime_enable_auto_init]
        Firebase.messaging.isAutoInitEnabled = true
        // [END fcm_runtime_enable_auto_init]
    }

    fun deviceGroupUpstream() {
        // [START fcm_device_group_upstream]
        val to = "a_unique_key" // the notification key
        val msgId = AtomicInteger()
        Firebase.messaging.send(
            remoteMessage(to) {
                setMessageId(msgId.get().toString())
                addData("hello", "world")
            },
        )
        // [END fcm_device_group_upstream]
    }

    fun sendUpstream() {
        val SENDER_ID = "YOUR_SENDER_ID"
        val messageId = 0 // Increment for each
        // [START fcm_send_upstream]
        val fm = Firebase.messaging
        fm.send(
            remoteMessage("$SENDER_ID@fcm.googleapis.com") {
                setMessageId(messageId.toString())
                addData("my_message", "Hello World")
                addData("my_action", "SAY_HELLO")
            },
        )
        // [END fcm_send_upstream]
    }

    fun subscribeTopics() {
        // [START subscribe_topics]
        Firebase.messaging.subscribeToTopic("weather")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
        // [END subscribe_topics]
    }

    fun logRegToken() {
        // [START log_reg_token]
        Firebase.messaging.getToken().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "FCM Registration token: $token"
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }
        // [END log_reg_token]
    }

    // [START ask_post_notifications]
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {

            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    fun isRunning(): Boolean {
        return isRunningValue
    }
    override fun onDestroy() {
        super.onDestroy()
        isRunningValue = false
    }
    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(listener)
        startService(Intent(this@MainActivity, MyFirebaseMessagingService::class.java))
        isRunningValue = true
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(listener)
        super.onPause()
    }




}