package io.github.pyth0n14n.tastinggenie.data.master

import kotlinx.serialization.Serializable

@Serializable
data class MasterAsset(
    val items: List<MasterItemAsset>,
)

@Serializable
data class MasterItemAsset(
    val value: String,
    val label: String,
    val description: String? = null,
)

@Serializable
data class AromaMasterAsset(
    val categories: List<AromaCategoryAsset>,
)

@Serializable
data class AromaCategoryAsset(
    val group: String,
    val label: String,
    val items: List<MasterItemAsset>,
)
