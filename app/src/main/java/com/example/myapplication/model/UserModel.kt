package com.example.myapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserModel(
    @SerializedName(value = "userId")
    var userId: String? = null,
    @SerializedName(value = "userName")
    val userName: String? = null,
    @SerializedName(value = "userNickName")
    var userNickName: String? = null,
    @SerializedName("userSurname")
    val userSurname: String? = null,
    @SerializedName(value = "profilePhoto")
    var profilePhoto: String? = null,
    @SerializedName("followingArray")
    val followingArray: ArrayList<String>? = null,
    @SerializedName("followersArray")
    val followersArray: ArrayList<String>? = null,
    @SerializedName("userStory")
    val userStory: ArrayList<String>? = null,
    @SerializedName("bioInfo")
    val bioInfo: String? = null
): Parcelable



