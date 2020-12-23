package com.dirzaaulia.gamewish.util

import java.text.NumberFormat
import java.util.*

fun textCurrencyFormatter(value: String) : String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance("USD")

    return format.format(value.toDouble())
}