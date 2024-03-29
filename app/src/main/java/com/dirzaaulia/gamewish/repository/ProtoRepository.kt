package com.dirzaaulia.gamewish.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.dirzaaulia.gamewish.UserPreferences
import com.dirzaaulia.gamewish.util.DATA_STORE_FILE_NAME
import com.dirzaaulia.gamewish.util.ProtoSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val Context.userPreferencesStore: DataStore<UserPreferences> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = ProtoSerializer
    )

    val userPreferencesFlow: Flow<UserPreferences> = context.userPreferencesStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Timber.e("Error reading sort order preferences : %s", exception.toString())
                emit(UserPreferences.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun updateMyAnimeListAccessToken(accessToken : String) {
        context.userPreferencesStore.updateData { preference ->
            preference.toBuilder().setAccessToken(accessToken).build()
        }
    }

    suspend fun updateMyAnimeListRefreshToken(refreshToken : String) {
        context.userPreferencesStore.updateData { preference ->
            preference.toBuilder().setRefreshToken(refreshToken).build()
        }
    }

    suspend fun updateMyAnimeListExpresIn(expiresIn : Int) {
        context.userPreferencesStore.updateData { preference ->
            preference.toBuilder().setExpiresIn(expiresIn).build()
        }
    }

    suspend fun updateLocalDataStatus(status : Boolean) {
        context.userPreferencesStore.updateData { preference ->
            preference.toBuilder().setIsLocalData(status).build()
        }
    }

    suspend fun updateUserAuthId(uid : String) {
        context.userPreferencesStore.updateData { preference ->
            preference.toBuilder().setUserAuthId(uid).build()
        }
    }
}