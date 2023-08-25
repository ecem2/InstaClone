package com.example.myapplication.model

data class Message(
    var messageId: String? = null,
    var message: String? = null,
    var senderId: String? = null,
    var receiverId: String? = null,
    var imageUrl: String? = null,
    var timeStamp: Long? = 0
)