-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# android-youtube-player: the IFrame page calls these methods through addJavascriptInterface("YouTubePlayerBridge").
-keepclassmembers class com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayerBridge {
    @android.webkit.JavascriptInterface <methods>;
}
