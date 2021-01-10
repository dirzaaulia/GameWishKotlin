package com.dirzaaulia.gamewish.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.text.Html
import android.text.Spanned
import timber.log.Timber
import vas.com.currencyconverter.CurrencyConverter
import java.lang.Exception
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

fun htmlToTextFormatter(value: String?) : Spanned? {
    return Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT)
}

fun currencyFormatter(value: Double?) : String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance(Locale.getDefault())

    return format.format(value)
}

fun textDateFormatter(value: String) : String {

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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