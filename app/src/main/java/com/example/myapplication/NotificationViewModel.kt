package com.example.myapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Message
import com.example.myapplication.model.PostModel
import com.example.myapplication.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel: ViewModel() {
    private var database: DatabaseReference = Firebase.database.reference
    private val _notificationData: MutableLiveData<ArrayList<Message>> = MutableLiveData()
    val notificationData: LiveData<ArrayList<Message>> get() = _notificationData


}