package com.dirzaaulia.gamewish.modules.search.details

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dirzaaulia.gamewish.data.models.Games
import com.dirzaaulia.gamewish.repository.RawgRepository
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class SearchDetailsViewModel @AssistedInject constructor(
    private val repository: RawgRepository,
     @Assisted games: Games
) : ViewModel() {

    val gamesItem = games

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(games: Games): SearchDetailsViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            games: Games
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(games) as T
            }
        }
    }
}