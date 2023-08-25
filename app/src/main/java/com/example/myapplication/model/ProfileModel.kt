package com.example.myapplication.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileModel(
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("profilePhoto")
    val profilePhoto : String? = null,
    @SerializedName("userNickName")
    val userNickName: String? = null,
    @SerializedName(value = "postPhoto")
    val postPhoto: ArrayList<String>? = null,
    @SerializedName("userName")
    val userName: String? = null,
    @SerializedName("userSurname")
    val userSurname: String? = null,
    @SerializedName("followingArray")
    val followingArray: ArrayList<String>? = null,
    @SerializedName("followersArray")
    val followersArray: ArrayList<String>? = null,
    @SerializedName("bioInfo")
    val bioInfo: String? = null,
): Parcelable