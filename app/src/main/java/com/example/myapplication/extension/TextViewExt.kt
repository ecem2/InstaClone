package com.example.myapplication.extension

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.TextView

fun TextView.setClickableText(font1: Typeface, font2: Typeface, text: String): TextView {
    val spanStr = SpannableStringBuilder(text)
    spanStr.setSpan(CustomTypefaceSpan("", font1), 0, 16, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    spanStr.setSpan(CustomTypefaceSpan("", font2), 16, 25, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
    this.text = spanStr
    return this
}