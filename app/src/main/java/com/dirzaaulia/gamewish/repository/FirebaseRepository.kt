package com.dirzaaulia.gamewish.repository

import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.util.FIREBASE_DATABASE_URL
import com.dirzaaulia.gamewish.util.FIREBASE_TABLE_NAME
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class FirebaseRepository {

    private val auth = Firebase.auth
    private val realtimeDatabase = FirebaseDatabase.getInstance(FIREBASE_DATABASE_URL)

    fun getFirebaseAuth(): FirebaseAuth { return auth }

    fun getGoogleLoginStatus() : Boolean { return auth.currentUser != null }

    fun authGoogleLogin(idToken: String): AuthCredential {
        return GoogleAuthProvider.getCredential(idToken, null)
    }

    fun getAllWishlistFromRealtimeDatabase(uid: String): DatabaseReference {
        return realtimeDatabase.reference.child(FIREBASE_TABLE_NAME).child(uid)
    }

    fun getWishlistFromRealtimeDatabase(uid: String, gameId : String): DatabaseReference {
        return realtimeDatabase.reference.child(FIREBASE_TABLE_NAME).child(uid).child(gameId)
    }

    fun addWishlistToRealtimeDatabase(uid : String, wishlist: Wishlist) {
        realtimeDatabase.reference
            .child(FIREBASE_TABLE_NAME)
            .child(uid)
            .child(wishlist.id.toString())
            .setValue(
            wishlist, DatabaseReference.CompletionListener { error, ref ->
                if (error != null) {
                    Timber.w("Unable to write wishlist to database : %s", error.toException().toString())
                    return@CompletionListener
                } else {
                    Timber.i("Database Reference : %s", ref.toString())
                }
            })
    }

    fun removeWishlistFromRealtimeDatabase(uid : String, wishlist: Wishlist) {
        realtimeDatabase.reference
            .child(FIREBASE_TABLE_NAME)
            .child(uid)
            .child(wishlist.id.toString())
            .removeValue()
    }

}