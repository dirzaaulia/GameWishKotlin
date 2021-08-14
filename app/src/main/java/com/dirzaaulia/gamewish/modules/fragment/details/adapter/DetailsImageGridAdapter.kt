package com.dirzaaulia.gamewish.modules.fragment.details.adapter

//class DetailsImageGridAdapter(
//    private val spans : Int
//) : DetailsImageAdapter() {
//
//    val variableSpanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//
//        private var indexSpanCounts : List<Int> = emptyList()
//
//        override fun getSpanSize(position: Int): Int {
//            return indexSpanCounts[position]
//        }
//
//        private fun generateSpanCountForItems(count: Int) : List<Int> {
//
//            val list = mutableListOf<Int>()
//
//            var rowSpanOccupied = 0
//            repeat(count) {
//                val size = Random.nextInt(1, spans + 1 - rowSpanOccupied)
//                rowSpanOccupied += size
//                if (rowSpanOccupied >= 3) rowSpanOccupied = 0
//                list.add(size)
//            }
//
//            return list
//        }
//
//        override fun invalidateSpanIndexCache() {
//            super.invalidateSpanIndexCache()
//            indexSpanCounts = generateSpanCountForItems(itemCount)
//        }
//    }
//
//    override fun getLayoutIdForPosition(position: Int): Int = R.layout.item_details_image
//}