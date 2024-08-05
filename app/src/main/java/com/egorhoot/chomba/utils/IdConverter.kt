package com.egorhoot.chomba.utils

import android.content.Context
import javax.inject.Inject

class IdConverter @Inject constructor(
    private val context: Context) {
    fun getString(id: Int): String {
        return context.getString(id)
    }
}