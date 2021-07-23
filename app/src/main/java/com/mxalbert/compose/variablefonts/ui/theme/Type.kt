package com.mxalbert.compose.variablefonts.ui.theme

import android.content.res.AssetManager
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.fonts.FontVariationAxis
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import android.graphics.fonts.Font as NativeFont
import android.graphics.fonts.FontFamily as NativeFontFamily

val Typography: Typography
    @Composable
    get() {
        val assetManager = LocalContext.current.assets
        return remember {
            Typography(
                defaultFontFamily = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> FontFamily(
                        assetManager,
                        "Roboto.ttf",
                        "SourceHanSans.otf"
                    )
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> FontFamily(
                        Typeface.createFromAsset(assetManager, "SourceHanSans.otf")
                    )
                    else -> FontFamily.Default
                }
            )
        }
    }

@RequiresApi(Build.VERSION_CODES.Q)
private fun FontFamily(assetManager: AssetManager, vararg fontPaths: String): FontFamily =
    FontFamily(
        Typeface.CustomFallbackBuilder(
            buildFontFamily(assetManager, fontPaths[0])
        ).apply {
            for (i in 1 until fontPaths.size) {
                addCustomFallback(buildFontFamily(assetManager, fontPaths[i]))
            }
        }.build()
    )

@RequiresApi(Build.VERSION_CODES.Q)
private fun buildFontFamily(
    assetManager: AssetManager,
    path: String
): NativeFontFamily = NativeFontFamily.Builder(
    NativeFont.Builder(assetManager, path)
        .setFontVariationSettings(arrayOf(FontVariationAxis("wght", NormalWeight)))
        .build()
).build()

private const val NormalWeight = 400f

// This method will be injected into Compose UI framework
@Suppress("unused")
fun Paint.applyFontWeight(style: SpanStyle) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val weight = style.fontWeight ?: FontWeight.Normal
        if (weight != FontWeight.Normal) {
            fontVariationSettings = "'wght' ${weight.weight}"
        }
    }
}
