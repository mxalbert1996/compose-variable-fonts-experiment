# Experiment of using variable fonts in Compose UI

## Method

Use a [custom plugin](app/build.gradle.kts#L69) to inject a [custom call](app/src/main/java/com/mxalbert/compose/variablefonts/ui/theme/Type.kt#L66)
into Compose UI [typeface resolving code](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/ui/ui-text/src/androidMain/kotlin/androidx/compose/ui/text/platform/extensions/TextPaintExtensions.android.kt;l=37).

## Limitations

- Variable fonts are only supported on API 26+.
- Using multiple variable fonts (when you need fallback fonts e.g. for CJK) only works on API 29+.
- Not really a variable font specific limitation, but due to Android's incorrect usage of yMin and yMax to determine the line height of a font,
if you use the original version of [Source Han Sans](https://github.com/adobe-fonts/source-han-sans) as the main font, the line height would be way too high.
  - This can be fixed by using [fonttools](https://github.com/fonttools/fonttools) to manually change yMin and yMax.
    - `ttx -t head SourceHanSansVF.otf`
    - Modify yMin and yMax (I changed it to be the same as Roboto) in `SourceHanSansVF.ttx`
    - `ttx -m SourceHanSansVF.otf -b SourceHanSansVF.ttx`.
