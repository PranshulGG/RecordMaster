package com.pranshulgg.recordmaster.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pranshulgg.recordmaster.ui.components.tiles.ActionTile
import com.pranshulgg.recordmaster.ui.components.tiles.CategoryTile
import com.pranshulgg.recordmaster.ui.components.tiles.DialogOptionTile
import com.pranshulgg.recordmaster.ui.components.tiles.DialogSliderTile
import com.pranshulgg.recordmaster.ui.components.tiles.DialogTextFieldTile
import com.pranshulgg.recordmaster.ui.components.tiles.SingleSwitchTile
import com.pranshulgg.recordmaster.ui.components.tiles.SwitchTile
import com.pranshulgg.recordmaster.ui.components.tiles.TextTile

sealed class SettingTile {
    abstract val title: String
    abstract val description: String?

    data class TextTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
    ) : SettingTile()


    data class SwitchTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val enabled: Boolean = true
    ) : SettingTile()

    data class SingleSwitchTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val enabled: Boolean = true
    ) : SettingTile()

    data class CategoryTile(
        override val title: String,
        override val description: String? = null,
        val leading: Int,
        val color: Color,
        val onColor: Color,
        val onClick: () -> Unit

    ) : SettingTile()

    data class DialogOptionTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val options: List<String>,
        val selectedOption: String?,
        val onOptionSelected: (String) -> Unit,
        val optionLabel: (String) -> String = { it },
        val dialogTitle: String? = null
    ) : SettingTile()

    data class ActionTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val onClick: () -> Unit,
        val colorDesc: Color = Color.Unspecified,
        val danger: Boolean = false
    ) : SettingTile()


    data class DialogTextFieldTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val onTextSubmitted: (String) -> Unit,
        val placeholder: String,
        val placeholderTextField: String,
        val initialText: String = ""
    ) : SettingTile()

    data class DialogSliderTile(
        override val title: String,
        override val description: String? = null,
        val leading: (@Composable (() -> Unit))? = null,
        val onValueSubmitted: (Float) -> Unit,
        val initialValue: Float = 0.5f,
        val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
        val steps: Int = 0,
        val labelFormatter: (Float) -> String = { it.toString() },
        val dialogTitle: String,
        val isDescriptionAsValue: Boolean = false

    ) : SettingTile()

}


@Composable
fun SettingSection(
    tiles: List<SettingTile>,
    title: String? = null,
    primarySwitch: Boolean = false,
    noPadding: Boolean = false,
    errorTile: Boolean = false
) {
    Column(
        modifier = Modifier
            .padding(horizontal = if (noPadding) 0.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        title?.let {
            Text(
                text = it,
                modifier = Modifier.padding(bottom = 5.dp, top = 5.dp, start = 3.dp),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.W700
            )
        }



        tiles.forEachIndexed { index, tile ->
            val isFirst = index == 0
            val isLast = index == tiles.lastIndex
            val isOnly = tiles.size == 1
            val primarySwitch = primarySwitch


            val shape = when {
                primarySwitch -> RoundedCornerShape(50.dp)
                isOnly -> RoundedCornerShape(18.dp)
                isFirst -> RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 2.6.dp,
                    bottomEnd = 2.6.dp
                )

                isLast -> RoundedCornerShape(
                    topStart = 2.6.dp,
                    topEnd = 2.6.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                )

                else -> RoundedCornerShape(2.6.dp)
            }

            when (tile) {
                is SettingTile.TextTile -> TextTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape
                )

                is SettingTile.CategoryTile -> CategoryTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    color = tile.color,
                    iconColor = tile.onColor,
                    onClick = tile.onClick
                )


                is SettingTile.DialogOptionTile -> DialogOptionTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    options = tile.options,
                    selectedOption = tile.selectedOption,
                    onOptionSelected = tile.onOptionSelected,
                    optionLabel = tile.optionLabel,
                    dialogTitle = tile.dialogTitle
                )

                is SettingTile.ActionTile -> ActionTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    onClick = tile.onClick,
                    colorDesc = tile.colorDesc,
                    danger = tile.danger
                )

                is SettingTile.SwitchTile -> SwitchTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    checked = tile.checked,
                    onCheckedChange = tile.onCheckedChange,
                    shapes = shape,
                    switchEnabled = tile.enabled
                )

                is SettingTile.SingleSwitchTile -> SingleSwitchTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    checked = tile.checked,
                    onCheckedChange = tile.onCheckedChange,
                    switchEnabled = tile.enabled
                )

                is SettingTile.DialogTextFieldTile -> DialogTextFieldTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    onTextSubmitted = tile.onTextSubmitted,
                    placeholder = tile.placeholder,
                    placeholderTextField = tile.placeholderTextField,
                    initialText = tile.initialText

                )

                is SettingTile.DialogSliderTile -> DialogSliderTile(
                    headline = tile.title,
                    description = tile.description,
                    leading = tile.leading,
                    shapes = shape,
                    onValueSubmitted = tile.onValueSubmitted,
                    initialValue = tile.initialValue,
                    valueRange = tile.valueRange,
                    steps = tile.steps,
                    labelFormatter = tile.labelFormatter,
                    dialogTitle = tile.dialogTitle,
                    isDescriptionAsValue = tile.isDescriptionAsValue

                )
            }
        }
    }
}

