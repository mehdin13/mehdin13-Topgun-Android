package app.topgun.android.designsystem.components

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import app.topgun.android.designsystem.TGAccountColors
import app.topgun.android.designsystem.TGAccountSize
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace

/** FavoriteView "remove" button: white 40dp circle with a red filled heart, inside a 48dp tap area. */
@Composable
fun FavoriteHeartButton(aircraftName: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.size(TGSize.tapMin).clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.size(TGAccountSize.heartCircle).shadow(TGSpace.xs / 2, CircleShape).background(TGAccountColors.white, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Favorite,
                contentDescription = stringResource(R.string.ds_remove_from_favorites_cd, aircraftName),
                tint = TGBrand.red,
                modifier = Modifier.padding(TGSpace.m),
            )
        }
    }
}
