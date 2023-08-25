package com.example.myapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName


data class ItemModel(
    val image: Int,
    val profilePhoto: Int,
    val text: String
// val description: String,
    //val longText: String
)
