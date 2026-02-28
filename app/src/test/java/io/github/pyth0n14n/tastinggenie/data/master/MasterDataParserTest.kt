package io.github.pyth0n14n.tastinggenie.data.master

import io.github.pyth0n14n.tastinggenie.domain.model.enums.AromaGroup
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class MasterDataParserTest {
    @Test
    fun parseMasterData_returnsBundle() {
        val source =
            FakeAssetTextSource(
                mapOf(
                    "master/sake_type.json" to """{"items":[{"value":"JUNMAI","label":"純米"}]}""",
                    "master/classification.json" to """{"items":[{"value":"KIMOTO","label":"生酛"}]}""",
                    "master/temperature.json" to """{"items":[{"value":"JOON","label":"常温"}]}""",
                    "master/color.json" to """{"items":[{"value":"AMBER","label":"琥珀色"}]}""",
                    "master/prefecture.json" to """{"items":[{"value":"KYOTO","label":"京都府"}]}""",
                    "master/intensity.json" to """{"items":[{"value":"MEDIUM","label":"中程度"}]}""",
                    "master/taste_scale.json" to """{"items":[{"value":"STRONG","label":"強い"}]}""",
                    "master/overall_review.json" to """{"items":[{"value":"GOOD","label":"良い"}]}""",
                    "master/aroma.json" to
                        """
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
        val parsed =
            parseMasterData(
                source = source,
                json = Json { ignoreUnknownKeys = true },
            )

        assertEquals("純米", parsed.sakeGrades.first().label)
        assertEquals("生酛", parsed.classifications.first().label)
        assertEquals("京都府", parsed.prefectures.first().label)
        assertEquals(AromaGroup.FLORAL, parsed.aromaCategories.first().group)
        assertEquals(
            "桜",
            parsed.aromaCategories
                .first()
                .items
                .first()
                .label,
        )
    }

    @Test
    fun parseMasterData_throwsOnBrokenJson() {
        val source =
            baseSourceWith(
                "master/sake_type.json" to """{"items":[{"value":"JUNMAI","label":"純米"}""",
            )

        expectFailure<SerializationException> {
            parseMasterData(source = source, json = Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun parseMasterData_throwsOnEnumMismatch() {
        val source =
            baseSourceWith(
                "master/aroma.json" to
                    """
                    {
                      "categories":[
                        {
                          "group":"UNKNOWN_GROUP",
                          "label":"不正",
                          "items":[{"value":"SAKURA","label":"桜"}]
                        }
                      ]
                    }
                    """.trimIndent(),
            )

        expectFailure<IllegalArgumentException> {
            parseMasterData(source = source, json = Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun parseMasterData_throwsOnMissingRequiredKey() {
        val source =
            baseSourceWith(
                "master/temperature.json" to """{"items":[{"value":"JOON"}]}""",
            )

        expectFailure<SerializationException> {
            parseMasterData(source = source, json = Json { ignoreUnknownKeys = true })
        }
    }

    private fun baseSourceWith(vararg overrides: Pair<String, String>): FakeAssetTextSource {
        // 正常系ベースを使い、テストごとに壊したいファイルだけ差し替える。
        val base =
            mutableMapOf(
                "master/sake_type.json" to """{"items":[{"value":"JUNMAI","label":"純米"}]}""",
                "master/classification.json" to """{"items":[{"value":"KIMOTO","label":"生酛"}]}""",
                "master/temperature.json" to """{"items":[{"value":"JOON","label":"常温"}]}""",
                "master/color.json" to """{"items":[{"value":"AMBER","label":"琥珀色"}]}""",
                "master/prefecture.json" to """{"items":[{"value":"KYOTO","label":"京都府"}]}""",
                "master/intensity.json" to """{"items":[{"value":"MEDIUM","label":"中程度"}]}""",
                "master/taste_scale.json" to """{"items":[{"value":"STRONG","label":"強い"}]}""",
                "master/overall_review.json" to """{"items":[{"value":"GOOD","label":"良い"}]}""",
                "master/aroma.json" to
                    """
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
            )
        overrides.forEach { (key, value) -> base[key] = value }
        return FakeAssetTextSource(base)
    }

    private inline fun <reified T : Throwable> expectFailure(block: () -> Unit) {
        try {
            block()
            fail("Expected ${T::class.simpleName} but no exception was thrown.")
        } catch (expected: Throwable) {
            if (expected !is T) {
                fail("Expected ${T::class.simpleName}, but was ${expected::class.simpleName}.")
            }
        }
    }
}

private class FakeAssetTextSource(
    private val map: Map<String, String>,
) : AssetTextSource {
    override fun read(path: String): String = map[path] ?: error("Missing asset: $path")
}
