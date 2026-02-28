package io.github.pyth0n14n.tastinggenie.data.master

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface AssetTextSource {
    fun read(path: String): String
}

class AndroidAssetTextSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : AssetTextSource {
    override fun read(path: String): String {
        return context.assets.open(path).bufferedReader().use { it.readText() }
    }
}
