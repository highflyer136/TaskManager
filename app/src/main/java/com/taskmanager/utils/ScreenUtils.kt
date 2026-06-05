package com.taskmanager.utils

import android.content.Context
import android.content.res.Configuration

fun Context.isTablet(): Boolean {
    return resources.configuration.smallestScreenWidthDp >= 600
}

fun Context.isLandscape(): Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun androidx.fragment.app.Fragment.isTablet(): Boolean {
    return requireContext().isTablet()
}
