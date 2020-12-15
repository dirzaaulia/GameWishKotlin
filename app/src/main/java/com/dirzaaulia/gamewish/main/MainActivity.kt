package com.dirzaaulia.gamewish.main

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.dirzaaulia.gamewish.R
import com.dirzaaulia.gamewish.databinding.ActivityMainBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity(){

//    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

//        mBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet.bottomSheet)
//        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//
//        binding.buttonMenu.setOnClickListener {
//            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
//                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            } else {
//                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//            }
//        }
//
//        mBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//
//            }
//
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                binding.buttonMenu.rotation = slideOffset * 180
//            }
//
//        })
    }
}
