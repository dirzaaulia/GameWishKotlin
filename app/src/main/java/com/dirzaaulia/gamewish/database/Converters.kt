package com.dirzaaulia.gamewish.database

import androidx.room.TypeConverter
import com.dirzaaulia.gamewish.data.models.EsrbRating
import com.dirzaaulia.gamewish.data.models.Platforms
import com.dirzaaulia.gamewish.data.models.ShortScreenshots
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {
    @TypeConverter fun fromstringToListPlatforms(value: String): List<Platforms> {
        val listType: Type = object : TypeToken<List<Platforms?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromListPlatformsToString(value: List<Platforms>) : String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun fromstringToEsrbRating(value: String): EsrbRating {
        val listType: Type = object : TypeToken<EsrbRating?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromEsrbRatingToString(value: EsrbRating) : String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun fromstringToListShortScreenshots(value: String): List<ShortScreenshots> {
        val listType: Type = object : TypeToken<List<ShortScreenshots?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromListShortScreenshotsToString(value: List<ShortScreenshots>) : String {
        val gson = Gson()
        return gson.toJson(value)
    }
}