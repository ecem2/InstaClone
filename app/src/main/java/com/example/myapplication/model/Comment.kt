package com.example.myapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comment(
    @SerializedName(value = "commentId")
    val commentId: String? = null,
    @SerializedName(value = "commentText")
    val commentText: String? = null,
    @SerializedName(value = "commentTime")
    val commentTime: String? = null,
    @SerializedName(value = "userId")
    val userId: String? = null,

) : Parcelable
