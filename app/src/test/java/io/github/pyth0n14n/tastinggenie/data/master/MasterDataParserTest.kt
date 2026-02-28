package io.github.pyth0n14n.tastinggenie.data.master

import io.github.pyth0n14n.tastinggenie.domain.model.enums.AromaGroup
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class MasterDataParserTest {
    @Test
    fun parseMasterData_returnsBundle() {
        val source = FakeAssetTextSource(
            mapOf(
                "master/sake_type.json" to """{"items":[{"value":"JUNMAI","label":"純米"}]}""",
                "master/classification.json" to """{"items":[{"value":"KIMOTO","label":"生酛"}]}""",
                "master/temperature.json" to """{"items":[{"value":"JOON","label":"常温"}]}""",
                "master/color.json" to """{"items":[{"value":"AMBER","label":"琥珀色"}]}""",
                "master/prefecture.json" to """{"items":[{"value":"KYOTO","label":"京都府"}]}""",
                "master/intensity.json" to """{"items":[{"value":"MEDIUM","label":"中程度"}]}""",
                "master/taste_scale.json" to """{"items":[{"value":"STRONG","label":"強い"}]}""",
                "master/overall_review.json" to """{"items":[{"value":"GOOD","label":"良い"}]}""",
                "master/aroma.json" to """
                    {
                      "categories":[
                        {
                          "group":"FLORAL",
                          "label":"フローラル",
                          "items":[{"value":"SAKURA","label":"桜"}]
                        }
                      ]
                    }
                """.trimIndent(),
            ),
        )
        val parsed = parseMasterData(
            source = source,
            json = Json { ignoreUnknownKeys = true },
        )

        assertEquals("純米", parsed.sakeGrades.first().label)
        assertEquals("生酛", parsed.classifications.first().label)
        assertEquals("京都府", parsed.prefectures.first().label)
        assertEquals(AromaGroup.FLORAL, parsed.aromaCategories.first().group)
        assertEquals("桜", parsed.aromaCategories.first().items.first().label)
    }
}

private class FakeAssetTextSource(
    private val map: Map<String, String>,
) : AssetTextSource {
    override fun read(path: String): String = map[path] ?: error("Missing asset: $path")
}
