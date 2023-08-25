package com.example.myapplication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Story(
    var index: Int = 0,
    var user: UserModel?
): Parcelable