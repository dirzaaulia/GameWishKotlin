package com.dirzaaulia.gamewish.util

import android.icu.text.SimpleDateFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


fun textCurrencyFormatter(value: String) : String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance("USD")

    return format.format(value.toDouble())
}

fun textDateFormatter(value: String) : String {

    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val inputFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date: LocalDate = LocalDate.parse(value, inputFormat)
        val outputFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        date.format(outputFormat)
    } else {
        val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date = dateParser.parse(value)
        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

        dateFormatter.format(date)
    }
}

fun textDateFormatter2(value: String) : String {

    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val inputFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date: LocalDate = LocalDate.parse(value, inputFormat)
        val outputFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

        date.format(outputFormat)
    } else {
        val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date = dateParser.parse(value)
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        dateFormatter.format(date)
    }
}