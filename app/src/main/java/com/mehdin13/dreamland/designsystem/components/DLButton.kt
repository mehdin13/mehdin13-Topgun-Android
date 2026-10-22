package com.mehdin13.dreamland.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mehdin13.dreamland.designsystem.DL
import com.mehdin13.dreamland.designsystem.DLRadius
import com.mehdin13.dreamland.designsystem.DLSize
import com.mehdin13.dreamland.designsystem.DLSpacing

enum class DLButtonVariant { Primary, Secondary, Tertiary, Destructive }
enum class DLButtonSize { Large, Medium, Small }

/** Every button in the app. Capsule shape, tap target of at least 44dp, loading state. */
@Composable
fun DLButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: DLButtonVariant = DLButtonVariant.Primary,
    size: DLButtonSize = DLButtonSize.Large,
    icon: DLIconName? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    fullWidth: Boolean = false,
) {
    val c = DL.colors
    val foreground = when {
        !enabled -> if (variant == DLButtonVariant.Tertiary) c.disabled else c.textTertiary
        else -> when (variant) {
            DLButtonVariant.Primary -> c.textOnAccent
            DLButtonVariant.Secondary -> c.accentSecondaryText
            DLButtonVariant.Tertiary -> c.accentPrimaryText
            DLButtonVariant.Destructive -> c.error
        }
    }
    val container = when {
        !enabled -> if (variant == DLButtonVariant.Primary) c.disabled else Color.Transparent
        else -> when (variant) {
            DLButtonVariant.Primary -> c.accentPrimary
            DLButtonVariant.Secondary -> c.accentSecondarySoft
            else -> Color.Transparent
        }
    }
    val minHeight = if (size == DLButtonSize.Large) DLSize.controlH else DLSize.tapMin
    val horizontal = if (size == DLButtonSize.Small) DLSpacing.space16 else DLSpacing.space24
    val textStyle = if (size == DLButtonSize.Small) DL.type.bodySmall else DL.type.buttonLabel

    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = minHeight),
        shape = DLRadius.pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = foreground,
            disabledContainerColor = container,
            disabledContentColor = foreground,
        ),
        border = if (variant == DLButtonVariant.Destructive) BorderStroke(2.dp, if (enabled) c.error else c.disabled) else null,
        contentPadding = PaddingValues(horizontal = horizontal),
        elevation = null,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(DLSpacing.space8), verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(DLSize.iconS), color = foreground, strokeWidth = 2.dp)
            } else if (icon != null) {
                DLIcon(icon, tint = foreground, size = DLSize.iconS)
            }
            Text(title, style = textStyle, color = foreground)
        }
    }
}

/** Round icon-only button. [label] is required: it is the TalkBack description and the 44dp tap target stays. */
@Composable
fun DLIconButton(
    icon: DLIconName,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    tint: Color = DL.colors.textPrimary,
    container: Color = Color.Transparent,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.sizeIn(minWidth = DLSize.tapMin, minHeight = DLSize.tapMin).semantics { role = Role.Button },
        colors = IconButtonDefaults.iconButtonColors(containerColor = container, contentColor = tint),
    ) {
        Icon(imageVector = icon.vector(filled), contentDescription = label, modifier = Modifier.size(DLSize.iconM), tint = tint)
    }
}
