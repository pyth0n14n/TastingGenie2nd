package io.github.pyth0n14n.tastinggenie.data.master

import io.github.pyth0n14n.tastinggenie.domain.model.AromaCategoryMaster
import io.github.pyth0n14n.tastinggenie.domain.model.MasterDataBundle
import io.github.pyth0n14n.tastinggenie.domain.model.MasterOption
import io.github.pyth0n14n.tastinggenie.domain.model.enums.AromaGroup
import kotlinx.serialization.json.Json

private const val PATH_SAKE_TYPE = "master/sake_type.json"
private const val PATH_CLASSIFICATION = "master/classification.json"
private const val PATH_TEMPERATURE = "master/temperature.json"
private const val PATH_COLOR = "master/color.json"
private const val PATH_PREFECTURE = "master/prefecture.json"
private const val PATH_INTENSITY = "master/intensity.json"
private const val PATH_TASTE_SCALE = "master/taste_scale.json"
private const val PATH_OVERALL_REVIEW = "master/overall_review.json"
private const val PATH_AROMA = "master/aroma.json"

fun parseMasterData(
    source: AssetTextSource,
    json: Json,
): MasterDataBundle =
    MasterDataBundle(
        sakeGrades = parseMasterList(source, json, PATH_SAKE_TYPE),
        classifications = parseMasterList(source, json, PATH_CLASSIFICATION),
        temperatures = parseMasterList(source, json, PATH_TEMPERATURE),
        colors = parseMasterList(source, json, PATH_COLOR),
        prefectures = parseMasterList(source, json, PATH_PREFECTURE),
        intensityLevels = parseMasterList(source, json, PATH_INTENSITY),
        tasteLevels = parseMasterList(source, json, PATH_TASTE_SCALE),
        overallReviews = parseMasterList(source, json, PATH_OVERALL_REVIEW),
        aromaCategories = parseAroma(source, json),
    )

private fun parseMasterList(
    source: AssetTextSource,
    json: Json,
    path: String,
): List<MasterOption> {
    val raw = source.read(path)
    val parsed = json.decodeFromString<MasterAsset>(raw)
    return parsed.items.map { item ->
        MasterOption(
            value = item.value,
            label = item.label,
            description = item.description,
        )
    }
}

private fun parseAroma(
    source: AssetTextSource,
    json: Json,
): List<AromaCategoryMaster> {
    val raw = source.read(PATH_AROMA)
    val parsed = json.decodeFromString<AromaMasterAsset>(raw)
    return parsed.categories.map { category ->
        AromaCategoryMaster(
            group = enumValueOf<AromaGroup>(category.group),
            label = category.label,
            items =
                category.items.map { item ->
                    MasterOption(value = item.value, label = item.label, description = item.description)
                },
        )
    }
}
