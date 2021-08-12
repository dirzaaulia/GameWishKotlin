//package com.dirzaaulia.gamewish.modules.details.adapter
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.lifecycle.LiveData
//import com.dirzaaulia.gamewish.data.models.rawg.Screenshots
//import com.dirzaaulia.gamewish.databinding.ItemScreenshotsListBinding
//import com.smarteist.autoimageslider.SliderViewAdapter
//
//class DetailsImageBannerAdapter(
//    private val list: LiveData<List<Screenshots>?>
//) : SliderViewAdapter<DetailsImageBannerAdapter.ViewHolder>() {
//
//    override fun getCount(): Int {
//        return list.value!!.size
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup?): ViewHolder {
//        return ViewHolder(
//            ItemScreenshotsListBinding.inflate(
//                LayoutInflater.from(parent?.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun onBindViewHolder(viewHolder: ViewHolder?, position: Int) {
//        val screenshots = list.value?.get(position)
//
//        if (screenshots != null) {
//            viewHolder?.bind(screenshots)
//        }
//    }
//
//    class ViewHolder(
//        private val binding : ItemScreenshotsListBinding
//    ) : SliderViewAdapter.ViewHolder(binding.root) {
//
//        fun bind(screenshots: Screenshots) {
//            binding.apply {
//                screenshotsItem = screenshots
//                executePendingBindings()
//            }
//        }
//    }
//
//}