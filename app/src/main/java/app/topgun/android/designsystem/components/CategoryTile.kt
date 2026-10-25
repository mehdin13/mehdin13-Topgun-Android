package app.topgun.android.designsystem.components

import app.topgun.android.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGHomeColors
import app.topgun.android.designsystem.TGHomeSize
import app.topgun.android.designsystem.TGHomeSpace
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGText

/**
 * Home category chip (iOS `CategoryItem`): 60dp tinted rounded square with the category icon, the name in
 * Futura Bold 10 underneath and a 2dp green underline while selected.
 */
@Composable
fun CategoryTile(
    name: String,
    iconRes: Int,
    tint: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryDescription = stringResource(R.string.ds_category_cd, name)
    Column(
        modifier
            .clip(TGRadius.image)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected; contentDescription = categoryDescription }
            .padding(TGHomeSpace.categoryTile),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(TGHomeSize.categoryTile).clip(TGRadius.image).background(tint.copy(alpha = TGHomeColors.categoryTintAlpha)),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(iconRes), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.size(TGHomeSize.categoryImage))
        }
        Text(name, style = TGText.futuraBold(10), color = TG.colors.textPrimary, maxLines = 1)
        if (selected) {
            Box(Modifier.width(TGHomeSize.categoryTile).height(TGHomeSize.categoryUnderline).background(TGBrand.green))
        } else {
            Box(Modifier.height(TGHomeSize.categoryUnderline))
        }
    }
}
