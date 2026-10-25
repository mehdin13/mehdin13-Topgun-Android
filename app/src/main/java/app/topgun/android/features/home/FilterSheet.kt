package app.topgun.android.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import app.topgun.android.R
import app.topgun.android.designsystem.TG
import app.topgun.android.designsystem.TGAsset
import app.topgun.android.designsystem.TGBrand
import app.topgun.android.designsystem.TGHomeColors
import app.topgun.android.designsystem.TGHomeRadius
import app.topgun.android.designsystem.TGHomeSize
import app.topgun.android.designsystem.TGHomeSpace
import app.topgun.android.designsystem.TGHomeText
import app.topgun.android.designsystem.TGRadius
import app.topgun.android.designsystem.TGSize
import app.topgun.android.designsystem.TGSpace
import coil.compose.AsyncImage

/** The filter sheet (iOS `FilterView`, presented as a sheet). Done applies and closes; Reset clears every selection. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    state: FilterState,
    isPremium: Boolean,
    onChange: (FilterState) -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = TG.colors.background,
    ) {
        FilterSheetContent(state, isPremium, onChange, onReset, onDone, onUpgrade)
    }
}

@Composable
fun FilterSheetContent(
    state: FilterState,
    isPremium: Boolean,
    onChange: (FilterState) -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = TG.colors
    Column(modifier.fillMaxSize().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = TGSpace.l),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarButton(stringResource(R.string.home_reset), TGBrand.red, onReset)
            Text(stringResource(R.string.home_filter), style = TGHomeText.headline, color = c.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            BarButton(stringResource(R.string.home_done), c.accent, onDone, bold = true)
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = TGSpace.l, vertical = TGSpace.s),
            verticalArrangement = Arrangement.spacedBy(TGSpace.l),
        ) {
            section(R.string.home_section_aircraft_type, Icons.Outlined.Flight) {
                FilterOptions.aircraftTypes.forEach { t ->
                    RadioRow(optionLabel(t), state.category == t) { onChange(state.copy(category = if (state.category == t) null else t)) }
                }
            }
            section(R.string.home_section_country_of_origin, Icons.Outlined.Flag) {
                FilterOptions.originCountries.forEach { co ->
                    FlagRow(co, selected = state.countryOfOrigin == co.name) {
                        onChange(state.copy(countryOfOrigin = if (state.countryOfOrigin == co.name) null else co.name))
                    }
                }
            }
            section(R.string.home_section_generation, Icons.Outlined.Settings) {
                FilterOptions.generations.forEach { g -> CheckRow(optionLabel(g), g in state.generations) { onChange(state.copy(generations = state.generations.toggled(g))) } }
            }
            section(R.string.home_section_role, Icons.Outlined.GpsFixed) {
                FilterOptions.roles.forEach { r -> CheckRow(optionLabel(r), r in state.roles) { onChange(state.copy(roles = state.roles.toggled(r))) } }
            }
            section(R.string.home_section_status, Icons.Outlined.EmojiEvents) {
                FilterOptions.statuses.forEach { s ->
                    RadioRow(optionLabel(s), state.status == s) { onChange(state.copy(status = if (state.status == s) null else s)) }
                }
            }
            section(R.string.home_section_stealth, Icons.Outlined.VisibilityOff) {
                FilterOptions.stealth.forEach { s -> CheckRow(optionLabel(s), s in state.stealth) { onChange(state.copy(stealth = state.stealth.toggled(s))) } }
            }
            section(R.string.home_section_price, Icons.Outlined.MonetizationOn) {
                SliderRow(
                    value = state.priceMax, range = FilterOptions.PRICE_MIN..FilterOptions.PRICE_MAX, step = FilterOptions.PRICE_STEP,
                    label = if (state.priceMax >= FilterOptions.PRICE_MAX) stringResource(R.string.home_any_price) else stringResource(R.string.home_up_to_price, state.priceMax.toInt()),
                    description = stringResource(R.string.home_max_price),
                ) { onChange(state.copy(priceMax = it)) }
            }
            section(R.string.home_section_speed, Icons.Outlined.Speed) {
                FilterOptions.speeds.forEach { sp ->
                    Column {
                        RadioRow(stringResource(sp.labelRes), state.speed == sp) { onChange(state.copy(speed = if (state.speed == sp) null else sp)) }
                        if (state.speed == sp) {
                            Text(
                                stringResource(R.string.home_speed_range, sp.minKph, sp.maxKph), style = TGHomeText.caption, color = c.textSecondary,
                                modifier = Modifier.padding(start = TGSpace.m, bottom = TGSpace.s),
                            )
                        }
                    }
                }
            }
            section(R.string.home_section_primary_user, Icons.Outlined.Group) {
                FilterOptions.primaryUsers.forEach { u ->
                    CheckRow(optionLabel(u), u in state.primaryUsers, square = true) { onChange(state.copy(primaryUsers = state.primaryUsers.toggled(u))) }
                }
            }
            section(R.string.home_section_hardpoints, Icons.Outlined.GpsFixed) {
                FilterOptions.hardpoints.forEach { h ->
                    RadioRow(optionLabel(h), state.hardpoints == h) { onChange(state.copy(hardpoints = if (state.hardpoints == h) null else h)) }
                }
            }
            section(R.string.home_section_range, Icons.Outlined.Map) {
                SliderRow(
                    value = state.rangeKm, range = 0f..FilterOptions.RANGE_MAX, step = FilterOptions.RANGE_STEP,
                    label = if (state.rangeKm >= FilterOptions.RANGE_MAX) stringResource(R.string.home_any_range) else stringResource(R.string.home_up_to_range, state.rangeKm.toInt()),
                    description = stringResource(R.string.home_max_range),
                ) { onChange(state.copy(rangeKm = it)) }
            }
            section(R.string.home_section_warfare, Icons.Outlined.CalendarMonth) {
                SwitchRow(stringResource(R.string.home_electronic_warfare_equipped), state.electronicWarfare, enabled = true) { onChange(state.copy(electronicWarfare = it)) }
            }
            section(R.string.home_section_manufacturer, Icons.Outlined.CalendarMonth) {
                FilterOptions.manufacturers.forEach { m ->
                    RadioRow(m, state.manufacturer == m) { onChange(state.copy(manufacturer = if (state.manufacturer == m) null else m)) }
                }
            }
            section(R.string.home_section_intro_year, Icons.Outlined.CalendarMonth) {
                SliderRow(
                    value = state.introYear, range = FilterOptions.YEAR_MIN..FilterOptions.YEAR_MAX, step = FilterOptions.YEAR_STEP,
                    label = if (state.introYear <= FilterOptions.YEAR_MIN) stringResource(R.string.home_any_year) else stringResource(R.string.home_from_year, state.introYear.toInt()),
                    description = stringResource(R.string.home_intro_year_description),
                ) { onChange(state.copy(introYear = it)) }
            }
            section(R.string.home_section_vip, Icons.Outlined.WorkspacePremium) {
                SwitchRow(
                    stringResource(R.string.home_vip_only), state.vipOnly && isPremium, enabled = isPremium,
                    locked = !isPremium, onLockedClick = onUpgrade,
                ) { onChange(state.copy(vipOnly = it)) }
            }
            section(R.string.home_section_crew, Icons.Outlined.Group) {
                Row(Modifier.fillMaxWidth().padding(vertical = TGSpace.s), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (1..2).forEach { size ->
                        val on = state.crewSize == size
                        val tint = if (on) TGHomeColors.systemGreen else TGHomeColors.systemGray
                        val label = pluralStringResource(R.plurals.home_crew_pilots, size, size)
                        Column(
                            Modifier
                                .heightIn(min = TGSize.tapMin)
                                .selectable(on, role = Role.RadioButton) { onChange(state.copy(crewSize = size)) }
                                .padding(TGSpace.s),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(if (size == 1) Icons.Outlined.Person else Icons.Outlined.Group, contentDescription = null, tint = tint, modifier = Modifier.size(TGHomeSize.crewIcon))
                            Text(label, style = TGHomeText.caption, color = tint)
                        }
                    }
                }
            }
            section(R.string.home_section_operation_countries, Icons.Outlined.Public) {
                FilterOptions.operatingCountries.forEach { co ->
                    FlagRow(co, selected = co.name in state.countries, checkStyle = true) { onChange(state.copy(countries = state.countries.toggled(co.name))) }
                }
            }
        }
    }
}

private fun List<String>.toggled(value: String) = if (value in this) this - value else this + value

private fun androidx.compose.foundation.lazy.LazyListScope.section(
    titleRes: Int,
    icon: ImageVector,
    rows: @Composable () -> Unit,
) {
    item(key = titleRes) {
        Column(verticalArrangement = Arrangement.spacedBy(TGSpace.s)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TGSpace.s)) {
                Icon(icon, contentDescription = null, tint = TGBrand.blue, modifier = Modifier.size(TGHomeSize.sectionIcon))
                Text(stringResource(titleRes), style = TGHomeText.headline, color = TG.colors.textPrimary)
            }
            Column(
                Modifier.fillMaxWidth().clip(TGRadius.card).background(TG.colors.card).padding(horizontal = TGSpace.l, vertical = TGSpace.xs),
            ) { rows() }
        }
    }
}

@Composable
private fun BarButton(text: String, color: Color, onClick: () -> Unit, bold: Boolean = false) {
    Box(
        Modifier.heightIn(min = TGSize.tapMin).clickable(role = Role.Button, onClick = onClick).padding(horizontal = TGSpace.s),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = TGHomeText.body.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal), color = color)
    }
}

@Composable
private fun RadioRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = TGSize.tapMin).selectable(selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = TGHomeText.body, color = TG.colors.textPrimary, modifier = Modifier.weight(1f))
        val ring = if (selected) TGBrand.blue else TGHomeColors.systemGray
        Box(Modifier.size(TGHomeSize.radio).border(TGHomeSize.radioStroke, ring, CircleShape), contentAlignment = Alignment.Center) {
            if (selected) Box(Modifier.size(TGHomeSize.radioDot).background(TGBrand.blue, CircleShape))
        }
    }
}

/** Checkbox row: round check (iOS `AnimatedCheckbox`) or square check ([square], iOS `Checkbox`). */
@Composable
private fun CheckRow(title: String, checked: Boolean, square: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = TGSize.tapMin).toggleable(checked, role = Role.Checkbox) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = TGHomeText.body, color = TG.colors.textPrimary, modifier = Modifier.weight(1f))
        CheckIcon(checked, square)
    }
}

@Composable
private fun CheckIcon(checked: Boolean, square: Boolean) {
    val icon = when {
        square && checked -> Icons.Filled.CheckBox
        square -> Icons.Outlined.CheckBoxOutlineBlank
        checked -> Icons.Filled.CheckCircle
        else -> Icons.Outlined.Circle
    }
    Icon(icon, contentDescription = null, tint = if (checked) TGBrand.blue else TGHomeColors.systemGray)
}

/** Country row with its flag. [checkStyle] = multi-select (circle toggles); otherwise single-select (blue name + check). */
@Composable
private fun FlagRow(country: FilterCountry, selected: Boolean, checkStyle: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = TGSize.tapMin).selectable(selected, role = if (checkStyle) Role.Checkbox else Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TGSpace.m),
    ) {
        val flag = TGAsset.flagSvg(country.code)
        if (flag != null) {
            AsyncImage(
                model = flag, contentDescription = null, contentScale = ContentScale.Fit,
                modifier = Modifier.size(TGHomeSize.flagWidth, TGHomeSize.flagHeight).clip(TGHomeRadius.flag),
            )
        } else {
            Box(Modifier.size(TGHomeSize.flagWidth, TGHomeSize.flagHeight))
        }
        Text(
            countryLabel(country), style = TGHomeText.body,
            color = if (selected && !checkStyle) TGBrand.blue else TG.colors.textPrimary, modifier = Modifier.weight(1f),
        )
        if (checkStyle) CheckIcon(selected, square = false)
        else if (selected) Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = TGBrand.blue)
    }
}

@Composable
private fun SliderRow(value: Float, range: ClosedFloatingPointRange<Float>, step: Float, label: String, description: String, onChange: (Float) -> Unit) {
    val steps = ((range.endInclusive - range.start) / step).toInt() - 1
    Column(Modifier.fillMaxWidth().padding(vertical = TGSpace.s)) {
        Slider(
            value = value, onValueChange = onChange, valueRange = range, steps = steps.coerceAtLeast(0),
            modifier = Modifier.semantics { contentDescription = description },
        )
        Text(label, style = TGHomeText.caption, color = TG.colors.textPrimary)
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    locked: Boolean = false,
    onLockedClick: () -> Unit = {},
    onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = TGSize.tapMin)
            .then(if (locked) Modifier.clickable(role = Role.Button, onClick = onLockedClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TGSpace.s),
    ) {
        Text(title, style = TGHomeText.body, color = if (checked) TGBrand.blue else TGHomeColors.systemGray, modifier = Modifier.weight(1f))
        if (locked) Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.home_pro_only), tint = TGBrand.orange, modifier = Modifier.size(TGSpace.l))
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}
