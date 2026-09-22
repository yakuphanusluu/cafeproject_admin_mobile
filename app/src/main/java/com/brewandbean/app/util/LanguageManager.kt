package com.brewandbean.app.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object LanguageManager {
    private const val PREFS_NAME = "LanguagePrefs"
    private const val KEY_IS_EN = "is_english"

    private val _isEnglish = MutableStateFlow(false)
    val isEnglish: StateFlow<Boolean> = _isEnglish

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isEnglish.value = prefs?.getBoolean(KEY_IS_EN, false) ?: false
    }

    fun toggleLanguage() {
        setEnglish(!isEnglish.value)
    }

    fun setEnglish(isEn: Boolean) {
        _isEnglish.value = isEn
        prefs?.edit()?.putBoolean(KEY_IS_EN, isEn)?.apply()
    }
}
