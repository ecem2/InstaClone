package com.example.myapplication.ui.camera

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

fun Context.dimenToPx(@DimenRes dimen: Int): Int {
    return resources.dimenToPx(dimen)
}

fun Context.call(phone: String?) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phone")
    startActivity(intent)
}

fun Context.font(@FontRes resource: Int): Typeface? {
    return ResourcesCompat.getFont(this, resource)
}

fun Context.color(@ColorRes resource: Int): Int {
    return ContextCompat.getColor(this, resource)
}

fun Context.colorStateList(@ColorRes resource: Int): ColorStateList? {
    return ContextCompat.getColorStateList(this, resource)
}

fun Context.drawable(@DrawableRes resource: Int): Drawable? {
    return ContextCompat.getDrawable(this, resource)
}
