package app.topgun.android.platform.ui

private val idPattern = Regex("^[A-Za-z0-9_-]{11}$")

/**
 * The 11-character video id from any YouTube link form stored in the data: `watch?v=`, `youtu.be/`, `/embed/`,
 * `/shorts/`, `/live/`, `/v/`, `m.` / `music.` / `www.` hosts, youtube-nocookie.com, extra query params in any order,
 * with or without a scheme, and a bare id. Null when the text holds no recognisable id.
 */
fun youTubeVideoId(link: String?): String? {
    val raw = link?.trim().orEmpty()
    if (raw.isEmpty()) return null
    if (idPattern.matches(raw)) return raw
    val noScheme = raw.substringAfter("://", raw).removePrefix("//")
    val host = noScheme.substringBefore('/').substringBefore('?').substringBefore('#').substringBefore(':').lowercase()
    val rest = noScheme.removePrefix(noScheme.substringBefore('/').substringBefore('?').substringBefore('#'))
    val path = rest.substringBefore('?').substringBefore('#')
    val query = rest.substringAfter('?', "").substringBefore('#').ifEmpty { noScheme.substringAfter('?', "").substringBefore('#') }
    val isYouTube = host == "youtu.be" || host.endsWith("youtube.com") || host.endsWith("youtube-nocookie.com")
    if (!isYouTube) return null
    if (host == "youtu.be") {
        return path.trim('/').substringBefore('/').takeIf { idPattern.matches(it) }
    }
    query.split('&').forEach { pair ->
        if (pair.substringBefore('=') == "v") pair.substringAfter('=', "").takeIf { idPattern.matches(it) }?.let { return it }
    }
    val parts = path.split('/').filter { it.isNotEmpty() }
    val i = parts.indexOfFirst { it == "embed" || it == "shorts" || it == "live" || it == "v" }
    if (i >= 0) parts.getOrNull(i + 1)?.takeIf { idPattern.matches(it) }?.let { return it }
    return null
}

/** `https://img.youtube.com/vi/<id>/hqdefault.jpg` */
fun youTubeThumbnailUrl(id: String): String = "https://img.youtube.com/vi/$id/hqdefault.jpg"

/** `https://www.youtube.com/watch?v=<id>`: opens in the YouTube app when installed, else the browser. */
fun youTubeWatchUrl(id: String): String = "https://www.youtube.com/watch?v=$id"
