package com.example.myapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostModel(
    @SerializedName(value = "userId")
    val userId: String? = null,
    @SerializedName(value = "timestamp")
    val timestamp: String? = null,
    @SerializedName(value = "photoInfo")
    var isLike: Boolean = false,
    @SerializedName(value = "postPhoto")
    val postPhoto: ArrayList<String>? = null,
    @SerializedName(value = "likeArray")
    val likeArray: ArrayList<String>? = null,
    @SerializedName(value = "postId")
    val postId: String? = null
//    @SerializedName(value = "comments")
//    var comments: ArrayList<Comment>? = null
): Parcelable
