package com.example.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//@Parcelize
//data class Comment(
//    var userId: String? = null,
//    var commentLikeArray: ArrayList<String>? = null,
//    var commentText: String? = null,
//    var commentId: String? = null,
//    var commentTime: String? = null
//): Parcelable

@Parcelize
data class Comment(
    var userId: String? = null,
    var commentLikeArray: List<String>? = null,
    var commentText: String? = null,
    var commentId: String? = null,
    var commentTime: String? = null
) : Parcelable
