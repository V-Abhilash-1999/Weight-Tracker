package com.example.weighttracker.ui.util

import com.example.weighttracker.R


enum class WTSignInOption(
    val const: String,
    val icon: Int
) {
    ANONYMOUS("ANONYMOUS", R.drawable.ic_wt_anonymous),
    GOOGLE("GOOGLE", R.drawable.ic_wt_google),
    FACEBOOK("META", R.drawable.ic_wt_meta),
    MOBILE("MOBILE", R.drawable.ic_wt_call),
}