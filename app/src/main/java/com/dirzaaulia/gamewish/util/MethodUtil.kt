package com.dirzaaulia.gamewish.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            Timber.i("NetworkCapabilities.TRANSPORT_CELLULAR")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Timber.i("NetworkCapabilities.TRANSPORT_WIFI")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            Timber.i("NetworkCapabilities.TRANSPORT_ETHERNET")
            return true
        }
    }
    return false
}

fun showSnackbarShort(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
}

fun showInfiniteSnackbar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).show()
}

fun openRawgLink(context: Context) {
    val url = "https://www.rawg.io"
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(context, intent, null)
}