package app.topgun.android.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGHomeColors
import app.topgun.android.designsystem.TGHomeSpace
import app.topgun.android.designsystem.TGHomeText
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace

/**
 * Capsule search field (iOS Home `SearchBar`): magnifier, text, clear button while typing and a "Cancel"
 * text button while the field is being edited. [onSubmit] runs on the keyboard's search action and after
 * Cancel (with an empty text), exactly like the iOS `onCommit`.
 */
@Composable
fun TGSearchField(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search for aircraft...",
) {
    val focus = LocalFocusManager.current
    var editing by remember { mutableStateOf(false) }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .weight(1f)
                .sizeIn(minHeight = TGSize.tapMin)
                .clip(CircleShape)
                .background(TG.colors.card)
                .padding(horizontal = TGHomeSpace.searchHorizontal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = TGHomeColors.systemGray)
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (text.isEmpty()) Text(placeholder, style = TGHomeText.field, color = TG.colors.textSecondary)
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    singleLine = true,
                    textStyle = TGHomeText.field.copy(color = TG.colors.textPrimary),
                    cursorBrush = SolidColor(TG.colors.accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); onSubmit(text) }),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) editing = true },
                )
            }
            if (text.isNotEmpty()) {
                Icon(
                    Icons.Filled.Cancel, contentDescription = "Clear search", tint = TGHomeColors.systemGray,
                    modifier = Modifier.size(TGSize.tapMin).clickable(role = Role.Button) { onTextChange("") }.padding(TGSpace.m),
                )
            }
        }
        if (editing) {
            Box(
                Modifier
                    .padding(start = TGSpace.xs)
                    .sizeIn(minHeight = TGSize.tapMin)
                    .clickable(role = Role.Button) {
                        editing = false
                        onTextChange("")
                        focus.clearFocus()
                        onSubmit("")
                    }
                    .padding(horizontal = TGSpace.s),
                contentAlignment = Alignment.Center,
            ) { Text("Cancel", style = TGHomeText.body, color = TG.colors.accent) }
        }
    }
}
