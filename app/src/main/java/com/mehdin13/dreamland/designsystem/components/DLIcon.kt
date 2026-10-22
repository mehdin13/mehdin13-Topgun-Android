package com.mehdin13.dreamland.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.mehdin13.dreamland.designsystem.DLSize

/** Icon names shared with the iOS `DLIconName`, mapped to Material icons. `filled` is the selected/active form. */
enum class DLIconName(private val outlined: ImageVector, private val solid: ImageVector? = null) {
    Sparkles(Icons.Outlined.AutoAwesome),
    Moon(Icons.Outlined.Bedtime),
    Play(Icons.Outlined.PlayArrow),
    Pause(Icons.Outlined.Pause),
    Mic(Icons.Outlined.Mic),
    Photo(Icons.Outlined.Photo),
    Book(Icons.Outlined.MenuBook),
    Lock(Icons.Outlined.Lock),
    Check(Icons.Outlined.Check),
    Alert(Icons.Outlined.Warning),
    Info(Icons.Outlined.Info),
    Close(Icons.Outlined.Close),
    Heart(Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
    Chevron(Icons.Outlined.ChevronRight),
    ChevronLeft(Icons.Outlined.ChevronLeft),
    Plus(Icons.Outlined.Add),
    Speaker(Icons.Outlined.VolumeUp),
    Doc(Icons.Outlined.Description),
    Library(Icons.Outlined.LocalLibrary),
    Gear(Icons.Outlined.Settings),
    Trash(Icons.Outlined.Delete),
    House(Icons.Outlined.Home, Icons.Filled.Home),
    Person(Icons.Outlined.Person, Icons.Filled.Person),
    Star(Icons.Outlined.StarBorder, Icons.Filled.Star),
    People(Icons.Outlined.Group),
    Bookmark(Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
    Share(Icons.Outlined.Share),
    Search(Icons.Outlined.Search),
    Refresh(Icons.Outlined.Refresh),
    Brush(Icons.Outlined.Brush),
    Compass(Icons.Outlined.Explore),
    Rocket(Icons.Outlined.RocketLaunch),
    Paw(Icons.Outlined.Pets),
    Download(Icons.Outlined.Download),
    SignOut(Icons.AutoMirrored.Outlined.Logout);

    fun vector(filled: Boolean): ImageVector = if (filled) solid ?: outlined else outlined
}

/** A decorative or labelled icon. Pass [contentDescription] when the icon carries meaning on its own (VoiceOver/TalkBack). */
@Composable
fun DLIcon(
    name: DLIconName,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = DLSize.iconM,
    filled: Boolean = false,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = name.vector(filled),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}
