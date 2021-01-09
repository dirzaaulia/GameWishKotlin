package com.dirzaaulia.gamewish.modules.details.imageviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dirzaaulia.gamewish.data.models.Screenshots
import com.dirzaaulia.gamewish.data.models.ShortScreenshots
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject

class DetailsImageViewerViewModel @AssistedInject constructor(
    @Assisted screenshots: Screenshots
) : ViewModel() {

    val screenshotsItems = screenshots

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(screenshots: Screenshots) : DetailsImageViewerViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            screenshots: Screenshots
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(screenshots) as T
            }

        }
    }
}