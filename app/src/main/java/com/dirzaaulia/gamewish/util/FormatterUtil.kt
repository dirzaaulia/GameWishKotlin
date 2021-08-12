package com.dirzaaulia.gamewish.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.text.Html
import android.text.Spanned
import timber.log.Timber
import vas.com.currencyconverter.CurrencyConverter
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

fun currencyConverterLocaletoUSD(inputValue: Int) : Int {
    var convertedValue = 0.0

    CurrencyConverter.calculate(
        inputValue.toDouble(),
        Currency.getInstance(Locale.getDefault()),
        Currency.getInstance("USD")
    ) { value, e ->
        if (e != null) {
            Timber.i(e.localizedMessage)
        } else {
            convertedValue = value
        }
    }

    return convertedValue.toInt()
}

fun currencyConverterUSDtoLocale(inputValue: Int) : Double {
    var convertedValue = 0.0

    CurrencyConverter.calculate(
        inputValue.toDouble(),
        Currency.getInstance("USD"),
        Currency.getInstance(Locale.getDefault())
    ) { value, e ->
        if (e != null) {
            Timber.i(e.localizedMessage)
        } else {
           convertedValue = value
        }
    }

    return convertedValue
}

fun htmlToTextFormatter(value: String?) : Spanned? {
    return Html.fromHtml(value, Html.FROM_HTML_MODE_COMPACT)
}

fun numberFormatter(value: Double) : String {
    val format = NumberFormat.getNumberInstance(Locale.US)
    return format.format(value)
}

fun currencyFormatter(value: Double?) : String {
    val format: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    format.maximumFractionDigits = 2

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