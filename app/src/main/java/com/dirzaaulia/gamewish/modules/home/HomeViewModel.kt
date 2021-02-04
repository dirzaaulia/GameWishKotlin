package com.dirzaaulia.gamewish.modules.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dirzaaulia.gamewish.data.models.Wishlist
import com.dirzaaulia.gamewish.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject internal constructor(
    private val wishlistRepository: WishlistRepository
) : ViewModel() {

    val listWishlist: LiveData<List<Wishlist>> = wishlistRepository.getAllWishlist().asLiveData()
}